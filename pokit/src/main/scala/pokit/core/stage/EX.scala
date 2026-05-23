package pokit.core.stage

import chisel3._
import chisel3.util._
import pokit.core._

class EXIO extends Bundle {
    val pc = Input(UInt(32.W))
    val rs1 = Input(UInt(32.W))
    val rs2 = Input(UInt(32.W))
    val imm = Input(UInt(32.W))
    val ctrl = Input(new ControlBundle)
    val aluOut = Output(UInt(32.W))
    val branchTaken = Output(Bool())
    val branchTarget = Output(UInt(32.W))
    val rs1Addr = Input(UInt(5.W))
    val rs2Addr = Input(UInt(5.W))
    val exmemRd = Input(UInt(6.W))
    val exmemAluOut = Input(UInt(32.W))
    val exmemRegWrite = Input(Bool())
    val exmemMemToReg = Input(Bool())
    val memOut = Input(UInt(32.W))
    val wbRd = Input(UInt(6.W))
    val wbData = Input(UInt(32.W))
    val wbRegWrite = Input(Bool())
    val fwdRs2Out = Output(UInt(32.W))
    val dbgFwdRs1 = Output(UInt(32.W))
    val dbgFwdRs2 = Output(UInt(32.W))
    val dbgOp2 = Output(UInt(32.W))
}

class EX extends Module {
    val io = IO(new EXIO)

    val exmemFwdData = Mux(io.exmemMemToReg, io.memOut, io.exmemAluOut)

    val fwdRs1 = MuxCase(io.rs1, Seq(
        (io.rs1Addr =/= 0.U && io.rs1Addr === io.exmemRd(4, 0) && io.exmemRegWrite) -> exmemFwdData,
        (io.rs1Addr =/= 0.U && io.rs1Addr === io.wbRd(4, 0) && io.wbRegWrite) -> io.wbData
    ))
    val fwdRs2 = MuxCase(io.rs2, Seq(
        (io.rs2Addr =/= 0.U && io.rs2Addr === io.exmemRd(4, 0) && io.exmemRegWrite) -> exmemFwdData,
        (io.rs2Addr =/= 0.U && io.rs2Addr === io.wbRd(4, 0) && io.wbRegWrite) -> io.wbData
    ))

    val op2 = Mux(io.ctrl.aluSrc, io.imm, fwdRs2)

    val brType = io.ctrl.aluOp(2, 0)
    val branchCond = Wire(Bool())
    branchCond := false.B
    switch(brType) {
        is(0.U) { branchCond := fwdRs1 === fwdRs2 }
        is(1.U) { branchCond := fwdRs1 =/= fwdRs2 }
        is(4.U) { branchCond := (fwdRs1.asSInt < fwdRs2.asSInt) }
        is(5.U) { branchCond := (fwdRs1.asSInt >= fwdRs2.asSInt) }
        is(6.U) { branchCond := (fwdRs1 < fwdRs2) }
        is(7.U) { branchCond := (fwdRs1 >= fwdRs2) }
    }

    val doBranch = io.ctrl.branch && branchCond
    val doJump = io.ctrl.jump
    io.branchTaken := doBranch || doJump

    val branchTarget = Mux(doJump && io.ctrl.aluSrc,
        (fwdRs1 + io.imm) & "hFFFFFFFE".U(32.W),
        io.pc + io.imm
    )
    io.branchTarget := branchTarget

    val shamt = op2(4, 0)

    val aluOut = Wire(UInt(32.W))
    aluOut := MuxLookup(io.ctrl.aluOp, 0.U)(Seq(
        0.U -> (fwdRs1 + op2),
        1.U -> (fwdRs1 - op2),
        2.U -> (fwdRs1 << shamt),
        3.U -> (fwdRs1.asSInt < op2.asSInt).asUInt,
        4.U -> (fwdRs1 < op2).asUInt,
        5.U -> (fwdRs1 ^ op2),
        6.U -> (fwdRs1 >> shamt),
        7.U -> (fwdRs1 | op2),
        8.U -> (fwdRs1 & op2),
        9.U -> io.imm,
        10.U -> (io.pc + io.imm),
        11.U -> (io.pc + 4.U),
        14.U -> Mux(io.ctrl.aluOp(3), (fwdRs1.asSInt >> shamt).asUInt, (fwdRs1 >> shamt))
    ))

    io.fwdRs2Out := fwdRs2
    io.dbgFwdRs1 := fwdRs1
    io.dbgFwdRs2 := fwdRs2
    io.dbgOp2 := op2
    io.aluOut := aluOut



}
