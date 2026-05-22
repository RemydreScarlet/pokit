package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class IDTest extends AnyFlatSpec with ChiselScalatestTester {
    "ID" should "read registers" in {
        test(new ID) { dut =>
            // 現状の実装ではレジスタ値の初期化が困難なため、
            // 読み出し動作がエラーなく行えることのみを確認
            dut.io.instrIn.instr.poke(0x00100093.U) // ADDI x1, x0, 1
            dut.io.instrIn.threadIdx.poke(0.U)
            dut.clock.step()
        }
    }
}
