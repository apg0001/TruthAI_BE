## TruthAI Server 개요

다양한 LLM(GPT/Claude/Gemini/Perplexity)의 응답을 수집·저장하고, 모델 간 내용 일치도(유사도)를 기반으로 환각(hallucination) 수준을 산정하여 교차검증 결과를 제공합니다. 또한 프롬프트 최적화, 폴더 관리, 출처(레퍼런스) 추출, 인증/JWT 재발급 기능을 포함합니다.

### 구성 요약
- **LLM 응답 수집**: 선택한 모델들로부터 질문에 대한 답변을 수집 및 저장
- **프롬프트 최적화**: 템플릿 기반으로 프롬프트를 개선하고 결과 저장
- **교차검증(환각 분석)**: 모델별 응답 문장을 임베딩 후, 상호 유사도를 바탕으로 점수와 환각 레벨 계산
- **출처 처리**: 답변에서 수집한 URL의 본문 추출 및 요약을 저장해 참고자료 제공
- **폴더/사이드바**: 최근/관심 프롬프트, 교차검증 결과를 사이드바 형태로 조회
- **인증/토큰**: 구글 OAuth 로그인 및 JWT 재발급

---

## 컨트롤러별 기능과 내부 동작

### `AnswerController` (`/llm-answer`)
- **POST `/models`**: 유저가 직접 입력한 질문으로 선택한 모델들(GPT/Claude/Gemini/Perplexity)에 질의
  - 내부 동작: 
    - `PromptService.summarizePrompts(question)`로 질문 요약을 생성해 저장용 제목으로 활용
    - `AnswerService.selectAnswer(models, question)`가 모델별 서비스로 분기 호출하여 응답을 수집
    - 질문/응답을 프롬프트 엔티티와 함께 저장 (`PromptService.savePromptAnswer`) 후, `AnswerResultDto` 반환

### `PromptController` (`/prompt`)
- **GET `/side-bar/list`**: 프롬프트 사이드바 목록 조회
- **GET `/side-bar/details`**: 특정 프롬프트 상세 조회
- **POST `/create-best-prompt`**: 편집 가능한 템플릿(`editable`)로 최적화 프롬프트 생성 후, GPT로 실행하여 결과 저장
- **POST `/get-best/organized`**: 최적화 프롬프트로 LLM 실행 → 응답 저장 → 정돈된 소스(레퍼런스) 포함 결과 반환
- **GET `/optimized-prompt-list`**: 최적화 프롬프트 실행 결과 리스트
- **GET `/crosscheck-list`**: 교차검증(환각) 결과 리스트
- **GET `/{promptId}`**: 최적화 전/후 프롬프트 상세 결과 조회

내부 핵심:
- `PromptService.getOptimizedPrompt(dto)`로 템플릿 기반 메시지를 만들고, `GptService.createGptAnswerWithPrompt`로 실행
- 실행 결과와 요약을 `PromptService.saveOptimizedPrompt` 등에 저장
- 모델별 실행은 `PromptService.runByModel(dto)`가 LLM별 어댑터/서비스를 통해 수행

### `CrossCheckController` (`/crosscheck`)
- **POST `/{promptId}`**: 해당 프롬프트의 모델 답변 간 비교(교차검증) 실행
- **GET `/{promptId}`**: 프롬프트 ID에 대한 환각 검증 결과 조회
- **GET `/side-bar/list`**: 교차검증 사이드바 리스트 조회

내부 핵심은 `CrossCheckService.crossCheckPrompt(promptId)`와 `getCrossChecklist(promptId)`에서 처리합니다.

### `FolderController` (`/folder`)
- **POST**: 폴더 생성
- **GET `/folderList/{folderType}`**: 폴더 목록 조회(타입별)
- **PUT `/{folderID}/prompts/{promptId}`**: 프롬프트를 폴더로 이동/저장
- **PATCH `/{folderId}`**: 폴더 이름 변경

### `SourceController` (`/source`)
- **POST `/test`**: URL 유효성 검증 후 본문을 추출하여 반환(간이 테스트용)
  - 내부에서 `SourceService.extractMainContent(url)`로 본문 추출

### `UserController`
- **POST `/auth/login`**: 구글 OAuth 인가 코드 + redirectUri로 사용자 인증, JWT 발급
- **POST `/auth/logout`**: 세션 무효화(옵션)
- **POST `/persona`**, **GET `/persona`**: 사용자 기본 페르소나 설정/조회

### `TokenController` (`/auth/token`)
- **POST `/refresh`**: 리프레시 토큰으로 액세스/리프레시 토큰 재발급

---

## 서비스 레이어 핵심 로직

### LLM 호출 계층
- `AnswerService.selectAnswer(models, question)`: 입력 모델 리스트를 순회하며 각 모델 서비스로 분기
  - GPT: `GptService.createGptAnswer`
  - Claude: `ClaudeService.createClaudeAnswer`
  - Gemini: `LlmService.createGeminiAnswer`
  - Perplexity: `LlmService.createPerplexityAnswer`
- `PromptService.runByModel(dto)`: 최적화 프롬프트 기반으로 모델 실행, 응답과 레퍼런스 정리 후 저장

### Embedding 기반 유사도 계산
- 구현 위치: `EmbeddingService`
  - 초기화 시 벡터 파일(`embedding.vec-path`, 예: `classpath:models/ko-emb.vec`)을 로드하고 토큰→벡터 맵 구성
  - 문장 임베딩: 한국어 토크나이저(OpenKoreanText)로 토큰화 → 각 토큰 벡터의 평균으로 문장 벡터 생성
  - 코사인 유사도: `cosine(float[], float[])`로 두 벡터 간 유사도 산출
  - 캐싱: 문장→벡터 캐시로 반복 계산 비용 절감

### 교차검증과 점수 산정(`CrossCheckService`)
1) 모델별 답변을 문장 단위로 분할하고 전 모델의 문장 합집합을 구성
2) 피벗 문장(pivot)에 대해 각 모델 답변이 해당 내용을 “포함”하는지 판단
   - 포함 기준: `cosine(embed(pivot), embed(문장)) >= SENTENCE_SIM_THRESHOLD`
   - 포함 시 동의 모델 수(agree count)를 집계하고 코어 주장(core statement)을 선택(최다 동의)
3) 모델별 유사도 점수 계산 `calculateScore(targetModel, modelToSentences)`
   - 타겟 모델의 각 문장을 임베딩하여 “다른 모든 모델의 모든 문장 임베딩”과 코사인 유사도 계산
   - 해당 문장의 점수 = 상위 K개의 유사도 평균(Top-K 평균)
   - 모델 점수 = 타겟 모델 내 모든 문장 점수의 평균
4) 출처 품질 계산 `calculateSourceQuality(model, answers)`
   - URL 유효성, 출처 다양성 등으로 품질 점수 산출
5) 환각 레벨 계산 `calculateHallucinationLevel(score, sourceQuality)`
   - 기본 레벨: 점수 임계치에 따른 3단계(낮음/중간/높음)
   - 보정: 출처 품질이 높으면 1단계 낮추고, 낮으면 1단계 높임(경계값 내 클램핑)
6) 계산된 점수와 레벨을 `Answer` 엔티티에 반영 후 저장
7) 응답 DTO(`CrossCheckModelDto`)에는 각 모델의 환각 레벨, 유사도 백분율(0~100), 레퍼런스 목록을 포함

### 출처 처리(`SourceService`, `SourcesService`)
- URL 본문 추출, 제목/요약 생성 및 `Answer` 연계 저장
- 교차검증 응답에 레퍼런스(`CrossCheckReferenceDto`)로 제공

### 폴더/사이드바(`FolderService`, 일부 `PromptService`/`CrossCheckService`)
- 사용자별 폴더 생성/이동/이름 변경
- 사이드바 리스트: 최근 프롬프트, 교차검증 결과를 요약 형태로 제공

### 인증/토큰(`AuthService`, `LoginService`, `TokenRefreshService`)
- 구글 OAuth 인가 코드 + redirectUri로 사용자 정보 조회 및 회원 처리, JWT 발급
- 리프레시 토큰 검증 후 신규 토큰 재발급

---

## 유사도 계산 상세
- 전처리: 특수문자 제거, 소문자화, 한국어 조사/어미 제거, 불용어(stopwords) 제거 등(`preprocessForEmbedding`)
- 토크나이즈: OpenKoreanText 기반 토큰화
- 문장 임베딩: 토큰 벡터 평균
- 유사도: 코사인 유사도
- 포함 판단 임계치: `SENTENCE_SIM_THRESHOLD`(코드 상 상수)
- Top-K: 모델 점수 계산 시 상위 K개 유사도의 평균 사용(`TOP_K` 상수)

---

## 환각 레벨 산정 로직
- 입력: 모델 유사도 점수 `score`(0~1), 출처 품질 `sourceQuality`
- 기본 레벨: 
  - `score >= 0.7` → 0(낮음)
  - `0.4 <= score < 0.7` → 1(중간)
  - `score < 0.4` → 2(높음)
- 보정:
  - `sourceQuality >= 0.5` → 레벨 1단계 낮춤(최소 0)
  - `sourceQuality <= 0.3` → 레벨 1단계 높임(최대 2)

---

## 데이터 저장 흐름(요약)
1) 질문 제출 → 프롬프트 요약 생성 → 프롬프트/질문 저장
2) 모델별 답변 수집 및 저장, 출처 수집·요약 저장
3) 교차검증 실행 시 문장 분할 → 임베딩 유사도 계산 → 모델 점수/환각 레벨 반영
4) 조회 API에서 모델별 유사도(%)와 환각 레벨, 레퍼런스 반환

---

## 설정
- 임베딩 벡터 경로: `embedding.vec-path` (예: `classpath:models/ko-emb.vec`)
- 임베딩 차원 강제 지정(옵션): `embedding.dim`
- LLM/외부 API 키 및 엔드포인트: `application-llm.yml` 등 프로필별 설정 파일 참고
