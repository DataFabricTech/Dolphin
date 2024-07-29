# Bugs

## 해야 할 것
- create model API 에서 `MODEL` 타입에 대한 모호함이 있음
  - OpenMetadata 의 테이블(FQN) 인지 trino 의 view 인지 구분을 할 수 있도록 분리 해야함 

## 해결 완료
- ANTLR4-Visitor -- [수정커밋](https://github.com/DataFabricTech/Dolphin/commit/eb7483dd2cf8d2fb367d348eed21f165f0848ec2)
  - from 절 alias 가 컬럼명 앞에 붙은 경우에 대한 처리가 안되고 있음
    - ex) `select a.bb from abc a` -> `select internal.defualt.a.bb from test.public.abc as a`
