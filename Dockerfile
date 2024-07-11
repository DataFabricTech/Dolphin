FROM openjdk:21-jdk-slim as package-image

COPY gradle /build/gradle
COPY --chmod=755 gradlew /build/
COPY gradle.properties /build/
COPY aggregation /build/aggregation
COPY build-logic /build/build-logic
COPY build.gradle.kts /build/
COPY settings.gradle.kts /build/
COPY src /build/src

WORKDIR /build

ARG project_version=v0.1.0
ARG skip_test=true

RUN if [ "$skip_test" = "true" ]; then \
      echo "build without test"; \
      ./gradlew build -x test --parallel --stacktrace --debug; \
    else \
      echo "build with test"; \
      ./gradlew build --parallel --stacktrace --debug  --scan ; \
    fi

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

COPY --from=package-image /build/libs/dolphin-1.0-SNAPSHOT.jar /app/dolphin.jar

WORKDIR /app
CMD java -jar /app/dolphin.jar

FROM package-image as test-result-image

COPY --from=package-image /build /app
COPY --from=package-image /root/.m2 /app/.m2

WORKDIR /app

CMD ls -la /app
