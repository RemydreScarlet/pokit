OUTPUT_ARCH("riscv")
ENTRY(_start)

SECTIONS {
    . = 0x00000000;
    .text.init : { *(.text.init) }
    . = ALIGN(0x1000);
    .text : { *(.text) }
    . = ALIGN(0x1000);
    .rodata : { *(.rodata) }
    . = ALIGN(0x1000);
    .data : { *(.data) }
    . = ALIGN(4);
    _bss_start = .;
    .bss : { *(.bss) }
    .sbss : { *(.sbss) }
    _bss_end = .;
    _end = .;
}
