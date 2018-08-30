import com.google.protobuf.gradle.ExecutableLocator
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufConfigurator

val grpcVersion = "1.14.0"

plugins {
    java
    kotlin("jvm")
    idea
    id("com.google.protobuf") version "0.8.6"
}

buildscript {
    val kotlinVersion = "1.2.61"
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.6")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")
    compile ("com.google.api.grpc:proto-google-common-protos:1.12.0")
    compile ("io.grpc:grpc-netty-shaded:$grpcVersion")
    compile ("io.grpc:grpc-protobuf:$grpcVersion")
    compile ("io.grpc:grpc-stub:$grpcVersion")
    testCompile ("io.grpc:grpc-testing:${grpcVersion}")
}

protobuf.protobuf.run {
    protoc(delegateClosureOf<ExecutableLocator> {
        artifact = "com.google.protobuf:protoc:3.6.1"
    })
    plugins(delegateClosureOf<NamedDomainObjectContainer<ExecutableLocator>> {
        this {
            "grpc" {
                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
            }
        }
    })
    generateProtoTasks(delegateClosureOf<ProtobufConfigurator.GenerateProtoTaskCollection> {
        all().forEach {
            it.plugins(delegateClosureOf<NamedDomainObjectContainer<GenerateProtoTask.PluginOptions>> {
                this {
                    "grpc"{
                        option("enable_deprecated=false")
                    }
                }
            })
        }
    })
}