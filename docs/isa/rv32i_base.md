# RV32I Base Integer Instructions

Pokitの基本命令セット。

## 1. 算術・論理命令 (R-Type / I-Type)
- **ADD, SUB:** 整数加減算。EXステージでALU処理。
- **SLL, SRL, SRA:** シフト演算。バレルシフタの実装を検討（面積と速度のトレードオフ）。
- **XOR, OR, AND:** 論理演算。
- **SLT, SLTU:** 比較演算。

**設計考慮:**
- 全命令はEXステージで1サイクルで終了させる。

## 2. ロード・ストア命令 (I-Type / S-Type)
- **LB, LH, LW, LBU, LHU:** ロード。
- **SB, SH, SW:** ストア。

**設計考慮:**
- SRAMアクセスはMEMステージで行う。SRAMのレイテンシを1サイクルに収める設計が必要。
- ベース・バウンド方式によるアドレスチェックをIDまたはEXステージで並行実施。

## 3. 分岐・ジャンプ命令 (B-Type / J-Type)
- **BEQ, BNE, BLT, BGE, BLTU, BGEU:** 分岐。
- **JAL, JALR:** ジャンプ。

**設計考慮:**
- 2-Thread Interleavingにより分岐ハザードを隠蔽するが、分岐予測（動的または静的）を併用し効率化する。
