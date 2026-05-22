FIRRTL version 1.2.0
circuit WB :
  module WB : @[src/main/scala/pokit/core/stage/WB.scala 6:7]
    input clock : Clock @[src/main/scala/pokit/core/stage/WB.scala 6:7]
    input reset : UInt<1> @[src/main/scala/pokit/core/stage/WB.scala 6:7]
    input io_aluOut : UInt<32> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    input io_rd : UInt<6> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    input io_ctrl_aluOp : UInt<3> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    input io_ctrl_regWrite : UInt<1> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    input io_ctrl_aluSrc : UInt<1> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    output io_wen : UInt<1> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    output io_wAddr : UInt<6> @[src/main/scala/pokit/core/stage/WB.scala 7:16]
    output io_wData : UInt<32> @[src/main/scala/pokit/core/stage/WB.scala 7:16]

    io_wen <= io_ctrl_regWrite @[src/main/scala/pokit/core/stage/WB.scala 18:12]
    io_wAddr <= io_rd @[src/main/scala/pokit/core/stage/WB.scala 19:14]
    io_wData <= io_aluOut @[src/main/scala/pokit/core/stage/WB.scala 20:14]
