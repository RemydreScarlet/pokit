package pokit.core

import chisel3._
import chiseltest._
import chiseltest.VerilatorBackendAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import scala.io.Source
import java.io.File

class CoreMarkTest extends AnyFlatSpec with ChiselScalatestTester {
    val testBinDir = "/home/momoi/GitHub/pokit/pokit/test_binaries"
    val signatureAddr = 0x3FFC
    val printBufAddr = 0x3F00
    val maxCycles = 100000000

    def loadMemBoth(dut: Pipeline, path: String): Unit = {
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

    def readPrintBuf(dut: Pipeline): String = {
        val buf = new StringBuilder
        var done = false
        var i = 0
        while (i < 1024 && !done) {
            val addr = printBufAddr + i
            val wordAddr = addr & 0xFFFFFFFC
            val byteOff = addr & 3
            dut.io.dmemDbgAddr.poke(wordAddr.U)
            val word = dut.io.dmemDbgData.peek().litValue.toInt
            val byte = (word >> (byteOff * 8)) & 0xFF
            if (byte == 0) done = true
            else { buf.append(byte.toChar); i += 1 }
        }
        buf.toString
    }

    "CoreMark" should "run and produce valid results" in {
        test(new Pipeline).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
            dut.clock.setTimeout(maxCycles + 100)
            dut.io.singleThread.poke(true.B)
            dut.io.testMode.poke(false.B)
            dut.io.branchValid.poke(false.B)
            dut.io.initRegWen.poke(false.B)
            dut.io.dmemInitWen.poke(false.B)

            val memFile = new File(testBinDir, "coremark.mem")
            loadMemBoth(dut, memFile.getAbsolutePath)

            // Check print_buf_idx (BSS at 0x5800)
            dut.io.dmemDbgAddr.poke(0x5800.U)
            println(f"  print_buf_idx @ 0x5800 = ${dut.io.dmemDbgData.peek().litValue}")

            dut.reset.poke(true.B)
            dut.clock.step()
            dut.reset.poke(false.B)

            var signature = BigInt(0)
            var done = false
            var cycles = 0
            var timeout = true

            for (i <- 0 until maxCycles if !done) {
                dut.clock.step()
                cycles += 1

                if (i == 10000) {
                    // Check that the CPU is making progress (PC non-zero)
                    // by looking at a known memory location
                    dut.io.dmemDbgAddr.poke(0x3FFC.U)
                    println(f"  @10k cycles: 0x3FFC = 0x${dut.io.dmemDbgData.peek().litValue}%x")
                }

                dut.io.dmemDbgAddr.poke(signatureAddr.U)
                val sig = dut.io.dmemDbgData.peek().litValue
                if (sig == 1) {
                    signature = sig
                    done = true
                    timeout = false
                }
            }

            if (timeout) {
                println(s"CoreMark timed out after $maxCycles cycles")
            }

            // Debug reads
            dut.io.dmemDbgAddr.poke(0x5800.U)
            val bufIdx = dut.io.dmemDbgData.peek().litValue.toInt
            println(f"  print_buf_idx (final) = $bufIdx")

            dut.io.dmemDbgAddr.poke(0x3FFC.U)
            val sigWord = dut.io.dmemDbgData.peek().litValue
            println(f"  DMem[0x3FFC] = 0x${sigWord}%x")

            // Debug: dump stack area where local buf should be
            println("\n=== Stack area (0xFB00-0xFC00) ===")
            for (a <- 0xFB00 to 0xFC00 by 16) {
                val wordAddr = a & 0xFFFFFFFC
                dut.io.dmemDbgAddr.poke(wordAddr.U)
                val w = dut.io.dmemDbgData.peek().litValue.toInt
                val chars = (0 until 4).map { j =>
                    val b = (w >> (j * 8)) & 0xFF
                    if (b >= 32 && b < 127) b.toChar else '.'
                }.mkString
                println(f"  0x$a%04x: 0x$w%08x  $chars")
            }

            val output = readPrintBuf(dut)
            println(s"  output length = ${output.length}")
            println("\n=== CoreMark Output ===")
            println(output)
            println("=======================")
            println(f"Cycles: $cycles")

            // CoreMark/MHz = (iterations × 1,000,000) / total_cycles
            val iterations = 1
            val coremarkMhz = iterations.toDouble * 1000000.0 / cycles.toDouble
            println(f"\n  CoreMark/MHz (1 iter): $coremarkMhz%.2f")

            assert(!timeout, s"CoreMark timed out after $maxCycles cycles")
            assert(signature == 1, s"CoreMark failed: signature=$signature (expected 1)")
        }
    }
}
