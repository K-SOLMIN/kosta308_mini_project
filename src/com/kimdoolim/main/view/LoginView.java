package com.kimdoolim.main.view;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import static com.kimdoolim.common.AppScanner.*;

import java.util.Scanner;

public class LoginView {

    public void loginView() {
        Scanner scanner = AppScanner.getScanner();

        showSplash(scanner);

        while (true) {
            AppScanner.cls();
            System.out.println("================================================================================");
            System.out.println("                                   로  그  인");
            System.out.println("================================================================================");

            System.out.print("아이디: ");
            String userId = scanner.nextLine();

            System.out.print("비밀번호: ");
            String password = scanner.nextLine();

            if (Auth.login(userId, password) == 1) break;
            System.out.println("아이디 또는 비밀번호가 올바르지 않습니다. 다시 시도해주세요.");
        }
    }

    private void showSplash(Scanner scanner) {
        AppScanner.cls();
        System.out.println(BLUE + "================================================================================" + RESET);
        System.out.println();
        System.out.println(CYAN + "               ███  ███  █ █  ███  ███  █     ███  ███  ███  ███" + RESET);
        System.out.println(CYAN + "               █    █    █ █  █ █  █ █  █     █    █ █  █ █  █  " + RESET);
        System.out.println(CYAN + "               ███  █    ███  █ █  █ █  █     █    ███  ███  ███" + RESET);
        System.out.println(CYAN + "                 █  █    █ █  █ █  █ █  █     █    █ █  ██   █  " + RESET);
        System.out.println(CYAN + "               ███  ███  █ █  ███  ███  ███   ███  █ █  █ █  ███" + RESET);
        System.out.println();
        System.out.println(BOLD + CYAN + "                          ★   스  쿨  케  어   ★                              " + RESET);
        System.out.println();
        System.out.println(BLUE + "                      학교 시설 및 비품 예약 관리 시스템                      " + RESET);
        System.out.println();
        System.out.println(CYAN + "                  [ 아무 키나 누르고 Enter 를 눌러주세요 ]               " + RESET);
        System.out.println();
        System.out.println(BLUE + "================================================================================" + RESET);
        scanner.nextLine();
    }
}
