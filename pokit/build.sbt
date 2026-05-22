scalaVersion := "2.13.14"

// 最新の Chisel 6.x 系を使用
libraryDependencies ++= Seq(
  "org.chipsalliance" %% "chisel" % "6.4.0",
  "edu.berkeley.cs" %% "chiseltest" % "6.0.0" % "test"
)

// コンパイラプラグインの設定（6.x系では以下のように記述）
addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % "6.4.0" cross CrossVersion.full)
