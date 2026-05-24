package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class LuiTest extends AnyFlatSpec with ChiselScalatestTester {
    "LUI instruction" should "load upper immediate" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)

            // LUI x1, 0x12345 -> 0x123450b7
            val instr = "h123450b7".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect("h12345000".U)
        }
    }
}
