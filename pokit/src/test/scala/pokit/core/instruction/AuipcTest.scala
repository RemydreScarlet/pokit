package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class AuipcTest extends AnyFlatSpec with ChiselScalatestTester {
    "AUIPC instruction" should "add upper immediate to PC" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)

            // AUIPC x1, 0x12345 -> 0x12345097
            // PC starts at 0, so x1 = PC + 0x12345000 = 0x12345000
            val instr = "h12345097".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect("h12345000".U)
        }
    }
}
