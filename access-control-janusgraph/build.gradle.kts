import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val kotlinVersion = "1.2.61"
plugins {
    val kotlinVersion = "1.2.61"
    application
    java
    idea
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
}

application{
    mainClassName="br.com.ntopus.accesscontrol.MainKt"
}
dependencies {
    compile ("com.fasterxml.jackson.core:jackson-databind:2.9.6")
    compile ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compile ("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    compile ("org.apache.tinkerpop:gremlin-core:3.3.3")
    compile ("org.janusgraph:janusgraph-core:0.3.0")
    compile ("org.janusgraph:janusgraph-cassandra:0.3.0")
    compile ("org.janusgraph:janusgraph-es:0.3.0")
    compile ("org.janusgraph:janusgraph-cql:0.3.0")
    compile ("org.jetbrains.kotlin:kotlin-runtime:${kotlinVersion}")
}
//distributions {
//     getByName("main"){
//         contents {
//             from("resources")
//             into("/")
//             fileMode = 755
//         }
//     }
// }

//val sourceSets = java.sourceSets
//fun sourceSets(block: SourceSetContainer.() -> Unit) = sourceSets.apply(block)
//
//val SourceSetContainer.main: SourceSet get() = getByName("main")
//fun SourceSetContainer.main(block: SourceSet.() -> Unit) = main.apply(block)
//
//val SourceSet.kotlin: SourceDirectorySet
//    get() = (this as HasConvention).convention.getPlugin<KotlinSourceSet>().kotlin
//var SourceDirectorySet.sourceDirs: Iterable<File>
//    get() = srcDirs
//    set(value) { setSrcDirs(value) }

java.sourceSets {
    getByName("main") {
        resources.srcDirs("src/main/resources")
    }
}
