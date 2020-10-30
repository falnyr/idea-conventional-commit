import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  id("org.jetbrains.intellij") version "0.4.26"
  kotlin("jvm") version "1.4.20-M2"
}

group = "com.github.lppedd"
version = "0.15.3"

repositories {
  maven("https://dl.bintray.com/kotlin/kotlin-eap")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8", "1.4.20-M2"))

  implementation("cglib:cglib-nodep:3.3.0")
  implementation("org.json", "json", "20200518")
  implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.12.1")

  testImplementation("junit:junit:4.12")
}

intellij {
  version = "IU-192.5728.98"
  downloadSources = true
  pluginName = "idea-conventional-commit"
  setPlugins("java")
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

/** Points to the Java executable (usually `java.exe`) of a DCEVM-enabled JVM. */
val dcevmExecutable: String? by project

tasks {
  runIde {
    if (project.property("dcevm") == true) {
      dcevmExecutable?.let(::setExecutable)
    }
  }

  val kotlinSettings: KotlinCompile.() -> Unit = {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
      "-Xno-call-assertions",
      "-Xno-receiver-assertions",
      "-Xno-param-assertions",
      "-Xjvm-default=enable",
      "-Xallow-kotlin-package",
      "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
      "-Xopt-in=kotlin.contracts.ExperimentalContracts",
      "-XXLanguage:+InlineClasses"
    )
  }

  compileKotlin(kotlinSettings)
  compileTestKotlin(kotlinSettings)

  patchPluginXml {
    version(project.version)
    sinceBuild("192.5728")
    untilBuild("203.*")
    pluginDescription(File("plugin-description.html").readText(Charsets.UTF_8))
    changeNotes(File("change-notes/${version.replace('.', '_')}.html").readText(Charsets.UTF_8))
  }
}
