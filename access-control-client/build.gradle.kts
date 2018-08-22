plugins {
    application
    java
    idea
    kotlin("jvm")
}

application {
    mainClassName = "br.com.ntopus.accesscontrol.MainKt"
}

val kotlinVersion = "1.2.61"
dependencies {
    compile(project(":access-control-proto"))
    compile ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile ("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}

idea {
    module {
        sourceDirs = sourceDirs.plus(file("${projectDir}/build/generated/source/proto/main/java"))
        sourceDirs = sourceDirs.plus(file("${projectDir}/build/generated/source/proto/main/grpc"))
    }
}

java.sourceSets{
    getByName("main").java.srcDirs("build/generated/source/proto/main/java")
    getByName("main").java.srcDirs("build/generated/source/proto/main/grpc")
}
