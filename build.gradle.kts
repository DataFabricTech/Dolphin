plugins {
    antlr
    id("com.mobigen.java-library")
    id("com.mobigen.java-application")
    id("org.springframework.boot") version "3.3.0"
}

apply(plugin = "io.spring.dependency-management")

allprojects {
    group = "${group}.dolphin"
    version = "1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

configurations.forEach {
    it.exclude("org.springframework.boot", "spring-boot-starter-logging")
}

dependencies {
    // antlr
    antlr("org.antlr:antlr4:4.13.1")
    compileOnly("org.antlr:antlr4-runtime:4.13.1")

    // api
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    // test
    implementation("org.springframework.boot:spring-boot-starter-test")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // - local
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:42.7.3")

    // - trino
    implementation("io.trino:trino-jdbc:450")
    compileOnly("io.trino:trino-spi:450")
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.25.0-alpha")
    implementation("io.opentelemetry:opentelemetry-api:1.40.0")

    // - open metadata
    implementation("org.springframework.boot:spring-boot-starter-webflux")
//    implementation("org.open-metadata:openmetadata-spec:1.4.1")
//    implementation("org.open-metadata:openmetadata-service:1.4.1")

    // jaeger
//    implementation("io.opentracing.contrib:opentracing-spring-jaeger-web-starter:3.3.1")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:3.1.11"){
        exclude(group = "org.springframework.cloud", module = "spring-cloud-sleuth-brave")
    }
    implementation("org.springframework.cloud:spring-cloud-sleuth-otel-autoconfigure:1.1.4")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.39.0")
    implementation("io.micrometer:micrometer-tracing-bridge-otel:1.3.2")
//    implementation("io.opentelemetry:opentelemetry-exporter-jaeger:1.39.0")

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
