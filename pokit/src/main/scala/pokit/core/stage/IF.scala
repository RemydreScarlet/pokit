package pokit.core.stage

import chisel3._

class PipelineStageIO extends Bundle {
    val instr = UInt(32.W)
    val threadIdx = UInt(1.W)
    val pc = UInt(32.W)
}

class IF extends Module {
    val io = IO(new Bundle {
        val pc = Output(UInt(32.W))
        val instr = Input(UInt(32.W))
        val instrOut = Output(new PipelineStageIO)
        val branchPC = Input(UInt(32.W))
        val branchValid = Input(Bool())
        val singleThread = Input(Bool())
    })

    val pc0 = RegInit(0.U(32.W))
    val pc1 = RegInit(0.U(32.W))
    val threadIdx = RegInit(0.U(1.W))

    val actualThreadIdx = Mux(io.singleThread, 0.U, threadIdx)

    when(io.branchValid) {
        when(actualThreadIdx === 0.U) { pc0 := io.branchPC }
        .otherwise { pc1 := io.branchPC }
    }.otherwise {
        when(actualThreadIdx === 0.U) { pc0 := pc0 + 4.U }
        .otherwise { pc1 := pc1 + 4.U }
    }

    threadIdx := Mux(io.singleThread, 0.U, ~threadIdx)

    val currentPC = Mux(actualThreadIdx === 0.U, pc0, pc1)

    io.pc := currentPC
    io.instrOut.instr := io.instr
    io.instrOut.threadIdx := actualThreadIdx
    io.instrOut.pc := currentPC
}
