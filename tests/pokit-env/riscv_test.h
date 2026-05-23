#ifndef _ENV_POKIT_H
#define _ENV_POKIT_H

//=======================================================================
// Pokit Minimal Test Environment
//=======================================================================
// A stripped-down riscv_test.h for the Pokit RV32I core.
// No CSR, ecall, mret, or machine-mode support is required.

#define RVTEST_RV64U  .macro init; .endm
#define RVTEST_RV32U  .macro init; .endm

#define TESTNUM gp

//-----------------------------------------------------------------------
// Code Begin - minimal startup, no CSRs
//-----------------------------------------------------------------------
#define RVTEST_CODE_BEGIN                                               \
        .section .text.init;                                            \
        .align  6;                                                      \
        .globl _start;                                                  \
_start:                                                                 \
        li gp, 0;                                                       \
        .align 2;                                                       \
1:

//-----------------------------------------------------------------------
// Code End - infinite loop (should not be reached)
//-----------------------------------------------------------------------
#define RVTEST_CODE_END                                                 \
1:      j 1b

//-----------------------------------------------------------------------
// Pass/Fail - store result to signature address
//-----------------------------------------------------------------------
#define RVTEST_PASS                                                     \
        fence;                                                          \
        li gp, 1;                                                       \
        li t5, 0x3FFC;                                                  \
        sw gp, 0(t5);                                                   \
1:      j 1b

#define RVTEST_FAIL                                                     \
        fence;                                                          \
        li t5, 0x3FFC;                                                  \
        sw gp, 0(t5);                                                   \
1:      j 1b

//-----------------------------------------------------------------------
// Data Section - simplified, no tohost/fromhost
//-----------------------------------------------------------------------
#define RVTEST_DATA_BEGIN                                               \
        .align 4;                                                       \
        .global begin_signature;                                        \
begin_signature:

#define RVTEST_DATA_END                                                 \
        .align 4;                                                       \
        .global end_signature;                                          \
end_signature:

// Empty stubs for trap/exception handling
#define EXTRA_TVEC_USER
#define EXTRA_TVEC_MACHINE
#define EXTRA_INIT
#define EXTRA_INIT_TIMER
#define EXTRA_DATA

#endif
