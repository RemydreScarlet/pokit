package pokit.core.stage

import chisel3._
import pokit.core._

class WB extends Module {
    val io = IO(new Bundle {
        val aluOut = Input(UInt(32.W))
        val rd = Input(UInt(6.W))
        val ctrl = Input(new ControlBundle)
        
        // RegFileへの書き込み出力
        val wen = Output(Bool())
        val wAddr = Output(UInt(6.W))
        val wData = Output(UInt(32.W))
    })

    io.wen := io.ctrl.regWrite
    io.wAddr := io.rd
    io.wData := io.aluOut
}
