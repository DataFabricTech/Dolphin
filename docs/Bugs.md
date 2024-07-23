# Bugs


## 해결 완료
- ANTLR4-Visitor -- [수정커밋](https://github.com/DataFabricTech/Dolphin/commit/eb7483dd2cf8d2fb367d348eed21f165f0848ec2)
  - from 절 alias 가 컬럼명 앞에 붙은 경우에 대한 처리가 안되고 있음
    - ex) `select a.bb from abc a` -> `select internal.defualt.a.bb from test.public.abc as a`
