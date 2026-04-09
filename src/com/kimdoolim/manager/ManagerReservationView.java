package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.dto.*;
import com.kimdoolim.service.ReservationService;

import java.util.List;
import java.util.Scanner;

public class ManagerReservationView {

  private final ReservationService reservationService = new ReservationService();
  private final Scanner scanner = AppScanner.getScanner();

  // ─────────────────────────────────────────────────────
  // 관리자 예약 관리 메뉴
  // 상위/중간 관리자 둘 다 이 메뉴 사용
  // Service에서 권한에 따라 자동으로 분기됨
  // ─────────────────────────────────────────────────────
  public void managerReservationMenu() {
    while (true) {
      System.out.println("\n=============================");
      System.out.println("      관리자 예약 관리 메뉴    ");
      System.out.println("=============================");
      System.out.println(" 1. 예약 승인 / 반려");
      System.out.println(" 2. 예약 강제 취소");
      System.out.println(" 0. 뒤로 가기");
      System.out.println("=============================");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: approveOrRejectFlow(); break;
        case 2: forceCancelFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 예약 승인 / 반려 흐름
  // 상위 관리자 → 전체 대기 예약 목록
  // 중간 관리자 → 담당 시설/비품 대기 예약 목록만
  // ─────────────────────────────────────────────────────
  private void approveOrRejectFlow() {

    System.out.println("\n[예약 승인 / 반려]");

    List<Reservation> list = reservationService.getPendingReservations();

    if (list.isEmpty()) {
      System.out.println("대기 중인 예약이 없습니다.");
      return;
    }

    printReservationList(list);

    System.out.print("처리할 예약 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Reservation target = list.get(index - 1);

    System.out.println("\n── 처리 선택 ──────────────────────");
    System.out.println(" 1. 승인");
    System.out.println(" 2. 반려");
    System.out.println(" 0. 뒤로");
    System.out.print("선택: ");

    int action = readInt();

    switch (action) {
      case 1:
        String approveMsg = reservationService.approveReservation(target.getReservationId());
        System.out.println(">> " + approveMsg);
        break;
      case 2:
        System.out.print("반려 사유를 입력하세요: ");
        String reason = scanner.nextLine().trim();
        if (reason.isEmpty()) {
          System.out.println("반려 사유는 필수입니다.");
          return;
        }
        String rejectMsg = reservationService.rejectReservation(target.getReservationId(), reason);
        System.out.println(">> " + rejectMsg);
        break;
      case 0:
        return;
      default:
        System.out.println("잘못된 입력입니다.");
    }
  }

  // ─────────────────────────────────────────────────────
  // 예약 강제 취소 흐름
  // 상위 관리자 → 전체 승인 예약 목록
  // 중간 관리자 → 담당 시설/비품 승인 예약 목록만
  // ─────────────────────────────────────────────────────
  private void forceCancelFlow() {
    System.out.println("\n[예약 강제 취소]");

    List<Reservation> list = reservationService.getApprovedReservations();

    if (list.isEmpty()) {
      System.out.println("강제 취소할 예약이 없습니다. (승인된 예약만 강제 취소 가능)");
      return;
    }

    printReservationList(list);

    System.out.print("강제 취소할 예약 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Reservation target = list.get(index - 1);

    System.out.print("강제 취소 사유를 입력하세요: ");
    String reason = scanner.nextLine().trim();
    if (reason.isEmpty()) {
      System.out.println("강제 취소 사유는 필수입니다.");
      return;
    }

    System.out.print("정말 강제 취소하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();
    if (!confirm.equals("Y")) {
      System.out.println("강제 취소가 중단되었습니다.");
      return;
    }

    String msg = reservationService.forceCancelReservation(target.getReservationId(), reason);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 예약 목록 출력 (예약자 이름 포함)
  // ─────────────────────────────────────────────────────
  private void printReservationList(List<Reservation> list) {
    System.out.println("──────────────────────────────────────────────────────────────");
    System.out.printf("%-4s %-8s %-12s %-8s %-6s %-15s %-6s%n",
        "번호", "예약자", "예약날짜", "교시", "구분", "시설/비품명", "상태");
    System.out.println("──────────────────────────────────────────────────────────────");

    for (int i = 0; i < list.size(); i++) {
      Reservation r = list.get(i);
      String targetName = r.getTargetType().equals("FACILITY")
          ? (r.getFacility() != null ? r.getFacility().getName() : "-")
          : (r.getEquipment() != null ? r.getEquipment().getName() : "-");

      System.out.printf("%-4d %-8s %-12s %-8s %-6s %-15s %-6s%n",
          i + 1,
          r.getUser().getName(),
          r.getReservationDate().toString(),
          r.getPeriod().getPeriodName(),
          r.getTargetType().equals("FACILITY") ? "시설" : "비품",
          targetName,
          r.getStatus()
      );
    }
    System.out.println("──────────────────────────────────────────────────────────────");
  }

  // ─────────────────────────────────────────────────────
  // 숫자 입력 헬퍼
  // ─────────────────────────────────────────────────────
  private int readInt() {
    try {
      return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}