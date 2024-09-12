#!/bin/sh

# 기본값 설정
INCLUDE_TESTS="true"

# 인자값이 제공되었는지 확인
if [ "$1" != "" ]; then
    INCLUDE_TESTS=$1
fi

# 인자값 출력
echo "Include tests: $INCLUDE_TESTS"

# 조건에 따라 Gradle 빌드 수행
if [ "$INCLUDE_TESTS" = "false" ]; then
    echo "Running Gradle build without tests..."
    gradle build -x test -x :testClasses --no-daemon --stacktrace --parallel
else
    echo "Running Gradle build with tests..."
    gradle build --no-daemon --stacktrace --parallel
fi