package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class IFTest extends AnyFlatSpec with ChiselScalatestTester {
    "IF" should "increment PC correctly for each thread" in {
        test(new IF) { dut =>
            dut.io.branchValid.poke(false.B)
            
            // スレッド0 (pc0) のインクリメント確認
            // 初期状態(pc0=0, pc1=0, thread=0)
            dut.clock.step() // threadIdx: 0 -> 1, pc0: 0 -> 4
            dut.io.pc.expect(0.U) // threadIdx=0の時
            
            dut.clock.step() // threadIdx: 1 -> 0, pc1: 0 -> 4
            dut.io.pc.expect(4.U) // threadIdx=1の時
            
            dut.clock.step() // threadIdx: 0 -> 1, pc0: 4 -> 8
            dut.io.pc.expect(4.U) // threadIdx=0の時
        }
    }
}
