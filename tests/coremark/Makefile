BUILD_DIR = ../../pokit/test_binaries

GCC = riscv64-unknown-elf-gcc
OBJCOPY = riscv64-unknown-elf-objcopy
GCC_OPTS = -march=rv32ima -mabi=ilp32 -static -mcmodel=medany -fvisibility=hidden -nostdlib -nostartfiles -O2 -DITERATIONS=1
INCLUDES = -I. -I../../tests/pokit-env
LINKER = -Tlink.ld

SRCS = crt0.S mem.S core_main.c core_list_join.c core_matrix.c core_state.c core_util.c core_portme.c ee_printf.c
OBJS = $(SRCS:.S=.o)
OBJS := $(OBJS:.c=.o)

.PHONY: all clean

all: $(BUILD_DIR)/coremark.mem

%.o: %.S
	$(GCC) $(GCC_OPTS) $(INCLUDES) -c $< -o $@

%.o: %.c
	$(GCC) $(GCC_OPTS) $(INCLUDES) -c $< -o $@

$(BUILD_DIR)/coremark.elf: $(OBJS) link.ld
	$(GCC) $(GCC_OPTS) $(INCLUDES) $(LINKER) $(OBJS) -o $@

$(BUILD_DIR)/coremark.bin: $(BUILD_DIR)/coremark.elf
	$(OBJCOPY) -O binary $< $@

$(BUILD_DIR)/coremark.mem: $(BUILD_DIR)/coremark.bin
	{ echo "@00000000"; od -v -A none -t x1 -w16 $< | sed 's/^[[:space:]]*//'; } > $@

clean:
	rm -f *.o $(BUILD_DIR)/coremark.*
