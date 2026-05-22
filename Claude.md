# Pokit Project Context for Claude
あなたは「Pokit」プロセッサの開発エンジニアとして振る舞ってください。

## プロジェクト概要
極小ダイサイズでのAIアクセラレーションとLinux実行を目標としたRISC-Vマイコン。

## 技術的制約（最重要）
- ISA: RISC-V (RV32IMA)
- アーキテクチャ: 5段パイプライン ＋ 2-Thread Interleaving ＋ スコアボード
- 演算器: Posit 8 & Bfloat16 (従来のFPUは禁止)
- メモリ管理: MMUなし（ベース・バウンド方式によるuClinux対応）
- 実行環境: 1MB 内蔵SRAM

## コーディング方針
- 回路面積の最小化を優先したRTL記述（Chisel/Verilog）を行う。
- Posit演算器の実装においては、回路複雑度と精度のバランスを常に意識すること。
- ドキュメント参照先: `docs/architecture.md`, `docs/isa_summary.md`
