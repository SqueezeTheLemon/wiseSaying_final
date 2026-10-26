package com.back.domain.wiseSaying.service;

import com.back.domain.wiseSaying.entity.WiseSaying;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;

import java.sql.SQLException;

public class WiseSayingService {

    private final WiseSayingRepository wiseSayingRepository;

    // service가 구체적인 구현체를 직접 생성하지 않게 함
    public WiseSayingService(WiseSayingRepository repository) {
        this.wiseSayingRepository = repository;
    }

    // 명언 enroll - service에서 content, author를 받아옴
    public WiseSaying enroll(String content, String author) throws SQLException {
        return wiseSayingRepository.saveNew(content, author);
    }

    // 명언 목록 조회 - 전체 조회
    public WiseSaying[] getAllList() throws SQLException {
        WiseSaying[] allList = wiseSayingRepository.findAll();

        return wiseSayingRepository.findAll();
    }

    // 명언 목록 검색 기능
    public WiseSaying[] getByKeyword(String type, String keyword) throws SQLException {
        return wiseSayingRepository.findByKeyword(type, keyword);

    }

    public WiseSaying[] getPagedList(WiseSaying[] wiseSaying, int pageNumber) throws SQLException {
        return wiseSayingRepository.divideToPages(wiseSaying, pageNumber);

    }

    // id로 WiseSaying 조회
    public WiseSaying findById(int id) throws SQLException {
        return wiseSayingRepository.findById(id);
    }

    // 명언 삭제
    public boolean deleteById(int id) throws SQLException {
        WiseSaying saying = wiseSayingRepository.findById(id);

        if(saying == null) return false; // 삭제하지 못했으면 false

        wiseSayingRepository.deleteById(id);
        return true;
    }

    // 명언 수정
    public boolean editById(int id, String content, String author) throws SQLException {
        WiseSaying saying = wiseSayingRepository.findById(id);

        if (saying == null) {
            return false;
        }

        saying.setContent(content);
        saying.setAuthor(author);
        wiseSayingRepository.editById(saying);

        return true;
    }

    public void build() throws SQLException {
        wiseSayingRepository.build();
    }



}
