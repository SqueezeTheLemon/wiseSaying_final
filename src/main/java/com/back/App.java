// 사용자 입력을 받고 ,  그것이 옳은 인자인지 판단하여 WiseSayingController 에게 넘김
// 스캐너와 출력 사용 가능

package com.back;

import com.back.domain.wiseSaying.controller.WiseSayingController;
import com.back.domain.system.controller.SystemController;
import com.back.global.rq.Rq;

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

            Rq rq = new Rq(input);
            String command = rq.getActionName();

            // IntelliJ 터미널에서 한글 씹힘 이슈로 영어 입력 받음
            switch (command) {

                case "exit" -> {
                    systemController.exit();
                    return;
                }
                case "enroll" -> wiseSayingController.enroll();
                case "show" -> wiseSayingController.show(rq);
                case "delete" -> wiseSayingController.delete(rq);
                case "edit" -> wiseSayingController.edit(rq);
                case "search" -> wiseSayingController.search(rq);

                default -> System.out.println("옳지 않은 INPUT. 다시 입력해주세요.");
            }

        }
    }
}
