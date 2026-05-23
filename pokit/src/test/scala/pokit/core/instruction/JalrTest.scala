package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class JalrTest extends AnyFlatSpec with ChiselScalatestTester {
    "JALR instruction" should "work" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
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
            
            // TODO: Update machine code for JALR
            val instr = "h00000000".U 
            dut.io.instr.poke(instr)
            
            dut.clock.step(6)
            
            // TODO: Add verification
        }
    }
}
