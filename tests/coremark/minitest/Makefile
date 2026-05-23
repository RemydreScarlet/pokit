# minimal test to check if ee_printf works for long strings
BUILD_DIR = ../../pokit/test_binaries

GCC = riscv64-unknown-elf-gcc
OBJCOPY = riscv64-unknown-elf-objcopy
GCC_OPTS = -march=rv32ima -mabi=ilp32 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -O2
INCLUDES = -I../../tests/pokit-env
LINKER = -Tlink.ld

SRCS = crt0.S main.c
OBJS = $(SRCS:.S=.o)
OBJS := $(OBJS:.c=.o)

.PHONY: all clean

all: $(BUILD_DIR)/minitest.mem

%.o: %.S
	$(GCC) $(GCC_OPTS) $(INCLUDES) -c $< -o $@

%.o: %.c
	$(GCC) $(GCC_OPTS) $(INCLUDES) -c $< -o $@

$(BUILD_DIR)/minitest.elf: $(OBJS) link.ld
	$(GCC) $(GCC_OPTS) $(INCLUDES) $(LINKER) $(OBJS) -o $@

$(BUILD_DIR)/minitest.bin: $(BUILD_DIR)/minitest.elf
	$(OBJCOPY) -O binary $< $@

$(BUILD_DIR)/minitest.mem: $(BUILD_DIR)/minitest.bin
	{ echo "@00000000"; od -v -A none -t x1 -w16 $< | sed 's/^[[:space:]]*//'; } > $@

clean:
	rm -f *.o $(BUILD_DIR)/minitest.* $(BUILD_DIR)/minitest.elf $(BUILD_DIR)/minitest.bin
