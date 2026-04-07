package com.kimdoolim.main.view;

import com.kimdoolim.dto.*;
import com.kimdoolim.service.ReservationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ReservationView {

  private final ReservationService reservationService = new ReservationService();
  private final Scanner scanner = new Scanner(System.in);

  // ─────────────────────────────────────────────────────
  // 예약 메인 메뉴
  // MainView에서 "예약" 선택 시 이 메서드 호출
  // ─────────────────────────────────────────────────────
  public void reservationMenu() {
    while (true) {
      System.out.println("\n=============================");
      System.out.println("         예약 메뉴            ");
      System.out.println("=============================");
      System.out.println(" 1. 시설 예약 신청");
      System.out.println(" 2. 비품 예약 신청");
      System.out.println(" 3. 예약 취소");
      System.out.println(" 0. 뒤로 가기");
      System.out.println("=============================");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: facilityReservationFlow(); break;
        case 2: equipmentReservationFlow(); break;
        case 3: cancelReservationFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 시설 예약 신청 흐름
  // 날짜 입력 → 교시 선택 → 시설 선택 → 목적 입력 → 확인 → 신청
  // ─────────────────────────────────────────────────────
  private void facilityReservationFlow() {
    System.out.println("\n[시설 예약 신청]");

    // 1. 날짜 입력
    LocalDate date = inputDate();
    if (date == null) return;

    // 2. 교시 선택
    Period period = selectPeriod();
    if (period == null) return;

    // 3. 시설 선택
    Facility facility = selectFacility();
    if (facility == null) return;

    // 4. 사용 목적 입력
    System.out.print("사용 목적을 입력하세요: ");
    String purpose = scanner.nextLine();

    // 5. 최종 확인
    System.out.println("\n── 예약 정보 확인 ──────────────────");
    System.out.println(" 날짜  : " + date);
    System.out.println(" 교시  : " + period.getPeriodName()
        + " (" + period.getStartTime() + " ~ " + period.getEndTime() + ")");
    System.out.println(" 시설  : " + facility.getName()
        + " [" + facility.getLocation() + "]");
    System.out.println(" 목적  : " + purpose);
    System.out.println("────────────────────────────────────");
    System.out.print("예약을 신청하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("예약이 취소되었습니다.");
      return;
    }

    // 6. Service 호출 → 결과 출력
    String msg = reservationService.requestFacilityReservation(date, period, facility, purpose);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 비품 예약 신청 흐름
  // 날짜 입력 → 교시 선택 → 비품 선택 → 목적 입력 → 확인 → 신청
  // ─────────────────────────────────────────────────────
  private void equipmentReservationFlow() {
    System.out.println("\n[비품 예약 신청]");

    // 1. 날짜 입력
    LocalDate date = inputDate();
    if (date == null) return;

    // 2. 교시 선택
    Period period = selectPeriod();
    if (period == null) return;

    // 3. 비품 선택
    Equipment equipment = selectEquipment();
    if (equipment == null) return;

    // 4. 사용 목적 입력
    System.out.print("사용 목적을 입력하세요: ");
    String purpose = scanner.nextLine();

    // 5. 최종 확인
    System.out.println("\n── 예약 정보 확인 ──────────────────");
    System.out.println(" 날짜  : " + date);
    System.out.println(" 교시  : " + period.getPeriodName()
        + " (" + period.getStartTime() + " ~ " + period.getEndTime() + ")");
    System.out.println(" 비품  : " + equipment.getName()
        + " [" + equipment.getLocation() + "]");
    System.out.println(" 목적  : " + purpose);
    System.out.println("────────────────────────────────────");
    System.out.print("예약을 신청하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("예약이 취소되었습니다.");
      return;
    }

    // 6. Service 호출 → 결과 출력
    String msg = reservationService.requestEquipmentReservation(date, period, equipment, purpose);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 내 예약 목록 출력
  // ─────────────────────────────────────────────────────
  public void showMyReservations() {
    System.out.println("\n[내 예약 목록]");

    List<Reservation> list = reservationService.getMyReservations();

    if (list.isEmpty()) {
      System.out.println("예약 내역이 없습니다.");
      return;
    }

    System.out.println("────────────────────────────────────────────────────────");
    System.out.printf("%-4s %-12s %-8s %-6s %-15s %-6s%n",
        "번호", "예약날짜", "교시", "구분", "시설/비품명", "상태");
    System.out.println("────────────────────────────────────────────────────────");

    for (int i = 0; i < list.size(); i++) {
      Reservation r = list.get(i);

      String targetName = r.getTargetType().equals("FACILITY")
          ? (r.getFacility() != null ? r.getFacility().getName() : "-")
          : (r.getEquipment() != null ? r.getEquipment().getName() : "-");

      System.out.printf("%-4d %-12s %-8s %-6s %-15s %-6s%n",
          i + 1,
          r.getReservationDate().toString(),
          r.getPeriod().getPeriodName(),
          r.getTargetType().equals("FACILITY") ? "시설" : "비품",
          targetName,
          r.getStatus()
      );
    }
    System.out.println("────────────────────────────────────────────────────────");
  }

  // ─────────────────────────────────────────────────────
  // 예약 취소 흐름
  // ─────────────────────────────────────────────────────
  private void cancelReservationFlow() {
    System.out.println("\n[예약 취소]");

    List<Reservation> list = reservationService.getMyReservations();

    if (list.isEmpty()) {
      System.out.println("취소할 예약이 없습니다.");
      return;
    }

    // 목록 보여주기
    showMyReservations();

    System.out.print("취소할 예약 번호 입력 (0: 뒤로): ");
    int index = readInt();
    scanner.nextLine(); // 버퍼 비우기

    if (index == 0) return;

    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Reservation target = list.get(index - 1);

    System.out.print("정말 취소하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("취소가 중단되었습니다.");
      return;
    }

    String msg = reservationService.cancelReservation(target.getReservationId());
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 날짜 입력 (yyyy-MM-dd 형식)
  // ─────────────────────────────────────────────────────
  private LocalDate inputDate() {
    System.out.print("예약 날짜를 입력하세요 (예: 2026-04-10): ");
    String input = scanner.nextLine().trim();

    try {
      return LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } catch (DateTimeParseException e) {
      System.out.println("날짜 형식이 올바르지 않습니다. (예: 2026-04-10)");
      return null;
    }
  }

  // ─────────────────────────────────────────────────────
  // 교시 선택
  // ─────────────────────────────────────────────────────
  private Period selectPeriod() {
    List<Period> periods = reservationService.getAvailablePeriods();

    if (periods.isEmpty()) {
      System.out.println("등록된 교시가 없습니다.");
      return null;
    }

    System.out.println("\n── 교시 선택 ──────────────────────");
    for (int i = 0; i < periods.size(); i++) {
      Period p = periods.get(i);
      System.out.printf(" %d. %s  (%s ~ %s)%n",
          i + 1, p.getPeriodName(), p.getStartTime(), p.getEndTime());
    }
    System.out.println(" 0. 뒤로");
    System.out.print("교시 선택: ");

    int choice = readInt();
    scanner.nextLine(); // 버퍼 비우기

    if (choice == 0 || choice < 1 || choice > periods.size()) return null;
    return periods.get(choice - 1);
  }

  // ─────────────────────────────────────────────────────
  // 시설 선택
  // ─────────────────────────────────────────────────────
  private Facility selectFacility() {
    List<Facility> facilities = reservationService.getAvailableFacilities();

    if (facilities.isEmpty()) {
      System.out.println("예약 가능한 시설이 없습니다.");
      return null;
    }

    System.out.println("\n── 시설 선택 ──────────────────────");
    for (int i = 0; i < facilities.size(); i++) {
      Facility f = facilities.get(i);
      System.out.printf(" %d. %s  [위치: %s]  (수용인원: %d명)%n",
          i + 1, f.getName(), f.getLocation(), f.getMaxCapacity());
    }
    System.out.println(" 0. 뒤로");
    System.out.print("시설 선택: ");

    int choice = readInt();
    scanner.nextLine(); // 버퍼 비우기

    if (choice == 0 || choice < 1 || choice > facilities.size()) return null;
    return facilities.get(choice - 1);
  }

  // ─────────────────────────────────────────────────────
  // 비품 선택
  // ─────────────────────────────────────────────────────
  private Equipment selectEquipment() {
    List<Equipment> equipments = reservationService.getAvailableEquipments();

    if (equipments.isEmpty()) {
      System.out.println("예약 가능한 비품이 없습니다.");
      return null;
    }

    System.out.println("\n── 비품 선택 ──────────────────────");
    for (int i = 0; i < equipments.size(); i++) {
      Equipment e = equipments.get(i);
      System.out.printf(" %d. %s  [위치: %s]%n",
          i + 1, e.getName(), e.getLocation());
    }
    System.out.println(" 0. 뒤로");
    System.out.print("비품 선택: ");

    int choice = readInt();
    scanner.nextLine(); // 버퍼 비우기

    if (choice == 0 || choice < 1 || choice > equipments.size()) return null;
    return equipments.get(choice - 1);
  }

  // ─────────────────────────────────────────────────────
  // 숫자 입력 헬퍼 (잘못 입력 시 -1 반환)
  // ─────────────────────────────────────────────────────
  private int readInt() {
    try {
      return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}