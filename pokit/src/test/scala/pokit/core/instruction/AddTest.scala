package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class AddTest extends AnyFlatSpec with ChiselScalatestTester {
    "ADD instruction" should "add two register values" in {
        test(new Pipeline) { dut =>
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(true.B)
            
            // x2 = 10, x3 = 20
            dut.io.initRegAddr.poke(2.U)
            dut.io.initRegData.poke(10.U)
            dut.clock.step()
            dut.io.initRegAddr.poke(3.U)
            dut.io.initRegData.poke(20.U)
            dut.clock.step()
            dut.io.initRegWen.poke(false.B)
            
            // ADD x1, x2, x3 -> 0x003100b3
            val instr = "h003100b3".U
            dut.io.instr.poke(instr)
            
            dut.clock.step(6)
            
            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect(30.U)
        }
    }
}
