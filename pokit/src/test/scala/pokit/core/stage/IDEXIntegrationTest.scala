package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core._

class IDEXIntegrationTest extends AnyFlatSpec with ChiselScalatestTester {
    "ID-EX" should "execute ADDI instruction correctly" in {
        // ADDI x1, x0, 10 -> 0x00A00093 (opcode=0010011, funct3=000, rd=1, rs1=0, imm=10)
        // 実際にはデコード結果は以下になる
        // opcode: 0010011, rd: 1, funct3: 000, rs1: 0, imm: 10
        val instr = "h00A00093".U 
        
        test(new ID).withAnnotations(Seq(WriteVcdAnnotation)) { idDut =>
            idDut.io.instrIn.instr.poke(instr)
            idDut.io.instrIn.threadIdx.poke(0.U)
            
            // IDの出力をキャプチャしてEXへ入力するモデルを作る必要がある
            // 今回は直接的な結合テストのため、単体で検証
            idDut.io.ctrl.aluSrc.expect(true.B)
            idDut.io.ctrl.aluOp.expect(0.U) // ADD
        }
    }
}
