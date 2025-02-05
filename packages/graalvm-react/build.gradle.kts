@file:Suppress(
  "UnstableApiUsage",
  "unused",
  "DSL_SCOPE_VIOLATION",
)

plugins {
  id("dev.elide.build.js.node")
}

group = "dev.elide"
version = rootProject.version as String

dependencies {
  api(npm("esbuild", libs.versions.npm.esbuild.get()))
  api(npm("prepack", libs.versions.npm.prepack.get()))
  api(npm("buffer", libs.versions.npm.buffer.get()))
  api(npm("readable-stream", libs.versions.npm.stream.get()))

  implementation(project(":packages:graalvm-js"))

  implementation(libs.kotlinx.wrappers.node)
  implementation(libs.kotlinx.wrappers.react)
  implementation(libs.kotlinx.wrappers.react.dom)
  implementation(libs.kotlinx.coroutines.core.js)
  implementation(libs.kotlinx.serialization.core.js)
  implementation(libs.kotlinx.serialization.json.js)

  // Testing
  testImplementation(project(":packages:test"))
}
