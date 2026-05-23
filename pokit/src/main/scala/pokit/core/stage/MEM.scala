package pokit.core.stage

import chisel3._
import pokit.core._

class MEM extends Module {
    val io = IO(new Bundle {
        val aluOut = Input(UInt(32.W))
        val rs2    = Input(UInt(32.W)) // Storeデータ
        val ctrl   = Input(new ControlBundle)
        val memOut = Output(UInt(32.W))
        
        // メモリインターフェース
        val memAddr = Output(UInt(32.W))
        val memWData = Output(UInt(32.W))
        val memRData = Input(UInt(32.W))
        val memWen   = Output(Bool())
    })

    io.memAddr := io.aluOut
    io.memWData := io.rs2
    io.memWen := io.ctrl.memWrite
    
    // LOAD時はメモリから、その他はALU出力（aluOut）を選択
    io.memOut := Mux(io.ctrl.memToReg, io.memRData, io.aluOut)
}
