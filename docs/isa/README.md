# RISC-V (RV32IMA) 詳細ドキュメント

Pokitプロセッサで実装する命令セットアーキテクチャのガイドラインです。
本プロジェクトでは、面積最適化のため、5段パイプラインでの動作を前提に設計します。

## 構造
- [RV32I Base Integer Instructions](rv32i_base.md)
- [M-Extension (Multiply/Divide)](m_extension.md)
- [A-Extension (Atomic)](a_extension.md)

## 設計指針
すべての命令は、以下のパイプラインステージで処理することを基本とします。
- **IF:** 2-Thread Interleavingによる命令取得
- **ID:** デコード
- **EX:** 演算/アドレス計算
- **MEM:** SRAMアクセス（ロード/ストアのみ）
- **WB:** 書き戻し
