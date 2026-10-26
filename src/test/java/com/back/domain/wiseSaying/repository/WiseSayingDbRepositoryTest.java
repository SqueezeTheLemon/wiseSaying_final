package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.global.db.SimpleDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public
class WiseSayingDbRepositoryTest {

    private SimpleDb simpleDb;
    private WiseSayingDbRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        String host = System.getenv("DB_HOST");
        String username = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        String testDbName = "wise_saying_test";

        // 테스트 DB만 사용하도록 제한
        if (!testDbName.equals("wise_saying_test")) {
            throw new IllegalStateException("테스트 DB가 아닙니다.");
        }

        System.out.println("DB_HOST = [" + System.getenv("DB_HOST") + "]");
        System.out.println("DB_USERNAME = [" + System.getenv("DB_USERNAME") + "]");
        System.out.println("DB_PASSWORD 설정 여부 = "
                + (System.getenv("DB_PASSWORD") != null));

        simpleDb = new SimpleDb(
                host, username, password, testDbName
        );

        // 테스트 시작 전 데이터 초기화
        simpleDb.run("TRUNCATE TABLE wise_saying");

        repository = new WiseSayingDbRepository(simpleDb);
    }

    @Test
    @DisplayName("명언 등록 후 조회")
    void testSaveAndFind() throws SQLException {
        // 1. 명언 등록
        WiseSaying saved = repository.saveNew(
                "현재를 사랑하라.",
                "작자미상"
        );

        // 2. 발급된 ID 확인
        assertTrue(saved.getId() > 0);

        // 3. MySQL에서 다시 조회
        WiseSaying found = repository.findById(saved.getId());

        // 4. 저장한 내용과 일치하는지 확인
        assertNotNull(found);
        assertEquals("현재를 사랑하라.", found.getContent());
        assertEquals("작자미상", found.getAuthor());
    }

    @Test
    @DisplayName("TRUNCATE 이후 첫 등록 ID는 1")
    void testFirstIdAfterTruncate() throws SQLException {
        WiseSaying saved = repository.saveNew("현재를 사랑하라.", "작자미상");

        assertEquals(1, saved.getId());
    }

    @Test
    @DisplayName("명언 수정")
    void testEdit() throws SQLException {
        WiseSaying saved = repository.saveNew("현재를 사랑하라.", "작자미상");

        saved.setContent("오늘을 사랑하라.");
        saved.setAuthor("새로운 작가");
        repository.editById(saved);

        WiseSaying found = repository.findById(saved.getId());
        assertEquals("오늘을 사랑하라.", found.getContent());
        assertEquals("새로운 작가", found.getAuthor());
    }

    @Test
    @DisplayName("명언 삭제")
    void testDelete() throws SQLException {
        WiseSaying saved = repository.saveNew("현재를 사랑하라.", "작자미상");

        repository.deleteById(saved.getId());

        assertNull(repository.findById(saved.getId()));
    }

    @Test
    @DisplayName("전체 조회는 최신순")
    void testFindAllOrder() throws SQLException {
        repository.saveNew("현재를 사랑하라.", "작자미상");
        repository.saveNew("과거에 집착하지 마라.", "작자미상");

        WiseSaying[] sayings = repository.findAll();

        assertEquals(2, sayings.length);
        assertEquals("과거에 집착하지 마라.", sayings[0].getContent());
    }


    @AfterEach
    void tearDown() {
        simpleDb.close();
    }
}

