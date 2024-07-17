# Deployment

## Build Docker Image

```bash
DOCKER_BUILDKIT=1 docker buildx build \
  --target=prod \
  --platform=linux/amd64 \
  -f ./Dockerfile \
  -t test-prod-image:v1.0 \  
  .
```

## Dolphin Settings

### dolphin (application.yml)

| key                                                   | example                                             | Description                                                                                 |
|-------------------------------------------------------|-----------------------------------------------------|---------------------------------------------------------------------------------------------|
| dolphin.model.om-trino-database-service               | datamodels                                          | OpenMetadata 에서 Trino 를 DatabaseService 에 등록한 이름                                            |
| dolphin.model.catalog                                 | internalhive                                        | trino 가 hive metastore 를 연결 할 수 있도록 설정한 trino 의 catalog 이름                                  |
| dolphin.model.schema.db                               | default                                             | Hive metastore 의 schema                                                                     |
| dolphin.model.schema.file                             | fileModels                                          | Hive metastore 의 schema                                                                     |
| dolphin.job.datasource.hikari.driver-class-name       | com.mysql.cj.jdbc.Driver<br />org.postgresql.Driver | Dolphin 의 데이터를 저장하는 DB 연결을 위한 driver class path                                             |
| dolphin.job.datasource.hikari.username                | admin                                               | Dolphin 의 데이터를 저장하는 DB 연결을 위한 user                                                          |
| dolphin.job.datasource.hikari.password                |                                                     | Dolphin 의 데이터를 저장하는 DB 연결을 위한 password                                                      |
| dolphin.job.datasource.hikari.jdbc-url                |                                                     | Dolphin 의 데이터를 저장하는 DB 연결을 위한 jdbc url                                                      |
| dolphin.job.hibernate.property.hibernate.hbm2ddl.auto | none                                                | spring.jpa.hibernate.ddl-auto 값<br />none, create, create-drop, update, validate            |
| dolphin.trino.datasource.hikari.driver-class-name     | io.trino.jdbc.TrinoDriver                           | Trino JDBC 연결을 위한 driver class path                                                         |
| dolphin.trino.datasource.hikari.username              |                                                     | trino username                                                                              |
| dolphin.trino.datasource.hikari.password              |                                                     | trino password                                                                              |
| dolphin.trino.datasource.hikari.jdbc-url              |                                                     | trino JDBC url                                                                              |
| dolphin.openmetadata.api-url                          |                                                     | Open Metadata api url                                                                       |
| dolphin.openmetadata.bot-token                        |                                                     | OpenMetadata 의 API 사용을 위한 Bot token                                                         |
| dolphin.openmetadata.ingestion.metadata               |                                                     | Metadata Ingestion Id<br />데이터 모델 생성 후 OpenMetadata 에서 인식 할 수 있도록 Ingestion 호출을 위한 값        |
| dolphin.openmetadata.ingestion.profiler               |                                                     | Profiler Ingestion Id<br />데이터 모델 생성 후 OpenMetadata 에서 sample 을 등록 하기 위해 Ingestion 호출을 위한 값 |
| dolphin.hive-metastore.uri                            |                                                     | trino->hive metastore catalog 생성을 위한 HMS Host/IP                                            |
| dolphin.hive-metastore.options                        |                                                     | trino->hive metastore catalog 생성을 위한 HMS Port                                               |

### Environment Settings

- `application.yml.template` 를 그대로 사용하는 경우 아래와 같은 환경변수를 세팅해주면 실행 가능

| key                      | example                                             | Description                                                                                 |
|--------------------------|-----------------------------------------------------|---------------------------------------------------------------------------------------------|
| OM_TRINO_SERVICE_NAME    | datamodels                                          | OpenMetadata 에서 Trino 를 DatabaseService 에 등록한 이름                                            |
| TRINO_DATA_MODEL_CATALOG | internalhive                                        | trino 가 hive metastore 를 연결 할 수 있도록 설정한 trino 의 catalog 이름                                  |
| TRINO_DB_BASE_SCHEMA     | default                                             | Hive metastore 의 schema                                                                     |
| TRINO_FILE_BASE_SCHEMA   | fileModels                                          | Hive metastore 의 schema                                                                     |
| DB_DRIVER_CLASSPATH      | com.mysql.cj.jdbc.Driver<br />org.postgresql.Driver | Dolphin 의 데이터를 저장하는 DB 연결을 위한 driver class path                                             |
| DB_USER                  | admin                                               | Dolphin 의 데이터를 저장하는 DB 연결을 위한 user                                                          |
| DB_PASSWORD              |                                                     | Dolphin 의 데이터를 저장하는 DB 연결을 위한 password                                                      |
| DB_TYPE                  | mysql                                               | Dolphin 의 데이터를 저장하는 DB 의 타입<br />mysql, postgresql                                          |
| DB_HOST                  |                                                     | Dolphin 의 데이터를 저장하는 DB 의 Host/IP                                                            |
| DB_PORT                  |                                                     | Dolphin 의 데이터를 저장하는 DB 의 Port                                                               |
| DB_NAME                  |                                                     | Dolphin 의 데이터를 저장하는 DB 명                                                                    |
| DB_DDL_AUTO              | none                                                | spring.jpa.hibernate.ddl-auto 값<br />none, create, create-drop, update, validate            |
| TRINO_HOST               | 0.0.0.0                                             | trino 의 Host/IP                                                                             |
| TRINO_PORT               | 8080                                                | trino 의 Port                                                                                |
| OPEN_METADATA_HOST       | 0.0.0.0                                             | OpenMetadata 의 Host/IP                                                                      |
| OPEN_METADATA_PORT       | 8585                                                | OpenMetadata 의 Port                                                                         |
| OPEN_METADATA_TOKEN      |                                                     | OpenMetadata 의 API 사용을 위한 Bot token                                                         |
| INGESTION_ID_METADATA    |                                                     | Metadata Ingestion Id<br />데이터 모델 생성 후 OpenMetadata 에서 인식 할 수 있도록 Ingestion 호출을 위한 값        |
| INGESTION_ID_PROFILER    |                                                     | Profiler Ingestion Id<br />데이터 모델 생성 후 OpenMetadata 에서 sample 을 등록 하기 위해 Ingestion 호출을 위한 값 |
| HIVE_METASTORE_HOST      |                                                     | trino->hive metastore catalog 생성을 위한 HMS Host/IP                                            |
| HIVE_METASTORE_PORT      | 9083                                                | trino->hive metastore catalog 생성을 위한 HMS Port                                               |

## Dependencies

- open metadata 설치는 따로 진행 해야 한다.
    - `docker-compose.yml` 에 해당 설정을 수정 해야 한다.
        - 연결정보, token, ingestion ids(metadata, profiler)

## Deploy - Docker Compose

```bash
# 디렉토리 이동
cd deploy/docker-compose

# 설정 맞추기
## 기본적으로 바로 배포 가능 하나 몇 가지 확인 하면 좋음
vi docker-compose.yml
## 각 service 의 port forwarding 확인 해서 수정
## dolphin 쪽 open_metadata 관련 환경 변수 설정
vi .env
## 각 이미지 태그 수정
## DB 관련 설정 수정 

docker compose up
```

## Deploy - Kubernetes
