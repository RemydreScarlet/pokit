.section .text.init
.globl _start
_start:
    li t0, 0xDEADBEEF
    li t1, 0x3FFC
    sw t0, 0(t1)
    li t0, 1
    li t1, 0x3FFC
    sw t0, 0(t1)
1:  j 1b
