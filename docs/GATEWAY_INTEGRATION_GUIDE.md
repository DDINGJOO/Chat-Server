# Chat Server - API Gateway 연동 가이드

## 1. 개요

본 문서는 API Gateway에서 Chat Server와 연동할 때 필요한 정보를 제공합니다.

### 1.1 기본 정보

| 항목 | 값 |
|-----|-----|
| Base URL | `http://chat-server:8080` (내부 서비스) |
| API Version | `v1` |
| Content-Type | `application/json` |

### 1.2 인증 처리

Chat Server는 인증을 직접 수행하지 않습니다. **Gateway에서 JWT 검증 후 사용자 정보를 헤더로 전달**해야 합니다.

---

## 2. 필수 헤더

모든 API 요청 시 Gateway가 설정해야 하는 헤더입니다.

| 헤더 | 필수 | 설명 |
|-----|-----|------|
| `X-User-Id` | Y | 인증된 사용자의 userId (Long) |
| `X-Agent-Id` | 조건부 | 상담원 배정 API에서만 사용 |
| `Content-Type` | Y | `application/json` |

---

## 3. 응답 형식

### 3.1 성공 응답

```json
{
  "success": true,
  "data": { ... }
}
```

### 3.2 에러 응답

```json
{
  "success": false,
  "error": {
    "code": "CHAT_001",
    "message": "채팅방을 찾을 수 없습니다"
  }
}
```

---

## 4. API 엔드포인트

### 4.1 채팅방 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/rooms` | 채팅방 목록 조회 |
| GET | `/api/v1/rooms/{roomId}` | 채팅방 상세 조회 |

### 4.2 메시지 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/rooms/{roomId}/messages` | 메시지 전송 |
| GET | `/api/v1/rooms/{roomId}/messages` | 메시지 목록 조회 |
| POST | `/api/v1/rooms/{roomId}/messages/read` | 읽음 처리 |
| DELETE | `/api/v1/rooms/{roomId}/messages/{messageId}` | 메시지 삭제 |

### 4.3 공간 문의 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/chat/inquiry` | 공간 문의 생성 |
| GET | `/api/v1/chat/inquiry/host` | 호스트 문의 목록 조회 |

### 4.4 고객 상담 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/chat/support` | 상담 요청 생성 |
| GET | `/api/v1/chat/support/queue` | 상담 대기열 조회 |
| POST | `/api/v1/chat/support/{roomId}/assign` | 상담원 배정 |
| POST | `/api/v1/chat/support/{roomId}/close` | 상담 종료 |

---

## 5. API 상세

### 5.1 채팅방 목록 조회

```
GET /api/v1/rooms
```

**Request Headers**
```
X-User-Id: 123
```

**Response**
```json
{
  "success": true,
  "data": {
    "chatRooms": [
      {
        "roomId": "507f1f77bcf86cd799439011",
        "type": "DM",
        "name": null,
        "participantIds": [123, 456],
        "lastMessage": "안녕하세요",
        "lastMessageAt": "2024-01-15T10:30:00",
        "unreadCount": 3
      }
    ]
  }
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|-----|------|------|
| roomId | String | 채팅방 ID |
| type | String | `DM`, `GROUP`, `PLACE_INQUIRY`, `SUPPORT` |
| name | String | 채팅방 이름 (GROUP인 경우) |
| participantIds | List<Long> | 참여자 userId 목록 |
| lastMessage | String | 마지막 메시지 내용 |
| lastMessageAt | DateTime | 마지막 메시지 시간 |
| unreadCount | Long | 안읽은 메시지 수 |

---

### 5.2 메시지 목록 조회

```
GET /api/v1/rooms/{roomId}/messages?cursor={cursor}&limit={limit}
```

**Request Headers**
```
X-User-Id: 123
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|-----|-------|------|
| cursor | String | N | null | 페이징 커서 (마지막 messageId) |
| limit | Integer | N | 50 | 조회 개수 |

**Response**
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "messageId": "507f1f77bcf86cd799439012",
        "roomId": "507f1f77bcf86cd799439011",
        "senderId": 123,
        "content": "안녕하세요",
        "readCount": 1,
        "deleted": false,
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "nextCursor": "507f1f77bcf86cd799439010",
    "hasMore": true
  }
}
```

**Response Fields**

| 필드 | 타입 | 설명 |
|-----|------|------|
| messageId | String | 메시지 ID |
| roomId | String | 채팅방 ID |
| senderId | Long | 발신자 userId |
| content | String | 메시지 내용 (삭제된 경우 마스킹됨) |
| readCount | Integer | 읽은 사용자 수 |
| deleted | Boolean | 삭제 여부 |
| createdAt | DateTime | 생성 시간 |

---

### 5.3 메시지 전송

```
POST /api/v1/rooms/{roomId}/messages
```

**Request Headers**
```
X-User-Id: 123
Content-Type: application/json
```

**Request Body**
```json
{
  "content": "안녕하세요"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|-----|------|
| content | String | Y | 메시지 내용 (최대 5,000자) |

**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "messageId": "507f1f77bcf86cd799439012",
    "roomId": "507f1f77bcf86cd799439011",
    "senderId": 123,
    "content": "안녕하세요",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

### 5.4 읽음 처리

```
POST /api/v1/rooms/{roomId}/messages/read
```

**Request Headers**
```
X-User-Id: 123
```

**Request Body** (선택)
```json
{
  "lastMessageId": "507f1f77bcf86cd799439012"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "roomId": "507f1f77bcf86cd799439011",
    "lastReadAt": "2024-01-15T10:35:00",
    "unreadCount": 0
  }
}
```

---

### 5.5 메시지 삭제

```
DELETE /api/v1/rooms/{roomId}/messages/{messageId}
```

**Request Headers**
```
X-User-Id: 123
```

**Response**
```json
{
  "success": true,
  "data": {
    "messageId": "507f1f77bcf86cd799439012",
    "hardDeleted": false,
    "deletedAt": "2024-01-15T10:35:00"
  }
}
```

| 필드 | 타입 | 설명 |
|-----|------|------|
| hardDeleted | Boolean | `true`: 모든 참여자에게 삭제, `false`: 요청자에게만 삭제 |

---

### 5.6 공간 문의 생성

```
POST /api/v1/chat/inquiry
```

**Request Headers**
```
X-User-Id: 123
Content-Type: application/json
```

**Request Body**
```json
{
  "placeId": 12345,
  "placeName": "강남 스터디룸 A",
  "hostId": 200,
  "initialMessage": "예약 문의드립니다"
}
```

| 필드 | 타입 | 필수 | 설명 |
|-----|------|-----|------|
| placeId | Long | Y | 공간 ID |
| placeName | String | Y | 공간 이름 |
| hostId | Long | Y | 호스트 userId |
| initialMessage | String | N | 초기 메시지 (최대 5,000자) |

**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "roomId": "507f1f77bcf86cd799439011",
    "type": "PLACE_INQUIRY",
    "context": {
      "contextType": "PLACE",
      "contextId": 12345,
      "contextName": "강남 스터디룸 A"
    },
    "createdAt": "2024-01-15T10:00:00"
  }
}
```

---

### 5.7 상담원 배정 (관리자)

```
POST /api/v1/chat/support/{roomId}/assign
```

**Request Headers**
```
X-Agent-Id: 999
```

**Response**
```json
{
  "success": true,
  "data": {
    "roomId": "507f1f77bcf86cd799439011",
    "agentId": 999,
    "status": "IN_PROGRESS"
  }
}
```

---

## 6. 프로필 정보 병합 (Gateway 역할)

Chat Server는 `userId`만 반환합니다. **Gateway에서 Profile Server를 조회하여 닉네임/프로필 이미지를 병합**해야 합니다.

### 6.1 병합 흐름

```
1. Client → Gateway: GET /api/v1/rooms/{roomId}/messages
2. Gateway → Chat Server: 동일 요청 (X-User-Id 헤더 추가)
3. Chat Server → Gateway: messages (senderId만 포함)
4. Gateway → Profile Server: POST /api/v1/profiles/batch (senderIds)
5. Profile Server → Gateway: profiles (userId → nickname, profileImage)
6. Gateway: messages + profiles 병합
7. Gateway → Client: 최종 응답
```

### 6.2 병합 후 응답 예시

```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "messageId": "507f1f77bcf86cd799439012",
        "sender": {
          "userId": 123,
          "nickname": "홍길동",
          "profileImage": "https://cdn.example.com/profile/123.jpg"
        },
        "content": "안녕하세요",
        "readCount": 1,
        "deleted": false,
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "nextCursor": "507f1f77bcf86cd799439010",
    "hasMore": true
  }
}
```

### 6.3 병합 대상 API

| API | 병합 필드 |
|-----|----------|
| `GET /api/v1/rooms` | `participantIds` → 참여자 프로필 |
| `GET /api/v1/rooms/{roomId}/messages` | `senderId` → 발신자 프로필 |
| `GET /api/v1/chat/inquiry/host` | `guestId` → 게스트 프로필 |

---

## 7. 에러 코드

### 7.1 Chat Server 에러 코드

| 코드 | HTTP Status | 설명 |
|-----|-------------|------|
| CHAT_001 | 404 | 채팅방을 찾을 수 없음 |
| CHAT_002 | 403 | 채팅방 접근 권한 없음 (참여자 아님) |
| CHAT_003 | 400 | 잘못된 채팅방 유형 |
| CHAT_004 | 400 | 메시지 내용이 비어있음 |
| CHAT_005 | 400 | 메시지 길이 초과 (5,000자) |
| CHAT_006 | 404 | 메시지를 찾을 수 없음 |
| CHAT_007 | 403 | 메시지 삭제 권한 없음 |
| CHAT_008 | 400 | 수신자가 지정되지 않음 |
| CHAT_009 | 400 | 그룹 채팅 최대 인원 초과 |
| CHAT_010 | 409 | 이미 진행 중인 상담 있음 |
| CHAT_011 | 409 | 동일 공간에 문의 채팅방 존재 |
| CHAT_012 | 409 | 이미 종료된 채팅방 |
| CHAT_013 | 400 | 상담 채팅방이 아님 |
| CHAT_014 | 409 | 이미 상담원이 배정됨 |
| CHAT_500 | 500 | 서버 내부 오류 |

### 7.2 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|-----|-------------|------|
| VALIDATION_ERROR | 400 | 요청 유효성 검증 실패 |
| MISSING_HEADER | 400 | 필수 헤더 누락 (X-User-Id 등) |
| BAD_REQUEST | 400 | 잘못된 요청 |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |

---

## 8. 채팅방 유형별 특성

| 유형 | 설명 | 최대 참여자 | 중복 생성 |
|-----|------|-----------|----------|
| DM | 1:1 개인 대화 | 2명 | 불가 (동일 참여자) |
| GROUP | 그룹 채팅 | 100명 | 허용 |
| PLACE_INQUIRY | 공간 문의 (게스트→호스트) | 2명 | 불가 (동일 게스트-공간) |
| SUPPORT | 고객 상담 | 2명 | 허용 |

---

## 9. 주의 사항

1. **X-User-Id 헤더는 필수**: Gateway에서 JWT 검증 후 반드시 설정
2. **프로필 병합은 Gateway 책임**: Chat Server는 userId만 반환
3. **409 Conflict 처리**: 중복 생성 시 기존 채팅방 정보 활용 권장
4. **삭제된 메시지 처리**: `deleted: true`인 메시지는 content가 마스킹됨
5. **페이징**: 커서 기반 페이징 사용, `hasMore: false`까지 조회

---

## 10. 연락처

Chat Server 관련 문의는 백엔드 팀에 연락해 주세요.