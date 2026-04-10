package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.Period;
import com.kimdoolim.service.BlockScheduleService;
import com.kimdoolim.service.FacilityEquipmentService;
import com.kimdoolim.service.ReservationService;
import static com.kimdoolim.common.AppScanner.fit;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BlockScheduleView {

  private final BlockScheduleService service = new BlockScheduleService();
  private final ReservationService reservationService = new ReservationService();
  private final FacilityEquipmentService facilityEquipmentService = new FacilityEquipmentService();
  private final Scanner scanner = AppScanner.getScanner();

  private static final String[] DAY_NAMES = {"", "일", "월", "화", "수", "목", "금", "토"};

  public void blockScheduleMenu() {
    while (true) {
      AppScanner.cls();
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────");
      System.out.println("                    [ 교시별 예약 차단 관리 ]");
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────");
      System.out.println(" 1.차단 목록 조회 || 2.특정 날짜+교시 차단 || 3.반복 요일+교시 차단 || 4.차단 삭제 || 0.뒤로 가기");
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────");
      System.out.print("메뉴 선택: ");

      int choice = readInt();
      switch (choice) {
        case 1: showList(); break;
        case 2: addSpecificFlow(); break;
        case 3: addRepeatFlow(); break;
        case 4: deleteFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 1. 목록 조회
  // ─────────────────────────────────────────────────────
  private void showList() {
    AppScanner.cls();
    List<Map<String, Object>> list = service.getAllBlockSchedules();
    if (list.isEmpty()) {
      System.out.println("등록된 차단 일정이 없습니다.");
      System.out.println(" 0. 뒤로가기");
      System.out.print("선택: ");
      scanner.nextLine();
      return;
    }

    System.out.println("\n[교시별 차단 목록]");
    // ID(4) 유형(8) 날짜/요일(30) 교시(8) 적용대상(12) 사유
    String sep = "─".repeat(78);
    System.out.println(sep);
    System.out.println(
        fit("ID", 4) + " " + fit("유형", 8) + " " +
        fit("날짜/요일", 30) + " " + fit("교시", 8) + " " +
        fit("적용대상", 12) + " " + "사유");
    System.out.println(sep);

    for (Map<String, Object> m : list) {
      String type, dateInfo;
      if (m.get("date") != null) {
        type = "특정날짜";
        dateInfo = m.get("date").toString();
      } else {
        type = "반복요일";
        int day = (Integer) m.get("repeatDay");
        dateInfo = DAY_NAMES[day] + "요일 (" + m.get("repeatStart") + "~" + m.get("repeatEnd") + ")";
      }
      String target = "전체";
      if (m.get("facilityId") != null) target = "시설ID:" + m.get("facilityId");
      else if (m.get("equipmentId") != null) target = "비품ID:" + m.get("equipmentId");

      System.out.println(
          fit(m.get("id").toString(), 4) + " " + fit(type, 8) + " " +
          fit(dateInfo, 30) + " " + fit(m.get("periodName").toString(), 8) + " " +
          fit(target, 12) + " " + m.get("desc"));
    }
    System.out.println(sep);
    System.out.println(" 0. 뒤로가기");
    System.out.print("선택: ");
    scanner.nextLine();
  }

  // ─────────────────────────────────────────────────────
  // 2. 특정 날짜 + 교시 차단
  // ─────────────────────────────────────────────────────
  private void addSpecificFlow() {
    AppScanner.cls();
    System.out.println("\n[특정 날짜 + 교시 차단 등록]");

    System.out.print("날짜 (yyyy-MM-dd): ");
    LocalDate date = parseDate(scanner.nextLine().trim());
    if (date == null) return;

    Period period = selectPeriod();
    if (period == null) return;

    // 상위관리자: 전체 적용 / 중간관리자: 담당 시설/비품 선택
    Long facilityId = null;
    Long equipmentId = null;
    if (Auth.getUserInfo().getPermission() == Permission.MIDDLEADMIN) {
      Long[] ids = selectManagedTarget();
      if (ids == null) return;
      facilityId = ids[0];
      equipmentId = ids[1];
    }

    System.out.print("차단 사유: ");
    String desc = scanner.nextLine().trim();
    if (desc.isEmpty()) { System.out.println("사유를 입력해주세요."); return; }

    System.out.println("\n> " + date + " " + period.getPeriodName() + " 차단 / 사유: " + desc);
    System.out.print("등록하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); return; }

    System.out.println(">> " + service.addSpecificBlock(date, period.getPeriodId(), desc, facilityId, equipmentId));
    System.out.println(" 0. 뒤로가기");
    System.out.print("선택: ");
    scanner.nextLine();
  }

  // ─────────────────────────────────────────────────────
  // 3. 반복 요일 + 교시 차단
  // ─────────────────────────────────────────────────────
  private void addRepeatFlow() {
    AppScanner.cls();
    System.out.println("\n[반복 요일 + 교시 차단 등록]");
    System.out.println(" 1.일 2.월 3.화 4.수 5.목 6.금 7.토");
    System.out.print("요일 선택: ");
    int day = readInt();
    if (day < 1 || day > 7) { System.out.println("잘못된 입력입니다."); return; }

    System.out.print("반복 시작일 (yyyy-MM-dd): ");
    LocalDate startDate = parseDate(scanner.nextLine().trim());
    if (startDate == null) return;

    System.out.print("반복 종료일 (yyyy-MM-dd): ");
    LocalDate endDate = parseDate(scanner.nextLine().trim());
    if (endDate == null) return;

    Period period = selectPeriod();
    if (period == null) return;

    // 상위관리자: 전체 적용 / 중간관리자: 담당 시설/비품 선택
    Long facilityId = null;
    Long equipmentId = null;
    if (Auth.getUserInfo().getPermission() == Permission.MIDDLEADMIN) {
      Long[] ids = selectManagedTarget();
      if (ids == null) return;
      facilityId = ids[0];
      equipmentId = ids[1];
    }

    System.out.print("차단 사유: ");
    String desc = scanner.nextLine().trim();
    if (desc.isEmpty()) { System.out.println("사유를 입력해주세요."); return; }

    System.out.println("\n> 매주 " + DAY_NAMES[day] + "요일 " + period.getPeriodName()
        + " (" + startDate + "~" + endDate + ") / 사유: " + desc);
    System.out.print("등록하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); return; }

    System.out.println(">> " + service.addRepeatBlock(day, startDate, endDate, period.getPeriodId(), desc, facilityId, equipmentId));
    System.out.println(" 0. 뒤로가기");
    System.out.print("선택: ");
    scanner.nextLine();
  }

  // ─────────────────────────────────────────────────────
  // 4. 삭제
  // ─────────────────────────────────────────────────────
  private void deleteFlow() {
    AppScanner.cls();
    List<Map<String, Object>> list = service.getAllBlockSchedules();
    if (list.isEmpty()) {
      System.out.println("등록된 차단 일정이 없습니다.");
      System.out.println(" 0. 뒤로가기");
      System.out.print("선택: ");
      scanner.nextLine();
      return;
    }
    showList();
    System.out.print("삭제할 ID 입력 (0: 뒤로): ");
    int id = readInt();
    if (id == 0) return;

    System.out.print("삭제하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); return; }

    System.out.println(">> " + service.deleteBlockSchedule(id));
    System.out.println(" 0. 뒤로가기");
    System.out.print("선택: ");
    scanner.nextLine();
  }

  // ─────────────────────────────────────────────────────
  // 중간관리자 담당 시설/비품 선택
  // 반환: [facilityId, equipmentId] (선택한 쪽만 값, 나머지 null)
  // ─────────────────────────────────────────────────────
  private Long[] selectManagedTarget() {
    System.out.println("\n차단 적용 대상:");
    System.out.println(" 1. 시설");
    System.out.println(" 2. 비품");
    System.out.print("선택: ");
    int type = readInt();

    if (type == 1) {
      List<Facility> facilities = facilityEquipmentService.getAllFacilities();
      if (facilities.isEmpty()) { System.out.println("담당 시설이 없습니다."); return null; }
      System.out.println("\n── 시설 목록 ──");
      for (int i = 0; i < facilities.size(); i++) {
        System.out.printf(" %d. %s [%s]%n", i + 1, facilities.get(i).getName(), facilities.get(i).getLocation());
      }
      System.out.print("선택: ");
      int idx = readInt();
      if (idx < 1 || idx > facilities.size()) { System.out.println("잘못된 입력입니다."); return null; }
      return new Long[]{facilities.get(idx - 1).getFacilityId(), null};

    } else if (type == 2) {
      List<Equipment> equipments = facilityEquipmentService.getAllEquipments();
      if (equipments.isEmpty()) { System.out.println("담당 비품이 없습니다."); return null; }
      System.out.println("\n── 비품 목록 ──");
      for (int i = 0; i < equipments.size(); i++) {
        System.out.printf(" %d. %s [%s]%n", i + 1, equipments.get(i).getName(), equipments.get(i).getLocation());
      }
      System.out.print("선택: ");
      int idx = readInt();
      if (idx < 1 || idx > equipments.size()) { System.out.println("잘못된 입력입니다."); return null; }
      return new Long[]{null, equipments.get(idx - 1).getEquipmentId()};

    } else {
      System.out.println("잘못된 입력입니다.");
      return null;
    }
  }

  // ─────────────────────────────────────────────────────
  // 교시 선택 헬퍼
  // ─────────────────────────────────────────────────────
  private Period selectPeriod() {
    List<Period> periods = reservationService.getAvailablePeriods();
    System.out.println("\n── 교시 선택 ──");
    for (int i = 0; i < periods.size(); i++) {
      Period p = periods.get(i);
      System.out.printf(" %d. %s (%s~%s)%n", i + 1, p.getPeriodName(), p.getStartTime(), p.getEndTime());
    }
    System.out.print("선택: ");
    int choice = readInt();
    if (choice < 1 || choice > periods.size()) { System.out.println("잘못된 입력입니다."); return null; }
    return periods.get(choice - 1);
  }

  private LocalDate parseDate(String input) {
    try {
      return LocalDate.parse(input);
    } catch (DateTimeParseException e) {
      System.out.println("날짜 형식이 올바르지 않습니다. (예: 2026-04-11)");
      return null;
    }
  }

  private int readInt() {
    try {
      return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}