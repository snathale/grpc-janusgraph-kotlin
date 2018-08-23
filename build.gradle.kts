import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

//import com.google.protobuf.gradle.ExecutableLocator
//import com.google.protobuf.gradle.GenerateProtoTask
//import com.google.protobuf.gradle.ProtobufConfigurator
//import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//val grpcVersion = "1.14.0"
//
//plugins {
//    base
////    application
//    idea
//    kotlin("jvm") version "1.2.61"
//    id("com.google.protobuf") version "0.8.6"
//    java
////    distribution
//}
//
//allprojects {
//    group = "br.com.ntopus"
//    version = "1.0-SNAPSHOT"
//
//    repositories {
//        mavenCentral()
//    }
//}
//
//buildscript {
//    val kotlinVersion = "1.2.61"
//    repositories {
//        mavenCentral()
//    }
//    dependencies {
//        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.6")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
//    }
//}
//
//val kotlinVersion = "1.2.61"
//dependencies {
//    subprojects.forEach{
//        archives(it)
//    }
//    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
//    compile ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
//    compile ("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
//    compile ("org.apache.tinkerpop:gremlin-core:3.3.3")
//    compile ("org.janusgraph:janusgraph-core:0.2.1")
//    compile ("org.janusgraph:janusgraph-cassandra:0.2.1")
//    compile ("org.janusgraph:janusgraph-es:0.2.1")
//    compile ("org.janusgraph:janusgraph-cql:0.2.1")
//    compile ("org.jetbrains.kotlin:kotlin-runtime:$kotlinVersion")
//    compile ("com.google.api.grpc:proto-google-common-protos:1.12.0")
//    compile ("io.grpc:grpc-netty-shaded:$grpcVersion")
//    compile ("io.grpc:grpc-protobuf:$grpcVersion")
//    compile ("io.grpc:grpc-stub:$grpcVersion")
//}
//
//protobuf.protobuf.run {
//    protoc(delegateClosureOf<ExecutableLocator> {
//        artifact = "com.google.protobuf:protoc:3.6.1"
//    })
//    plugins(delegateClosureOf<NamedDomainObjectContainer<ExecutableLocator>> {
//        this {
//            "grpc" {
//                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
//            }
//        }
//    })
//    generateProtoTasks(delegateClosureOf<ProtobufConfigurator.GenerateProtoTaskCollection> {
//        all().forEach {
//            it.plugins(delegateClosureOf<NamedDomainObjectContainer<GenerateProtoTask.PluginOptions>> {
//                this {
//                    "grpc"{
//                        option("enable_deprecated=false")
//                    }
//                }
//            })
//        }
//    })
//}
//
//idea {
//    module {
//        sourceDirs = sourceDirs.plus(file("${projectDir}/build/generated/source/proto/main/java"))
//        sourceDirs = sourceDirs.plus(file("${projectDir}/build/generated/source/proto/main/grpc"))
//    }
//}
//val compileKotlin by tasks.getting(KotlinCompile::class) {
//    doLast { println("Finished compiling Kotlin source code") }
//}
//val compileTestKotlin by tasks.getting(KotlinCompile::class) {
//    // Customise the “compileTestKotlin” task.
//    kotlinOptions.jvmTarget = "1.8"
//
//    doLast { println("Finished compiling Kotlin source code for testing") }
//}
//
//compileKotlin.dependsOn(":generateProto")
//
//java.sourceSets{
//    getByName("main").java.srcDirs("build/generated/source/proto/main/java")
//    getByName("main").java.srcDirs("build/generated/source/proto/main/grpc")
//}
//
////val startScripts: CreateStartScripts by tasks
//
//////startScripts.enabled = false
////val jar: Jar by tasks
////
////val accessControlClient by tasks.creating(CreateStartScripts::class){
////    mainClassName = "br.com.ntopus.accesscontrol.client.MainKt"
////    applicationName = "access-control-client"
//////    outputDir = File(project.buildDir, "tmp")
////    outputDir = startScripts.outputDir
////    classpath = startScripts.classpath
//////    classpath = jar.outputs.files + project.configurations.runtime
////
////    doLast {
////        println("Finished creating client script")
////    }
////}
////
////val accessControlServer by tasks.creating(CreateStartScripts::class){
////    mainClassName = "br.com.ntopus.accesscontrol.server.MainKt"
////
////    applicationName = "access-control-server"
//////    outputDir = File(project.buildDir, "tmp")
////    outputDir = startScripts.outputDir
////    classpath = startScripts.classpath
//////    classpath = jar.outputs.files + project.configurations.runtime
////
////    doLast {
////        println("Finished creating server script")
////    }
////}
//
////application {
////    mainClassName = "br.com.ntopus.accesscontrol.server.MainKt"
////    distributions {
////        getByName("main") {
////            contents {
////                from(accessControlServer)
////                from(accessControlClient)
////                into("bin")
////                fileMode = 755
////            }
////        }
////    }
////}

val kotlinVersion = "1.2.61"
plugins {
    base
    kotlin("jvm") version "1.2.61"
}


allprojects {

    group = "br.com.ntopus.accesscontrol"

    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven ("https://jitpack.io")
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    compile ("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    subprojects.forEach {
        archives(it)
    }
}
