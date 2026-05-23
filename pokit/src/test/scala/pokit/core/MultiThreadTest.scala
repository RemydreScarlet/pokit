package pokit.core

import chisel3._
import chiseltest._
import chiseltest.VerilatorBackendAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import scala.io.Source
import java.io.File

class MultiThreadTest extends AnyFlatSpec with ChiselScalatestTester {
    val testBinDir = "/home/momoi/GitHub/pokit/pokit/test_binaries"

    def loadMemFromFile(dut: Pipeline, path: String): Unit = {
        val lines = Source.fromFile(path).getLines().toList
        var addr = 0
        var allBytes = List[Int]()
        for (line <- lines) {
            val trimmed = line.trim
            if (trimmed.startsWith("@")) {
                if (allBytes.nonEmpty) writeWords(dut, addr, allBytes)
                addr = Integer.parseInt(trimmed.drop(1), 16)
                allBytes = List[Int]()
            } else if (trimmed.nonEmpty) {
                allBytes ++= trimmed.split("\\s+").filter(_.nonEmpty).map(b => Integer.parseInt(b, 16))
            }
        }
        if (allBytes.nonEmpty) writeWords(dut, addr, allBytes)
    }

    private def writeWords(dut: Pipeline, baseAddr: Int, bytes: Seq[Int]): Unit = {
        val words = bytes.grouped(4).toList
        var byteAddr = baseAddr
        for (wordBytes <- words) {
            val word = wordBytes.zipWithIndex.map { case (b, i) => (b & 0xFF).toLong << (i * 8) }.sum
            val alignedAddr = byteAddr & 0xFFFFFFFC
            dut.io.imemInitAddr.poke(alignedAddr.U)
            dut.io.imemInitData.poke(word.U(32.W))
            dut.io.imemInitWen.poke(true.B)
            dut.io.dmemInitAddr.poke(alignedAddr.U)
            dut.io.dmemInitData.poke(word.U(32.W))
            dut.io.dmemInitWen.poke(true.B)
            dut.clock.step()
            dut.io.imemInitWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)
            byteAddr += 4
        }
    }

    "Dual thread" should "run independent code on each hart" in {
        test(new Pipeline).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
            dut.clock.setTimeout(50000)
            dut.io.singleThread.poke(false.B)
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)

            loadMemFromFile(dut, new File(testBinDir, "mt_basic.mem").getAbsolutePath)

            dut.reset.poke(true.B)
            dut.clock.step()
            dut.reset.poke(false.B)

            var cycles = 0
            var done = false

            for (i <- 0 until 1000 if !done) {
                dut.clock.step()
                cycles += 1

                dut.io.dmemDbgAddr.poke(0x3FFC.U)
                val s0 = dut.io.dmemDbgData.peek().litValue
                dut.io.dmemDbgAddr.poke(0x3FF8.U)
                val s1 = dut.io.dmemDbgData.peek().litValue

                if (s0 == 0xAA && s1 == 0xBB) done = true
            }

            assert(done, s"Dual-thread test timed out after 1000 cycles")
        }
    }
}
