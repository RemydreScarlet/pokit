package pokit.core.stage

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class IFTest extends AnyFlatSpec with ChiselScalatestTester {
    "IF" should "keep running the same thread when both are ready" in {
        test(new IF) { dut =>
            dut.io.branchValid.poke(false.B)
            dut.io.branchThreadIdx.poke(0.U)
            dut.io.threadBlocked.poke(0.U)

            // Both threads ready → scheduler keeps current thread (thread 0)
            dut.clock.step()
            dut.io.pc.expect(4.U) // pc0: 0 -> 4, threadIdx stays 0

            dut.clock.step()
            dut.io.pc.expect(8.U) // pc0: 4 -> 8, threadIdx stays 0

            dut.clock.step()
            dut.io.pc.expect(12.U) // pc0: 8 -> 12, threadIdx stays 0
        }
    }

    it should "switch to other thread when current is blocked" in {
        test(new IF) { dut =>
            dut.io.branchValid.poke(false.B)
            dut.io.branchThreadIdx.poke(0.U)
            dut.io.threadBlocked.poke(0.U)

            // Run thread 0 for one cycle
            dut.clock.step()
            dut.io.pc.expect(4.U) // pc0: 0 -> 4

            // Now block thread 0
            dut.io.threadBlocked.poke(1.U)
            dut.clock.step()
            dut.io.pc.expect(0.U) // pc1: 0 (thread 1 runs, pc1 hasn't been updated)

            // Unblock thread 0, block thread 1
            dut.io.threadBlocked.poke(2.U)
            dut.clock.step()
            dut.io.pc.expect(8.U) // pc0: 4 -> 8 (thread 0 resumes)

            // Both blocked → PC still increments (IF has no stall mechanism)
            dut.io.threadBlocked.poke(3.U)
            dut.clock.step()
            dut.io.pc.expect(12.U) // pc0: 8 -> 12, threadIdx kept at 0
        }
    }
}
