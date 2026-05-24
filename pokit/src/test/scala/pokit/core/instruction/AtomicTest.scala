package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class AtomicTest extends AnyFlatSpec with ChiselScalatestTester {
    val nop = 0x00000013L

    def initReg(dut: Pipeline, addr: Int, data: Int): Unit = {
        dut.io.initRegWen.poke(true.B)
        dut.io.initRegAddr.poke(addr.U)
        dut.io.initRegData.poke(data.U)
        dut.clock.step()
        dut.io.initRegWen.poke(false.B)
    }

    def writeIMem(dut: Pipeline, addr: Int, data: Long): Unit = {
        dut.io.imemInitWen.poke(true.B)
        dut.io.imemInitAddr.poke(addr.U)
        dut.io.imemInitData.poke(data.U(32.W))
        dut.clock.step()
        dut.io.imemInitWen.poke(false.B)
    }

    def writeDMem(dut: Pipeline, addr: Int, data: Int): Unit = {
        dut.io.dmemInitWen.poke(true.B)
        dut.io.dmemInitAddr.poke(addr.U)
        dut.io.dmemInitData.poke(data.U)
        dut.clock.step()
        dut.io.dmemInitWen.poke(false.B)
    }

    def runProgram(dut: Pipeline, program: Array[Long], baseAddr: Int = 0): Unit = {
        for (i <- program.indices) {
            writeIMem(dut, baseAddr + i * 4, program(i))
        }
        dut.reset.poke(true.B)
        dut.clock.step()
        dut.reset.poke(false.B)
        for (_ <- 0 until 30) {
            dut.clock.step()
        }
    }

    "LR.W and SC.W" should "execute atomically" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 42)

            val program = Array(
                0x000010B7L,  // lui x1, 1        -> x1 = 0x1000
                0x1000A12FL,  // lr.w x2, (x1)    -> x2 = 42, set reservation
                0x06400193L,  // addi x3, x0, 100 -> x3 = 100
                0x1830A22FL,  // sc.w x4, x3, (x1) -> store 100, x4 = 0
                nop, nop, nop, nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(2.U)
            dut.io.dbgRegData.expect(42.U)

            dut.io.dbgRegAddr.poke(4.U)
            dut.io.dbgRegData.expect(0.U)

            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal == 100, s"dmem[0x1000] = $dmemVal, expected 100")
        }
    }

    "SC.W" should "fail when reservation is lost" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 42)

            val program = Array(
                0x000010B7L,  // lui x1, 1        -> x1 = 0x1000
                0x1000A12FL,  // lr.w x2, (x1)    -> x2 = 42, set reservation
                0x00C12023L,  // sw x2, 0(x2)? No...
                // Let me use a store instruction that writes to x1's value (0x1000) to clear reservation
                // sw x3, 0(x1) -> stores x3 (which we havent set) to 0x1000
                // Actually, let's just do sw x0, 0(x1) to clear reservation by writing to same addr
                0x0000A023L,  // sw x0, 0(x1)     -> store 0 to 0x1000, clears reservation
                0x06400193L,  // addi x3, x0, 100 -> x3 = 100
                0x1830A22FL,  // sc.w x4, x3, (x1) -> should fail, x4 = 1
                nop, nop, nop, nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(4.U)
            dut.io.dbgRegData.expect(1.U)

            // dmem[0x1000] should still be 0 (from sw), NOT 100
            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal != 100, s"SC.W should have failed, but dmem[0x1000] = $dmemVal")
        }
    }

    "AMOADD.W" should "atomically add" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 10)

            // amoadd.w x3, x2, (x1): mem[0x1000] += x2, rd=x3 = old value
            // funct5=00000, rs2=x2, rs1=x1, funct3=010, rd=x3, opcode=0101111
            // = 0 << 27 | 2 << 20 | 1 << 15 | 2 << 12 | 3 << 7 | 0x2F
            val amoadd = 0x0020A1AFL  // amoadd.w x3, x2, (x1)
            // Hmm, let me recalculate. rs2=2, rs1=1, rd=3
            // 0 << 27 = 0
            // 2 << 20 = 0x200000
            // 1 << 15 = 0x8000
            // 2 << 12 = 0x2000 [funct3=010=2]
            // 3 << 7 = 0x180
            // opcode = 0x2F
            // = 0x200000 + 0x8000 + 0x2000 + 0x180 + 0x2F = 0x202A1AF? No...
            // = 0x00200000 + 0x8000 = 0x00208000
            // + 0x2000 = 0x0020A000
            // + 0x180 = 0x0020A180
            // + 0x2F = 0x0020A1AF

            val program = Array(
                0x000010B7L,  // lui x1, 1        -> x1 = 0x1000
                0x00500113L,  // addi x2, x0, 5   -> x2 = 5
                0x0020A1AFL,  // amoadd.w x3, x2, (x1) -> old=10, mem=15, x3=10
                0x00000013L,  // nop
                0x00000013L,  // nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(3.U)
            dut.io.dbgRegData.expect(10.U)

            // Verify x2=5 to ensure addi worked
            dut.io.dbgRegAddr.poke(2.U)
            dut.io.dbgRegData.expect(5.U)

            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal == 15, s"dmem[0x1000] = $dmemVal, expected 15")
        }
    }

    "AMOSWAP.W" should "atomically swap" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 42)

            // amoswap.w x3, x2, (x1) -> old=42, mem=99, x3=42
            // funct5=00001=1, rs2=2, rs1=1, funct3=2, rd=3
            // = 1 << 27 | 2 << 20 | 1 << 15 | 2 << 12 | 3 << 7 | 0x2F
            // = 0x08000000 + 0x200000 + 0x8000 + 0x2000 + 0x180 + 0x2F
            // = 0x0820A1AF
            val amoswap = 0x0820A1AFL

            val program = Array(
                0x000010B7L,  // lui x1, 1
                0x06300113L,  // addi x2, x0, 99 -> x2 = 99
                amoswap,
                nop, nop, nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(3.U)
            dut.io.dbgRegData.expect(42.U)

            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal == 99, s"dmem[0x1000] = $dmemVal, expected 99")
        }
    }

    "AMOAND.W" should "atomically AND" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 0xFF)

            // amoand.w x3, x2, (x1) -> old=0xFF, mem=0xFF&0x0F=0x0F, x3=0xFF
            // funct5=01000=8, rs2=2, rs1=1, funct3=2, rd=3
            // = 8 << 27 | 2 << 20 | 1 << 15 | 2 << 12 | 3 << 7 | 0x2F
            // = 0x40000000 + 0x200000 + 0x8000 + 0x2000 + 0x180 + 0x2F
            // = 0x4020A1AF
            val amoand = 0x4020A1AFL

            val program = Array(
                0x000010B7L,  // lui x1, 1
                0x00F00113L,  // addi x2, x0, 15 -> x2 = 0x0F
                amoand,
                nop, nop, nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(3.U)
            dut.io.dbgRegData.expect(0xFF.U)

            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal == 0x0F, s"dmem[0x1000] = $dmemVal, expected 0x0F")
        }
    }

    "AMOOR.W" should "atomically OR" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 0xF0)

            // amoor.w x3, x2, (x1) -> old=0xF0, mem=0xF0|0x0F=0xFF, x3=0xF0
            // funct5=01100=12, rs2=2, rs1=1, funct3=2, rd=3
            // = 12 << 27 | 2 << 20 | 1 << 15 | 2 << 12 | 3 << 7 | 0x2F
            // = 0x60000000 + 0x200000 + 0x8000 + 0x2000 + 0x180 + 0x2F
            // = 0x6020A1AF
            val amoor = 0x6020A1AFL

            val program = Array(
                0x000010B7L,  // lui x1, 1
                0x00F00113L,  // addi x2, x0, 15 -> x2 = 0x0F
                amoor,
                nop, nop, nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(3.U)
            dut.io.dbgRegData.expect(0xF0.U)

            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal == 0xFF, s"dmem[0x1000] = $dmemVal, expected 0xFF")
        }
    }

    "AMOXOR.W" should "atomically XOR" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.singleThread.poke(true.B)

            writeDMem(dut, 0x1000, 0xFF)

            // amoxor.w x3, x2, (x1) -> old=0xFF, mem=0xFF^0x0F=0xF0, x3=0xFF
            // funct5=00100=4, rs2=2, rs1=1, funct3=2, rd=3
            // = 4 << 27 | 2 << 20 | 1 << 15 | 2 << 12 | 3 << 7 | 0x2F
            // = 0x20000000 + 0x200000 + 0x8000 + 0x2000 + 0x180 + 0x2F
            // = 0x2020A1AF
            val amoxor = 0x2020A1AFL

            val program = Array(
                0x000010B7L,  // lui x1, 1
                0x00F00113L,  // addi x2, x0, 15 -> x2 = 0x0F
                amoxor,
                nop, nop, nop
            )
            runProgram(dut, program)

            dut.io.dbgRegAddr.poke(3.U)
            dut.io.dbgRegData.expect(0xFF.U)

            dut.io.dmemDbgAddr.poke(0x1000.U)
            val dmemVal = dut.io.dmemDbgData.peek().litValue
            assert(dmemVal == 0xF0, s"dmem[0x1000] = $dmemVal, expected 0xF0")
        }
    }
}
