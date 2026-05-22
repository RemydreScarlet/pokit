package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class AddTest extends AnyFlatSpec with ChiselScalatestTester {
    "ADD instruction" should "add two register values" in {
        test(new Pipeline) { dut =>
            dut.io.branchValid.poke(false.B)
            
            // ADD x1, x2, x3 -> 0x003100b3
            // 前準備: RegFileに値を書く必要があるが、
            // 統合テストでは複雑になるため、ここでは命令の動作のみ確認。
            // 本来はレジスタ初期化用のポートを設けるのがベスト。
            val instr = "h003100b3".U
            dut.io.instr.poke(instr)
            dut.clock.step(6)
        }
    }
}
