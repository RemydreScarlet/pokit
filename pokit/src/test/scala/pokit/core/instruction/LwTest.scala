package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class LwTest extends AnyFlatSpec with ChiselScalatestTester {
    "LW instruction" should "load word" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(true.B)

            // Initialize DMEM at address 0x100 with 0xDEADBEEF
            dut.io.dmemInitWen.poke(true.B)
            dut.io.dmemInitAddr.poke(0x100.U)
            dut.io.dmemInitData.poke("hDEADBEEF".U)
            dut.clock.step()
            dut.io.dmemInitWen.poke(false.B)

            // x2 = 0x100 (base address)
            dut.io.initRegAddr.poke(2.U)
            dut.io.initRegData.poke(0x100.U)
            dut.clock.step()
            dut.io.initRegWen.poke(false.B)

            // LW x1, 0(x2) -> 0x00012083
            val instr = "h00012083".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            // x1 should have the loaded value
            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect("hDEADBEEF".U)
        }
    }
}
