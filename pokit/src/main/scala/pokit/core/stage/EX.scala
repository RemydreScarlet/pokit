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
    val threadIdx = Input(UInt(1.W))
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

    val predTaken = Input(Bool())
    val predTarget = Input(UInt(32.W))
    val redirectValid = Output(Bool())
    val redirectTarget = Output(UInt(32.W))

    val bpuUpdateValid = Output(Bool())
    val bpuUpdatePC = Output(UInt(32.W))
    val bpuUpdateTaken = Output(Bool())
    val bpuUpdateTarget = Output(UInt(32.W))

    val dbgBranchMispred = Output(Bool())
    val dbgFalsePositive = Output(Bool())
    val dbgTargetMismatch = Output(Bool())
    val dbgIsJALR = Output(Bool())
}

class EX extends Module {
    val io = IO(new EXIO)

    val exmemFwdData = Mux(io.exmemMemToReg, io.memOut, io.exmemAluOut)

    val fullRs1Addr = io.threadIdx ## io.rs1Addr
    val fullRs2Addr = io.threadIdx ## io.rs2Addr

    val fwdRs1 = MuxCase(io.rs1, Seq(
        (io.rs1Addr =/= 0.U && fullRs1Addr === io.exmemRd && io.exmemRegWrite) -> exmemFwdData,
        (io.rs1Addr =/= 0.U && fullRs1Addr === io.wbRd && io.wbRegWrite) -> io.wbData
    ))
    val fwdRs2 = MuxCase(io.rs2, Seq(
        (io.rs2Addr =/= 0.U && fullRs2Addr === io.exmemRd && io.exmemRegWrite) -> exmemFwdData,
        (io.rs2Addr =/= 0.U && fullRs2Addr === io.wbRd && io.wbRegWrite) -> io.wbData
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
    val actualTaken = doBranch || doJump
    io.branchTaken := actualTaken

    val branchTarget = Mux(doJump && io.ctrl.aluSrc,
        (fwdRs1 + io.imm) & "hFFFFFFFE".U(32.W),
        io.pc + io.imm
    )
    io.branchTarget := branchTarget

    val isBranchLike = io.ctrl.branch || io.ctrl.jump
    val isJALR = doJump && io.ctrl.aluSrc
    val branchMispred = isBranchLike && (io.predTaken =/= actualTaken)
    val falsePositive = !isBranchLike && io.predTaken
    val targetMismatch = actualTaken && io.predTaken && (io.predTarget =/= branchTarget) && !isJALR

    io.redirectValid := branchMispred || falsePositive || isJALR || targetMismatch
    io.redirectTarget := Mux(actualTaken, branchTarget, io.pc + 4.U)

    io.dbgBranchMispred := branchMispred
    io.dbgFalsePositive := falsePositive
    io.dbgTargetMismatch := targetMismatch
    io.dbgIsJALR := isJALR

    io.bpuUpdateValid := isBranchLike || io.predTaken
    io.bpuUpdatePC := io.pc
    io.bpuUpdateTaken := actualTaken
    io.bpuUpdateTarget := branchTarget

    val shamt = op2(4, 0)

    // M-extension: multiplier
    val mulFull = fwdRs1 * op2
    val mulhFull = fwdRs1.asSInt * op2.asSInt
    val mulhuHi = mulFull(63, 32)
    val mulhsuCorr = Mux(fwdRs1(31), op2, 0.U)
    val mulhsuResult = mulhuHi - mulhsuCorr

    // M-extension: divider (edge cases: x/0=-1, INT_MIN/-1=INT_MIN, x%0=x, INT_MIN%-1=0)
    val divSigned = Mux(op2 === 0.U, "hFFFFFFFF".U,
        Mux(fwdRs1 === "h80000000".U && op2 === "hFFFFFFFF".U, "h80000000".U,
            (fwdRs1.asSInt / op2.asSInt).asUInt))
    val divUnsigned = Mux(op2 === 0.U, "hFFFFFFFF".U, fwdRs1 / op2)
    val remSigned = Mux(op2 === 0.U, fwdRs1,
        Mux(fwdRs1 === "h80000000".U && op2 === "hFFFFFFFF".U, 0.U,
            (fwdRs1.asSInt % op2.asSInt).asUInt))
    val remUnsigned = Mux(op2 === 0.U, fwdRs1, fwdRs1 % op2)

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
        14.U -> Mux(io.ctrl.aluOp(3), (fwdRs1.asSInt >> shamt).asUInt, (fwdRs1 >> shamt)),
        16.U -> mulFull(31, 0),
        17.U -> mulhFull(63, 32).asUInt,
        18.U -> mulhsuResult,
        19.U -> mulhuHi,
        20.U -> divSigned,
        21.U -> divUnsigned,
        22.U -> remSigned,
        23.U -> remUnsigned
    ))

    io.fwdRs2Out := fwdRs2
    io.dbgFwdRs1 := fwdRs1
    io.dbgFwdRs2 := fwdRs2
    io.dbgOp2 := op2
    io.aluOut := aluOut


}
