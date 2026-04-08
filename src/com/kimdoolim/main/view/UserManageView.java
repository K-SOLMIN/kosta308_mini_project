package com.kimdoolim.main.view;

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
      System.out.println("\n=============================");
      System.out.println("        사용자 관리 메뉴        ");
      System.out.println("=============================");
      System.out.println(" 1. 사용자 목록 조회");
      System.out.println(" 2. 사용자 등록");
      System.out.println(" 3. 상태 변경 (활성/비활성)");
      System.out.println(" 4. 권한 변경 (일반 ↔ 중간 관리자)");
      System.out.println(" 0. 뒤로 가기");
      System.out.println("=============================");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: showUserList(); break;
        case 2: registerUserFlow(); break;
        case 3: toggleActiveFlow(); break;
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
    System.out.println("\n[사용자 목록]");
    List<User> list = userService.getAllUsers();

    if (list.isEmpty()) {
      System.out.println("등록된 사용자가 없습니다.");
      return;
    }

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
      String active     = u.isActive() ? "활성" : "비활성(휴직/전근)";
      String gradeClass = u.getGradeNo() + "학년 " + u.getClassNo() + "반";

      System.out.println(
          pad(String.valueOf(i + 1), 4) + "  " +
              pad(u.getName(), 8)           + "  " +
              pad(u.getId(), 14)            + "  " +
              pad(gradeClass, 8)            + "  " +
              pad(permission, 12)           + "  " +
              active
      );
    }
    System.out.println(div);
  }

  // ─────────────────────────────────────────────────────
  // 2. 사용자 등록
  // ─────────────────────────────────────────────────────
  private void registerUserFlow() {
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
  }

  // ─────────────────────────────────────────────────────
  // 3. 상태 변경 (활성 ↔ 비활성)
  // ─────────────────────────────────────────────────────
  private void toggleActiveFlow() {
    System.out.println("\n[상태 변경]");
    showUserList();

    List<User> list = userService.getAllUsers();
    if (list.isEmpty()) return;

    System.out.print("변경할 사용자 번호 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) { System.out.println("잘못된 번호입니다."); return; }

    User target = list.get(index - 1);
    String currentState = target.isActive() ? "활성" : "비활성";
    String nextState    = target.isActive() ? "비활성(휴직/전근)" : "활성(복직)";

    System.out.println("\n대상: " + target.getName() + " (" + target.getId() + ")");
    System.out.println("현재 상태: " + currentState + "  →  변경 후: " + nextState);
    System.out.print("변경하시겠습니까? (Y/N): ");

    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) {
      System.out.println("취소되었습니다.");
      return;
    }

    System.out.println(">> " + userService.toggleUserActive(target.getUserId(), target.isActive()));
  }

  // ─────────────────────────────────────────────────────
  // 4. 권한 변경 (USER ↔ MIDDLEADMIN)
  // ─────────────────────────────────────────────────────
  private void togglePermissionFlow() {
    System.out.println("\n[권한 변경]");
    showUserList();

    List<User> list = userService.getAllUsers();
    if (list.isEmpty()) return;

    System.out.print("변경할 사용자 번호 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) { System.out.println("잘못된 번호입니다."); return; }

    User target = list.get(index - 1);
    String currentPerm = target.getPermission() == Permission.MIDDLEADMIN ? "중간 관리자" : "일반 사용자";
    String nextPerm    = target.getPermission() == Permission.MIDDLEADMIN ? "일반 사용자" : "중간 관리자";

    System.out.println("\n대상: " + target.getName() + " (" + target.getId() + ")");
    System.out.println("현재 권한: " + currentPerm + "  →  변경 후: " + nextPerm);
    System.out.print("변경하시겠습니까? (Y/N): ");

    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) {
      System.out.println("취소되었습니다.");
      return;
    }

    System.out.println(">> " + userService.togglePermission(target.getUserId(), target.getPermission()));
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