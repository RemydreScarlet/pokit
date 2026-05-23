package pokit.core.stage

import chisel3._
import chisel3.util._
import pokit.core._

class EXIO extends Bundle {
    val pc = Input(UInt(32.W)) // PCを追加
    val rs1 = Input(UInt(32.W))
    val rs2 = Input(UInt(32.W))
    val imm = Input(UInt(32.W))
    val ctrl = Input(new ControlBundle)
    val aluOut = Output(UInt(32.W))
    val branchTaken = Output(Bool()) // 分岐成立フラグ
    val branchTarget = Output(UInt(32.W)) // 分岐先PC
}

class EX extends Module {
    val io = IO(new EXIO)
    
    // ALUのオペランド選択
    val op2 = Mux(io.ctrl.aluSrc, io.imm, io.rs2)
    
    // 分岐判定 (B-type)
    val branchCond = MuxLookup(io.ctrl.aluOp(2,0), false.B)(Seq(
        0.U -> (io.rs1 === io.rs2), // BEQ
        1.U -> (io.rs1 =/= io.rs2)  // BNE
    ))
    io.branchTaken := io.ctrl.branch && branchCond
    io.branchTarget := io.pc + io.imm // 単純な分岐先計算
    
    val aluOut = Wire(UInt(32.W))
    
    // シフト用
    val shamt = op2(4, 0)
    
    aluOut := MuxLookup(io.ctrl.aluOp, 0.U)(Seq(
        0.U -> (io.rs1 + op2),
        1.U -> (io.rs1 - op2),
        2.U -> (io.rs1 << shamt),
        3.U -> (io.rs1.asSInt < op2.asSInt).asUInt,
        4.U -> (io.rs1 < op2).asUInt,
        5.U -> (io.rs1 ^ op2),
        6.U -> Mux(io.ctrl.aluOp(3), (io.rs1.asSInt >> shamt).asUInt, (io.rs1 >> shamt)), // SRL/SRA (aluOp[3]で使い分け)
        7.U -> (io.rs1 | op2),
        8.U -> (io.rs1 & op2)
    ))

    io.aluOut := aluOut
}
