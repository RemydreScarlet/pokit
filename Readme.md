# Pokit Processor

**「ポケットに入る1ドルのLinux動くチップ」**

RISC-V RV32IMA 互換プロセッサの Chisel 実装。

---

## 1. プロジェクト構成

```
pokit/          # Chisel ソース (メインRTL)
├── build.sbt   # SBT ビルド設定
├── src/main/scala/pokit/core/
│   ├── Pipeline.scala        # トップレベルパイプライン
│   ├── RegFile.scala         # レジスタファイル (2-Thread)
│   ├── Control.scala         # 制御信号バンドル
│   ├── Memory.scala          # IMem / DMem (SRAM)
│   ├── BranchPredictor.scala # 分岐予測器
│   └── stage/
│       ├── IF.scala   # 命令取得
│       ├── ID.scala   # 命令デコード
│       ├── EX.scala   # 実行 (ALU/MUL/DIV + フォワーディング)
│       ├── MEM.scala  # メモリアクセス (AMO含む)
│       ├── WB.scala   # 書き戻し
│       └── register/  # パイプラインレジスタ (IDEXReg, EXMEMReg)
├── src/test/scala/pokit/core/
│   ├── RISCVTest.scala        # RISC-V公式テストスイート
│   ├── MultiThreadTest.scala  # マルチスレッド動作検証
│   ├── BranchTest.scala       # 分岐予測検証
│   ├── CoreMarkTest.scala     # CoreMarkベンチマーク
│   └── instruction/           # 個別命令テスト
docs/           # 設計ドキュメント
tests/          # テスト資源
├── riscv/      # RISC-V公式テスト (rv32i-*, rv32im-*)
├── coremark/   # CoreMarkベンチマーク (C言語)
└── pokit-env/  # テスト環境定義
```

## 2. 実装済み機能

### ISA
- **RV32I**: 全命令 (ADD/SUB/SLT/SLTU/XOR/OR/AND/SLL/SRL/SRA, LUI/AUIPC, JAL/JALR, Bxx, LB/LH/LW/LBU/LHU, SB/SH/SW, FENCE, ECALL/EBREAK/CSR)
- **M-extension**: MUL/MULH/MULHSU/MULHU/DIV/DIVU/REM/REMU
- **A-extension**: LR.W/SC.W/AMOADD/AMOSWAP/AMOXOR/AMOAND/AMOOR/AMOMIN/AMOMAX/AMOMINU/AMOMAXU

### パイプライン
- **5段構成**: IF → ID → EX → MEM → WB
- **2-Thread Interleaving**: サイクルごとに Thread0/Thread1 を自動切替、パイプラインストールを隠蔽
- **フォワーディング**: EX/MEM/WB ステージからのデータフォワーディング (データハザード解決)
- **IDEXフラッシュ**: リダイレクト時に誤命令をフラッシュ (スレッド単位)

### 分岐予測
- **BTB (Branch Target Buffer)**: 64エントリ
- **2ビット飽和カウンタ**: 分岐方向予測
- **リダイレクト機構**: 予測ミス・誤検知・JALR・ターゲット不一致に対応
- **BPU更新**: 全分岐命令と予測ヒット時に学習

### メモリ
- **IMem**: 64KB (16384 words) SRAM
- **DMem**: 64KB (16384 words) SRAM、バイト/半語/ワードアクセス対応
- **予約セット (LR/SC)**: スレッドごとのロードリンク/ストアコンディショナル

### A-extension 詳細
- LR.W: アドレス予約設定、メモリからデータ読み出し
- SC.W: 予約確認後に条件付きストア、結果を rd に書き戻し
- AMO: 全9種類のアトミックメモリ演算 (RMW)
- 予約破棄: 他スレッドの書き込みが予約アドレスにヒットすると全予約クリア

## 3. ビルドとテスト

```bash
cd pokit

# コンパイル
sbt compile

# 全テスト実行
sbt test

# RV32I公式テスト
sbt "testOnly pokit.core.RISCVTest"

# マルチスレッドテスト
sbt "testOnly pokit.core.MultiThreadTest"

# 分岐予測テスト
sbt "testOnly pokit.core.BranchTest"

# CoreMark
sbt "testOnly pokit.core.CoreMarkTest"
```

## 4. 開発ロードマップ

- [x] RTL設計 (Chisel): 5段パイプライン + 2-Thread Interleaving
- [x] RV32I 全命令実装・検証
- [x] M-extension (乗除算)
- [x] A-extension (アトミック命令)
- [x] 分岐予測器 (BTB + 2ビット飽和カウンタ)
- [x] フォワーディング (データハザード対策)
- [x] RISC-V公式テストスイート対応
- [x] CoreMarkベンチマーク実行
- [ ] ベース・バウンド方式メモリ保護
- [ ] uClinux移植
- [ ] 28nm/40nm テストチップ製造
