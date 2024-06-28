# Developed

- API
  - 데이터 모델
    - [생성](../src/main/java/com/mobigen/dolphin/controller/ApiController.java#L43)
    - [리스트](../src/main/java/com/mobigen/dolphin/controller/ApiController.java#L37)
  - 쿼리
    - [blocking 실행](../src/main/java/com/mobigen/dolphin/controller/ApiController.java#L57)
    - [nonblocking 실행](../src/main/java/com/mobigen/dolphin/controller/ApiController.java#L63)
    - [작업 상태 조회](../src/main/java/com/mobigen/dolphin/controller/ApiController.java#L81)
    - [작업 결과 조회](../src/main/java/com/mobigen/dolphin/controller/ApiController.java#L69)
  - OpenMetadata notify
    - [notify](../src/main/java/com/mobigen/dolphin/controller/OMNotifyController.java#L27)
  - 시스템
    - [상태 조회](../src/main/java/com/mobigen/dolphin/controller/StatusController.java#L28)
- 임시 저장
  - nonblocking query 수행시 trino 결과를 csv 로 저장/읽기 동작을 하고 있음
  - 로컬에 파일로 저장함
    - 서비스 레플리카 수행시 nonblocking 쿼리 결과 조회시 문제가 발생 할 수 있음

# Not Yet

- History
  - input 쿼리 -> trino 쿼리 로 변환 되므로, 히스토리 저장 필요
  - input 쿼리에서 데이터 모델을 지정하여 사용하기 때문에 히스토리 조회시 해당 모델을 함께 조회하기 위한 저장/조회 방법 필요
  - 쿼리 수행의 결과/상태 히스토리 저장
- Cache
  - 동일 쿼리 수행시 작업물을 빠르게 반환 하기 위한 것
- Jaeger
  - 시스템간 상태/성능 모니터링을 위함
- 파일 조회/융합
  - trino 를 이용하여 1개의 쿼리로 여러 minio 데이터를 조회/융합 하는 방법
  - [trino-storage](https://github.com/snowlift/trino-storage) plugin 프로젝트 이용
    - 배포 버전은 아니고, 개발은 꾸준히 되는 중 (trino 버전에 맞춰서)
    - trino 의 legacy 버전의 s3 를 사용하고 있어서 연결은 가능
    - trino 를 아래 조건으로 실행 시, 5~6 번 minio 데이터를 요청하는 쿼리를 수행하면 자원이 없다고 Timeout 이 발생
      - 1 worker
      - [jvm config](../config/trino/jvm.config)
    - trino-storage 를 native-s3 를 이용하도록 변경을 해보았으나, 코드 이해도가 적어서 테스트가 제대로 안됨
- OpenMetadata(OM)
  - 생성한 모델(trino-view)을 OM 에 ingestion 실행
  - OM 에 생성한 모델이 등록된 후, 기존 모델(OpenMetadata 의 선택한 테이블)을 Lineage 로 연결하도록 OM API 호출
  - OM 연결을 위한 bot getOrCreate 로직 필요 (token 발급용)
  - OM 의 모델 리스트를 조회 하도록 ?? (현재는 trino 모델만 조회되는 중)
- 임시 저장
  - nonblocking 쿼리 수행시 csv 외 포멧 지원
  - nonblocking 쿼리 수행시 minio 에 저장 지원
