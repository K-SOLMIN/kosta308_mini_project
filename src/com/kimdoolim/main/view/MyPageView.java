package com.kimdoolim.main.view;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.service.UserService;

import java.util.Scanner;

public class MyPageView {

  private final UserService userService = new UserService();
  private final Scanner scanner = AppScanner.getScanner();

  public void myPageMenu() {
    while (true) {
      AppScanner.cls();
      User loginUser = Auth.getUserInfo();

      System.out.println("───────────────────────────────────────");
      System.out.println("             [ 마이페이지 ]");
      System.out.println("───────────────────────────────────────");
      printUserInfo(loginUser);
      System.out.println("───────────────────────────────────────");
      System.out.println("     1.비밀번호 변경 || 0.뒤로 가기");
      System.out.println("───────────────────────────────────────");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: changePasswordFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 내 정보 출력
  // ─────────────────────────────────────────────────────
  private void printUserInfo(User u) {
    String permission = u.getPermission() == Permission.ADMIN      ? "최상위 관리자"
        : u.getPermission() == Permission.MIDDLEADMIN ? "중간 관리자"
        : "일반 사용자";
    System.out.println(" 이름    : " + u.getName());
    System.out.println(" 아이디  : " + u.getId());
    System.out.println(" 학년/반 : " + u.getGradeNo() + "학년 " + u.getClassNo() + "반");
    System.out.println(" 전화    : " + u.getPhone());
    System.out.println(" 권한    : " + permission);
  }

  // ─────────────────────────────────────────────────────
  // 비밀번호 변경
  // ─────────────────────────────────────────────────────
  private void changePasswordFlow() {
    AppScanner.cls();
    System.out.println("\n[비밀번호 변경]");

    System.out.print("현재 비밀번호: ");
    String currentPwd = scanner.nextLine().trim();

    System.out.print("새 비밀번호: ");
    String newPwd = scanner.nextLine().trim();

    System.out.print("새 비밀번호 확인: ");
    String confirmPwd = scanner.nextLine().trim();

    String msg = userService.changePassword(currentPwd, newPwd, confirmPwd);
    System.out.println(">> " + msg);
    System.out.println(" 0. 뒤로가기");
    System.out.print("선택: ");
    scanner.nextLine();
  }

  private int readInt() {
    try {
      return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}