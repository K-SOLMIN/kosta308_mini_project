package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.Period;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.service.BlockScheduleService;
import com.kimdoolim.service.FacilityEquipmentService;
import com.kimdoolim.service.ReservationService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.kimdoolim.common.AppScanner.fit;

public class BlockScheduleView {

  private final BlockScheduleService service = new BlockScheduleService();
  private final ReservationService reservationService = new ReservationService();
  private final FacilityEquipmentService facilityEquipmentService = new FacilityEquipmentService();
  private final Scanner scanner = AppScanner.getScanner();

  private static final String[] DAY_NAMES = {"", "일", "월", "화", "수", "목", "금", "토"};
  private static final String SEP = "─".repeat(70);

  public void blockScheduleMenu() {
    while (true) {
      AppScanner.cls();
      System.out.println("─".repeat(85));
      System.out.println("                            [ 교시별 예약 차단 관리 ]");
      System.out.println("─".repeat(85));
      System.out.println(" 1.차단 목록 조회 || 2.특정 날짜+교시 차단 || 3.반복 요일+교시 차단");
      System.out.println(" 4.차단 적용 || 5.적용 대상 조회 || 6.차단 삭제 || 0.뒤로 가기");
      System.out.println("─".repeat(85));
      System.out.print("메뉴 선택: ");

      int choice = readInt();
      switch (choice) {
        case 1: showList(); waitBack(); break;
        case 2: addSpecificFlow(); break;
        case 3: addRepeatFlow(); break;
        case 4: applyTargetFlow(); break;
        case 5: showTargetsFlow(); break;
        case 6: deleteFlow(); break;
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
    if (list.isEmpty()) { System.out.println("등록된 차단 일정이 없습니다."); return; }

    System.out.println("\n[교시별 차단 목록]");
    System.out.println(SEP);
    System.out.println(
        fit("ID", 4) + " " + fit("유형", 8) + " " +
        fit("날짜/요일", 28) + " " + fit("교시", 8) + " " + "사유");
    System.out.println(SEP);

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
      System.out.println(
          fit(m.get("id").toString(), 4) + " " + fit(type, 8) + " " +
          fit(dateInfo, 28) + " " + fit(m.get("periodName").toString(), 8) + " " +
          m.get("desc"));
    }
    System.out.println(SEP);
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

    System.out.print("차단 사유: ");
    String desc = scanner.nextLine().trim();
    if (desc.isEmpty()) { System.out.println("사유를 입력해주세요."); waitBack(); return; }

    System.out.println("\n> " + date + " " + period.getPeriodName() + " 차단 / 사유: " + desc);
    System.out.print("등록하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); waitBack(); return; }

    long generatedId = service.addSpecificBlock(date, period.getPeriodId(), desc);
    if (generatedId > 0) {
      System.out.println(">> 차단 일정이 등록되었습니다. (ID: " + generatedId + ")");
      System.out.println("   ※ [4.차단 적용] 메뉴에서 적용 대상 시설/비품을 설정해주세요.");
    } else {
      System.out.println(">> 등록 중 오류가 발생했습니다.");
    }
    waitBack();
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
    if (day < 1 || day > 7) { System.out.println("잘못된 입력입니다."); waitBack(); return; }

    System.out.print("반복 시작일 (yyyy-MM-dd): ");
    LocalDate startDate = parseDate(scanner.nextLine().trim());
    if (startDate == null) return;

    System.out.print("반복 종료일 (yyyy-MM-dd): ");
    LocalDate endDate = parseDate(scanner.nextLine().trim());
    if (endDate == null) return;

    Period period = selectPeriod();
    if (period == null) return;

    System.out.print("차단 사유: ");
    String desc = scanner.nextLine().trim();
    if (desc.isEmpty()) { System.out.println("사유를 입력해주세요."); waitBack(); return; }

    System.out.println("\n> 매주 " + DAY_NAMES[day] + "요일 " + period.getPeriodName()
        + " (" + startDate + "~" + endDate + ") / 사유: " + desc);
    System.out.print("등록하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); waitBack(); return; }

    long generatedId = service.addRepeatBlock(day, startDate, endDate, period.getPeriodId(), desc);
    if (generatedId > 0) {
      System.out.println(">> 차단 일정이 등록되었습니다. (ID: " + generatedId + ")");
      System.out.println("   ※ [4.차단 적용] 메뉴에서 적용 대상 시설/비품을 설정해주세요.");
    } else {
      System.out.println(">> 등록 중 오류가 발생했습니다.");
    }
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 4. 차단 적용 (대상 시설/비품 설정)
  // ─────────────────────────────────────────────────────
  private void applyTargetFlow() {
    AppScanner.cls();
    System.out.println("\n[차단 적용]");
    showList();
    if (service.getAllBlockSchedules().isEmpty()) return;

    System.out.print("적용할 차단 일정 ID (0: 뒤로): ");
    int id = readInt();
    if (id == 0) return;

    System.out.println(SEP);
    if (Auth.getUserInfo().getPermission() == Permission.ADMIN) {
      System.out.println(" 1. 전체 시설/비품 적용");
      System.out.println(" 2. 특정 시설 적용");
      System.out.println(" 3. 특정 비품 적용");
      System.out.println(SEP);
      System.out.print("선택: ");
      int type = readInt();

      if (type == 1) {
        System.out.print("전체 시설/비품에 적용하시겠습니까? (Y/N): ");
        if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); waitBack(); return; }
        System.out.println(">> " + service.applyToAll(id));

      } else if (type == 2) {
        BlockPeriodController ctrl = BlockPeriodController.getBlockPeriodController();
        List<Map<String, Object>> facilities = ctrl.getAllFacilities();
        System.out.println(SEP);
        System.out.println(fit("ID", 4) + "  " + fit("시설명", 20) + "  " + "위치");
        System.out.println(SEP);
        for (Map<String, Object> f : facilities)
          System.out.println(fit(f.get("id").toString(), 4) + "  " + fit((String) f.get("name"), 20) + "  " + f.get("location"));
        System.out.println(SEP);
        System.out.print("시설 ID: ");
        long facilityId = readLong();
        System.out.println(">> " + service.applyToTarget(id, facilityId, null));

      } else if (type == 3) {
        BlockPeriodController ctrl = BlockPeriodController.getBlockPeriodController();
        List<Map<String, Object>> equipments = ctrl.getAllEquipments();
        System.out.println(SEP);
        System.out.println(fit("ID", 4) + "  " + fit("비품명", 20) + "  " + "위치");
        System.out.println(SEP);
        for (Map<String, Object> e : equipments)
          System.out.println(fit(e.get("id").toString(), 4) + "  " + fit((String) e.get("name"), 20) + "  " + e.get("location"));
        System.out.println(SEP);
        System.out.print("비품 ID: ");
        long equipmentId = readLong();
        System.out.println(">> " + service.applyToTarget(id, null, equipmentId));

      } else {
        System.out.println("잘못된 입력입니다.");
      }

    } else {
      // 중간관리자: 담당 시설/비품만 선택
      Long[] ids = selectManagedTarget();
      if (ids == null) return;
      System.out.println(">> " + service.applyToTarget(id, ids[0], ids[1]));
    }
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 5. 적용 대상 조회
  // ─────────────────────────────────────────────────────
  @SuppressWarnings("unchecked")
  private void showTargetsFlow() {
    AppScanner.cls();
    System.out.println("\n[차단 적용 대상 조회]");
    List<Map<String, Object>> list = service.getAllBlockSchedules();

    System.out.println(SEP);
    if (list.isEmpty()) {
      System.out.println("  등록된 차단 일정이 없습니다.");
      System.out.println(SEP);
      waitBack();
      return;
    }

    System.out.println(
        fit("ID", 4) + " " + fit("유형", 8) + " " +
        fit("날짜/요일", 28) + " " + fit("교시", 8) + " " + "사유");
    System.out.println(SEP);

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
      System.out.println(
          fit(m.get("id").toString(), 4) + " " + fit(type, 8) + " " +
          fit(dateInfo, 28) + " " + fit(m.get("periodName").toString(), 8) + " " +
          m.get("desc"));

      long scheduleId = (long) m.get("id");
      Map<String, Object> details = service.getBlockDetails(scheduleId);
      boolean isAll = (boolean) details.get("isAll");
      List<String> facilities = (List<String>) details.get("facilities");
      List<String> equipments = (List<String>) details.get("equipments");

      if (isAll) {
        System.out.println("      └ 전체 시설/비품 적용");
      } else if (facilities.isEmpty() && equipments.isEmpty()) {
        System.out.println("      └ 적용된 시설/비품 없음");
      } else {
        boolean hasBoth = !facilities.isEmpty() && !equipments.isEmpty();
        if (!facilities.isEmpty())
          System.out.println("      " + (hasBoth ? "├" : "└") + " [시설] " + String.join(" / ", facilities));
        if (!equipments.isEmpty())
          System.out.println("      └ [비품] " + String.join(" / ", equipments));
      }
      System.out.println(SEP);
    }
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 6. 삭제
  // ─────────────────────────────────────────────────────
  private void deleteFlow() {
    AppScanner.cls();
    List<Map<String, Object>> checkList = service.getAllBlockSchedules();
    if (checkList.isEmpty()) {
      System.out.println("등록된 차단 일정이 없습니다.");
      waitBack();
      return;
    }
    showList();
    System.out.print("삭제할 ID 입력 (0: 뒤로): ");
    int id = readInt();
    if (id == 0) return;

    System.out.print("삭제하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); waitBack(); return; }

    System.out.println(">> " + service.deleteBlockSchedule(id));
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 중간관리자 담당 시설/비품 선택
  // ─────────────────────────────────────────────────────
  private Long[] selectManagedTarget() {
    System.out.println("\n차단 적용 대상:");
    System.out.println(" 1. 시설");
    System.out.println(" 2. 비품");
    System.out.print("선택: ");
    int type = readInt();

    if (type == 1) {
      List<Facility> facilities = facilityEquipmentService.getManagedFacilities();
      if (facilities.isEmpty()) { System.out.println("담당 시설이 없습니다."); waitBack(); return null; }
      System.out.println("\n── 시설 목록 ──");
      for (int i = 0; i < facilities.size(); i++)
        System.out.printf(" %d. %s [%s]%n", i + 1, facilities.get(i).getName(), facilities.get(i).getLocation());
      System.out.print("선택: ");
      int idx = readInt();
      if (idx < 1 || idx > facilities.size()) { System.out.println("잘못된 입력입니다."); waitBack(); return null; }
      return new Long[]{facilities.get(idx - 1).getFacilityId(), null};

    } else if (type == 2) {
      List<Equipment> equipments = facilityEquipmentService.getManagedEquipments();
      if (equipments.isEmpty()) { System.out.println("담당 비품이 없습니다."); waitBack(); return null; }
      System.out.println("\n── 비품 목록 ──");
      for (int i = 0; i < equipments.size(); i++)
        System.out.printf(" %d. %s [%s]%n", i + 1, equipments.get(i).getName(), equipments.get(i).getLocation());
      System.out.print("선택: ");
      int idx = readInt();
      if (idx < 1 || idx > equipments.size()) { System.out.println("잘못된 입력입니다."); waitBack(); return null; }
      return new Long[]{null, equipments.get(idx - 1).getEquipmentId()};

    } else {
      System.out.println("잘못된 입력입니다.");
      waitBack();
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
    if (choice < 1 || choice > periods.size()) { System.out.println("잘못된 입력입니다."); waitBack(); return null; }
    return periods.get(choice - 1);
  }

  private void waitBack() {
    while (true) {
      System.out.println(" 0. 뒤로가기");
      System.out.print("선택: ");
      if ("0".equals(scanner.nextLine().trim())) return;
    }
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

  private long readLong() {
    try {
      return Long.parseLong(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
