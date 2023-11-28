plugins {
    java
}

group = "io.github.gaming32"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val include by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    val classTransformVersion = "1.12.1"
    include("net.lenni0451.classtransform:core:$classTransformVersion")
    include("net.lenni0451.classtransform:mixinsdummy:$classTransformVersion")
    include("net.lenni0451.classtransform:mixinstranslator:$classTransformVersion")

    include("net.lenni0451:Reflect:1.3.0")
}

tasks.jar {
    dependsOn(include)
    from({
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        include.map { zipTree(it) }
    }) {
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}

tasks.compileJava {
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
    options.compilerArgs.add("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
