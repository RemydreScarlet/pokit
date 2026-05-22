# RV32IMA Opcode Map

## 命令フォーマット識別
命令のビット [6:0] (opcode) で基本タイプを識別します。

| Opcode | Type | Description |
| :--- | :--- | :--- |
| 0110111 | U | LUI |
| 0010111 | U | AUIPC |
| 1101111 | J | JAL |
| 1100111 | I | JALR |
| 1100011 | B | Branch (BEQ, BNE, BLT, BGE, BLTU, BGEU) |
| 0000011 | I | Load (LB, LH, LW, LBU, LHU) |
| 0100011 | S | Store (SB, SH, SW) |
| 0010011 | I | OP-IMM (ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI) |
| 0110011 | R | OP (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND) |
| 0001111 | I | FENCE (省略可) |
| 1110011 | I | SYSTEM (ECALL, EBREAK) |

## 拡張命令 (Extension)
以下の命令は、メインのopcodeに対して `funct3` や `funct7` で分岐します。

### M-Extension (OP opcode: 0110011, funct7: 0000001)
| funct3 | Instruction |
| :--- | :--- |
| 000 | MUL |
| 001 | MULH |
| 010 | MULHSU |
| 011 | MULHU |
| 100 | DIV |
| 101 | DIVU |
| 110 | REM |
| 111 | REMU |

### A-Extension (AMO opcode: 0101111, funct3: 010)
※ `funct7` [31:27] で詳細動作（AMOタイプ）を決定
| aq | rl | funct5 (AMO) | Instruction |
| :--- | :--- | :--- | :--- |
| - | - | 00001 | AMOSWAP.W |
| - | - | 00000 | AMOADD.W |
| - | - | 00100 | AMOXOR.W |
| - | - | 01100 | AMOAND.W |
| - | - | 01000 | AMOOR.W |
| - | - | 10000 | AMOMIN.W |
| - | - | 10100 | AMOMAX.W |
| - | - | 11000 | AMOMINU.W |
| - | - | 11100 | AMOMAXU.W |
