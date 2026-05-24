#!/bin/bash
dir="/home/momoi/GitHub/pokit/pokit/src/test/scala/pokit/core/instruction"

# Instructions and their opcodes/hex for test
declare -A instructions=(
  ["Slt"]="SLT"
  ["Sltu"]="SLTU"
  ["And"]="AND"
  ["Or"]="OR"
  ["Xor"]="XOR"
  ["Sll"]="SLL"
  ["Srl"]="SRL"
  ["Sra"]="SRA"
  ["Lw"]="LW"
  ["Sw"]="SW"
  ["Beq"]="BEQ"
  ["Bne"]="BNE"
  ["Blt"]="BLT"
  ["Bge"]="BGE"
  ["Bltu"]="BLTU"
  ["Bgeu"]="BGEU"
  ["Jal"]="JAL"
  ["Jalr"]="JALR"
  ["Lui"]="LUI"
  ["Auipc"]="AUIPC"
)

# Template
for name in "${!instructions[@]}"; do
  instr="${instructions[$name]}"
  
  cat <<END > "$dir/${name}Test.scala"
package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class ${name}Test extends AnyFlatSpec with ChiselScalatestTester {
    "$instr instruction" should "work" in {
        test(new Pipeline) { dut =>
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(true.B)
            
            // Initialization for test
            dut.io.initRegAddr.poke(2.U)
            dut.io.initRegData.poke(10.U)
            dut.clock.step()
            dut.io.initRegAddr.poke(3.U)
            dut.io.initRegData.poke(20.U)
            dut.clock.step()
            dut.io.initRegWen.poke(false.B)
            
            // $instr x1, x2, x3
            // TODO: Update machine code for $instr
            val instr = "h00000000".U 
            dut.io.instr.poke(instr)
            
            dut.clock.step(6)
            
            // TODO: Add verification (e.g., check x1 via dbgRegData)
        }
    }
}
END
done
