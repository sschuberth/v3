@file:Suppress(
  "UnstableApiUsage",
  "unused",
  "DSL_SCOPE_VIOLATION",
)

import dev.elide.buildtools.gradle.plugin.BuildMode
import tools.elide.assets.EmbeddedScriptLanguage

plugins {
  id("dev.elide.build.samples.backend")
  id("dev.elide.build.docker")
  id("dev.elide.buildtools.plugin")
  id("io.micronaut.application")
  id("io.micronaut.aot")
  id("dev.elide.build.native.app")
  alias(libs.plugins.jib)
}

group = "dev.elide.samples"
version = rootProject.version as String

elide {
  mode = if (devMode) {
    BuildMode.DEVELOPMENT
  } else {
    BuildMode.PRODUCTION
  }

  server {
    ssr(EmbeddedScriptLanguage.JS) {
      bundle(project(":samples:fullstack:react-ssr:node"))
    }
    assets {
      bundler {
        format(tools.elide.assets.ManifestFormat.BINARY)
        compression {
          minimumSizeBytes(0)
          keepAllVariants()
        }
      }

      // stylesheet: `styles.base`
      stylesheet("styles.base") {
        sourceFile("src/main/assets/basestyles.css")
      }

      script("scripts.ui") {
        from(project(":samples:fullstack:react-ssr:frontend"))
      }
    }
  }
}

micronaut {
  version.set(libs.versions.micronaut.lib.get())
  runtime.set(io.micronaut.gradle.MicronautRuntime.NETTY)
  processing {
    incremental.set(true)
    annotations.addAll(listOf(
      "$mainPackage.*",
    ))
  }
  aot {
    optimizeServiceLoading.set(true)
    convertYamlToJava.set(true)
    precomputeOperations.set(true)
    cacheEnvironment.set(true)
    optimizeClassLoading.set(true)
    optimizeNetty.set(true)

    netty {
      enabled.set(true)
    }
  }
}

val mainPackage = "fullstack.reactssr"
val mainEntry = "$mainPackage.App"
val devMode = (project.property("elide.buildMode") ?: "dev") == "dev"

application {
  mainClass.set(mainEntry)
  if (project.hasProperty("elide.vm.inspect") && project.properties["elide.vm.inspect"] == "true") {
    applicationDefaultJvmArgs = listOf(
      "-Delide.vm.inspect=true",
    )
  }
}

dependencies {
  api(libs.graalvm.sdk)
  api(kotlin("stdlib"))
  api(kotlin("stdlib-jdk8"))
  implementation(project(":packages:base"))
  implementation(project(":packages:server"))
  implementation(project(":packages:graalvm"))

  implementation(libs.jsoup)
  implementation(libs.micronaut.context)
  implementation(libs.micronaut.runtime)
  implementation(libs.kotlinx.html.jvm)
  implementation(libs.kotlinx.serialization.core.jvm)
  implementation(libs.kotlinx.serialization.json.jvm)
  implementation(libs.kotlinx.serialization.protobuf.jvm)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.core.jvm)
  implementation(libs.kotlinx.coroutines.jdk8)
  implementation(libs.kotlinx.coroutines.jdk9)
  implementation(libs.kotlinx.wrappers.css)
  implementation(libs.bouncycastle)
  implementation(libs.bouncycastle.pkix)
  implementation(libs.conscrypt)
  implementation(libs.tink)
  runtimeOnly(libs.logback)

  testImplementation(kotlin("test"))
  testImplementation(kotlin("test-junit5"))
  testImplementation(project(":packages:test"))
  testImplementation(libs.micronaut.test.junit5)
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuild") {
  images.set(listOf(
    "${project.properties["elide.publish.repo.docker.samples"]}/fullstack/react-ssr/jvm:latest"
  ))
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("optimizedDockerBuild") {
  images.set(listOf(
    "${project.properties["elide.publish.repo.docker.samples"]}/fullstack/react-ssr/jvm:opt-latest"
  ))
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("dockerBuildNative") {
  images.set(listOf(
    "${project.properties["elide.publish.repo.docker.samples"]}/fullstack/react-ssr/native:latest"
  ))
}

tasks.named<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("optimizedDockerBuildNative") {
  images.set(listOf(
    "${project.properties["elide.publish.repo.docker.samples"]}/fullstack/react-ssr/native:opt-latest"
  ))
}

tasks {
  jib {
    from {
      image = "us-docker.pkg.dev/elide-fw/tools/runtime/jvm17:latest"
    }
    to {
      image = "us-docker.pkg.dev/elide-fw/samples/fullstack/react-ssr/jvm"
      tags = setOf("latest", "jib")
    }
    container {
      jvmFlags = listOf("-Ddyme.runtime=JVM", "-Xms512m", "-Xdebug")
      mainClass = mainEntry
      ports = listOf("8080", "50051")
      format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.Docker
    }
  }
}
