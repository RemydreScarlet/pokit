package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class SubTest extends AnyFlatSpec with ChiselScalatestTester {
    "SUB instruction" should "subtract two register values" in {
        test(new Pipeline) { dut =>
            dut.io.branchValid.poke(false.B)
            
            // SUB x1, x2, x3 -> 0x403100b3
            val instr = "h403100b3".U
            dut.io.instr.poke(instr)
            dut.clock.step(6)
        }
    }
}
