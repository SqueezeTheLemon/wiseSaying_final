package com.back.domain.wiseSaying.controller;

import com.back.AppTestRunner;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class WiseSayingControllerTest {

    private final Path testDbPath = Path.of("db/wiseSayingTest");

    @BeforeEach
    void setUp() throws IOException {
        // Controller 테스트는 파일 Repository + 테스트 전용 경로 사용 (MySQL과 분리)
        System.setProperty("wiseSaying.repository", "file");
        System.setProperty("wiseSaying.dbPath", testDbPath.toString());

        // 매 테스트 전에 테스트 DB 초기화 (lastId.txt 포함)
        if (Files.exists(testDbPath)) {
            try (Stream<Path> paths = Files.walk(testDbPath)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }

        Files.createDirectories(testDbPath);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("wiseSaying.repository");
        System.clearProperty("wiseSaying.dbPath");
    }

    @Test
    @DisplayName("등록")
    void t1() {
        String out = AppTestRunner.run("""
                enroll
                현재를 사랑하라.
                작자미상
                exit
                """);


        assertTrue(out.contains("1번 명언이 등록되었습니다."), out);
        assertTrue(Files.exists(testDbPath.resolve("1.json")));
    }

    @Test
    @DisplayName("목록")
    void t2() {
        String out = AppTestRunner.run("""
                enroll
                현재를 사랑하라.
                작자미상
                enroll
                과거에 집착하지 마라.
                작자미상
                list?page=1
                exit
                """);


        assertTrue(out.contains("현재를 사랑하라."), "실제 출력:\n" + out);
        assertTrue(out.contains("과거에 집착하지 마라."), "실제 출력:\n" + out);
    }

    @Test
    @DisplayName("삭제")
    void t3() {
        String out = AppTestRunner.run("""
                enroll
                현재를 사랑하라.
                작자미상
                delete?id=1
                exit
                """);

        assertTrue(out.contains("1번 명언이 삭제되었습니다."));
        assertFalse(Files.exists(testDbPath.resolve("1.json")));
    }

    @Test
    @DisplayName("수정")
    void t4() throws IOException {
        String out = AppTestRunner.run("""
                enroll
                현재를 사랑하라.
                작자미상
                edit?id=1
                오늘을 사랑하라.
                새로운 작가
                exit
                """);

        assertTrue(out.contains("1번 명언이 수정되었습니다."));

        String json = Files.readString(testDbPath.resolve("1.json"));

        assertTrue(json.contains("오늘을 사랑하라."));
        assertTrue(json.contains("새로운 작가"));
    }
}