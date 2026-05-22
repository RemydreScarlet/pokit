package pokit.core.stage

import chisel3._
import chisel3.util._

class EXIO extends Bundle {
    val rs1 = Input(UInt(32.W))
    val rs2 = Input(UInt(32.W))
    val aluOp = Input(UInt(3.W)) // 命令の種類を識別する信号
    val aluOut = Output(UInt(32.W))
}

class EX extends Module {
    val io = IO(new EXIO)
    
    // 仮のALU: aluOp 0=ADD, 1=SUB, 2=AND
    io.aluOut := MuxCase(0.U, Seq(
        (io.aluOp === 0.U) -> (io.rs1 + io.rs2),
        (io.aluOp === 1.U) -> (io.rs1 - io.rs2),
        (io.aluOp === 2.U) -> (io.rs1 & io.rs2)
    ))
}
