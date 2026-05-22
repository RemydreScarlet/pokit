FIRRTL version 1.2.0
circuit WB :
  module WB : @[src/main/scala/pokit/core/stage/WB.scala 5:7]
    input clock : Clock @[src/main/scala/pokit/core/stage/WB.scala 5:7]
    input reset : UInt<1> @[src/main/scala/pokit/core/stage/WB.scala 5:7]
    input io_memOut : UInt<32> @[src/main/scala/pokit/core/stage/WB.scala 6:16]
    output io_regWriteData : UInt<32> @[src/main/scala/pokit/core/stage/WB.scala 6:16]

    io_regWriteData <= io_memOut @[src/main/scala/pokit/core/stage/WB.scala 11:21]
