package com.kimdoolim.main.view;

import java.util.Scanner;

public class LoginView {
    public void loginView() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=============================");
        System.out.println("         로그인              ");
        System.out.println("=============================");

        System.out.print("아이디: ");
        String userId = scanner.nextLine();

        System.out.print("비밀번호: ");
        String password = scanner.nextLine();

        System.out.println("=============================");
        System.out.println("입력된 아이디: " + userId);
        System.out.println("입력된 비밀번호: " + password);
        System.out.println("=============================");

        scanner.close();
    }
}
