val kotlinVersion = "1.2.61"
plugins {
    val kotlinVersion = "1.2.61"
    application
    java
    idea
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
}

application {
    mainClassName = "br.com.ntopus.accesscontrol.MainKt"
}

buildscript {
    val kotlinVersion = "1.2.61"
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.6")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")
    }
}

dependencies {
    compile(project(":access-control-janusgraph"))
    compile(project(":access-control-proto"))
    compile ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile ("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    compile ("org.apache.tinkerpop:gremlin-core:3.3.3")
    compile ("org.janusgraph:janusgraph-core:0.3.0")
    compile ("org.janusgraph:janusgraph-cassandra:0.3.0")
    compile ("org.janusgraph:janusgraph-es:0.3.0")
    compile ("org.janusgraph:janusgraph-cql:0.3.0")
    compile ("org.jetbrains.kotlin:kotlin-runtime:${kotlinVersion}")
    implementation ("com.github.BAData:protobuf-converter:1.1.5")
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