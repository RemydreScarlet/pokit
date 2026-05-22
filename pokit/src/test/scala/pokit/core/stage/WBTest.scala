package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WBTest extends AnyFlatSpec with ChiselScalatestTester {
    "WB" should "pass through memOut" in {
        test(new WB) { dut =>
            dut.io.memOut.poke(456.U)
            dut.io.regWriteData.expect(456.U)
        }
    }
}
