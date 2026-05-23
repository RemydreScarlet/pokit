package pokit.core

import chisel3._

class ControlBundle extends Bundle {
    val aluOp = UInt(4.W) // 4 bits for more operations
    val regWrite = Bool()
    val aluSrc = Bool() // 0: rs2, 1: immediate
    val memRead = Bool()
    val memWrite = Bool()
    val memToReg = Bool()
    val memSize = UInt(2.W) // 0: byte, 1: halfword, 2: word
    val memSigned = Bool() // true: signed load (lb/lh), false: unsigned (lbu/lhu/sw)
    val branch = Bool()
    val jump = Bool()
}
