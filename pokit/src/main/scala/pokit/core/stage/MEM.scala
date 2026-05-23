package pokit.core.stage

import chisel3._
import chisel3.util._
import pokit.core._

class MEM extends Module {
    val io = IO(new Bundle {
        val aluOut = Input(UInt(32.W))
        val rs2    = Input(UInt(32.W))
        val ctrl   = Input(new ControlBundle)
        val memOut = Output(UInt(32.W))
        
        val memAddr = Output(UInt(32.W))
        val memWData = Output(UInt(32.W))
        val memRData = Input(UInt(32.W))
        val memWen   = Output(Bool())
        val memByteWen = Output(UInt(4.W))
    })

    io.memAddr := io.aluOut
    io.memWen := io.ctrl.memWrite

    val byteShift = io.aluOut(1, 0) << 3.U
    val halfShift = io.aluOut(1) << 4.U

    io.memByteWen := Mux(io.ctrl.memWrite, MuxLookup(io.ctrl.memSize, 0xf.U)(Seq(
        0.U -> (1.U << io.aluOut(1, 0)),
        1.U -> Mux(io.aluOut(1), 0xc.U, 0x3.U),
        2.U -> 0xf.U
    )), 0.U)

    io.memWData := Mux(io.ctrl.memWrite, MuxLookup(io.ctrl.memSize, io.rs2)(Seq(
        0.U -> (io.rs2(7, 0) << byteShift),
        1.U -> (io.rs2(15, 0) << halfShift),
        2.U -> io.rs2
    )), io.rs2)

    val loadShifted = io.memRData >> Mux(io.ctrl.memSize === 0.U, byteShift, halfShift)
    val loadFormatted = MuxLookup(io.ctrl.memSize, 0.U(32.W))(Seq(
        0.U -> Mux(io.ctrl.memSigned,
            Cat(Fill(24, loadShifted(7)), loadShifted(7, 0)),
            loadShifted(7, 0)),
        1.U -> Mux(io.ctrl.memSigned,
            Cat(Fill(16, loadShifted(15)), loadShifted(15, 0)),
            loadShifted(15, 0)),
        2.U -> io.memRData
    ))

    io.memOut := Mux(io.ctrl.memToReg, loadFormatted, io.aluOut)
}
