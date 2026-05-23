package pokit.core

import chisel3._
import chisel3.util._

class IMem(size: Int) extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(32.W))
        val instr = Output(UInt(32.W))
        val initWen = Input(Bool())
        val initAddr = Input(UInt(32.W))
        val initData = Input(UInt(32.W))
    })

    val mem = Mem(size, UInt(32.W))
    val addrBits = log2Ceil(size)

    when(io.initWen) {
        mem(io.initAddr(addrBits + 1, 2)) := io.initData
    }

    io.instr := mem(io.addr(addrBits + 1, 2))
}

class DMem(size: Int) extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(32.W))
        val wen = Input(Bool())
        val byteWen = Input(UInt(4.W))
        val wData = Input(UInt(32.W))
        val rData = Output(UInt(32.W))
        val initWen = Input(Bool())
        val initAddr = Input(UInt(32.W))
        val initData = Input(UInt(32.W))
        val dbgAddr = Input(UInt(32.W))
        val dbgData = Output(UInt(32.W))
    })

    val mem = Mem(size, UInt(32.W))
    val addrBits = log2Ceil(size)

    when(io.initWen) {
        mem(io.initAddr(addrBits + 1, 2)) := io.initData
    }.elsewhen(io.wen) {
        val wordIdx = io.addr(addrBits + 1, 2)
        val oldWord = mem(wordIdx)
        val byteMask = Cat(
            Fill(8, io.byteWen(3)),
            Fill(8, io.byteWen(2)),
            Fill(8, io.byteWen(1)),
            Fill(8, io.byteWen(0))
        )
        mem(wordIdx) := (oldWord & ~byteMask) | (io.wData & byteMask)
    }

    io.rData := mem(io.addr(addrBits + 1, 2))
    io.dbgData := mem(io.dbgAddr(addrBits + 1, 2))
}
