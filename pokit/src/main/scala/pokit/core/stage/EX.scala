package pokit.core.stage

import chisel3._
import chisel3.util._
import pokit.core._

class EXIO extends Bundle {
    val rs1 = Input(UInt(32.W))
    val rs2 = Input(UInt(32.W))
    val imm = Input(UInt(32.W))
    val ctrl = Input(new ControlBundle)
    val aluOut = Output(UInt(32.W))
}

class EX extends Module {
    val io = IO(new EXIO)
    
    // ALUのオペランド選択
    val op2 = Mux(io.ctrl.aluSrc, io.imm, io.rs2)
    
    // 仮のALU: aluOp 0=ADD, 1=SUB
    io.aluOut := MuxCase(0.U, Seq(
        (io.ctrl.aluOp === 0.U) -> (io.rs1 + op2),
        (io.ctrl.aluOp === 1.U) -> (io.rs1 - op2)
    ))
}
