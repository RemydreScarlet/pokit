#!/bin/bash
set -e

POKIT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEST_DIR="$POKIT_DIR/tests/riscv"
POKIT_ENV="$POKIT_DIR/tests/pokit-env"
BUILD_DIR="$POKIT_DIR/pokit/test_binaries"
MACROS_DIR="$TEST_DIR/isa/macros/scalar"
SRC_DIR="$TEST_DIR/isa"
ISA="rv32ui"

# Clean option: remove all compiled test binaries
if [ "${1:-}" = "--clean" ]; then
    echo "Cleaning $BUILD_DIR..."
    rm -f "$BUILD_DIR"/*.mem "$BUILD_DIR"/*.hex "$BUILD_DIR"/*.bin "$BUILD_DIR"/rv32ui-p-*
    echo "Done."
    exit 0
fi

GCC="riscv64-unknown-elf-gcc"
OBJCOPY="riscv64-unknown-elf-objcopy"
GCC_OPTS="-march=rv32g -mabi=ilp32 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles"
INCLUDES="-I$POKIT_ENV -I$MACROS_DIR"
LINKER="-T$POKIT_ENV/link.ld"

mkdir -p "$BUILD_DIR"

# List of rv32ui test names from the Makefrag
TESTS="simple add addi and andi auipc beq bge bgeu blt bltu bne fence_i jal jalr lb lbu lh lhu lw ld_st lui ma_data or ori sb sh sw st_ld sll slli slt slti sltiu sltu sra srai srl srli sub xor xori"

echo "Compiling RV32UI tests with Pokit environment..."
for test in $TESTS; do
    src_file="$SRC_DIR/$ISA/${test}.S"
    if [ ! -f "$src_file" ]; then
        echo "ERROR: source not found: $src_file"
        continue
    fi
    
    echo "  Compiling: $test"
    elf="$BUILD_DIR/$ISA-p-$test"
    $GCC $GCC_OPTS $INCLUDES $LINKER "$src_file" -o "$elf"
    
    # Convert ELF to raw binary
    bin="$BUILD_DIR/$ISA-p-$test.bin"
    $OBJCOPY -O binary "$elf" "$bin"
    
    # Create .mem file in Verilog hex format
    mem="$BUILD_DIR/$ISA-p-$test.mem"
    {
        echo "@00000000"
        od -v -A none -t x1 -w16 "$bin" | while read -r line; do
            # Trim whitespace and output the hex bytes
            echo "$line" | sed 's/^[[:space:]]*//'
        done
    } > "$mem"
    
    # Clean up intermediate files
    rm -f "$elf" "$bin" "$BUILD_DIR/$ISA-p-$test.hex"
    
    echo "    -> $mem"
done

echo ""
echo "Done! Compiled $(ls "$BUILD_DIR"/*.mem 2>/dev/null | wc -l) tests."
