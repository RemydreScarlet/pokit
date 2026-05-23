# memset/memcpy helper implementations
# void *memset(void *s, int c, unsigned int n)
# void *memcpy(void *dest, const void *src, unsigned int n)

.text
.globl memset
.type memset, @function
memset:
    beqz    a2, 2f
    mv      a3, a0
1:
    sb      a1, 0(a3)
    addi    a3, a3, 1
    addi    a2, a2, -1
    bnez    a2, 1b
2:
    ret

.globl memcpy
.type memcpy, @function
memcpy:
    beqz    a2, 4f
    mv      a3, a0
    mv      a4, a1
3:
    lbu     a5, 0(a4)
    addi    a4, a4, 1
    sb      a5, 0(a3)
    addi    a3, a3, 1
    addi    a2, a2, -1
    bnez    a2, 3b
4:
    ret
