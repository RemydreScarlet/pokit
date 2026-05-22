package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class EXTest extends AnyFlatSpec with ChiselScalatestTester {
    "EX" should "perform addition correctly" in {
        test(new EX) { dut =>
            dut.io.rs1.poke(10.U)
            dut.io.rs2.poke(20.U)
            dut.io.aluOp.poke(0.U) // ADD
            dut.clock.step()
            dut.io.aluOut.expect(30.U)
        }
    }

    "EX" should "perform subtraction correctly" in {
        test(new EX) { dut =>
            dut.io.rs1.poke(20.U)
            dut.io.rs2.poke(10.U)
            dut.io.aluOp.poke(1.U) // SUB
            dut.clock.step()
            dut.io.aluOut.expect(10.U)
        }
    }
}
