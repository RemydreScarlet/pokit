package pokit.core.stage.register

import chisel3._
import pokit.core._

// ID/EXステージ間のパイプラインレジスタ
class IDEXReg extends Module {
    val io = IO(new Bundle {
        val inRs1 = Input(UInt(32.W))
        val inRs2 = Input(UInt(32.W))
        val inImm = Input(UInt(32.W))
        val inCtrl = Input(new ControlBundle)
        val inRd = Input(UInt(6.W))
        val inPc = Input(UInt(32.W)) // PCを追加

        val outRs1 = Output(UInt(32.W))
        val outRs2 = Output(UInt(32.W))
        val outImm = Output(UInt(32.W))
        val outCtrl = Output(new ControlBundle)
        val outRd = Output(UInt(6.W))
        val outPc = Output(UInt(32.W)) // PCを追加
    })

    io.outRs1 := RegNext(io.inRs1, 0.U)
    io.outRs2 := RegNext(io.inRs2, 0.U)
    io.outImm := RegNext(io.inImm, 0.U)
    io.outCtrl := RegNext(io.inCtrl, 0.U.asTypeOf(new ControlBundle))
    io.outRd := RegNext(io.inRd, 0.U)
    io.outPc := RegNext(io.inPc, 0.U) // PCを追加
}
