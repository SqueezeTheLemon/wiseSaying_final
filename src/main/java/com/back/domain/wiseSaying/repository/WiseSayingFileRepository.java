package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class WiseSayingFileRepository implements WiseSayingRepository{

    private final Path dbPath = Path.of(System.getProperty("wiseSaying.dbPath", "db/wiseSaying") );
    private final Path lastIdPath =dbPath.resolve("lastId.txt");

    private final ArrayList<WiseSaying> sayings = new ArrayList<>();
    private int index = 1;

    public WiseSayingFileRepository() {
        try {
            Files.createDirectories(dbPath);

            // 마지막 명언 번호 복원하고, 그 다음부터 작성해야 할 index를 가져옴
            if (Files.exists(lastIdPath)){
                index = Integer.parseInt(Files.readString(lastIdPath).trim()) + 1;
            }

            for (int i = 1; i < index; i++) {
                Path filePath = dbPath.resolve(i + ".json");

                if (Files.exists(filePath)) {
                    String json = Files.readString(filePath);
                    WiseSaying saying = fromJson(json);

                    sayings.add(saying); // 수정
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WiseSaying saveNew(String content, String author) {
        WiseSaying saying = new WiseSaying(index, content, author);

        Path filePath = dbPath.resolve(index + ".json");

        try {
            Files.writeString(filePath, toJson(saying));
            Files.writeString(lastIdPath, String.valueOf(index));

            sayings.add(saying);
            index++;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return saying;
    }

    @Override
    public WiseSaying[] findAll() {
        return sayings.stream()
                .sorted(Comparator.comparingInt(WiseSaying::getId).reversed())
                .toArray(WiseSaying[]::new);
    }

    @Override
    public WiseSaying[] findByKeyword(String type, String keyword) {
        ArrayList <WiseSaying> result = new ArrayList<>();
        if (type.equals("author")) {
            for (WiseSaying saying1 : sayings) {
                if (saying1 == null) continue;
                if(saying1.getAuthor().contains(keyword)){
                    result.add(saying1);
                }
            }
        }
        else if (type.equals("content")) {
            for (WiseSaying saying1 : sayings) {
                if (saying1 == null) continue;
                if(saying1.getContent().contains(keyword)){
                    result.add(saying1);
                }
            }
        }

        result.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return result.toArray(WiseSaying[]::new);

    }

    @Override
    public WiseSaying[] divideToPages(WiseSaying[] wiseSayings, int page) {
        int start = (page - 1) * 5;
        int end = Math.min(page * 5, wiseSayings.length);

        if (page < 1 || start >= wiseSayings.length) {
            return new WiseSaying[0];
        }

        WiseSaying[] result = new WiseSaying[end - start];

        int index = 0;

        while (start < end) {
            result[index] = wiseSayings[start];
            index++;
            start++;
        }

        return result;
    }

    @Override
    public WiseSaying findById(int id) {
        return sayings.stream() // 명언 목록에서 스트림 생성 (데이터를 반복문 없이 검색, 필터링, 정렬, 변환해주는 기능)
                .filter(saying -> saying != null && saying.getId() == id) // 조건 (id 일치)에 맞는 명언만 통과
                .findFirst() // 조건에 맞는 첫번째 명언만 선택
                .orElse(null); // 검색 결과 없다면 null 반환
    }

    @Override
    public void editById(WiseSaying saying) {
        int id = saying.getId();
        if (id < 1 || findById(id) == null) {
            throw new IllegalArgumentException("유효하지 않은 명언 번호입니다.");
        }

        Path filePath = dbPath.resolve(id + ".json");

        try {
            Files.writeString(filePath, toJson(saying));

            sayings.removeIf(s -> s.getId() == id);
            sayings.add(saying);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void deleteById(int id) {
        if (id < 1 || id >= index) {
            return;
        }

        Path filePath = dbPath.resolve(id + ".json");

        try {
            Files.deleteIfExists(filePath);
            sayings.removeIf(saying -> saying != null && saying.getId() == id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void build(){
        String json = sayings.stream()
                .sorted(Comparator.comparingInt(WiseSaying :: getId))
                .map(this::toJson)
                .collect(Collectors.joining(",\n", "[\n", "\n]"));

        Path filePath = dbPath.resolve("data.json");

        try {
            Files.writeString(filePath, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //파일 저장 방식에만 필요한 내부 메서드들

    // 명언을 Json 형태로 변환
    private String toJson(WiseSaying saying) {
        return """
                {
                    "id": %d,
                    "content": "%s",
                    "author": "%s"
                }
                """.formatted(
                saying.getId(),
                saying.getContent(),
                saying.getAuthor()
        );
    }

    // json을 명언 형태로 변환
    private WiseSaying fromJson(String json) {
        String[] lines = json.split("\n");

        int id = Integer.parseInt(
                lines[1].split(":")[1].trim().replace(",", "")
        );

        String content = lines[2].split(":", 2)[1].trim()
                .replace("\"", "").replaceAll(",$", "");

        String author = lines[3].split(":", 2)[1].trim()
                .replace("\"", "").replaceAll(",$", "");

        return new WiseSaying(id, content, author);
    }


}
