package com.kimdoolim.view;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.service.UserService;

import java.util.List;
import java.util.Scanner;

public class UserManageView {

  private final UserService userService = new UserService();
  private final Scanner scanner = AppScanner.getScanner();

  public void userManageMenu() {
    while (true) {
      AppScanner.cls();
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.println("                        [ 사용자 관리 ]");
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.println(" 1.목록 조회 || 2.사용자 등록 || 3.사용자 상태변경 || 4.권한 변경 || 0.뒤로 가기");
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: showUserList(); break;
        case 2: registerUserFlow(); break;
        case 3: changeUserStatusFlow(); break;
        case 4: togglePermissionFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 1. 사용자 목록 출력
  // ─────────────────────────────────────────────────────
  public void showUserList() {
    AppScanner.cls();
    System.out.println("\n[사용자 목록]");
    List<User> list = userService.getAllUsers();

    if (list.isEmpty()) {
      System.out.println("등록된 사용자가 없습니다.");
      waitBack();
      return;
    }

    printUserListTable(list);
    waitBack();
  }

  private void printUserListTable(List<User> list) {
    String div = "─".repeat(70);
    System.out.println(div);
    System.out.println(
        pad("번호", 4)  + "  " +
            pad("이름", 8)  + "  " +
            pad("아이디", 14) + "  " +
            pad("학년/반", 8) + "  " +
            pad("권한", 12) + "  " +
            "상태"
    );
    System.out.println(div);

    for (int i = 0; i < list.size(); i++) {
      User u = list.get(i);
      String permission = u.getPermission() == Permission.MIDDLEADMIN ? "중간 관리자" : "일반 사용자";
      String status = u.isActive() ? "활성" : (u.getUserStatus() != null ? u.getUserStatus() : "비활성");
      String gradeClass = u.getGradeNo() + "학년 " + u.getClassNo() + "반";

      System.out.println(
          pad(String.valueOf(i + 1), 4) + "  " +
              pad(u.getName(), 8)           + "  " +
              pad(u.getId(), 14)            + "  " +
              pad(gradeClass, 8)            + "  " +
              pad(permission, 12)           + "  " +
              status
      );
    }
    System.out.println(div);
  }

  // ─────────────────────────────────────────────────────
  // 2. 사용자 등록
  // ─────────────────────────────────────────────────────
  private void registerUserFlow() {
    AppScanner.cls();
    System.out.println("\n[사용자 등록]");

    System.out.print("아이디: ");
    String id = scanner.nextLine().trim();
    if (id.isEmpty()) { System.out.println("아이디를 입력해주세요."); return; }

    System.out.print("초기 비밀번호: ");
    String password = scanner.nextLine().trim();
    if (password.isEmpty()) { System.out.println("비밀번호를 입력해주세요."); return; }

    System.out.print("이름: ");
    String name = scanner.nextLine().trim();

    System.out.print("전화번호: ");
    String phone = scanner.nextLine().trim();

    System.out.print("학년: ");
    int gradeNo = readInt();

    System.out.print("반: ");
    int classNo = readInt();

    System.out.println("\n── 등록 정보 확인 ──────────────────");
    System.out.println(" 아이디  : " + id);
    System.out.println(" 이름    : " + name);
    System.out.println(" 전화    : " + phone);
    System.out.println(" 학년/반 : " + gradeNo + "학년 " + classNo + "반");
    System.out.println("────────────────────────────────────");
    System.out.print("등록하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("등록이 취소되었습니다.");
      waitBack();
      return;
    }

    User newUser = User.builder()
        .schoolId(1) // 학교 ID는 현재 단일 학교 기준
        .id(id)
        .password(password)
        .name(name)
        .phone(phone)
        .gradeNo(gradeNo)
        .classNo(classNo)
        .build();

    System.out.println(">> " + userService.registerUser(newUser));
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 3. 사용자 상태변경
  // ─────────────────────────────────────────────────────
  private void changeUserStatusFlow() {
    AppScanner.cls();
    System.out.println("\n[사용자 상태변경]");

    List<User> list = userService.getAllUsers();
    if (list.isEmpty()) { System.out.println("등록된 사용자가 없습니다."); waitBack(); return; }
    printUserListTable(list);

    System.out.print("사용자 번호 입력 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) { System.out.println("잘못된 번호입니다."); waitBack(); return; }

    User target = list.get(index - 1);

    String currentStatus = target.isActive() ? "활성" : (target.getUserStatus() != null ? target.getUserStatus() : "비활성");
    System.out.println("──────────────────────────────────────");
    System.out.println(" 이름  : " + target.getName());
    System.out.println(" 아이디: " + target.getId());
    System.out.println(" 현재  : " + currentStatus);
    System.out.println("──────────────────────────────────────");
    System.out.println(" 1. 휴직처리");
    System.out.println(" 2. 복직처리");
    System.out.println(" 3. 전근처리");
    System.out.println(" 0. 뒤로");
    System.out.println("──────────────────────────────────────");
    System.out.print("처리할 번호를 입력하세요: ");
    int action = readInt();

    String result;
    switch (action) {
      case 1: result = userService.setLeaveOfAbsence(target.getUserId()); break;
      case 2: result = userService.restoreFromLeave(target.getUserId()); break;
      case 3: result = userService.setTransfer(target.getUserId()); break;
      case 0: return;
      default: System.out.println("잘못된 입력입니다."); waitBack(); return;
    }
    System.out.println(">> " + result);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 서브 목록 출력 (번호 + 이름 + 아이디 + 상태)
  // ─────────────────────────────────────────────────────
  private void printUserSubList(List<User> list) {
    System.out.println("─".repeat(50));
    for (int i = 0; i < list.size(); i++) {
      User u = list.get(i);
      String status = u.isActive() ? "활성" : (u.getUserStatus() != null ? u.getUserStatus() : "비활성");
      System.out.println(pad(String.valueOf(i + 1), 4) + "  " +
          pad(u.getName(), 8) + "  " +
          pad(u.getId(), 14) + "  " + status);
    }
    System.out.println("─".repeat(50));
  }

  // ─────────────────────────────────────────────────────
  // 4. 권한 변경 (USER ↔ MIDDLEADMIN)
  // ─────────────────────────────────────────────────────
  private void togglePermissionFlow() {
    AppScanner.cls();
    System.out.println("\n[권한 변경]");

    List<User> list = userService.getAllUsers();
    if (list.isEmpty()) {
      System.out.println("등록된 사용자가 없습니다.");
      waitBack();
      return;
    }

    printUserListTable(list);

    System.out.print("변경할 사용자 번호 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) { System.out.println("잘못된 번호입니다."); waitBack(); return; }

    User target = list.get(index - 1);
    String currentPerm = target.getPermission() == Permission.MIDDLEADMIN ? "중간 관리자" : "일반 사용자";
    String nextPerm    = target.getPermission() == Permission.MIDDLEADMIN ? "일반 사용자" : "중간 관리자";

    System.out.println("\n대상: " + target.getName() + " (" + target.getId() + ")");
    System.out.println("현재 권한: " + currentPerm + "  →  변경 후: " + nextPerm);
    System.out.print("변경하시겠습니까? (Y/N): ");

    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) {
      System.out.println("취소되었습니다.");
      waitBack();
      return;
    }

    System.out.println(">> " + userService.togglePermission(target.getUserId(), target.getPermission()));
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 메시지 출력 후 엔터 대기
  // ─────────────────────────────────────────────────────
  private void waitBack() {
    while (true) {
      System.out.println(" 0. 뒤로가기");
      System.out.print("선택: ");
      if ("0".equals(scanner.nextLine().trim())) return;
    }
  }

  // ─────────────────────────────────────────────────────
  // 한글 패딩 헬퍼
  // ─────────────────────────────────────────────────────
  private int displayWidth(String s) {
    int width = 0;
    for (char c : s.toCharArray()) {
      width += (c >= '\uAC00' && c <= '\uD7A3') || (c >= '\u3000' && c <= '\u9FFF') ? 2 : 1;
    }
    return width;
  }

  private String pad(String s, int width) {
    int diff = width - displayWidth(s);
    if (diff <= 0) return s;
    StringBuilder sb = new StringBuilder(s);
    for (int i = 0; i < diff; i++) sb.append(' ');
    return sb.toString();
  }

  private int readInt() {
    try {
      return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}