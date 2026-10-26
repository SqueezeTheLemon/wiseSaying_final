// 데이터의 조회, 수정, 삭제, 생성 (CRUD)를 담당
// 스캐너 , 출력 사용 금지

package com.back.domain.wiseSaying.repository;

import com.back.domain.wiseSaying.entity.WiseSaying;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public interface WiseSayingRepository {

    // 명언 새로 등록
    WiseSaying saveNew(String content, String author) throws SQLException;

    // 명언 전체 목록 가져오기
    WiseSaying[] findAll() throws SQLException;

    // 입력받은 keyword 기반으로 검색
    WiseSaying[] findByKeyword(String type, String keyword) throws SQLException;

    // 5개 saying씩 묶고, 입력받은 page 번호에 맞게 5개 리스트 반환
    WiseSaying[] divideToPages(WiseSaying[] wiseSayings, int page) throws SQLException;


    // id에 맞는 명언 조회
    public WiseSaying findById(int id) throws SQLException;

    // 명언 수정
    public void editById(WiseSaying saying) throws SQLException;

    // 명언 삭제
    public void deleteById(int id) throws SQLException;

    public void build() throws SQLException;
}
