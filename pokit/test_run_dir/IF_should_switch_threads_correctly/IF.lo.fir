FIRRTL version 1.2.0
circuit IF :
  module IF : @[src/main/scala/pokit/core/stage/IF.scala 11:7]
    input clock : Clock @[src/main/scala/pokit/core/stage/IF.scala 11:7]
    input reset : UInt<1> @[src/main/scala/pokit/core/stage/IF.scala 11:7]
    output io_pc : UInt<32> @[src/main/scala/pokit/core/stage/IF.scala 12:16]
    input io_instr : UInt<32> @[src/main/scala/pokit/core/stage/IF.scala 12:16]
    output io_instrOut_instr : UInt<32> @[src/main/scala/pokit/core/stage/IF.scala 12:16]
    output io_instrOut_threadIdx : UInt<1> @[src/main/scala/pokit/core/stage/IF.scala 12:16]

    reg pc0 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), pc0) @[src/main/scala/pokit/core/stage/IF.scala 22:22]
    reg pc1 : UInt<32>, clock with :
      reset => (UInt<1>("h0"), pc1) @[src/main/scala/pokit/core/stage/IF.scala 23:22]
    reg threadIdx : UInt<1>, clock with :
      reset => (UInt<1>("h0"), threadIdx) @[src/main/scala/pokit/core/stage/IF.scala 24:28]
    node _threadIdx_T = not(threadIdx) @[src/main/scala/pokit/core/stage/IF.scala 27:18]
    node _currentPC_T = eq(threadIdx, UInt<1>("h0")) @[src/main/scala/pokit/core/stage/IF.scala 30:35]
    node currentPC = mux(_currentPC_T, pc0, pc1) @[src/main/scala/pokit/core/stage/IF.scala 30:24]
    io_pc <= currentPC @[src/main/scala/pokit/core/stage/IF.scala 32:11]
    io_instrOut_instr <= io_instr @[src/main/scala/pokit/core/stage/IF.scala 33:23]
    io_instrOut_threadIdx <= threadIdx @[src/main/scala/pokit/core/stage/IF.scala 34:27]
    pc0 <= mux(reset, UInt<32>("h0"), pc0) @[src/main/scala/pokit/core/stage/IF.scala 22:{22,22,22}]
    pc1 <= mux(reset, UInt<32>("h0"), pc1) @[src/main/scala/pokit/core/stage/IF.scala 23:{22,22,22}]
    threadIdx <= mux(reset, UInt<1>("h0"), _threadIdx_T) @[src/main/scala/pokit/core/stage/IF.scala 24:{28,28} 27:15]
