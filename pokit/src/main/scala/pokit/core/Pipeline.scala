package pokit.core

import chisel3._
import pokit.core.stage._
import pokit.core.stage.register._

class Pipeline extends Module {
    val io = IO(new Bundle {
        val instr = Input(UInt(32.W))
        val testMode = Input(Bool())
        val branchPC = Input(UInt(32.W))
        val branchValid = Input(Bool())

        val dbgRegAddr = Input(UInt(6.W))
        val dbgRegData = Output(UInt(32.W))
        val initRegWen = Input(Bool())
        val initRegAddr = Input(UInt(6.W))
        val initRegData = Input(UInt(32.W))

        val imemInitWen = Input(Bool())
        val imemInitAddr = Input(UInt(32.W))
        val imemInitData = Input(UInt(32.W))

        val dmemInitWen = Input(Bool())
        val dmemInitAddr = Input(UInt(32.W))
        val dmemInitData = Input(UInt(32.W))

        val dmemDbgAddr = Input(UInt(32.W))
        val dmemDbgData = Output(UInt(32.W))
        val singleThread = Input(Bool())
        val dbgFwdRs1 = Output(UInt(32.W))
        val dbgFwdRs2 = Output(UInt(32.W))
        val dbgAluOut = Output(UInt(32.W))
        val dbgExAluOut = Output(UInt(32.W))
        val dbgExRd = Output(UInt(6.W))
        val dbgExRegWrite = Output(Bool())
        val dbgWbAddr = Output(UInt(6.W))
        val dbgWbData = Output(UInt(32.W))
        val dbgWbWen = Output(Bool())
        val dbgPc = Output(UInt(32.W))
        val dbgRedirect = Output(Bool())
        val dbgBranchValid = Output(Bool())
        val dbgBpuHit = Output(Bool())
        val dbgRedirectMispred = Output(Bool())
        val dbgRedirectFalsePos = Output(Bool())
        val dbgRedirectTargetMis = Output(Bool())
        val dbgRedirectJALR = Output(Bool())
        val dbgThreadIdx = Output(UInt(1.W))
    })

    val ifStage = Module(new IF)
    val idStage = Module(new ID)
    val idexReg = Module(new IDEXReg)
    val exStage = Module(new EX)
    val exmemReg = Module(new EXMEMReg)
    val memStage = Module(new MEM)
    val wbStage = Module(new WB)
    val regFile = Module(new RegFile)

    val imem = Module(new IMem(16384)) // 64KB (16384 words)
    val dmem = Module(new DMem(16384)) // 64KB (16384 words)
    val bpu = Module(new BranchPredictor)

    ifStage.io.singleThread := io.singleThread

    imem.io.initWen := io.imemInitWen
    imem.io.initAddr := io.imemInitAddr
    imem.io.initData := io.imemInitData

    dmem.io.initWen := io.dmemInitWen
    dmem.io.initAddr := io.dmemInitAddr
    dmem.io.initData := io.dmemInitData
    dmem.io.dbgAddr := io.dmemDbgAddr
    io.dmemDbgData := dmem.io.dbgData

    imem.io.addr := ifStage.io.pc
    ifStage.io.instr := Mux(io.testMode, io.instr, imem.io.instr)

    bpu.io.lookupPC := ifStage.io.pc
    bpu.io.lookupThread := ifStage.io.instrOut.threadIdx
    ifStage.io.predictTaken := bpu.io.predictTaken
    ifStage.io.predictTarget := bpu.io.predictTarget

    ifStage.io.branchPC := Mux(exStage.io.redirectValid, exStage.io.redirectTarget, io.branchPC)
    ifStage.io.branchValid := exStage.io.redirectValid || io.branchValid
    ifStage.io.branchThreadIdx := Mux(exStage.io.redirectValid, exStage.io.threadIdx, ifStage.io.instrOut.threadIdx)
    idStage.io.instrIn := ifStage.io.instrOut

    regFile.io.rAddr1 := idStage.io.rAddr1
    regFile.io.rAddr2 := idStage.io.rAddr2
    idStage.io.rData1 := regFile.io.rData1
    idStage.io.rData2 := regFile.io.rData2

    regFile.io.dbgAddr := io.dbgRegAddr
    io.dbgRegData := regFile.io.dbgData
    regFile.io.initWen := io.initRegWen
    regFile.io.initAddr := io.initRegAddr
    regFile.io.initData := io.initRegData

    idexReg.io.inRs1 := idStage.io.rs1
    idexReg.io.inRs2 := idStage.io.rs2
    idexReg.io.inImm := idStage.io.imm
    idexReg.io.inCtrl := idStage.io.ctrl
    idexReg.io.inRd := idStage.io.rd
    idexReg.io.inPc := idStage.io.instrIn.pc
    idexReg.io.inRs1Addr := idStage.io.rs1Addr
    idexReg.io.inRs2Addr := idStage.io.rs2Addr
    idexReg.io.inPredTaken := ifStage.io.predOut
    idexReg.io.inPredTarget := ifStage.io.predTargetOut
    idexReg.io.inThreadIdx := idStage.io.instrIn.threadIdx

    // Flush IDEX when the instruction in ID was fetched from the same thread
    // that caused the redirect (wrong-path instruction).
    // In single-thread mode this matches the old flush-on-redirect behavior.
    // In multi-thread mode, the other thread's instruction is correctly fetched
    // and should NOT be flushed.
    idexReg.io.flush := io.branchValid || (exStage.io.redirectValid && exStage.io.threadIdx === idStage.io.instrIn.threadIdx)

    // Per-thread memory pending tracking: mark thread blocked when ID decodes
    // a LOAD/STORE, unblock when MEM completes.
    val threadBlocked = RegInit(0.U(2.W))
    val setThreadBlocked = Mux(idStage.io.ctrl.memRead || idStage.io.ctrl.memWrite,
        1.U << idStage.io.instrIn.threadIdx, 0.U(2.W))
    val clearThreadBlocked = Mux(memStage.io.ctrl.memRead || memStage.io.ctrl.memWrite,
        1.U << exmemReg.io.outThreadIdx, 0.U(2.W))

    threadBlocked := (threadBlocked | setThreadBlocked) & ~(clearThreadBlocked & ~setThreadBlocked)

    ifStage.io.threadBlocked := threadBlocked

    exStage.io.pc := idexReg.io.outPc
    exStage.io.rs1 := idexReg.io.outRs1
    exStage.io.rs2 := idexReg.io.outRs2
    exStage.io.imm := idexReg.io.outImm
    exStage.io.ctrl := idexReg.io.outCtrl
    exStage.io.rs1Addr := idexReg.io.outRs1Addr
    exStage.io.rs2Addr := idexReg.io.outRs2Addr
    exStage.io.threadIdx := idexReg.io.outThreadIdx
    exStage.io.predTaken := idexReg.io.outPredTaken
    exStage.io.predTarget := idexReg.io.outPredTarget

    exStage.io.exmemRd := exmemReg.io.outRd
    exStage.io.exmemAluOut := exmemReg.io.outAluOut
    exStage.io.exmemRegWrite := exmemReg.io.outCtrl.regWrite
    exStage.io.exmemMemToReg := exmemReg.io.outCtrl.memToReg
    exStage.io.memOut := memStage.io.memOut

    exStage.io.wbRd := wbStage.io.fwdAddr
    exStage.io.wbData := wbStage.io.fwdData
    exStage.io.wbRegWrite := wbStage.io.fwdWen

    bpu.io.updateValid := exStage.io.bpuUpdateValid
    bpu.io.updatePC := exStage.io.bpuUpdatePC
    bpu.io.updateThread := idexReg.io.outThreadIdx
    bpu.io.updateTaken := exStage.io.bpuUpdateTaken
    bpu.io.updateTarget := exStage.io.bpuUpdateTarget

    exmemReg.io.inAluOut := exStage.io.aluOut
    exmemReg.io.inRs2 := exStage.io.fwdRs2Out
    exmemReg.io.inRd := idexReg.io.outRd
    exmemReg.io.inCtrl := idexReg.io.outCtrl
    exmemReg.io.inThreadIdx := idexReg.io.outThreadIdx

    memStage.io.aluOut := exmemReg.io.outAluOut
    memStage.io.rs2 := exmemReg.io.outRs2
    memStage.io.ctrl := exmemReg.io.outCtrl

    dmem.io.addr := memStage.io.memAddr
    dmem.io.wData := memStage.io.memWData
    dmem.io.wen := memStage.io.memWen
    dmem.io.byteWen := memStage.io.memByteWen
    memStage.io.memRData := dmem.io.rData

    wbStage.io.aluOut := memStage.io.memOut
    wbStage.io.rd := exmemReg.io.outRd
    wbStage.io.ctrl := exmemReg.io.outCtrl

    regFile.io.wen := wbStage.io.wen
    regFile.io.wAddr := wbStage.io.wAddr
    regFile.io.wData := wbStage.io.wData

    io.dbgFwdRs1 := exStage.io.dbgFwdRs1
    io.dbgFwdRs2 := exStage.io.dbgFwdRs2
    io.dbgAluOut := exStage.io.aluOut
    io.dbgExAluOut := exmemReg.io.outAluOut
    io.dbgExRd := exmemReg.io.outRd
    io.dbgExRegWrite := exmemReg.io.outCtrl.regWrite
    io.dbgWbAddr := wbStage.io.wAddr
    io.dbgWbData := wbStage.io.wData
    io.dbgWbWen := wbStage.io.wen
    io.dbgPc := ifStage.io.instrOut.pc
    io.dbgRedirect := exStage.io.redirectValid
    io.dbgBranchValid := io.branchValid
    io.dbgBpuHit := bpu.io.dbgHit
    io.dbgRedirectMispred := exStage.io.dbgBranchMispred
    io.dbgRedirectFalsePos := exStage.io.dbgFalsePositive
    io.dbgRedirectTargetMis := exStage.io.dbgTargetMismatch
    io.dbgRedirectJALR := exStage.io.dbgIsJALR
    io.dbgThreadIdx := ifStage.io.instrOut.threadIdx
}
