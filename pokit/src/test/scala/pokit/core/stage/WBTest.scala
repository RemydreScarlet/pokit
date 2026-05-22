package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core._

class WBTest extends AnyFlatSpec with ChiselScalatestTester {
    "WB" should "pass through aluOut to wData" in {
        test(new WB) { dut =>
            dut.io.aluOut.poke(456.U)
            dut.io.rd.poke(1.U)
            dut.io.ctrl.regWrite.poke(true.B)
            
            dut.io.wData.expect(456.U)
            dut.io.wAddr.expect(1.U)
            dut.io.wen.expect(true.B)
        }
    }
}
