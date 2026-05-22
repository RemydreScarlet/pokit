# A-Extension (Atomic)

## 命令
- **LR.W (Load Reserved), SC.W (Store Conditional):** 排他制御の基本。
- **AMO...:** アトミックメモリアクセス。

## Pokit設計戦略
- 2-Thread Interleavingを前提とした実装。
- ロードストアユニット(MEMステージ)において、SRAMアクセスのアトミック性を保証するロック機構が必要。
- 極小ダイサイズを目指すため、高機能なキャッシュ・コヒーレンシ機構は実装せず、ベース・バウンドレジスタを利用したメモリ隔離と排他制御を基本とする。
