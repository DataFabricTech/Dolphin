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
