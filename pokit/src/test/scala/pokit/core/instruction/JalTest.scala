package pokit.core.instruction

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pokit.core.Pipeline

class JalTest extends AnyFlatSpec with ChiselScalatestTester {
    "JAL instruction" should "jump and link" in {
        test(new Pipeline) { dut =>
            dut.io.testMode.poke(true.B)
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            dut.io.branchValid.poke(false.B)

            // JAL x1, 8 -> 0x004000ef
            val instr = "h004000ef".U
            dut.io.instr.poke(instr)

            dut.clock.step(6)

            // x1 should get PC+4 = 4 (link register)
            dut.io.dbgRegAddr.poke(1.U)
            dut.io.dbgRegData.expect(4.U)
        }
    }
}
