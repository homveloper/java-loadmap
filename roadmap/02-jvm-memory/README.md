# 02. JVM과 메모리 관리

> **학습 기간**: 1주
> **난이도**: ⭐⭐⭐☆☆
> **전제 조건**: Java 기초, C++ 메모리 관리 경험

## 📚 학습 목표

C++의 메모리 관리 경험을 활용하여 JVM의 메모리 구조와 가비지 컬렉션을 이해합니다.

## 🎯 핵심 내용

### 1. JVM 아키텍처
- Class Loader
- Runtime Data Areas (힙, 스택, 메서드 영역)
- Execution Engine (인터프리터, JIT 컴파일러)
- Garbage Collector

### 2. 메모리 영역
- **힙 (Heap)**: 객체 저장
- **스택 (Stack)**: 메서드 호출, 지역 변수
- **메타스페이스 (Metaspace)**: 클래스 메타데이터
- **PC Register**: 현재 실행 중인 명령어
- **Native Method Stack**: JNI 호출

### 3. 가비지 컬렉션
- **GC 알고리즘**: Serial, Parallel, CMS, G1GC, ZGC
- **세대별 수집**: Young Generation (Eden, Survivor), Old Generation
- **GC 튜닝**: 힙 크기 조정, GC 로그 분석

### 4. 메모리 누수
- **원인**: 컬렉션에 객체 계속 추가, Static 필드 참조, 리스너 미제거
- **탐지**: VisualVM, JProfiler, MAT (Memory Analyzer Tool)

### 5. JVM 옵션
```bash
# 힙 크기 설정
java -Xms512m -Xmx2g MyApp

# GC 선택
java -XX:+UseG1GC MyApp

# GC 로그
java -Xlog:gc* MyApp
```

## 📖 학습 리소스

- **Java Performance** - Scott Oaks
- [JVM Internals](https://blog.jamesdbloom.com/JVMInternals.html)
- [Understanding Java Garbage Collection](https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html)

## ✅ 체크리스트

- [ ] JVM 구조 이해
- [ ] 힙과 스택 차이
- [ ] GC 알고리즘 비교
- [ ] 메모리 누수 디버깅
- [ ] JVM 옵션 튜닝

## 🚀 다음 단계

**→ [03. Java 동시성과 멀티스레딩](../03-java-concurrency/)**

## 💡 C++ 개발자를 위한 팁

- **수동 메모리 관리 vs GC**: new/delete 대신 GC가 자동 관리
- **RAII**: Java의 try-with-resources가 유사한 역할
- **스마트 포인터**: Java의 모든 참조가 자동 관리됨
