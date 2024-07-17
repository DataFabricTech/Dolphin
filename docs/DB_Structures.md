# DataBase Table Structures

## Job Table

- query 실행 요청에 대하여 job 을 생성하고 관리하기 위한 테이블

| 이름              | 타입        | 설명                                                          |
|-----------------|-----------|-------------------------------------------------------------|
| id              | uuid      | 아이디                                                         |
| status          | string    | Job 의 상태값 ([상태값표](#Job Status) 참고)                          |
| user_query      | string    | API 를 통해 받은 user 의 원본 쿼리 (limit, offset 은 분리되어 기록 될 수 도 있음) |
| converted_query | string    | Trino 에서 실행 가능한 쿼리                                          |
| offset_         | integer   | API 를 통해 받은 user 의 원본 쿼리에서 가져온 offset 값                     |
| limit_          | integer   | API 를 통해 받은 user 의 원본 쿼리에서 가져온 limit 값                      |
| created         | timestamp | Job 생성 시간                                                   |
| updated         | timestamp | Job 수정 시간                                                   |
| result_name     | string    | Async 실행시 결과 데이터의 저장 이름                                     |
| result_path     | string    | Async 실행시 결과 데이터의 저장 경로                                     |

### Job Status

| 이름       | 설명                               |
|----------|----------------------------------|
| INIT     | API 로 요청이 들어와 Job 을 최초 생성시 상태    |
| QUEUED   | User query 를 분석하고 쿼리가 실행 가능 한 상태 |
| RUNNING  | Trino 를 이용해 쿼리를 수행하는 상태          |
| FINISHED | Trino 쿼리가 완료되고, 결과를 리턴/저장 한 상태   |
| FAILED   | 실패한 상태                           |
| CANCELED | 중지된 상태                           |

## FusionModel Table

| 이름                   | 타입     | 설명                                      |
|----------------------|--------|-----------------------------------------|
| id                   | long   | 아이디                                     |
| job_id               | uuid   | foreignkey of Job 테이블의 id               |
| model_id_of_om       | string | OpenMetadata 의 테이블/파일 id                |
| fully_qualified_name | string | OpenMetadata 의 테이블/파일의 FQN              |
| trino_model_name     | string | Trino 에서 인식 가능한 catalog.schema.table 이름 |

## ModelQueue Table

| 이름             | 타입     | 설명                                            |
|----------------|--------|-----------------------------------------------|
| id             | long   | 아이디                                           |
| job_id         | uuid   | foreignkey of Job 테이블의 id                     |
| model_name_fqn | string | OpenMetadata Lineage 추가를 위한 목적모델 FQN          |
| from_fqn       | string | OpenMetadata Lineage 추가를 위한 소스모델 FQN          |
| command        | string | Scheduler 실행을 위한 command (LINEAGE, INGESTION) |

