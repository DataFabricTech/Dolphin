ARG PROJECT_VERSION=v1.0.1
ARG DESCRIPTION="Dolphin Image"
ARG INCLUDE_TESTS=false

FROM gradle:8.10-jdk21-alpine as build-image

# Multi Stage 에서는 ARG 를 FROM 다음에 재 선언해주어야한다.
ARG INCLUDE_TESTS

WORKDIR /app

COPY aggregation /app/aggregation
COPY build-logic /app/build-logic
COPY build.gradle.kts /app/
COPY settings.gradle.kts /app/

RUN gradle clean build -x test --parallel --continue > /dev/null 2>&1 || true

COPY libs /app/libs
COPY src /app/src
COPY build.sh /app/build.sh

RUN chmod 755 ./build.sh
RUN /app/build.sh ${INCLUDE_TESTS}

FROM eclipse-temurin:21.0.4_7-jre-alpine as prod

# Multi Stage 에서는 ARG 를 FROM 다음에 재 선언해주어야한다.
ARG PROJECT_VERSION
ARG DESCRIPTION

LABEL email="irisdev@mobigen.com"
LABEL name="mobigen-platform-team"
LABEL version="${PROJECT_VERSION}"
LABEL description="${DESCRIPTION}"

RUN apk add --no-cache \
    curl \
    iputils \
    net-tools \
    tzdata

# Set Locale
ENV LC_ALL=ko_KR.UTF-8 \
    LANG=ko_KR.UTF-8 \
    LANGUAGE=ko_KR.UTF-8

# Set Timezone
#RUN cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
#	echo "Asia/Seoul" > /etc/timezone

COPY --from=build-image /app/build/libs/dolphin-1.0-SNAPSHOT.jar /app/dolphin.jar
COPY src/main/resources/application.yml.template /app/application.yml

WORKDIR /app
ENV CONFIG_FILE "/app/application.yml"
CMD java -jar /app/dolphin.jar --spring.config.location=file:/app/application.yml

FROM package-image as test-result-image

COPY --from=package-image /build /app
COPY --from=package-image /root/.m2 /app/.m2

WORKDIR /app

CMD ls -la /app
