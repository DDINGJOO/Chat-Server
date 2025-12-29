# Chat Server - 프로젝트 정보

> **Version**: 0.0.1-SNAPSHOT
> **Team**: Bander Backend Team
> **Tech Stack**: Spring Boot 3.5.9, MongoDB, Redis, Kafka

---

## 프로젝트 개요

**Chat Server**는 플랫폼 내 사용자 간 실시간 메시징 기능을 제공하는 마이크로서비스입니다.

**핵심 기능**:

- 1:1 DM, 그룹 채팅, 공간 문의, 고객 상담
- 메시지 읽음 확인
- 푸시 알림 연동 (NOTIFICATION 서버 경유)
- Kafka 기반 이벤트 발행

**아키텍처**: Hexagonal Architecture + DDD

---

## 문서 구조

```
docs/
├── INFO.md                  # 프로젝트 개요 (현재 문서)
├── ISSUE_GUIDE.md           # 이슈 작성 가이드
├── PROJECT_SETUP.md         # AI 어시스턴트 운영 가이드
└── CHAT_SERVER_GUIDE.md     # 서비스 요구사항/API 명세
```

---

## 목차

- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [자동 라벨링 시스템](#자동-라벨링-시스템)
- [이슈 관리](#이슈-관리)
- [PR 자동화](#pr-자동화)
- [사용 가이드](#사용-가이드)

---

## 핵심 기능

### 채팅 유형

| 유형        | 설명                | 최대 참여자 |
|-----------|-------------------|--------|
| **DM**    | 1:1 개인 대화         | 2명     |
| **GROUP** | 다수 사용자 그룹 채팅      | 100명   |
| **PLACE_INQUIRY** | 공간 문의 (게스트 + 호스트) | 2명     |
| **SUPPORT** | 고객 상담 (사용자 + 상담원) | 2명     |

### 메시지 규칙

| 규칙       | 설명              |
|----------|-----------------|
| 최대 길이    | 5,000자          |
| 삭제       | 본인 화면에서만 삭제     |
| 수정       | 불가              |

### Kafka 이벤트

| 토픽명                | 설명          |
|--------------------|-------------|
| `chat-message-sent` | 새 메시지 알림    |
| `chat-inquiry-created` | 공간 문의 생성 알림 |
| `support-requested` | 상담 요청 알림    |

---

## 기술 스택

| 분류                 | 기술                             |
|--------------------|--------------------------------|
| **Framework**      | Spring Boot 3.5.9              |
| **Language**       | Java 21                        |
| **Architecture**   | Hexagonal Architecture + DDD   |
| **Database**       | MongoDB (Document Store)       |
| **Cache**          | Redis (읽음 상태, 세션)              |
| **Message Broker** | Apache Kafka                   |
| **Test**           | Spock Framework (Groovy)       |

---

## 패키지 구조

```
com.teambind.co.kr.chatdding
├── domain/                    # 핵심 도메인 (의존성 없음)
│   ├── chatroom/              # ChatRoom Aggregate
│   ├── message/               # Message Aggregate
│   └── support/               # Support 도메인
│
├── application/               # Use Cases
│   ├── port/in/               # Driving Ports (UseCase interfaces)
│   ├── port/out/              # Driven Ports (Repository interfaces)
│   └── service/               # UseCase 구현체
│
├── adapter/                   # 외부 연결
│   ├── in/web/                # REST Controller
│   └── out/
│       ├── persistence/       # MongoDB Adapter
│       ├── messaging/         # Kafka Adapter
│       └── cache/             # Redis Adapter
│
├── config/                    # 설정 클래스
└── common/exception/          # 공통 예외 처리
```

---

## 이슈 타입과 라벨

### 이슈 타입

| 타입                 | 라벨               | 용도                  | 소요 시간    |
|--------------------|------------------|---------------------|----------|
| **Epic**           | `epic`           | 큰 기능 (여러 Story로 구성) | 1~2주 이상  |
| **Story**          | `story`          | 사용자 관점의 완결된 기능      | 2~5일     |
| **Task**           | `task`           | 실제 개발 작업 단위         | 반나절~1일   |
| **Spike**          | `spike`          | 조사/실험 (시간 제한)       | 설정한 타임박스 |
| **Change Request** | `change-request` | 설계/AC 변경 제안         | -        |

### 이슈 계층 구조

```
Epic #1: Chat Server 핵심 기능 구현
  ↓
Story #2: 메시지 전송 기능
  ↓
Task #5: Message 도메인 모델 구현
Task #6: MessageRepository 구현
Task #7: SendMessageUseCase 구현
Task #8: 메시지 전송 API 구현
```

자세한 사용법은 [ISSUE_GUIDE.md](ISSUE_GUIDE.md)를 참고하세요.

---

## 자동 라벨링 시스템

PR이 생성되거나 업데이트되면 변경된 파일 경로를 분석하여 자동으로 라벨을 부여합니다.

### 백엔드 레이어 라벨

| 라벨                 | 매칭 규칙                            | 설명               |
|--------------------|----------------------------------|------------------|
| `layer:domain`     | `**/domain/**`                   | 도메인 모델           |
| `layer:application`| `**/application/**`              | UseCase/Service  |
| `layer:adapter`    | `**/adapter/**`                  | Adapter 구현       |
| `layer:config`     | `**/config/**`                   | 설정 클래스           |
| `layer:test`       | `src/test/**`                    | 테스트 코드           |

---

## PR 자동화

### 이슈 자동 닫기

PR 본문에 다음 키워드를 포함하면 PR 머지 시 해당 이슈가 자동으로 닫힙니다:

```markdown
Closes #10
Fixes #15
Resolves #20
```

### AI 어시스턴트를 통한 PR 생성

Claude Code를 사용하면 자동으로 관련 이슈를 찾아서 PR을 생성합니다:

```bash
# 1. 작업 브랜치에서 개발
git checkout -b feature/send-message
# 코딩...
git commit -m "메시지 전송 API 구현"
git push

# 2. Claude Code에게 요청
"PR 만들어줘"

# 3. AI가 자동으로:
# - 커밋 메시지 분석
# - 열린 이슈 검색
# - 관련 이슈 매칭
# - PR 생성 with "Closes #N"
```

---

## 사용 가이드

### 로컬 개발 환경 실행

```bash
# 1. 인프라 시작 (MongoDB, Redis, Kafka)
cd ChatDDing-service
docker-compose up -d

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. 관리 UI 접속
# - MongoDB: http://localhost:8081
# - Redis: http://localhost:8082
# - Kafka: http://localhost:8083
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# Spock 테스트만
./gradlew test --tests "*Spec"
```

### 일반적인 워크플로우

```
1. Epic 이슈 생성 (#1)
   ↓
2. Story 이슈 생성 (#2) - Epic과 연결
   ↓
3. Task 이슈 생성 (#5, #6, #7) - Story와 연결
   ↓
4. feature 브랜치에서 작업
   ↓
5. PR 생성 with "Closes #5"
   ↓
6. 코드 리뷰
   ↓
7. PR 머지 → 이슈 #5 자동 닫힘
```

---

## 문서 참조

| 문서                  | 설명              | 대상     |
|---------------------|-----------------|--------|
| CHAT_SERVER_GUIDE.md | 서비스 요구사항/API 명세 | 전체     |
| ISSUE_GUIDE.md       | 이슈 타입별 작성 예시    | 전체     |
| PROJECT_SETUP.md     | AI 어시스턴트 운영 가이드 | AI/개발자 |

---

**Last Updated**: 2025-12-29
