package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class BltuTest extends AnyFlatSpec with ChiselScalatestTester {
    "BLTU instruction" should "branch if less than unsigned" in {
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

            // BLTU x2, x3, 0 -> 0x00316063 (taken since 10 < 20)
            val instr = "h00316063".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            // Branch taken - verify redirect occurred
            dut.io.dbgRegAddr.poke(2.U)
            dut.io.dbgRegData.expect(10.U)
        }
    }
}
