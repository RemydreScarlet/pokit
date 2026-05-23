package pokit.core.stage.register

import chisel3._
import pokit.core._

class EXMEMReg extends Module {
    val io = IO(new Bundle {
        val inAluOut = Input(UInt(32.W))
        val inRs2 = Input(UInt(32.W))
        val inRd = Input(UInt(6.W))
        val inCtrl = Input(new ControlBundle)
        val inThreadIdx = Input(UInt(1.W))

        val outAluOut = Output(UInt(32.W))
        val outRs2 = Output(UInt(32.W))
        val outRd = Output(UInt(6.W))
        val outCtrl = Output(new ControlBundle)
        val outThreadIdx = Output(UInt(1.W))
    })

    io.outAluOut := RegNext(io.inAluOut, 0.U)
    io.outRs2 := RegNext(io.inRs2, 0.U)
    io.outRd := RegNext(io.inRd, 0.U)
    io.outCtrl := RegNext(io.inCtrl, 0.U.asTypeOf(new ControlBundle))
    io.outThreadIdx := RegNext(io.inThreadIdx, 0.U)
}
