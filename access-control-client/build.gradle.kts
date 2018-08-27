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
    compile ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    compile ("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    compile ("com.google.guava:guava:26.0-jre")
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

java.sourceSets {
    getByName("main") {
        java.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
}
