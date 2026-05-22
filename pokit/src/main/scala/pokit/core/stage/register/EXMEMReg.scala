package pokit.core.stage.register

import chisel3._
import pokit.core._

// EX/MEMステージ間のパイプラインレジスタ
class EXMEMReg extends Module {
    val io = IO(new Bundle {
        val inAluOut = Input(UInt(32.W))
        val inRd = Input(UInt(6.W))
        val inCtrl = Input(new ControlBundle)

        val outAluOut = Output(UInt(32.W))
        val outRd = Output(UInt(6.W))
        val outCtrl = Output(new ControlBundle)
    })

    io.outAluOut := RegNext(io.inAluOut, 0.U)
    io.outRd := RegNext(io.inRd, 0.U)
    io.outCtrl := RegNext(io.inCtrl, 0.U.asTypeOf(new ControlBundle))
}
