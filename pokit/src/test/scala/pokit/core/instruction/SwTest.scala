package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class SwTest extends AnyFlatSpec with ChiselScalatestTester {
    "SW instruction" should "store word" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(true.B)

            // x2 = 0xCAFEBABE (data to store), x3 = 0x100 (base address)
            dut.io.initRegAddr.poke(2.U)
            dut.io.initRegData.poke("hCAFEBABE".U)
            dut.clock.step()
            dut.io.initRegAddr.poke(3.U)
            dut.io.initRegData.poke(0x100.U)
            dut.clock.step()
            dut.io.initRegWen.poke(false.B)

            // SW x2, 0(x3) -> 0x00312023
            val instr = "h00312023".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            // Verify data was written to DMEM[0x100]
            dut.io.dmemDbgAddr.poke(0x100.U)
            dut.io.dmemDbgData.expect("hCAFEBABE".U)
        }
    }
}
