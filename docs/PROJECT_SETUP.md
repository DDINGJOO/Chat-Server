# Chat Server - 프로젝트 설정 가이드

이 문서는 Chat Server의 프로젝트 설정 및 이슈/PR 관리 시스템에 대한 정보를 담고 있습니다.
AI 어시스턴트(Claude Code)가 이 문서를 읽고 자동으로 이슈와 PR을 관리합니다.

---

## 프로젝트 개요

### 서비스 목적

플랫폼 내 사용자 간 실시간 메시징 기능을 제공하는 마이크로서비스

### 핵심 기능

- **1:1 DM**: 사용자 간 개인 대화
- **그룹 채팅**: 다수 사용자가 참여하는 대화
- **고객 상담**: 운영팀과 사용자 간 문의 처리
- **읽음 확인**: 메시지 읽음 상태 표시
- **푸시 알림**: 새 메시지 알림 (NOTIFICATION 서버 연동)

---

## 프로젝트 정보

### 기본 정보

- **저장소**: Bander/Chat-Server
- **메인 브랜치**: main
- **개발 브랜치**: develop
- **기본 브랜치 전략**: Feature Branch Workflow

### 기술 스택

| 분류             | 기술                             |
|----------------|--------------------------------|
| Framework      | Spring Boot 3.5.9              |
| Language       | Java 21                        |
| Architecture   | Hexagonal Architecture + DDD   |
| Database       | MongoDB                        |
| Cache          | Redis                          |
| Message Broker | Apache Kafka                   |
| Test           | Spock Framework (Groovy)       |

### 브랜치 네이밍 규칙

- `feature/기능명` - 새로운 기능 개발
- `bugfix/버그명` - 버그 수정
- `hotfix/긴급수정명` - 긴급 수정
- `refactor/리팩토링명` - 리팩토링

---

## 이슈 타입과 라벨

### 1. Epic (라벨: `epic`, 색상: #8B5CF6)

**정의**: 큰 기능 단위 (여러 Story로 구성)

**언제 사용?**

- 1~2주 이상 걸리는 큰 기능
- 여러 개의 하위 작업(Story)으로 쪼갤 수 있는 경우

**필수 항목**:

- 목표 및 성공 지표
- 범위 (포함/제외)
- 하위 스토리 체크리스트
- 마일스톤

---

### 2. Story (라벨: `story`, 색상: #10B981)

**정의**: 사용자 관점의 완결된 기능 (2~5일 소요)

**언제 사용?**

- 사용자가 직접 사용하는 기능 하나
- Epic의 하위 작업으로 분해된 기능

**필수 항목**:

- 배경 (왜 필요한지)
- 수용 기준(AC) - 체크리스트 형식
- 연결된 Epic 번호

---

### 3. Task (라벨: `task`, 색상: #3B82F6)

**정의**: 실제 개발 작업 단위 (반나절~1일 소요)

**언제 사용?**

- 실제로 코드를 작성하는 작업
- Story의 구현을 위한 세부 작업

**필수 항목**:

- 연결된 Story/Epic 번호
- 작업 범위 (구체적으로)
- Done 기준 체크리스트

---

### 4. Spike (라벨: `spike`, 색상: #F59E0B)

**정의**: 조사/실험 작업 (시간 제한 있음)

**언제 사용?**

- 기술 조사가 필요할 때
- 여러 방법 중 선택이 필요할 때
- POC(개념 증명)가 필요할 때

**필수 항목**:

- 타임박스 (예: 1일, 4시간)
- 핵심 질문
- 산출물 (문서, ADR 등)

---

### 5. Change Request (라벨: `change-request`, 색상: #EF4444)

**정의**: 기존 계획/설계의 변경 제안

**언제 사용?**

- 디자인/기획이 변경됐을 때
- 더 나은 구현 방법을 발견했을 때
- AC(수용 기준) 수정이 필요할 때

**필수 항목**:

- 영향받는 Epic/Story/Task 번호
- 제안 변경 사항
- 영향도 분석
- 결정 근거

---

## 워크플로우

### 이슈 생성 플로우

```
Epic 생성
  ↓
Story 생성 (Epic과 연결)
  ↓
Task 생성 (Story와 연결)
  ↓
개발 진행
  ↓
PR 생성 (Task와 연결)
  ↓
PR 머지 → 이슈 자동 닫힘
```

---

## AI 어시스턴트 사용 가이드

### 이슈 자동 생성

**사용자 요청**:

```
"채팅 기능 이슈들을 발행해줘"
```

**AI가 하는 일**:

1. Epic, Story, Task 계층 구조로 이슈 생성
2. 각 이슈를 연결 (#번호로 참조)
3. 적절한 라벨 자동 할당
4. 이슈 번호 반환

---

### PR 자동 생성 with 이슈 연결

**사용자 요청**:

```
"PR 만들어줘"
```

**AI가 하는 일**:

1. 현재 브랜치의 커밋 메시지 분석
2. 변경된 파일 확인
3. 열린 이슈 목록 조회 (`gh issue list --state open`)
4. 커밋 내용과 이슈 매칭:
   - 커밋 메시지 키워드 매칭
   - 파일 경로 매칭
   - 이슈의 작업 범위와 비교
5. 관련 이슈 찾으면 PR 본문에 `Closes #이슈번호` 자동 추가
6. PR 생성

**예시**:

```bash
# 현재 브랜치: feature/send-message
# 커밋: "메시지 전송 API 구현"
# 변경 파일: MessageController.java, MessageService.java

→ AI가 자동으로 찾음:
   - #10 [TASK] 메시지 전송 API 엔드포인트 구현

→ PR 생성 with "Closes #10"
```

---

### 이슈 번호 찾기 가이드 (AI용)

**AI가 이슈를 찾는 방법**:

```bash
# 1. 모든 열린 Task 이슈 조회
gh issue list --state open --label task --json number,title,body

# 2. 커밋 메시지 확인
git log develop..HEAD --pretty=format:"%s"

# 3. 변경된 파일 확인
git diff --name-status develop..HEAD

# 4. 매칭 로직
# - 커밋 메시지에 "메시지 전송" → 이슈 제목에 "메시지 전송"
# - 파일에 "Controller" → API 관련 Task
# - 파일에 "Service" → 비즈니스 로직 Task
```

**매칭 우선순위**:

1. 커밋 메시지와 이슈 제목의 키워드 일치 (최우선)
2. 변경 파일 경로와 이슈 작업 범위 일치
3. 브랜치 이름에 이슈 번호 포함 (예: `feature/10-send-message`)

---

## PR 템플릿 구조

```markdown
## Summary
작업 내용 요약 (1~2문장)

## Changes
- 변경 사항 1
- 변경 사항 2

## Related Issues
Closes #이슈번호

## Test Plan
- [ ] 테스트 항목 1
- [ ] 테스트 항목 2
```

**중요**: `Closes #이슈번호`, `Fixes #이슈번호`, `Resolves #이슈번호` 중 하나를 사용하면 PR 머지 시 해당 이슈가 자동으로 닫힙니다.

---

## 프로젝트 현황 파악 (AI용)

### 현재 열린 이슈 확인

```bash
# Epic 확인
gh issue list --state open --label epic --json number,title

# Story 확인
gh issue list --state open --label story --json number,title

# Task 확인 (가장 중요!)
gh issue list --state open --label task --json number,title,body
```

### 특정 이슈 상세 조회

```bash
gh issue view 10 --json title,body,labels,state
```

### PR 상태 확인

```bash
# 열린 PR 확인
gh pr list --state open

# 특정 브랜치의 PR 확인
gh pr list --head feature/send-message
```

---

## 실전 시나리오

### 시나리오 1: 새로운 기능 개발 시작

**사용자**:

```
"메시지 전송 기능 이슈들을 만들어줘"
```

**AI 처리 순서**:

1. Epic 생성: `[EPIC] Chat Server 핵심 기능 구현`
2. Story 생성:
   - `[STORY] 메시지 전송 기능`
   - `[STORY] 메시지 조회 기능`
   - `[STORY] 읽음 처리 기능`
3. Task 생성 (각 Story마다):
   - `[TASK] Message 도메인 모델 구현`
   - `[TASK] MessageRepository 구현`
   - `[TASK] SendMessageUseCase 구현`
   - `[TASK] 메시지 전송 API 구현`
4. 생성된 이슈 번호와 링크 반환

---

### 시나리오 2: 작업 완료 후 PR 생성

**사용자 작업**:

```bash
git checkout -b feature/send-message-api
# 코딩...
git commit -m "메시지 전송 API 구현"
git push -u origin feature/send-message-api
```

**사용자 요청**:

```
"PR 만들어줘"
```

**AI 처리 순서**:

1. 커밋 메시지 분석: "메시지 전송 API 구현"
2. 열린 Task 이슈 조회
3. 매칭: `#10 [TASK] 메시지 전송 API 엔드포인트 구현` 발견
4. PR 생성:
   ```
   제목: [TASK] 메시지 전송 API 엔드포인트 구현
   본문:
   ## Summary
   메시지 전송 API 구현 완료

   ## Related Issues
   Closes #10
   ```
5. PR 링크 반환

---

## AI 어시스턴트를 위한 체크리스트

**PR 생성 시 AI가 확인해야 할 것**:

- [ ] 현재 브랜치 확인
- [ ] 커밋 메시지 분석
- [ ] 변경된 파일 확인
- [ ] 열린 Task 이슈 조회
- [ ] 키워드 매칭으로 관련 이슈 찾기
- [ ] PR 본문에 `Closes #N` 형식으로 추가
- [ ] 이슈와 관련된 정보를 PR 본문에 포함
- [ ] PR 생성 후 URL 반환

**이슈 생성 시 AI가 확인해야 할 것**:

- [ ] Epic → Story → Task 계층 구조 유지
- [ ] 각 이슈에 연결된 상위 이슈 번호 포함
- [ ] 적절한 라벨 할당
- [ ] 체크리스트 형식의 AC(수용 기준) 포함
- [ ] 생성된 이슈 번호 기록 및 반환

---

## Kafka 토픽

| 토픽명                | Producer    | Consumer     | 설명          |
|--------------------|-------------|--------------|-------------|
| `chat-message-sent` | Chat Server | NOTIFICATION | 새 메시지 알림    |
| `support-requested` | Chat Server | NOTIFICATION | 상담 요청 알림    |

---

## 문서 참조

| 문서                  | 설명           |
|---------------------|--------------|
| CHAT_SERVER_GUIDE.md | 서비스 요구사항/API |
| ISSUE_GUIDE.md       | 이슈 작성 가이드    |
| INFO.md              | 프로젝트 개요      |

---

**Last Updated**: 2025-12-27
