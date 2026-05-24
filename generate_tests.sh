#!/bin/bash
dir="/home/momoi/GitHub/pokit/pokit/src/test/scala/pokit/core/instruction"

# Instructions and their opcodes/hex for test
# I'll use placeholders for hex to be updated later if needed.
# For now, let's create the files.

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
            // TODO: Implement test for $instr
            // Add testMode, imemInitWen, dmemInitWen, branchValid setup
            // Initialize registers, poke instruction, run pipeline, verify result
        }
    }
}
END
done
