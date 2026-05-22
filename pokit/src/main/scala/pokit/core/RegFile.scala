package pokit.core

import chisel3._

class RegFile extends Module {
    val io = IO(new Bundle {
        // Read Port (for ID)
        val rAddr1 = Input(UInt(6.W)) // ThreadId(1bit) + Addr(5bit)
        val rAddr2 = Input(UInt(6.W))
        val rData1 = Output(UInt(32.W))
        val rData2 = Output(UInt(32.W))
        
        // Debug/Init Port (for Test)
        val dbgAddr = Input(UInt(6.W))
        val dbgData = Output(UInt(32.W))
        val initWen = Input(Bool())
        val initAddr = Input(UInt(6.W))
        val initData = Input(UInt(32.W))
        
        // Write Port (for WB)
        val wen = Input(Bool())
        val wAddr = Input(UInt(6.W))
        val wData = Input(UInt(32.W))
    })

    val regs = Mem(64, UInt(32.W))
    
    // Read
    io.rData1 := Mux(io.rAddr1 === 0.U, 0.U, regs(io.rAddr1))
    io.rData2 := Mux(io.rAddr2 === 0.U, 0.U, regs(io.rAddr2))
    
    // Debug Read
    io.dbgData := Mux(io.dbgAddr === 0.U, 0.U, regs(io.dbgAddr))
    
    // Write (WB or Init)
    when(io.wen && io.wAddr =/= 0.U) {
        regs(io.wAddr) := io.wData
    }.elsewhen(io.initWen && io.initAddr =/= 0.U) {
        regs(io.initAddr) := io.initData
    }
}
