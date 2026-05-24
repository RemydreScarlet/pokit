# Pokit アーキテクチャ詳細設計

## 1. コアパイプライン (5-stage Pipeline)
- **IF (Instruction Fetch):** SRAMからの命令取得。2-Thread Interleavingによる競合回避。
- **ID (Instruction Decode):** RV32IMA命令デコード。レジスタ読み出し。
- **EX (Execute):**
  - ALU (整数演算)。
- **MEM (Memory Access):** SRAMへのアクセス。ベース・バウンド方式によるアドレスチェック。
- **WB (Write Back):** レジスタ書き戻し。

## 2. 2-Thread Interleaving
- 計算機リソースを時分割で共有する。
- サイクルごとにThread0とThread1を切り替える（T0 -> T1 -> T0 ...）。
- パイプライン・ストール（データハザード、分岐）をハードウェア的に完全に隠蔽する。

## 3. メモリ管理 (Base-Bound Addressing)
- MMUを廃止し、セグメンテーションによる軽量メモリ保護。
- 物理アドレス空間をベース・レジスタとバウンド・レジスタで区切る。
- OS(uClinux)とアプリケーションの隔離を実現。
