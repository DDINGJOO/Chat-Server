# 이슈 작성 가이드

## 이슈 종류 설명

### 1. Epic (대분류)

**언제 사용?** 큰 기능을 만들 때 (예: "채팅 기능 전체")

- 여러 개의 작은 작업(Story)으로 쪼갤 수 있는 큰 기능
- 보통 1~2주 이상 걸리는 작업

**작성 항목:**

- **목표**: 왜 이걸 만드는지, 완성되면 어떤 결과가 나오는지
- **범위**: 어디까지 만들 건지 / 무엇은 제외할 건지
- **디자인/문서 링크**: 피그마, 기획서 등의 링크
- **하위 스토리**: 이 Epic을 완성하기 위한 작은 Story들
- **마일스톤**: 언제까지 완성할 건지

---

### 2. Story (중분류)

**언제 사용?** 사용자 관점에서 하나의 완결된 기능 (예: "메시지 보내기")

- 사용자가 직접 사용하는 기능 하나
- 보통 2~5일 정도 걸리는 작업

**작성 항목:**

- **배경**: 왜 이 기능이 필요한지
- **수용 기준(AC)**: 이 기능이 완성됐다고 할 수 있는 조건들 (체크리스트)
- **디자인/계약 링크**: 화면 디자인, API 명세서 등
- **구현 메모/리스크**: 주의할 점이나 예상되는 문제
- **연결된 Epic**: 이 Story가 속한 Epic 번호

---

### 3. Task (소분류)

**언제 사용?** 개발자가 실제로 코딩하는 단위 작업 (예: "메시지 저장 API 개발")

- 실제로 코드를 작성하는 작은 작업
- 보통 반나절~1일 정도 걸리는 작업

**작성 항목:**

- **연결된 Story/Epic**: 어떤 Story의 일부인지
- **작업 범위**: 정확히 무엇을 만들 건지
- **Done 기준**: 완료 조건 (테스트, 문서, 코드리뷰 등)

---

### 4. Spike (조사)

**언제 사용?** 어떻게 만들지 모르겠어서 조사가 필요할 때

- 새로운 기술을 써야 하는데 잘 모를 때
- 여러 방법 중 어떤 게 좋은지 비교해야 할 때

**작성 항목:**

- **타임박스**: 조사에 쓸 시간 제한 (예: 1일, 4시간)
- **핵심 질문**: 무엇을 알아내고 싶은지
- **접근 방법**: 어떻게 조사할 건지
- **산출물**: 조사 결과를 어디에 정리할 건지

---

### 5. Change Request (변경 요청)

**언제 사용?** 이미 계획된 내용을 바꿔야 할 때

- 디자인이 바뀌었을 때
- 기획이 수정됐을 때
- 더 좋은 방법을 발견했을 때

**작성 항목:**

- **영향받는 Epic/Story/Task**: 어떤 이슈들이 영향을 받는지
- **제안 변경 사항**: 무엇을 어떻게 바꿀 건지
- **영향도**: 백엔드/프론트엔드/DB 등 어디에 영향이 있는지
- **결정/대안/근거**: 왜 이렇게 바꾸는 게 좋은지

---

## 실전 예시: 채팅 기능 구현

### 예시 시나리오

Chat Server의 핵심 기능인 메시지 전송 기능을 개발하려고 합니다.

---

### 1단계: Epic 생성

```
제목: [EPIC] Chat Server 핵심 기능 구현

목표:
사용자가 1:1 DM, 그룹 채팅, 고객 상담을 통해 실시간으로 소통할 수 있는
채팅 서비스를 제공한다.
성공 지표: 일 평균 메시지 1,000건 이상 처리

범위 / Not-in-scope:
포함: 메시지 전송, 조회, 읽음 처리, 채팅방 관리, Kafka 이벤트 발행
제외: 메시지 검색 (Phase 3), 파일 첨부 (추후 버전)

디자인/문서 링크:
docs/CHAT_SERVER_GUIDE.md

하위 스토리:
- [ ] #2 메시지 전송 기능
- [ ] #3 메시지 조회 기능
- [ ] #4 채팅방 관리 기능
- [ ] #5 읽음 처리 기능
- [ ] #6 Kafka 이벤트 연동

마일스톤:
2025-01-15
```

---

### 2단계: Story 생성 (Epic의 하위 작업)

```
제목: [STORY] 메시지 전송 기능 (채팅방 자동 생성 포함)

배경:
사용자가 다른 사용자에게 메시지를 보낼 때, 채팅방이 없으면 자동으로 생성되어야 한다.
이는 사용자 경험을 향상시키고, 채팅 시작의 진입 장벽을 낮춘다.

수용 기준(AC):
- [ ] DM: recipientId로 메시지 전송 시 기존 DM 채팅방이 있으면 재사용
- [ ] DM: 기존 채팅방이 없으면 새로 생성 후 메시지 저장
- [ ] GROUP: recipientIds로 새 그룹 채팅방 생성 후 메시지 저장
- [ ] 메시지 내용이 비어있으면 400 에러 반환
- [ ] 메시지 길이가 5,000자 초과 시 400 에러 반환
- [ ] 메시지 저장 후 Kafka로 chat-message-sent 이벤트 발행
- [ ] 응답에 isNewRoom 필드로 새 채팅방 여부 표시

디자인/계약 링크:
- API 명세: docs/CHAT_SERVER_GUIDE.md#4-api-명세

구현 메모/리스크:
- DM 채팅방 중복 방지를 위해 participantIds 정렬 후 유니크 인덱스 적용
- MongoDB 트랜잭션 고려 (채팅방 생성 + 메시지 저장 원자성)

연결된 Epic:
#1
```

---

### 3단계: Task 생성 (Story를 실제 작업으로 쪼개기)

#### Task 1: 도메인 모델

```
제목: [TASK] Message, ChatRoom 도메인 모델 구현

연결된 Story/Epic:
#2

작업 범위:
- Message Aggregate Root 구현 (id, roomId, senderId, content, readBy, deletedBy, createdAt)
- ChatRoom Aggregate Root 구현 (id, type, name, participantIds, ownerId, status, createdAt, lastMessageAt)
- ChatRoomType Enum (DM, GROUP, SUPPORT)
- ChatRoomStatus Enum (ACTIVE, CLOSED)
- 도메인 비즈니스 규칙 캡슐화 (메시지 길이 검증, 참여자 수 제한 등)

Done 기준:
- [ ] 도메인 모델 구현 완료
- [ ] 단위 테스트 작성 (Spock)
- [ ] 빌드 통과
- [ ] PR 리뷰/머지
```

#### Task 2: Repository Port/Adapter

```
제목: [TASK] ChatRoom, Message Repository 구현 (MongoDB)

연결된 Story/Epic:
#2

작업 범위:
- ChatRoomRepository Port (interface) 정의
- MessageRepository Port (interface) 정의
- ChatRoomDocument, MessageDocument 구현
- ChatRoomPersistenceAdapter 구현
- MessagePersistenceAdapter 구현
- MongoDB 인덱스 설정 (participantIds, roomId+createdAt)

Done 기준:
- [ ] Port/Adapter 구현 완료
- [ ] 통합 테스트 작성 (Embedded MongoDB)
- [ ] 빌드 통과
- [ ] PR 리뷰/머지
```

#### Task 3: UseCase 및 Service

```
제목: [TASK] SendMessageUseCase 구현

연결된 Story/Epic:
#2

작업 범위:
- SendMessageUseCase Port (interface) 정의
- SendMessageCommand DTO 정의
- MessageService 구현
  - DM: 기존 채팅방 조회 또는 생성
  - GROUP: 새 채팅방 생성
  - 메시지 저장
  - Kafka 이벤트 발행

Done 기준:
- [ ] UseCase/Service 구현 완료
- [ ] 단위 테스트 작성
- [ ] 빌드 통과
- [ ] PR 리뷰/머지
```

#### Task 4: Controller (REST API)

```
제목: [TASK] 메시지 전송 API 엔드포인트 구현

연결된 Story/Epic:
#2

작업 범위:
- POST /api/v1/chat/messages 엔드포인트 생성
- SendMessageRequest DTO (recipientId, recipientIds, roomType, content)
- SendMessageResponse DTO (messageId, roomId, senderId, content, createdAt, isNewRoom)
- 요청 유효성 검증 (@Valid)
- 에러 응답 처리

Done 기준:
- [ ] Controller 구현 완료
- [ ] API 통합 테스트 작성
- [ ] 빌드 통과
- [ ] PR 리뷰/머지
```

---

### 4단계: Spike 생성 (조사가 필요한 경우)

```
제목: [SPIKE] MongoDB 트랜잭션 vs 이벤트 소싱 비교

타임박스:
4시간

핵심 질문:
- 채팅방 생성 + 메시지 저장을 원자적으로 처리하는 최선의 방법은?
- MongoDB 트랜잭션의 성능 오버헤드는 어느 정도인가?
- 이벤트 소싱을 적용하면 어떤 장점/단점이 있는가?

접근 방법:
1. MongoDB 트랜잭션 POC 작성 및 성능 측정
2. 이벤트 소싱 패턴 조사
3. 각 방식의 장단점 비교

산출물:
- docs/adr/0001-message-atomicity-decision.md (ADR 문서)
```

---

### 5단계: Change Request 생성 (변경이 필요한 경우)

**상황:** 개발 중에 읽음 처리 방식을 변경해야 함

```
제목: [CR] 읽음 상태 저장 방식 변경 (Message 내장 -> Redis)

영향받는 Epic/Story/Task:
#1, #5, #10

제안 변경 사항:
읽음 상태를 Message Document의 readBy Map에 저장하는 대신
Redis에 저장하고 주기적으로 MongoDB에 동기화

- 기존: Message.readBy Map에 직접 저장
- 변경: Redis Hash에 저장 + 배치로 MongoDB 동기화

영향도:
- 백엔드: ReadReceiptAdapter 신규 구현, MessageService 수정
- 인프라: Redis 용량 산정 필요
- DB: Message Document 구조 변경 없음 (동기화 시 업데이트)

결정/대안/근거:
읽음 처리는 쓰기가 빈번하여 MongoDB에 직접 쓰면 부하가 큼.
Redis를 버퍼로 사용하면 쓰기 성능이 10배 이상 향상됨.
배치 동기화로 최종 일관성 보장.
```

---

## 작업 흐름 요약

```
Epic 생성 (큰 기능)
  ↓
Story 생성 (사용자 기능)
  ↓
Task 생성 (실제 개발 작업)
  ↓
개발 진행 (브랜치 생성 → 코딩 → PR)
  ↓
필요시 Spike (조사) 또는 Change Request (변경)
```

---

## 팁

1. **Epic은 크게, Task는 작게**
   - Epic: 1~2주 이상
   - Story: 2~5일
   - Task: 반나절~1일

2. **연결은 필수**
   - Task는 반드시 Story나 Epic과 연결
   - 고아 이슈를 만들지 마세요

3. **체크리스트 활용**
   - Story의 AC(수용 기준)은 체크리스트로
   - Task의 Done 기준도 체크리스트로
   - 완료 조건이 명확해집니다

4. **링크는 구체적으로**
   - "문서 참고"보다는 정확한 경로/URL
   - 나중에 찾기 쉽습니다

5. **변경은 CR로**
   - 기존 이슈를 함부로 수정하지 말고
   - Change Request로 변경 이력을 남기세요
