package pokit.core

import chisel3._
import pokit.core.stage._
import pokit.core.stage.register._

class Pipeline extends Module {
    val io = IO(new Bundle {
        val instr = Input(UInt(32.W)) // テスト用: 命令注入
        val testMode = Input(Bool()) // 追加: テストモード
        val branchPC = Input(UInt(32.W))
        val branchValid = Input(Bool())
        
        // 追加: レジスタ検証用デバッグポート
        val dbgRegAddr = Input(UInt(6.W))
        val dbgRegData = Output(UInt(32.W))
        // 追加: レジスタ初期化用
        val initRegWen = Input(Bool())
        val initRegAddr = Input(UInt(6.W))
        val initRegData = Input(UInt(32.W))
    })

    val ifStage = Module(new IF)
    val idStage = Module(new ID)
    val idexReg = Module(new IDEXReg)
    val exStage = Module(new EX)
    val exmemReg = Module(new EXMEMReg)
    val memStage = Module(new MEM)
    val wbStage = Module(new WB)
    val regFile = Module(new RegFile)
    
    // 追加: メモリモジュール
    val imem = Module(new IMem(4096))
    val dmem = Module(new DMem(4096))

    // IF -> ID
    // 命令メモリへの接続
    imem.io.addr := ifStage.io.pc
    ifStage.io.instr := Mux(io.testMode, io.instr, imem.io.instr)
    
    ifStage.io.branchPC := io.branchPC
    ifStage.io.branchValid := io.branchValid
    idStage.io.instrIn := ifStage.io.instrOut
    
    // RegFile
    regFile.io.rAddr1 := idStage.io.rAddr1
    regFile.io.rAddr2 := idStage.io.rAddr2
    idStage.io.rData1 := regFile.io.rData1
    idStage.io.rData2 := regFile.io.rData2
    
    // Debug/Initポート接続
    regFile.io.dbgAddr := io.dbgRegAddr
    io.dbgRegData := regFile.io.dbgData
    regFile.io.initWen := io.initRegWen
    regFile.io.initAddr := io.initRegAddr
    regFile.io.initData := io.initRegData
    
    // ID -> IDEXReg
    idexReg.io.inRs1 := idStage.io.rs1
    idexReg.io.inRs2 := idStage.io.rs2
    idexReg.io.inImm := idStage.io.imm
    idexReg.io.inCtrl := idStage.io.ctrl
    idexReg.io.inRd := idStage.io.rd
    idexReg.io.inPc := idStage.io.instrIn.pc // IFから渡されたPCをEXへ渡す
    
    // IDEXReg -> EX
    exStage.io.pc := idexReg.io.outPc
    exStage.io.rs1 := idexReg.io.outRs1
    exStage.io.rs2 := idexReg.io.outRs2
    exStage.io.imm := idexReg.io.outImm
    exStage.io.ctrl := idexReg.io.outCtrl

    // IFへの分岐信号
    ifStage.io.branchPC := exStage.io.branchTarget
    ifStage.io.branchValid := exStage.io.branchTaken
    
    // EX -> EXMEMReg
    exmemReg.io.inAluOut := exStage.io.aluOut
    exmemReg.io.inRs2 := idexReg.io.outRs2
    exmemReg.io.inRd := idexReg.io.outRd
    exmemReg.io.inCtrl := idexReg.io.outCtrl
    
    // EXMEMReg -> MEM -> WB
    memStage.io.aluOut := exmemReg.io.outAluOut
    memStage.io.rs2 := exmemReg.io.outRs2
    memStage.io.ctrl := exmemReg.io.outCtrl
    
    // メモリ接続
    dmem.io.addr := memStage.io.memAddr
    dmem.io.wData := memStage.io.memWData
    dmem.io.wen := memStage.io.memWen
    memStage.io.memRData := dmem.io.rData
    
    wbStage.io.aluOut := memStage.io.memOut
    wbStage.io.rd := exmemReg.io.outRd
    wbStage.io.ctrl := exmemReg.io.outCtrl
    
    // WB -> RegFile
    regFile.io.wen := wbStage.io.wen
    regFile.io.wAddr := wbStage.io.wAddr
    regFile.io.wData := wbStage.io.wData
}



