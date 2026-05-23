package pokit.core

import chisel3._
import chisel3.util._

class BranchPredictor extends Module {
    val io = IO(new Bundle {
        val lookupPC = Input(UInt(32.W))
        val lookupThread = Input(UInt(1.W))
        val predictTaken = Output(Bool())
        val predictTarget = Output(UInt(32.W))

        val updateValid = Input(Bool())
        val updatePC = Input(UInt(32.W))
        val updateThread = Input(UInt(1.W))
        val updateTaken = Input(Bool())
        val updateTarget = Input(UInt(32.W))

        val dbgHit = Output(Bool())
    })

    val numEntries = 64
    val idxBits = log2Ceil(numEntries)

    val valid  = RegInit(VecInit(Seq.fill(numEntries)(false.B)))
    val tag    = Reg(Vec(numEntries, UInt((32 - idxBits - 2 + 1).W))) // +1 for thread bit
    val target = Reg(Vec(numEntries, UInt(32.W)))
    val cnt    = RegInit(VecInit(Seq.fill(numEntries)(0.U(2.W))))

    val lookupIdx = io.lookupPC(idxBits + 1, 2)
    val lookupTag = io.lookupThread ## io.lookupPC(31, idxBits + 2)
    val lookupHit = valid(lookupIdx) && tag(lookupIdx) === lookupTag
    val predTaken = lookupHit && cnt(lookupIdx)(1)
    io.predictTaken := predTaken
    io.predictTarget := target(lookupIdx)
    io.dbgHit := lookupHit

    when(io.updateValid) {
        val updIdx = io.updatePC(idxBits + 1, 2)
        val updTag = io.updateThread ## io.updatePC(31, idxBits + 2)

        valid(updIdx) := true.B
        tag(updIdx) := updTag

        when(io.updateTaken) {
            target(updIdx) := io.updateTarget
            cnt(updIdx) := Mux(cnt(updIdx) === 3.U, 3.U, cnt(updIdx) + 1.U)
        }.otherwise {
            cnt(updIdx) := Mux(cnt(updIdx) === 0.U, 0.U, cnt(updIdx) - 1.U)
        }
    }
}
