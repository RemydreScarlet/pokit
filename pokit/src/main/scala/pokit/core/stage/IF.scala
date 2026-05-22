package pokit.core.stage

import chisel3._

// ステージ間のデータ受け渡し用 Bundle
class PipelineStageIO extends Bundle {
    val instr = UInt(32.W)
    val threadIdx = UInt(1.W)
}

class IF extends Module {
    val io = IO(new Bundle {
        // 命令メモリへのアドレス出力
        val pc = Output(UInt(32.W))
        // 命令入力（SRAM）
        val instr = Input(UInt(32.W))
        // パイプライン次段への出力
        val instrOut = Output(new PipelineStageIO)
    })

    // 2-Thread Interleaving 用のPC
    val pc0 = RegInit(0.U(32.W))
    val pc1 = RegInit(0.U(32.W))
    val threadIdx = RegInit(0.U(1.W)) // 0 or 1

    // スレッド切り替え
    threadIdx := ~threadIdx

    // 現在のPCを選択
    val currentPC = Mux(threadIdx === 0.U, pc0, pc1)
    
    io.pc := currentPC
    io.instrOut.instr := io.instr
    io.instrOut.threadIdx := threadIdx
}
