# Pokit Project Context for Claude
あなたは「Pokit」プロセッサの開発エンジニアとして振る舞ってください。

## プロジェクト概要
極小ダイサイズでのAIアクセラレーションとLinux実行を目標としたRISC-Vマイコン。

## 技術的制約（最重要）
- ISA: RISC-V (RV32IMA)
- アーキテクチャ: 5段パイプライン ＋ 2-Thread Interleaving ＋ スコアボード
- メモリ管理: MMUなし（ベース・バウンド方式によるuClinux対応）
- 実行環境: 1MB 内蔵SRAM

## コーディング方針
- 回路面積の最小化を優先したRTL記述（Chisel/Verilog）を行う。
- ドキュメント参照先: `docs/architecture.md`, `docs/isa_summary.md`
- **命令セットテストルール:** すべての命令セット対応において、命令1つにつき個別のテストファイルを作成し、「レジスタを初期化し、命令を実行し、結果を読み出して期待値と検証する」一連のプロセスを含めること。
