.section .text.init
.globl _start
_start:
    /* Test 1: simple sw to 0x3FFC with known value */
    li t0, 0xDEAD
    li t1, 0x3FFC
    sw t0, 0(t1)         /* write 0xDEAD to 0x3FFC */

    /* Test 2: overwrite with 1 */
    li t0, 1
    sw t0, 0(t1)         /* write 1 to 0x3FFC */

    /* Test 3: check another nearby address */
    li t2, 0x12345678
    li t3, 0x3FF8
    sw t2, 0(t3)         /* write 0x12345678 to 0x3FF8 */

    /* Fill print buffer area to check sb works */
    li t4, 0x3F00
    li t5, 0x41          /* 'A' */
    sb t5, 0(t4)         /* buf[0] = 'A' */
    li t5, 0x42          /* 'B' */
    sb t5, 1(t4)         /* buf[1] = 'B' */
    li t5, 0x43          /* 'C' */
    sb t5, 2(t4)         /* buf[2] = 'C' */

    /* Done - write final signature */
    li t0, 1
    li t1, 0x3FFC
    sw t0, 0(t1)

1:  j 1b
