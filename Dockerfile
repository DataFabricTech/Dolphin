FROM openjdk:21-jdk-slim as package-image

COPY gradle /app/gradle
COPY --chmod=755 gradlew /app/
COPY gradle.properties /app/
COPY aggregation /app/aggregation
COPY build-logic /app/build-logic
COPY build.gradle.kts /app/
COPY settings.gradle.kts /app/
COPY src /app/src

WORKDIR /app

RUN ./gradlew build -x test --parallel --continue > /dev/null 2>&1 || true

ARG project_version=v0.1.0
ARG skip_test=true

RUN ./gradlew clean build --no-parallel -Dorg.gradle.workers.max=1

FROM openjdk:21-jdk-slim as prod

ARG project_version=v0.1.0
ARG description="Dolphin Image"

LABEL email="irisdev@mobigen.com"
LABEL name="mobigen-platform-team"
LABEL version="${project_version}"
LABEL description="${description}"

RUN apt-get update && apt-get install -y locales \
    && sed -i 's/^# \(ko_KR.UTF-8\)/\1/' /etc/locale.gen \
    && locale-gen && localedef -f UTF-8 -i ko_KR ko_KR.UTF-8 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY --from=package-image /app/build/libs/dolphin-1.0-SNAPSHOT.jar /app/dolphin.jar
COPY src/main/resources/application.yml.template /app/application.yml

WORKDIR /app
ENV CONFIG_FILE "/app/application.yml"
CMD java -jar -Dspring.config.location=${CONFIG_FILE} /app/dolphin.jar

FROM package-image as test-result-image

COPY --from=package-image /build /app
COPY --from=package-image /root/.m2 /app/.m2

WORKDIR /app

CMD ls -la /app
