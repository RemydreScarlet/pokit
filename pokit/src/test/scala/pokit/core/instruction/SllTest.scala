package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class SllTest extends AnyFlatSpec with ChiselScalatestTester {
    "SLL instruction" should "shift left logical" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(true.B)

            // x2 = 10, x3 = 20
            dut.io.initRegAddr.poke(2.U)
            dut.io.initRegData.poke(10.U)
            dut.clock.step()
            dut.io.initRegAddr.poke(3.U)
            dut.io.initRegData.poke(20.U)
            dut.clock.step()
            dut.io.initRegWen.poke(false.B)

            // SLL x1, x2, x3 -> 0x003110b3
            val instr = "h003110b3".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect(10485760.U)
        }
    }
}
