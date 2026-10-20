// 사용자 입력을 받고 ,  그것이 옳은 인자인지 판단하여 WiseSayingController 에게 넘김
// 스캐너와 출력 사용 가능

package com.back;

import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.system.controller.SystemController;

import java.util.Scanner;

public class App  {
    private final Scanner sc = new Scanner(System.in);

    SystemController systemController = new SystemController();
    WiseSayingController wiseSayingController = new WiseSayingController(sc);

    public void run() {
        System.out.println("== 명언 앱 ==");

        while (true) {
            System.out.print("명령) ");
            String input = sc.nextLine().trim();

            int index = input.indexOf("?");

            String command;
            String query;

            if (index == -1) {
                command = input;
                query = "";
            }
            else{
                command = input.substring(0, index);
                query = input.substring(index + 1);
            }
            // IntelliJ 터미널에서 한글 씹힘 이슈로 영어 입력 받음
            switch (command) {
                case "enroll": // 등록
                    if (!query.isEmpty()) { // 등록은 뒤에 아무 query도 덧붙이지 않음.
                        System.out.println("올바르지 않은 query입니다. 다시 입력해주세요.");
                        break;
                    }

                    wiseSayingController.enroll();
                    break;

                case "show":
                    if (!query.isEmpty() && !query.matches("page=[1-9]\\d*")) {
                        System.out.println("올바르지 않은 query입니다. 다시 입력해주세요.");
                        break;
                    }

                    wiseSayingController.show(query);
                    break;

                case "search":  // 검색 기능 추가
                    if (!query.matches("keywordType=(content|author)&keyword=[^&]+(&page=[1-9]\\d*)?")) {
                        System.out.println("올바르지 않은 query입니다. 다시 입력해주세요.");
                        break;
                    }
                    wiseSayingController.search(query);
                    break;

                case "delete":
                    if (!query.matches("id=[1-9]\\d*")) {
                        System.out.println("올바르지 않은 query입니다. 다시 입력해주세요.");
                        break;
                    }

                    wiseSayingController.delete(query);
                    break;
                case "edit":
                    if (!query.matches("id=[1-9]\\d*")) {
                        System.out.println("올바르지 않은 query입니다. 다시 입력해주세요.");
                        break;
                    }

                    wiseSayingController.edit(query);
                    break;

                case "exit":
                    systemController.exit();
                    return;

                default:
                    System.out.println("옳지 않은 INPUT. 다시 입력해주세요.");
            }

        }
    }
}
