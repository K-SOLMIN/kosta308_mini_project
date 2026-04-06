package com.kimdoolim.main.view;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.User;

import java.util.Scanner;

public class MainView {

    public void managerMainView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("        관리자 메인 메뉴      ");
            System.out.println("=============================");
            System.out.println(" 1. 예약 관리");
            System.out.println(" 2. 사용자 관리");
            System.out.println(" 3. 마이페이지");
            System.out.println(" 0. 종료");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println(">> 예약 관리 메뉴로 이동합니다.");

                    break;
                case 2:
                    System.out.println(">> 사용자 관리 메뉴로 이동합니다.");

                    break;
                case 3:
                    System.out.println(">> 마이페이지로 이동합니다.");

                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    public void userMainView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("         사용자 메인 메뉴     ");
            System.out.println("=============================");
            System.out.println(" 1. 예약");
            System.out.println(" 2. 마이페이지");
            System.out.println(" 0. 종료");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println(">> 예약 메뉴로 이동합니다.");

                    break;
                case 2:
                    System.out.println(">> 마이페이지로 이동합니다.");

                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }
}
