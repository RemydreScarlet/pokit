package pokit.core

import chisel3._

class ControlBundle extends Bundle {
    val aluOp = UInt(4.W) // 4 bits for more operations
    val regWrite = Bool()
    val aluSrc = Bool() // 0: rs2, 1: immediate
    val memRead = Bool()
    val memWrite = Bool()
    val memToReg = Bool()
    val branch = Bool()
    val jump = Bool()
}
