.section .text.init
.globl _start
_start:
    li sp, 0xFFF0
    li gp, 0

    la t0, _bss_start
    la t1, _bss_end
1:  bgeu t0, t1, 2f
    sw zero, 0(t0)
    addi t0, t0, 4
    j 1b
2:
    call main
    li t0, 1
    li t1, 0x3FFC
    sw t0, 0(t1)
3:  j 3b

.balign 4
