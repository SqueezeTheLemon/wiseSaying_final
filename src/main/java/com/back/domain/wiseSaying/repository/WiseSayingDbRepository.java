package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.global.db.SimpleDb;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WiseSayingDbRepository implements WiseSayingRepository {

    private final SimpleDb simpleDb;

    public WiseSayingDbRepository (SimpleDb simpleDb){
        this.simpleDb=simpleDb;
    }

    @Override
    public WiseSaying saveNew(String content, String author) throws SQLException {
        // 1. mySQL에 명언 저장
        long newId = simpleDb.genSql()
                .append("INSERT INTO wise_saying (content, author) VALUES (?, ?)", content, author)
                .insert();

        // 2. mysql이 생성한 ID로 명언 객체 생성
        WiseSaying saying = new WiseSaying((int) newId, content, author);

        // 3. 생성된 명언 반환
        return saying;
    }

    @Override
    public WiseSaying[] findAll() throws SQLException {
        List<WiseSaying> sayings = simpleDb.genSql()
                .append("SELECT * FROM wise_saying ORDER BY id DESC")
                .selectRows(WiseSaying.class);

        return sayings.toArray(WiseSaying[]::new);
    }

    @Override
    public WiseSaying findById(int id) throws SQLException {
        WiseSaying saying = simpleDb.genSql()
                .append("SELECT * FROM wise_saying WHERE id = ?" , id)
                .selectRow(WiseSaying.class);
        return saying;
    }

    @Override
    public WiseSaying[] findByKeyword(String type, String keyword) throws SQLException {
        // 검색 가능한 컬럼 제한
        if (!type.equals("content") && !type.equals("author")) {
            throw new IllegalArgumentException("잘못된 검색 유형입니다.");
        }

        List<WiseSaying> sayings = simpleDb.genSql()
                .append("SELECT * FROM wise_saying")
                .append("WHERE " + type + " LIKE ?", "%" + keyword + "%")
                .append("ORDER BY id DESC")
                .selectRows(WiseSaying.class);

        return sayings.toArray(WiseSaying[]::new);
    }

    @Override
    public WiseSaying[] divideToPages(WiseSaying[] wiseSayings, int page) throws SQLException {
        int pageSize = 5;
        if (page < 1) { return new WiseSaying[0]; }

        int offset = (page - 1) * pageSize;

        List<WiseSaying> sayings  = simpleDb.genSql()
                .append("SELECT * FROM wise_saying")
                .append("ORDER BY id DESC")
                .append("LIMIT ? OFFSET ? ", pageSize, offset)
                .selectRows(WiseSaying.class);

        return sayings.toArray(WiseSaying[]::new);

    }



    @Override
    public void editById(WiseSaying saying) throws SQLException {
        int id = saying.getId();
        String content = saying.getContent();
        String author = saying.getAuthor();

        simpleDb.genSql()
                .append("UPDATE wise_saying")
                .append("SET content = ?, author = ?", content, author)
                .append("WHERE id = ?", id)
                .update();
    }

    @Override
    public void deleteById(int id) throws SQLException {
        int delete = simpleDb.genSql()
                .append("DELETE FROM wise_saying WHERE id = ?", id)
                .delete();
    }

    // mysql에 저장된 모든 명언을 data.json으로 내보내기
    @Override
    public void build() throws SQLException {
        // 1. DB에서 모든 명언을 조회
        WiseSaying [] sayings = findAll();

        // 2. 기존 build()와 같게, ID 오름차순 정렬
        Arrays.sort(sayings, Comparator.comparingInt(WiseSaying::getId));

        // 3. Json 파일이 저장될 경로
        Path filePath = Path.of("db/wiseSaying/data.json");

        // 4. Jackson 객체 생성
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 5. 파일이 저장될 폴더가 없으면 생성
            Files.createDirectories(filePath.getParent());

            // 6. Java 객체 배열을 JSON 파일로 변환하여 저장
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), sayings);

        } catch (IOException e) {
            throw new RuntimeException("data.json 생성 실패", e);
        }

    }
}
