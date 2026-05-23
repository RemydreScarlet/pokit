package pokit.core.stage

import chisel3._
import pokit.core._

class WB extends Module {
    val io = IO(new Bundle {
        val aluOut = Input(UInt(32.W))
        val rd = Input(UInt(6.W))
        val ctrl = Input(new ControlBundle)
        
        val wen = Output(Bool())
        val wAddr = Output(UInt(6.W))
        val wData = Output(UInt(32.W))

        val fwdWen = Output(Bool())
        val fwdAddr = Output(UInt(6.W))
        val fwdData = Output(UInt(32.W))
    })

    io.wAddr := io.rd
    io.wData := io.aluOut
    io.wen := io.ctrl.regWrite

    io.fwdWen := RegNext(io.ctrl.regWrite, false.B)
    io.fwdAddr := RegNext(io.rd, 0.U)
    io.fwdData := RegNext(io.aluOut, 0.U)
}