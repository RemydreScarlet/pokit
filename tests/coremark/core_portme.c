#include "coremark.h"
#include "core_portme.h"

#if VALIDATION_RUN
volatile ee_s32 seed1_volatile = 0x3415;
volatile ee_s32 seed2_volatile = 0x3415;
volatile ee_s32 seed3_volatile = 0x66;
#endif
#if PERFORMANCE_RUN
volatile ee_s32 seed1_volatile = 0x0;
volatile ee_s32 seed2_volatile = 0x0;
volatile ee_s32 seed3_volatile = 0x66;
#endif
#if PROFILE_RUN
volatile ee_s32 seed1_volatile = 0x8;
volatile ee_s32 seed2_volatile = 0x8;
volatile ee_s32 seed3_volatile = 0x8;
#endif
volatile ee_s32 seed4_volatile = ITERATIONS;
volatile ee_s32 seed5_volatile = 0;

CORETIMETYPE barebones_clock(void)
{
    static ee_u32 tick = 0;
    return tick++;
}

#define GETMYTIME(_t)              (*_t = barebones_clock())
#define MYTIMEDIFF(fin, ini)       ((fin) - (ini))
#define TIMER_RES_DIVIDER          1
#define SAMPLE_TIME_IMPLEMENTATION 1
#define EE_TICKS_PER_SEC           1000

static CORETIMETYPE start_time_val, stop_time_val;

void start_time(void)
{
    GETMYTIME(&start_time_val);
}

void stop_time(void)
{
    GETMYTIME(&stop_time_val);
}

CORE_TICKS get_time(void)
{
    return (CORE_TICKS)(MYTIMEDIFF(stop_time_val, start_time_val));
}

secs_ret time_in_secs(CORE_TICKS ticks)
{
    return ticks / EE_TICKS_PER_SEC;
}

ee_u32 default_num_contexts = 1;

void portable_init(core_portable *p, int *argc, char *argv[])
{
    (void)argc;
    (void)argv;
    if (sizeof(ee_ptr_int) != sizeof(ee_u8 *))
    {
        ee_printf("ERROR! Please define ee_ptr_int to a type that holds a pointer!\n");
    }
    if (sizeof(ee_u32) != 4)
    {
        ee_printf("ERROR! Please define ee_u32 to a 32b unsigned type!\n");
    }
    p->portable_id = 1;
}

void portable_fini(core_portable *p)
{
    p->portable_id = 0;
}
