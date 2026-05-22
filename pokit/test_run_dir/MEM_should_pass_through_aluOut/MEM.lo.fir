FIRRTL version 1.2.0
circuit MEM :
  module MEM : @[src/main/scala/pokit/core/stage/MEM.scala 5:7]
    input clock : Clock @[src/main/scala/pokit/core/stage/MEM.scala 5:7]
    input reset : UInt<1> @[src/main/scala/pokit/core/stage/MEM.scala 5:7]
    input io_aluOut : UInt<32> @[src/main/scala/pokit/core/stage/MEM.scala 6:16]
    output io_memOut : UInt<32> @[src/main/scala/pokit/core/stage/MEM.scala 6:16]

    io_memOut <= io_aluOut @[src/main/scala/pokit/core/stage/MEM.scala 11:15]
