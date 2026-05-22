package pokit.core

import chisel3._

class ControlBundle extends Bundle {
    val aluOp = UInt(3.W)
    val regWrite = Bool()
    val aluSrc = Bool() // 0: rs2, 1: immediate
}
