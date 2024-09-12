import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    antlr
    id("com.mobigen.spring-boot-application")
}

group = "${group}.dolphin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.6.0")
    }
}

dependencies {
    // antlr
    antlr("org.antlr:antlr4:4.13.1")
    compileOnly("org.antlr:antlr4-runtime:4.13.1")

    // openapi ui - swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // database driver
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:42.7.3")

    // trino
    implementation("io.trino:trino-jdbc:450")
    compileOnly("io.trino:trino-spi:450")

    // For M1 MAC - netty native library is not supported.
    if (Os.isArch("aarch_64")) {
        implementation("io.netty:netty-resolver-dns-native-macos:4.1.113.Final")
    }

    // Client : Integration With DataFabric Server(OM)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation(files("libs/openmetadata-spec-1.4.0.jar"))

    // jaeger
    // https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/getting-started/
    // https://www.jaegertracing.io/docs/1.59/deployment/
    // https://opentelemetry.io/docs/languages/sdk-configuration/general/
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("io.opentelemetry:opentelemetry-exporter-jaeger:1.34.1")

    // json?
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    runtimeOnly("com.h2database:h2")

    // csv
    implementation("com.opencsv:opencsv:5.9")

    // parquet
//    implementation("org.apache.parquet:parquet-avro:1.13.1")
//    implementation("org.apache.hadoop:hadoop-common:3.3.6")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages")
    outputDirectory = file("${outputDirectory}/com/mobigen/dolphin/antlr")
}