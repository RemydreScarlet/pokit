package pokit.core.stage

import chisel3._

class ID extends Module {
    val io = IO(new Bundle {
        val instrIn = Input(new PipelineStageIO)
        val rs1 = Output(UInt(32.W))
        val rs2 = Output(UInt(32.W))
    })

    // 2スレッド分のレジスタファイル (32 * 32bit * 2)
    val regFile = Mem(64, UInt(32.W))

    // デコード: rs1=bits[19:15], rs2=bits[24:20]
    val rs1Addr = io.instrIn.instr(19, 15)
    val rs2Addr = io.instrIn.instr(24, 20)

    // スレッドIDを使ってバンクを選択 (Thread0: 0-31, Thread1: 32-63)
    val regBase = io.instrIn.threadIdx << 5 
    
    // R0は常に0
    val rs1ReadAddr = Mux(rs1Addr === 0.U, 0.U, regBase | rs1Addr)
    val rs2ReadAddr = Mux(rs2Addr === 0.U, 0.U, regBase | rs2Addr)

    io.rs1 := regFile(rs1ReadAddr)
    io.rs2 := regFile(rs2ReadAddr)
}
