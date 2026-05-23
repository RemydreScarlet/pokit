.section .text.init
.globl _start
_start:
    # Set up stack and clear BSS (same as CoreMark's crt0.S)
    lui sp, 0x10
    addi sp, sp, -16

    # Clear BSS
    la t0, _bss_start
    la t1, _bss_end
1:  bgeu t0, t1, 2f
    sw zero, 0(t0)
    addi t0, t0, 4
    j 1b
2:
    # Test 1: Simple sw to 0x3FFC with imm-offset hazard
    # Same pattern as CoreMark's finish code
    li t0, 1
    lui t1, 0x4          # t1 = 0x4000
    addi t1, t1, -4      # t1 = 0x3FFC
    sw t0, 0(t1)         # should write 1 to 0x3FFC

    # Test 2: Verify with another write
    li t2, 0xDEAD
    lui t3, 0x4
    addi t3, t3, -8      # t3 = 0x3FF8
    sw t2, 0(t3)         # should write 0xDEAD to 0x3FF8

    # Test 3: Direct hazard test - consecutive li+sw
    li t4, 0xCAFE
    sw t4, 4(t3)         # should write 0xCAFE to 0x3FFC (overwriting 1)

    # Final signature
    li t5, 1
    sw t5, 0(t3)         # should write 1 to 0x3FF8

3:  j 3b
