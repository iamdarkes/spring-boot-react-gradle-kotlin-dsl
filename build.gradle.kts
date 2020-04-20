import com.moowork.gradle.node.npm.NpmTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.github.node-gradle.node") version "2.2.2"
    `maven-publish`
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
}

group = "me.darkes"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/iamdarkes/spring-boot-react-gradle-kotlin-dsl")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("spring-boot-react-gradle-kotlin-dsl") {
            from(components["java"])
        }
    }
}

val jar: Jar by tasks
val bootJar : BootJar by tasks
configurations {
    listOf(apiElements, runtimeElements).forEach { ndop ->
        ndop.get().outgoing.artifacts.removeIf { it.buildDependencies.getDependencies(null).contains(jar) }
        ndop.get().outgoing.artifact(bootJar)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.register<NpmTask>("appNpmInstall") {
    description = "Installs all dependencies from package.json"
    workingDir = file("${project.projectDir}/src/main/webapp")
    args = listOf("install")
}


tasks.register<NpmTask>("appNpmBuild") {
    dependsOn("appNpmInstall")
    description = "Builds project"
    workingDir = file("${project.projectDir}/src/main/webapp")
    args = listOf("run", "build")
}

tasks.register<Copy>("copyWebApp") {
    dependsOn("appNpmBuild")
    description = "Copies built project to where it will be served"
    from("src/main/webapp/build")
    into("build/resources/main/static/.")
}

node {
    download = true
    version = "12.13.1"
    npmVersion = "6.12.1"
    // Set the work directory for unpacking node
    workDir = file("${project.buildDir}/nodejs")
    // Set the work directory for NPM
    npmWorkDir = file("${project.buildDir}/npm")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    dependsOn("copyWebApp")
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
