plugins {
    antlr
    id("com.mobigen.java-library")
    id("com.mobigen.java-application")
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.0"
}

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

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.6.0")
    }
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
