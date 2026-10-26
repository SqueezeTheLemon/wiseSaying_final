plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // MySQL JDBC
    runtimeOnly("com.mysql:mysql-connector-j:9.5.0")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")

    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}