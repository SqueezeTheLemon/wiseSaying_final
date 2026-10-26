# 명언 앱 (wiseSaying)

콘솔에서 명언을 등록·조회·수정·삭제·검색하는 Java 앱입니다.
저장소를 **JSON 파일**과 **MySQL** 중에서 고를 수 있습니다. 기본값은 MySQL입니다.

- Java 25, Gradle
- MySQL 8.x (`mysql-connector-j` 9.5.0)
- JUnit 5

---

## 1. MySQL 띄우고 DB·테이블 만들기

### 1-1. MySQL 서버 실행

Homebrew(macOS)를 쓰는 경우:

```bash
brew install mysql
```

```bash
brew services start mysql
```

Docker를 쓰는 경우:

```bash
docker run -d --name wise-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=<비밀번호> mysql:8.4
```

앱은 `jdbc:mysql://<DB_HOST>:3306/<DB_NAME>` 으로 접속하므로 포트는 **3306** 이어야 합니다.

### 1-2. 데이터베이스 만들기

앱용 DB와 테스트용 DB를 따로 만듭니다. 테스트는 매번 `TRUNCATE` 로 테이블을 비우므로 반드시 분리해야 합니다.

```bash
mysql -h 127.0.0.1 -u root -p
```

```sql
CREATE DATABASE wise_saying      DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE wise_saying_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

한글 명언을 저장하므로 문자셋은 `utf8mb4` 를 사용합니다.

### 1-3. 테이블 만들기

스키마는 [`sql/schema.sql`](sql/schema.sql)에 있습니다.

```sql
CREATE TABLE IF NOT EXISTS wise_saying(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    author VARCHAR(255) NOT NULL
)
```

두 DB에 모두 적용합니다.

```bash
mysql -h 127.0.0.1 -u root -p wise_saying < sql/schema.sql
```

```bash
mysql -h 127.0.0.1 -u root -p wise_saying_test < sql/schema.sql
```

### 1-4. 접속 정보(환경변수) 설정

접속 정보는 코드에 적지 않고 환경변수로 넘깁니다.

| 환경변수 | 예시 | 설명 |
| --- | --- | --- |
| `DB_HOST` | `127.0.0.1` | MySQL 호스트 |
| `DB_USERNAME` | `root` | 계정 |
| `DB_PASSWORD` | (본인 비밀번호) | 비밀번호 |
| `DB_NAME` | `wise_saying` / `wise_saying_test` | 접속할 DB |

IntelliJ에서는 **Run/Debug Configurations → Environment variables** 에 넣습니다.
`Main` 실행 설정과 **JUnit 실행 설정(또는 JUnit 템플릿)** 양쪽에 모두 넣어야 합니다.
JUnit 쪽에서는 `DB_NAME=wise_saying_test` 를 사용합니다.

터미널에서 테스트할 때:

```bash
DB_HOST=127.0.0.1 DB_USERNAME=root DB_PASSWORD=<비밀번호> DB_NAME=wise_saying_test ./gradlew test
```

---

## 2. 리포지터리를 어떻게 갈아 끼웠나

### 2-1. 구조

```
App ──(구현체 선택)──▶ WiseSayingController ──▶ WiseSayingService ──▶ WiseSayingRepository (interface)
                                                                        ├── WiseSayingFileRepository  (JSON 파일)
                                                                        └── WiseSayingDbRepository    (MySQL)
```

처음에는 `WiseSayingRepository` 가 파일을 직접 읽고 쓰는 **클래스**였습니다. 이것을 바꾼 과정은 다음과 같습니다.

1. **인터페이스로 분리**: `WiseSayingRepository` 에는 메서드 선언만 남겼습니다.
   (`saveNew`, `findAll`, `findById`, `findByKeyword`, `divideToPages`, `editById`, `deleteById`, `build`)
2. **파일 구현체**: 기존 파일 저장 코드를 `WiseSayingFileRepository` 로 그대로 옮겼습니다.
   명언 하나를 `{id}.json` 하나로 저장하고, 마지막 번호는 `lastId.txt` 에 기록합니다.
3. **MySQL 구현체**: `WiseSayingDbRepository` 를 새로 만들었습니다.
   `SimpleDb` / `Sql`([`global/db`](src/main/java/com/back/global/db))로 SQL을 실행합니다.
   - 번호는 MySQL `AUTO_INCREMENT` 가 발급하고, `insert()` 가 생성된 키를 돌려줍니다.
   - 목록은 `ORDER BY id DESC`, 페이징은 `LIMIT ? OFFSET ?` 로 DB에서 처리합니다.
   - 검색 컬럼은 `content` / `author` 만 허용합니다. 컬럼명은 SQL 문자열에 직접 붙기 때문에 SQL 인젝션을 막으려는 것입니다.
   - `build` 는 DB의 모든 명언을 Jackson으로 `db/wiseSaying/data.json` 에 내보냅니다.
4. **의존성 주입**: `WiseSayingService` 는 구현체를 직접 `new` 하지 않습니다.
   생성자로 `WiseSayingRepository` 를 받습니다. Controller → Service 순서로 전달됩니다.
   그래서 Service·Controller 코드는 저장소가 바뀌어도 수정할 필요가 없습니다.

### 2-2. 구현체 선택

어떤 구현체를 쓸지는 [`App`](src/main/java/com/back/App.java) 한 곳에서만 정합니다.

```java
String repositoryType = System.getProperty("wiseSaying.repository", "db");

WiseSayingRepository repository;

if ("file".equals(repositoryType)) {
    repository = new WiseSayingFileRepository();
} else {
    SimpleDb simpleDb = new SimpleDb(
            System.getenv("DB_HOST"),
            System.getenv("DB_USERNAME"),
            System.getenv("DB_PASSWORD"),
            System.getenv("DB_NAME")
    );
    repository = new WiseSayingDbRepository(simpleDb);
}
```

| 시스템 프로퍼티 | 사용 저장소 | 데이터 위치 |
| --- | --- | --- |
| 없음 (기본값) / `-DwiseSaying.repository=db` | `WiseSayingDbRepository` | 환경변수 `DB_NAME` 의 `wise_saying` 테이블 |
| `-DwiseSaying.repository=file` | `WiseSayingFileRepository` | `wiseSaying.dbPath` (기본 `db/wiseSaying`) |

### 2-3. 테스트도 저장소별로 분리

| 테스트 | 저장소 | 초기화 |
| --- | --- | --- |
| `WiseSayingControllerTest` | 파일 (`wiseSaying.repository=file`, `wiseSaying.dbPath=db/wiseSayingTest`) | 매 테스트 전에 `db/wiseSayingTest` 폴더 전체 삭제 (`lastId.txt` 포함) |
| `WiseSayingDbRepositoryTest` | MySQL `wise_saying_test` | 매 테스트 전에 `TRUNCATE TABLE wise_saying` (ID가 1부터 다시 시작) |

Controller 테스트는 콘솔 입출력과 JSON 파일을 검증하므로 MySQL 없이도 돌아갑니다.
MySQL 동작(등록 ID, 수정, 삭제, 최신순 정렬)은 `WiseSayingDbRepositoryTest` 에서 검증합니다.

> **전환하면서 겪은 문제**
> Controller 테스트가 파일 저장소로 실행되는데 `setUp` 에서는 MySQL만 `TRUNCATE` 했습니다.
> 그래서 남아 있던 `db/wiseSayingTest/lastId.txt`(=1) 때문에 등록 번호가 `2번` 으로 나왔습니다.
> 테스트가 **실제로 어떤 저장소를 쓰는지**, 그 저장소를 초기화하는지를 반드시 맞춰야 합니다.

---

## 실행

IntelliJ에서 `com.back.Main` 을 실행합니다. 실행 설정에 1-4의 환경변수가 있어야 합니다.
파일 저장소로 실행하려면 VM options에 `-DwiseSaying.repository=file` 을 추가합니다.

명령어:

| 명령 | 설명 |
| --- | --- |
| `enroll` | 명언 등록 |
| `list?page=1` | 목록 (5개씩, 최신순) |
| `search?keywordType=content&keyWord=사랑` | 검색 (`content` / `author`) |
| `edit?id=1` | 수정 |
| `delete?id=1` | 삭제 |
| `build` | `data.json` 으로 내보내기 |
| `exit` | 종료 |
