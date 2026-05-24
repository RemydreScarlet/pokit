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

        // A-extension: reservation management
        val threadIdx = Input(UInt(1.W))
        val lrValid   = Input(Bool())
        val lrSet     = Output(Bool())
        val lrAddr    = Output(UInt(32.W))
        val scExe     = Output(Bool())
        val writeAddr = Output(UInt(32.W))
        val writeActive = Output(Bool())
    })

    val funct5 = io.ctrl.amoOp
    val isLR = funct5 === "b00010".U
    val isSC = funct5 === "b00011".U
    val isAMO = io.ctrl.memRead && io.ctrl.memWrite

    io.memAddr := io.aluOut

    val scResOK = !isSC || io.lrValid
    val memWenActual = io.ctrl.memWrite && (!isSC || io.lrValid)
    io.memWen := memWenActual

    val byteShift = io.aluOut(1, 0) << 3.U
    val halfShift = io.aluOut(1) << 4.U

    io.memByteWen := Mux(io.ctrl.memWrite, MuxLookup(io.ctrl.memSize, 0xf.U)(Seq(
        0.U -> (1.U << io.aluOut(1, 0)),
        1.U -> Mux(io.aluOut(1), 0xc.U, 0x3.U),
        2.U -> 0xf.U
    )), 0.U)

    // AMO ALU
    val oldData = io.memRData
    val amoResult = MuxLookup(funct5, oldData)(Seq(
        "b00000".U -> (oldData + io.rs2),             // AMOADD
        "b00001".U -> io.rs2,                         // AMOSWAP
        "b00100".U -> (oldData ^ io.rs2),             // AMOXOR
        "b01000".U -> (oldData & io.rs2),             // AMOAND
        "b01100".U -> (oldData | io.rs2),             // AMOOR
        "b10000".U -> Mux((oldData.asSInt < io.rs2.asSInt), oldData, io.rs2), // AMOMIN
        "b11000".U -> Mux((oldData.asSInt >= io.rs2.asSInt), oldData, io.rs2), // AMOMAX
        "b10001".U -> Mux(oldData < io.rs2, oldData, io.rs2),  // AMOMINU
        "b11001".U -> Mux(oldData >= io.rs2, oldData, io.rs2), // AMOMAXU
    ))

    // Write data: for AMO write amoResult, for regular store/SC write rs2 (possibly shifted)
    io.memWData := Mux(isAMO, amoResult,
        Mux(io.ctrl.memWrite, MuxLookup(io.ctrl.memSize, io.rs2)(Seq(
            0.U -> (io.rs2(7, 0) << byteShift),
            1.U -> (io.rs2(15, 0) << halfShift),
            2.U -> io.rs2
        )), io.rs2))

    // Load formatting (word = full memRData)
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

    val scResult = Mux(scResOK, 0.U, 1.U)
    io.memOut := Mux(isLR, oldData,
                 Mux(isSC, scResult,
                 Mux(isAMO, oldData,
                 Mux(io.ctrl.memToReg, loadFormatted, io.aluOut))))

    // Reservation management outputs
    io.lrSet := isLR
    io.lrAddr := io.aluOut
    io.scExe := isSC
    io.writeAddr := io.aluOut
    io.writeActive := memWenActual
}
