FIRRTL version 1.2.0
circuit ID :
  module ID : @[src/main/scala/pokit/core/stage/ID.scala 5:7]
    input clock : Clock @[src/main/scala/pokit/core/stage/ID.scala 5:7]
    input reset : UInt<1> @[src/main/scala/pokit/core/stage/ID.scala 5:7]
    input io_instrIn_instr : UInt<32> @[src/main/scala/pokit/core/stage/ID.scala 6:16]
    input io_instrIn_threadIdx : UInt<1> @[src/main/scala/pokit/core/stage/ID.scala 6:16]
    output io_rs1 : UInt<32> @[src/main/scala/pokit/core/stage/ID.scala 6:16]
    output io_rs2 : UInt<32> @[src/main/scala/pokit/core/stage/ID.scala 6:16]

    mem regFile : @[src/main/scala/pokit/core/stage/ID.scala 13:22]
      data-type => UInt<32>
      depth => 64
      read-latency => 0
      write-latency => 1
      reader => io_rs1_MPORT
      reader => io_rs2_MPORT
      read-under-write => undefined
    node rs1Addr = bits(io_instrIn_instr, 19, 15) @[src/main/scala/pokit/core/stage/ID.scala 16:35]
    node rs2Addr = bits(io_instrIn_instr, 24, 20) @[src/main/scala/pokit/core/stage/ID.scala 17:35]
    node regBase = shl(io_instrIn_threadIdx, 5) @[src/main/scala/pokit/core/stage/ID.scala 20:40]
    node _rs1ReadAddr_T = eq(rs1Addr, UInt<1>("h0")) @[src/main/scala/pokit/core/stage/ID.scala 23:35]
    node _rs1ReadAddr_T_1 = or(regBase, rs1Addr) @[src/main/scala/pokit/core/stage/ID.scala 23:57]
    node rs1ReadAddr = mux(_rs1ReadAddr_T, UInt<1>("h0"), _rs1ReadAddr_T_1) @[src/main/scala/pokit/core/stage/ID.scala 23:26]
    node _rs2ReadAddr_T = eq(rs2Addr, UInt<1>("h0")) @[src/main/scala/pokit/core/stage/ID.scala 24:35]
    node _rs2ReadAddr_T_1 = or(regBase, rs2Addr) @[src/main/scala/pokit/core/stage/ID.scala 24:57]
    node rs2ReadAddr = mux(_rs2ReadAddr_T, UInt<1>("h0"), _rs2ReadAddr_T_1) @[src/main/scala/pokit/core/stage/ID.scala 24:26]
    io_rs1 <= regFile.io_rs1_MPORT.data @[src/main/scala/pokit/core/stage/ID.scala 26:12]
    io_rs2 <= regFile.io_rs2_MPORT.data @[src/main/scala/pokit/core/stage/ID.scala 27:12]
    regFile.io_rs1_MPORT.addr <= rs1ReadAddr @[src/main/scala/pokit/core/stage/ID.scala 26:22]
    regFile.io_rs1_MPORT.en <= UInt<1>("h1") @[src/main/scala/pokit/core/stage/ID.scala 26:22]
    regFile.io_rs1_MPORT.clk <= clock @[src/main/scala/pokit/core/stage/ID.scala 26:22]
    regFile.io_rs2_MPORT.addr <= rs2ReadAddr @[src/main/scala/pokit/core/stage/ID.scala 27:22]
    regFile.io_rs2_MPORT.en <= UInt<1>("h1") @[src/main/scala/pokit/core/stage/ID.scala 27:22]
    regFile.io_rs2_MPORT.clk <= clock @[src/main/scala/pokit/core/stage/ID.scala 27:22]
