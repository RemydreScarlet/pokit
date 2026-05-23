package pokit.core

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.stage._

class BranchTest extends AnyFlatSpec with ChiselScalatestTester {
    "BEQ instruction" should "trigger branchTaken when rs1 == rs2" in {
        test(new EX) { dut =>
            dut.io.ctrl.branch.poke(true.B)
            dut.io.ctrl.aluOp.poke(0.U) // BEQ
            dut.io.rs1.poke(10.U)
            dut.io.rs2.poke(10.U)
            dut.io.pc.poke(0.U)
            dut.io.imm.poke(4.U)
            
            dut.io.branchTaken.expect(true.B)
            dut.io.branchTarget.expect(4.U)
        }
    }
}
