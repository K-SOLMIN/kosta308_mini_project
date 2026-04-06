package com.kimdoolim.main.view;

import com.kimdoolim.common.Auth;

import java.util.Scanner;

public class LoginView {
    public void loginView() {
        Scanner scanner = new Scanner(System.in);
        int result = 0;

        System.out.println("=============================");
        System.out.println("         로그인 화면           ");
        System.out.println("=============================");

        while(true) {
            System.out.print("아이디: ");
            String userId = scanner.nextLine();

            System.out.print("비밀번호: ");
            String password = scanner.nextLine();

            result = Auth.login(userId, password);

            if(result == 1) break;
            else System.out.println("회원정보가 일치하지 않습니다.");
        }
    }
}
