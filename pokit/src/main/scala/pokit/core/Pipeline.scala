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

    ifStage.io.branchPC := Mux(exStage.io.branchTaken, exStage.io.branchTarget, io.branchPC)
    ifStage.io.branchValid := exStage.io.branchTaken || io.branchValid
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

    idexReg.io.flush := exStage.io.branchTaken || io.branchValid

    exStage.io.pc := idexReg.io.outPc
    exStage.io.rs1 := idexReg.io.outRs1
    exStage.io.rs2 := idexReg.io.outRs2
    exStage.io.imm := idexReg.io.outImm
    exStage.io.ctrl := idexReg.io.outCtrl
    exStage.io.rs1Addr := idexReg.io.outRs1Addr
    exStage.io.rs2Addr := idexReg.io.outRs2Addr

    exStage.io.exmemRd := exmemReg.io.outRd
    exStage.io.exmemAluOut := exmemReg.io.outAluOut
    exStage.io.exmemRegWrite := exmemReg.io.outCtrl.regWrite
    exStage.io.exmemMemToReg := exmemReg.io.outCtrl.memToReg
    exStage.io.memOut := memStage.io.memOut

    exStage.io.wbRd := wbStage.io.fwdAddr
    exStage.io.wbData := wbStage.io.fwdData
    exStage.io.wbRegWrite := wbStage.io.fwdWen

    exmemReg.io.inAluOut := exStage.io.aluOut
    exmemReg.io.inRs2 := exStage.io.fwdRs2Out
    exmemReg.io.inRd := idexReg.io.outRd
    exmemReg.io.inCtrl := idexReg.io.outCtrl

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
}
