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
        val rs1Addr = Output(UInt(5.W))
        val rs2Addr = Output(UInt(5.W))
    })

    val instr = io.instrIn.instr
    val opcode = instr(6, 0)
    val funct3 = instr(14, 12)
    val funct7 = instr(31, 25)
    val rs1Addr = instr(19, 15)
    val rs2Addr = instr(24, 20)
    val rdAddr  = instr(11, 7)
    val regBase = io.instrIn.threadIdx << 5

    io.rs1Addr := rs1Addr
    io.rs2Addr := rs2Addr

    io.rAddr1 := regBase | rs1Addr
    io.rAddr2 := regBase | rs2Addr
    io.rs1 := io.rData1
    io.rs2 := io.rData2
    io.rd := regBase | rdAddr

    val immI = Cat(Fill(20, instr(31)), instr(31, 20))
    val immS = Cat(Fill(20, instr(31)), instr(31, 25), instr(11, 7))
    val immB = Cat(Fill(20, instr(31)), instr(7), instr(30, 25), instr(11, 8), 0.U(1.W))
    val immU = Cat(instr(31, 12), 0.U(12.W))
    val immJ = Cat(Fill(12, instr(31)), instr(19, 12), instr(20), instr(30, 21), 0.U(1.W))

    io.ctrl.regWrite := false.B
    io.ctrl.aluSrc   := false.B
    io.ctrl.memRead  := false.B
    io.ctrl.memWrite := false.B
    io.ctrl.memToReg := false.B
    io.ctrl.branch   := false.B
    io.ctrl.jump     := false.B
    io.ctrl.aluOp    := 0.U

    io.imm := immI

    switch(opcode) {
        is("b0110011".U) { // R-type
            io.ctrl.regWrite := true.B
            when(funct3 === "b000".U) {
                io.ctrl.aluOp := Mux(instr(30), 1.U, 0.U)
            }
            when(funct3 === "b001".U) { io.ctrl.aluOp := 2.U }
            when(funct3 === "b010".U) { io.ctrl.aluOp := 3.U }
            when(funct3 === "b011".U) { io.ctrl.aluOp := 4.U }
            when(funct3 === "b100".U) { io.ctrl.aluOp := 5.U }
            when(funct3 === "b101".U) {
                io.ctrl.aluOp := Mux(instr(30), 6.U | 8.U, 6.U)
            }
            when(funct3 === "b110".U) { io.ctrl.aluOp := 7.U }
            when(funct3 === "b111".U) { io.ctrl.aluOp := 8.U }
        }
        is("b0010011".U) { // I-type (arithmetic)
            io.ctrl.regWrite := true.B
            io.ctrl.aluSrc   := true.B
            io.imm := immI
            when(funct3 === "b000".U) { io.ctrl.aluOp := 0.U }
            when(funct3 === "b001".U) { io.ctrl.aluOp := 2.U }
            when(funct3 === "b010".U) { io.ctrl.aluOp := 3.U }
            when(funct3 === "b011".U) { io.ctrl.aluOp := 4.U }
            when(funct3 === "b100".U) { io.ctrl.aluOp := 5.U }
            when(funct3 === "b101".U) {
                io.ctrl.aluOp := Mux(instr(30), 6.U | 8.U, 6.U)
            }
            when(funct3 === "b110".U) { io.ctrl.aluOp := 7.U }
            when(funct3 === "b111".U) { io.ctrl.aluOp := 8.U }
        }
        is("b0110111".U) { // LUI
            io.ctrl.regWrite := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 9.U
            io.imm := immU
        }
        is("b0010111".U) { // AUIPC
            io.ctrl.regWrite := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 10.U
            io.imm := immU
        }
        is("b1101111".U) { // JAL
            io.ctrl.regWrite := true.B
            io.ctrl.jump := true.B
            io.ctrl.aluOp := 11.U
            io.imm := immJ
        }
        is("b1100111".U) { // JALR
            io.ctrl.regWrite := true.B
            io.ctrl.jump := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 11.U
            io.imm := immI
        }
        is("b1100011".U) { // BRANCH
            io.ctrl.branch := true.B
            io.ctrl.aluOp := Cat(0.U(1.W), funct3)
            io.imm := immB
        }
        is("b0000011".U) { // LOAD
            io.ctrl.regWrite := true.B
            io.ctrl.memRead := true.B
            io.ctrl.memToReg := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 0.U
            io.imm := immI
        }
        is("b0100011".U) { // STORE
            io.ctrl.memWrite := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 0.U
            io.imm := immS
        }
        is("b0001111".U) { // FENCE
        }
        is("b1110011".U) { // SYSTEM (ECALL/EBREAK/CSR)
        }
    }
}
