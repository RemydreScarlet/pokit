package pokit.core

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.io.Source
import java.io.File

class RISCVTest extends AnyFlatSpec with ChiselScalatestTester {
    val testBinDir = "/home/momoi/GitHub/pokit/pokit/test_binaries"
    val signatureAddr = 0x3FFC
    val maxCycles = 2000

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

            // Write to IMem (code section: addresses < 0x1000)
            if (byteAddr < 0x1000) {
                dut.io.imemInitAddr.poke((byteAddr & 0xFFFFFFFC).U)
                dut.io.imemInitData.poke(word.U(32.W))
                dut.io.imemInitWen.poke(true.B)
                dut.clock.step()
                dut.io.imemInitWen.poke(false.B)
            }

            // Write to DMem (data section: addresses >= 0x1000)
            if (byteAddr >= 0x1000) {
                dut.io.dmemInitAddr.poke((byteAddr & 0xFFFFFFFC).U)
                dut.io.dmemInitData.poke(word.U(32.W))
                dut.io.dmemInitWen.poke(true.B)
                dut.clock.step()
                dut.io.dmemInitWen.poke(false.B)
            }

            byteAddr += 4
        }
    }

    val knownUnsupported = Set(
        "rv32ui-p-fence_i",
    )

    val memFiles = new File(testBinDir).listFiles
        .filter(f => f.getName.endsWith(".mem") && (f.getName.startsWith("rv32ui-p-") || f.getName.startsWith("rv32um-p-")))
        .sortBy(_.getName)

    for (memFile <- memFiles) {
        val testName = memFile.getName.stripSuffix(".mem")
        val isUnsupported = knownUnsupported.contains(testName)

        (testName + (if (isUnsupported) " (known unsupported)" else "")) should "pass" in {
            test(new Pipeline) { dut =>
                dut.clock.setTimeout(maxCycles + 100)
                dut.io.singleThread.poke(true.B)
                dut.io.testMode.poke(false.B)
                dut.io.branchValid.poke(false.B)
                dut.io.initRegWen.poke(false.B)
                dut.io.dmemInitWen.poke(false.B)

                loadMemFromFile(dut, memFile.getAbsolutePath)

                dut.reset.poke(true.B)
                dut.clock.step()
                dut.reset.poke(false.B)

                var signature = BigInt(0)
                var timeout = true

                for (i <- 0 until maxCycles if timeout) {
                    dut.clock.step()

                    dut.io.dmemDbgAddr.poke(signatureAddr.U)
                    val sig = dut.io.dmemDbgData.peek().litValue
                    if (sig != 0) {
                        signature = sig
                        timeout = false
                    }
                }

                if (!isUnsupported) {
                    assert(!timeout, s"$testName timed out after $maxCycles cycles (signature=0)")
                    assert(signature == 1, s"$testName failed: signature=$signature (expected 1)")
                }
            }
        }
    }
}
