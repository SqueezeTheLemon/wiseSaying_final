// 사용자 입력을 받고 ,  그것이 옳은 인자인지 판단하여 WiseSayingController 에게 넘김
// 스캐너와 출력 사용 가능

package com.back;

import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.system.controller.SystemController;
import com.back.domain.wiseSaying.repository.WiseSayingDbRepository;
import com.back.domain.wiseSaying.repository.WiseSayingFileRepository;
import com.back.domain.wiseSaying.repository.WiseSayingRepository;
import com.back.global.db.SimpleDb;
import com.back.global.rq.Rq;

import java.sql.SQLException;
import java.util.Scanner;

public class App  {
    // file로 저장하는 경우 = new WiseSayingFileRepository();
    // DB에 저장하는 경우 = new WiseSayingDbRepository(simpleDb);
    public void run() throws SQLException {
        Scanner sc = new Scanner(System.in);

        String repositoryType = System.getProperty(
                "wiseSaying.repository", "db"
        );

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
            System.err.println(
                    "[APP] DB_NAME = " + System.getenv("DB_NAME")
            );
            repository = new WiseSayingDbRepository(simpleDb);
        }
        SystemController systemController = new SystemController();
        WiseSayingController wiseSayingController =
                new WiseSayingController(sc, repository);


        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            String input = sc.nextLine().trim();

            Rq rq = new Rq(input);
            String command = rq.getActionName();

            // IntelliJ 터미널에서 한글 씹힘 이슈로 영어 입력 받음
            switch (command) {

                case "exit" -> {
                    systemController.exit();
                    return;
                }
                case "enroll" -> wiseSayingController.enroll();
                case "list" -> wiseSayingController.show(rq);
                case "delete" -> wiseSayingController.delete(rq);
                case "edit" -> wiseSayingController.edit(rq);
                case "search" -> wiseSayingController.search(rq);
                case "build" -> wiseSayingController.build();

                default -> System.out.println("옳지 않은 INPUT. 다시 입력해주세요.");
            }

        }
    }
}
