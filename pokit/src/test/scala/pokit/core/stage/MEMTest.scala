package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MEMTest extends AnyFlatSpec with ChiselScalatestTester {
    "MEM" should "pass through aluOut" in {
        test(new MEM) { dut =>
            dut.io.aluOut.poke(123.U)
            dut.io.memOut.expect(123.U)
        }
    }
}
