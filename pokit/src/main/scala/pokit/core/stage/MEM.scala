package pokit.core.stage

import chisel3._

class MEM extends Module {
    val io = IO(new Bundle {
        val aluOut = Input(UInt(32.W))
        val memOut = Output(UInt(32.W))
    })
    // 仮実装
    io.memOut := io.aluOut
}
