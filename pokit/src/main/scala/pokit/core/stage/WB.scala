package pokit.core.stage

import chisel3._

class WB extends Module {
    val io = IO(new Bundle {
        val memOut = Input(UInt(32.W))
        val regWriteData = Output(UInt(32.W))
    })
    // 仮実装
    io.regWriteData := io.memOut
}
