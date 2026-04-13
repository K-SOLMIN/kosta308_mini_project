package com.kimdoolim.view;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;

import java.util.Scanner;

import static com.kimdoolim.common.AppScanner.*;

public class LoginView {

    public void loginView() {
        Scanner scanner = AppScanner.getScanner();

        showSplash(scanner);

        AppScanner.cls();
        while (true) {
            System.out.println("===============================");
            System.out.println("           로  그  인");
            System.out.println("===============================");

            System.out.print("아이디: ");
            String userId = scanner.nextLine();

            System.out.print("비밀번호: ");
            String password = scanner.nextLine();

            if (Auth.login(userId, password) == 1) break;

            System.out.println("\n  ※ 아이디 또는 비밀번호가 올바르지 않습니다.");
            System.out.print("  [ Enter 를 누르면 다시 시도합니다 ] ");
            System.out.flush();
            scanner.nextLine();
            AppScanner.cls();
        }
    }

    private void showSplash(Scanner scanner) {
        AppScanner.cls();
        System.out.println(BLUE + "================================================================================" + RESET);
        System.out.println();
        System.out.println(CYAN + "                ███████   ███████   ██       ████████   ██   ██        " + RESET);
        System.out.println(CYAN + "                ██        ██   ██   ██          ██      ███ ███        " + RESET);
        System.out.println(CYAN + "                ███████   ██████    ██          ██      ██ █ ██        " + RESET);
        System.out.println(CYAN + "                     ██   ██   ██   ██          ██      ██   ██        " + RESET);
        System.out.println(CYAN + "                ███████   ██████    ███████   ███████   ██   ██        " + RESET);
        System.out.println();
        System.out.println(BOLD + CYAN + "                          ★        S B L I M        ★                        " + RESET);
        System.out.println();
        System.out.println(BLUE + "                       학교 시설 및 비품 예약 관리 시스템                    " + RESET);
        System.out.println();
        System.out.println(CYAN + "                            [ Enter 를 눌러주세요 ]                         " + RESET);
        System.out.println();
        System.out.println(BLUE + "================================================================================" + RESET);
        scanner.nextLine();
    }

}
