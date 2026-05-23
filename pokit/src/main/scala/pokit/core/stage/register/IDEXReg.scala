package pokit.core.stage.register

import chisel3._
import pokit.core._

class IDEXReg extends Module {
    val io = IO(new Bundle {
        val inRs1 = Input(UInt(32.W))
        val inRs2 = Input(UInt(32.W))
        val inImm = Input(UInt(32.W))
        val inCtrl = Input(new ControlBundle)
        val inRd = Input(UInt(6.W))
        val inPc = Input(UInt(32.W))
        val inRs1Addr = Input(UInt(5.W))
        val inRs2Addr = Input(UInt(5.W))
        val inPredTaken = Input(Bool())
        val inPredTarget = Input(UInt(32.W))
        val inThreadIdx = Input(UInt(1.W))
        val flush = Input(Bool())

        val outRs1 = Output(UInt(32.W))
        val outRs2 = Output(UInt(32.W))
        val outImm = Output(UInt(32.W))
        val outCtrl = Output(new ControlBundle)
        val outRd = Output(UInt(6.W))
        val outPc = Output(UInt(32.W))
        val outRs1Addr = Output(UInt(5.W))
        val outRs2Addr = Output(UInt(5.W))
        val outPredTaken = Output(Bool())
        val outPredTarget = Output(UInt(32.W))
        val outThreadIdx = Output(UInt(1.W))
    })

    val rs1Reg = RegInit(0.U(32.W))
    val rs2Reg = RegInit(0.U(32.W))
    val immReg = RegInit(0.U(32.W))
    val ctrlReg = RegInit(0.U.asTypeOf(new ControlBundle))
    val rdReg = RegInit(0.U(6.W))
    val pcReg = RegInit(0.U(32.W))
    val rs1AddrReg = RegInit(0.U(5.W))
    val rs2AddrReg = RegInit(0.U(5.W))
    val predTakenReg = RegInit(false.B)
    val predTargetReg = RegInit(0.U(32.W))
    val threadIdxReg = RegInit(0.U(1.W))

    rs1Reg := Mux(io.flush, 0.U, io.inRs1)
    rs2Reg := Mux(io.flush, 0.U, io.inRs2)
    immReg := Mux(io.flush, 0.U, io.inImm)
    ctrlReg := Mux(io.flush, 0.U.asTypeOf(new ControlBundle), io.inCtrl)
    rdReg := Mux(io.flush, 0.U, io.inRd)
    pcReg := Mux(io.flush, 0.U, io.inPc)
    rs1AddrReg := Mux(io.flush, 0.U, io.inRs1Addr)
    rs2AddrReg := Mux(io.flush, 0.U, io.inRs2Addr)
    predTakenReg := Mux(io.flush, false.B, io.inPredTaken)
    predTargetReg := Mux(io.flush, 0.U, io.inPredTarget)
    threadIdxReg := Mux(io.flush, 0.U, io.inThreadIdx)

    io.outRs1 := rs1Reg
    io.outRs2 := rs2Reg
    io.outImm := immReg
    io.outCtrl := ctrlReg
    io.outRd := rdReg
    io.outPc := pcReg
    io.outRs1Addr := rs1AddrReg
    io.outRs2Addr := rs2AddrReg
    io.outPredTaken := predTakenReg
    io.outPredTarget := predTargetReg
    io.outThreadIdx := threadIdxReg
}
