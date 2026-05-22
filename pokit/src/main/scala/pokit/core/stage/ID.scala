package pokit.core.stage

import chisel3._
import chisel3.util._
import pokit.core._

class ID extends Module {
    val io = IO(new Bundle {
        val instrIn = Input(new PipelineStageIO)
        val rAddr1 = Output(UInt(6.W))
        val rAddr2 = Output(UInt(6.W))
        val rData1 = Input(UInt(32.W))
        val rData2 = Input(UInt(32.W))
        val rs1 = Output(UInt(32.W))
        val rs2 = Output(UInt(32.W))
        val imm = Output(UInt(32.W))
        val ctrl = Output(new ControlBundle)
        val rd = Output(UInt(6.W))
    })

    // デコードフィールド
    val opcode = io.instrIn.instr(6, 0)
    val funct3 = io.instrIn.instr(14, 12)
    val rs1Addr = io.instrIn.instr(19, 15)
    val rs2Addr = io.instrIn.instr(24, 20)
    val rdAddr  = io.instrIn.instr(11, 7)

    // スレッドIDを使ってバンクを選択
    val regBase = io.instrIn.threadIdx << 5 
    
    io.rAddr1 := regBase | rs1Addr
    io.rAddr2 := regBase | rs2Addr
    io.rs1 := io.rData1
    io.rs2 := io.rData2
    io.rd := regBase | rdAddr

    // 即値抽出 (I-type: [31:20])
    io.imm := Cat(Fill(20, io.instrIn.instr(31)), io.instrIn.instr(31, 20))

    // 制御信号デコード
    io.ctrl.regWrite := (opcode === "b0110011".U || opcode === "b0010011".U)
    io.ctrl.aluSrc := (opcode === "b0010011".U) // I-type
    
    // ALU操作信号 (ADD=0, SUB=1)
    io.ctrl.aluOp := Mux(opcode === "b0110011".U && funct3 === "b000".U && io.instrIn.instr(30), 1.U, 0.U)
}
