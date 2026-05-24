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
    io.ctrl.memSize  := 0.U
    io.ctrl.memSigned := false.B
    io.ctrl.branch   := false.B
    io.ctrl.jump     := false.B
    io.ctrl.aluOp    := 0.U
    io.ctrl.amoOp    := 0.U

    io.imm := immI

    switch(opcode) {
        is("b0110011".U) { // R-type (RV32I + M-extension)
            io.ctrl.regWrite := true.B
            when(instr(25)) { // M-extension (funct7[5] = 1)
                switch(funct3) {
                    is("b000".U) { io.ctrl.aluOp := 16.U } // MUL
                    is("b001".U) { io.ctrl.aluOp := 17.U } // MULH
                    is("b010".U) { io.ctrl.aluOp := 18.U } // MULHSU
                    is("b011".U) { io.ctrl.aluOp := 19.U } // MULHU
                    is("b100".U) { io.ctrl.aluOp := 20.U } // DIV
                    is("b101".U) { io.ctrl.aluOp := 21.U } // DIVU
                    is("b110".U) { io.ctrl.aluOp := 22.U } // REM
                    is("b111".U) { io.ctrl.aluOp := 23.U } // REMU
                }
            }.otherwise { // RV32I R-type
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
        is("b0000011".U) { // LOAD: lb, lh, lw, lbu, lhu
            io.ctrl.regWrite := true.B
            io.ctrl.memRead := true.B
            io.ctrl.memToReg := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 0.U
            io.imm := immI
            when(funct3 === "b000".U) { // lb
                io.ctrl.memSize := 0.U
                io.ctrl.memSigned := true.B
            }.elsewhen(funct3 === "b001".U) { // lh
                io.ctrl.memSize := 1.U
                io.ctrl.memSigned := true.B
            }.elsewhen(funct3 === "b010".U) { // lw
                io.ctrl.memSize := 2.U
                io.ctrl.memSigned := false.B
            }.elsewhen(funct3 === "b100".U) { // lbu
                io.ctrl.memSize := 0.U
                io.ctrl.memSigned := false.B
            }.elsewhen(funct3 === "b101".U) { // lhu
                io.ctrl.memSize := 1.U
                io.ctrl.memSigned := false.B
            }
        }
        is("b0100011".U) { // STORE: sb, sh, sw
            io.ctrl.memWrite := true.B
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 0.U
            io.imm := immS
            when(funct3 === "b000".U) { // sb
                io.ctrl.memSize := 0.U
            }.elsewhen(funct3 === "b001".U) { // sh
                io.ctrl.memSize := 1.U
            }.elsewhen(funct3 === "b010".U) { // sw
                io.ctrl.memSize := 2.U
            }
        }
        is("b0001111".U) { // FENCE
        }
        is("b0101111".U) { // A-extension: LR.W / SC.W / AMO
            val funct5 = instr(31, 27)
            io.ctrl.amoOp := funct5
            io.ctrl.memSize := 2.U // word
            io.ctrl.aluSrc := true.B
            io.ctrl.aluOp := 0.U
            io.imm := 0.U
            when(funct5 === "b00010".U) { // LR.W
                io.ctrl.memRead := true.B
                io.ctrl.regWrite := true.B
                io.ctrl.memToReg := true.B
            }.elsewhen(funct5 === "b00011".U) { // SC.W
                io.ctrl.memWrite := true.B
                io.ctrl.regWrite := true.B
                io.ctrl.memToReg := true.B
            }.otherwise { // AMO: AMOADD, AMOSWAP, AMOXOR, AMOAND, AMOOR, AMOMIN, etc.
                io.ctrl.memRead := true.B
                io.ctrl.memWrite := true.B
                io.ctrl.regWrite := true.B
                io.ctrl.memToReg := true.B
            }
        }
        is("b1110011".U) { // SYSTEM (ECALL/EBREAK/CSR)
            when(funct3 === "b010".U) { // CSRRS (read-only CSR)
                val csrAddr = instr(31, 20)
                when(csrAddr === "hF14".U) { // mhartid
                    io.ctrl.regWrite := true.B
                    io.ctrl.aluSrc := true.B
                    io.ctrl.aluOp := 9.U // same as LUI: returns io.imm
                    io.imm := Cat(0.U(31.W), io.instrIn.threadIdx)
                }
            }
        }
    }
}
