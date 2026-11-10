# Velog 게시 및 GitHub 연결 가이드

이 문서는 수동 게시 절차입니다. Velog 프로필·블로그 제목·소개글·GitHub 소셜 링크와 GitHub 프로필의 소개·Velog 링크를 설정했습니다. 사용자 확인을 받아 공개 저장소 [jaeho99/backend-engineering-log](https://github.com/jaeho99/backend-engineering-log)를 생성했습니다. 자동 동기화 기능을 구현하거나 존재한다고 가정하지 않습니다.

## 1. 프로필과 분류 준비

- 블로그 이름: 재호의 백엔드 개발 기록
- 영문 브랜드: Backend Engineering Log
- 소개: Java/Spring 백엔드 개발자 | 문제 해결과 성장의 기록
- 슬로건: 실무에서 마주한 문제를 분석하고, 해결 과정을 기록합니다.
- Velog 주소: https://velog.io/@dev_resolution

Velog에서 사용할 대표 시리즈와 태그는 다음과 같습니다. 실제 메뉴·지원 기능은 게시 시 UI에서 확인합니다.

| 시리즈 | 기본 태그 |
| --- | --- |
| 01. 실무 트러블슈팅 | Troubleshooting, Ajax, Spring MVC |
| 02. Java & Spring | Java, Spring, Spring Boot |
| 03. Database & MyBatis | Oracle, SQL, MyBatis |
| 04. JSP & Web | JSP, jQuery, HTTP |
| 05. Architecture & DevOps | Git, Jenkins, Architecture |
| 06. 개발 성장 기록 | 회고, AI, Learning |

글마다 대표 시리즈는 하나를 선택하고 태그는 실제 내용과 관련된 것만 사용합니다. GitHub 폴더 번호와 Velog 시리즈 번호는 서로 다르므로 이름으로 대응합니다. 색상·썸네일 기준은 [assets](../assets/README.md)를 따릅니다.

## 2. 로컬 원문과 코드 준비

1. [템플릿](../posts/templates/post-template.md)을 복사해 글을 작성합니다.
2. 제목에 기술명과 구체적인 문제를 넣고 첫 문단에 현상·해결 방향을 씁니다.
3. 개발 환경의 정확한 버전을 기재하고 코드 블록 언어를 지정합니다.
4. 독립 예제를 실행해 예상 결과와 실제 결과를 구분해 기록합니다.
5. 게시글·예제 README·카테고리·최신 목록을 상호 연결합니다.

회사 소스, 고객사명, 내부 주소, 계정·인증 정보와 실제 데이터를 포함하지 않습니다. 예제는 독립적으로 재작성합니다. 로그·Network 캡처의 쿠키·토큰도 제거합니다. 외부 코드·문서·이미지 인용은 라이선스와 출처를 확인하며 공식 문서를 우선합니다. AI 생성 코드도 실행 후 게시하고 실제 하지 않은 테스트를 완료로 쓰지 않습니다.

## 3. GitHub 원격 연결 — 사용자 확인 이후 수행

이 단계 전에 원격 저장소 생성과 최초 push에 대한 사용자 확인을 받습니다. 현재 상위 작업 폴더에는 이미 `.git`이 있으므로 아래 예시를 그대로 실행해 중첩 저장소를 만들지 않습니다. 상위 저장소를 쓸지, 이 프로젝트를 별도 위치의 독립 저장소로 옮길지 먼저 정합니다.

확인할 설정:

- 저장소 이름: `backend-engineering-log`
- 설명: `Java & Spring backend engineering notes, troubleshooting cases, and reproducible examples.`
- 공개 범위 및 라이선스: 사용자 선택
- 소유자, 원격 URL, 포함할 파일과 커밋 내용 확인

독립 저장소 위치가 확정되고 사용자 확인을 받은 뒤 사용할 명령 예시입니다. URL은 실제 생성된 값으로 바꾸며 기존 remote를 임의로 덮어쓰지 않습니다.

```powershell
git init
git add .
git diff --cached --stat
git diff --cached
git commit -m "docs: initialize backend engineering log"
git branch -M main
# 아래 명령의 OWNER를 실제 소유자로 교체
git remote add origin https://github.com/OWNER/backend-engineering-log.git
git push -u origin main
```

## 4. Velog에 게시

1. 로컬 Markdown 원문을 Velog 편집기에 옮깁니다.
2. GitHub 상대 링크를 실제 공개 저장소의 `blob/main/…` 또는 `tree/main/…` 절대 URL로 바꿉니다. 아직 주소가 없으면 TODO를 유지하고 게시를 완료로 표시하지 않습니다.
3. 이미지도 접근 가능한 실제 URL로 연결하고 편집기 미리보기에서 확인합니다.
4. 제목, 도입부, H2/H3, 코드 문법, 표, 링크와 태그를 확인합니다.
5. 대표 시리즈에 포함하고 게시한 뒤 실제 공개 페이지에서 레이아웃과 링크를 다시 확인합니다.

첫 글은 실행 검증 전 초안이므로 검증 결과를 보완한 후 게시합니다. 검색 유입을 위해 오류 메시지와 기술 키워드를 구체적으로 쓰되 같은 키워드를 의미 없이 반복하지 않습니다.

## 5. 양방향 연결과 유지보수

게시 후 실제 Velog URL을 루트 README와 원문 메타 정보에 추가합니다. Velog에는 예제 디렉터리와 GitHub 원문 링크를 넣습니다. GitHub 프로필·저장소 소개의 웹사이트 항목은 실제 Velog 주소로 설정할 수 있습니다. 링크를 추가하는 것이 자동 동기화를 의미하지 않습니다.

원문과 코드는 GitHub를 기준으로 수정하고 Velog에도 수동 반영합니다. 오탈자 외 기술 변경은 수정일·변경 이유·재검증 결과를 함께 남깁니다. 공개 주소가 변경되면 양쪽 링크와 최신 게시글 목록을 갱신합니다.
