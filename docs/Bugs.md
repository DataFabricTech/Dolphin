# Bugs

- ANTLR4-Visitor
  - from 절 alias 가 컬럼명 앞에 붙은 경우에 대한 처리가 안되고 있음
    - ex) `select a.bb from abc a` -> `select internal.defualt.a.bb from test.public.abc as a`
