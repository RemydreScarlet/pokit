package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class AddiTest extends AnyFlatSpec with ChiselScalatestTester {
    "ADDI instruction" should "add immediate to register" in {
        test(new Pipeline) { dut =>
            dut.io.branchValid.poke(false.B)
            
            // ADDI x1, x0, 10 -> 0x00A00093
            val instr = "h00A00093".U
            dut.io.instr.poke(instr)
            dut.clock.step(6)
        }
    }
}
