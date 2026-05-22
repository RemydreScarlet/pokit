package pokit.core

import chisel3._
import pokit.core.stage._

class Pipeline extends Module {
    val io = IO(new Bundle {
        val start = Input(Bool())
    })

    val ifStage = Module(new IF)
    val idStage = Module(new ID)
    val exStage = Module(new EX)
    val memStage = Module(new MEM)
    val wbStage = Module(new WB)

    // 配線の接続
    // 命令メモリからの命令入力 (仮)
    ifStage.io.instr := 0.U 
    ifStage.io.branchPC := 0.U
    ifStage.io.branchValid := false.B
    
    idStage.io.instrIn := ifStage.io.instrOut
    
    exStage.io.rs1 := idStage.io.rs1
    exStage.io.rs2 := idStage.io.rs2
    exStage.io.aluOp := 0.U
    
    memStage.io.aluOut := exStage.io.aluOut
    wbStage.io.memOut := memStage.io.memOut

}
