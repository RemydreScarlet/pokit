package pokit.core

import chisel3._
import chisel3.util.experimental.loadMemoryFromFile

class IMem(size: Int) extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(32.W))
        val instr = Output(UInt(32.W))
    })

    val mem = Mem(size, UInt(32.W))
    // Load from file if needed, for testing
    // loadMemoryFromFile(mem, "path/to/test.hex") 
    
    io.instr := mem(io.addr(31, 2))
}

class DMem(size: Int) extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(32.W))
        val wen = Input(Bool())
        val wData = Input(UInt(32.W))
        val rData = Output(UInt(32.W))
    })

    val mem = Mem(size, UInt(32.W))
    
    when(io.wen) {
        mem(io.addr(31, 2)) := io.wData
    }
    
    io.rData := mem(io.addr(31, 2))
}
