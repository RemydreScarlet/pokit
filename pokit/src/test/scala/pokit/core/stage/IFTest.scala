package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class IFTest extends AnyFlatSpec with ChiselScalatestTester {
    "IF" should "switch threads correctly" in {
        test(new IF) { dut =>
            // スレッド0の検証
            dut.io.instr.poke("h12345678".U)
            dut.clock.step()
            dut.io.instrOut.threadIdx.expect(1.U) // 次のサイクルでスレッド1に切り替わる
            
            // スレッド1の検証
            dut.io.instr.poke("h87654321".U)
            dut.clock.step()
            dut.io.instrOut.threadIdx.expect(0.U)
        }
    }
}
