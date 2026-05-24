package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class JalrTest extends AnyFlatSpec with ChiselScalatestTester {
    "JALR instruction" should "jump and link register" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(true.B)

            // x2 = 8 (jump target), x3 = 20
            dut.io.initRegAddr.poke(2.U)
            dut.io.initRegData.poke(8.U)
            dut.clock.step()
            dut.io.initRegAddr.poke(3.U)
            dut.io.initRegData.poke(20.U)
            dut.clock.step()
            dut.io.initRegWen.poke(false.B)

            // JALR x1, x2, 0 -> 0x000100e7
            val instr = "h000100e7".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            // x1 should get PC+4 = 4 (link register)
            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect(4.U)
        }
    }
}
