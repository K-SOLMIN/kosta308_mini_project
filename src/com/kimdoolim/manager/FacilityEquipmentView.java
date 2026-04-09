package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.service.FacilityEquipmentService;

import java.util.List;
import java.util.Scanner;

public class FacilityEquipmentView {

  private final FacilityEquipmentService service = new FacilityEquipmentService();
  private final Scanner scanner = AppScanner.getScanner();

  // 상태 선택지
  private static final String[] STATUS_LIST = {"정상", "수리", "점검", "폐기", "분실"};

  // ─────────────────────────────────────────────────────
  // 시설/비품 관리 메인 메뉴
  // ─────────────────────────────────────────────────────
  public void facilityEquipmentMenu() {
    while (true) {
      System.out.println("\n=============================");
      System.out.println("      시설/비품 관리 메뉴      ");
      System.out.println("=============================");
      System.out.println(" 1. 시설 관리");
      System.out.println(" 2. 비품 관리");
      System.out.println(" 3. 교시별 예약 차단 관리");
      System.out.println(" 0. 뒤로 가기");
      System.out.println("=============================");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: facilityMenu(); break;
        case 2: equipmentMenu(); break;
        case 3: new BlockScheduleView().blockScheduleMenu(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 시설 관리 메뉴
  // ─────────────────────────────────────────────────────
  private void facilityMenu() {
    while (true) {
      System.out.println("\n=============================");
      System.out.println("         시설 관리 메뉴        ");
      System.out.println("=============================");
      System.out.println(" 1. 시설 목록 조회");
      System.out.println(" 2. 시설 등록");
      System.out.println(" 3. 시설 상태 수정");
      System.out.println(" 4. 시설 삭제");
      System.out.println(" 0. 뒤로 가기");
      System.out.println("=============================");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: showFacilityList(); break;
        case 2: registerFacilityFlow(); break;
        case 3: updateFacilityStatusFlow(); break;
        case 4: deleteFacilityFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 비품 관리 메뉴
  // ─────────────────────────────────────────────────────
  private void equipmentMenu() {
    while (true) {
      System.out.println("\n=============================");
      System.out.println("         비품 관리 메뉴        ");
      System.out.println("=============================");
      System.out.println(" 1. 비품 목록 조회");
      System.out.println(" 2. 비품 등록");
      System.out.println(" 3. 비품 상태 수정");
      System.out.println(" 4. 비품 삭제");
      System.out.println(" 0. 뒤로 가기");
      System.out.println("=============================");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: showEquipmentList(); break;
        case 2: registerEquipmentFlow(); break;
        case 3: updateEquipmentStatusFlow(); break;
        case 4: deleteEquipmentFlow(); break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 시설 목록 출력
  // ─────────────────────────────────────────────────────
  private void showFacilityList() {
    System.out.println("\n[시설 목록]");

    List<Facility> list = service.getAllFacilities();

    if (list.isEmpty()) {
      System.out.println("등록된 시설이 없습니다.");
      return;
    }

    System.out.println("──────────────────────────────────────────────────────────────");
    System.out.printf("%-4s %-15s %-10s %-8s %-8s%n",
        "번호", "시설명", "위치", "수용인원", "상태");
    System.out.println("──────────────────────────────────────────────────────────────");

    for (int i = 0; i < list.size(); i++) {
      Facility f = list.get(i);
      System.out.printf("%-4d %-15s %-10s %-8d %-8s%n",
          i + 1,
          f.getName(),
          f.getLocation(),
          f.getMaxCapacity(),
          f.getStatus()
      );
    }
    System.out.println("──────────────────────────────────────────────────────────────");
  }

  // ─────────────────────────────────────────────────────
  // 비품 목록 출력
  // ─────────────────────────────────────────────────────
  private void showEquipmentList() {
    System.out.println("\n[비품 목록]");

    List<Equipment> list = service.getAllEquipments();

    if (list.isEmpty()) {
      System.out.println("등록된 비품이 없습니다.");
      return;
    }

    System.out.println("──────────────────────────────────────────────────────────────");
    System.out.printf("%-4s %-15s %-10s %-15s %-8s%n",
        "번호", "비품명", "위치", "시리얼번호", "상태");
    System.out.println("──────────────────────────────────────────────────────────────");

    for (int i = 0; i < list.size(); i++) {
      Equipment e = list.get(i);
      System.out.printf("%-4d %-15s %-10s %-15s %-8s%n",
          i + 1,
          e.getName(),
          e.getLocation(),
          e.getSerialNo(),
          e.getStatus()
      );
    }
    System.out.println("──────────────────────────────────────────────────────────────");
  }

  // ─────────────────────────────────────────────────────
  // 시설 등록 흐름
  // ─────────────────────────────────────────────────────
  private void registerFacilityFlow() {
    System.out.println("\n[시설 등록]");

    System.out.print("시설 이름: ");
    String name = scanner.nextLine().trim();

    System.out.print("위치: ");
    String location = scanner.nextLine().trim();

    System.out.print("최대 수용인원: ");
    int maxCapacity = readInt();

    System.out.println("상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    Facility facility = Facility.builder()
        .name(name)
        .location(location)
        .maxCapacity(maxCapacity)
        .maxReservationUnit("DAY")  // 기본값
        .maxReservationValue(1)      // 기본값
        .status(status)
        .build();

    System.out.println("\n── 시설 정보 확인 ──────────────────");
    System.out.println(" 시설명  : " + name);
    System.out.println(" 위치    : " + location);
    System.out.println(" 수용인원: " + maxCapacity + "명");
    System.out.println(" 상태    : " + status);
    System.out.println("────────────────────────────────────");
    System.out.print("등록하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("등록이 취소되었습니다.");
      return;
    }

    String msg = service.registerFacility(facility);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 비품 등록 흐름
  // ─────────────────────────────────────────────────────
  private void registerEquipmentFlow() {
    System.out.println("\n[비품 등록]");

    System.out.print("비품 이름: ");
    String name = scanner.nextLine().trim();

    System.out.print("위치: ");
    String location = scanner.nextLine().trim();

    System.out.print("시리얼 번호: ");
    String serialNo = scanner.nextLine().trim();

    System.out.println("상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    Equipment equipment = Equipment.builder()
        .name(name)
        .location(location)
        .serialNo(serialNo)
        .status(status)
        .build();

    System.out.println("\n── 비품 정보 확인 ──────────────────");
    System.out.println(" 비품명    : " + name);
    System.out.println(" 위치      : " + location);
    System.out.println(" 시리얼번호: " + serialNo);
    System.out.println(" 상태      : " + status);
    System.out.println("────────────────────────────────────");
    System.out.print("등록하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("등록이 취소되었습니다.");
      return;
    }

    String msg = service.registerEquipment(equipment);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 시설 상태 수정 흐름
  // ─────────────────────────────────────────────────────
  private void updateFacilityStatusFlow() {
    System.out.println("\n[시설 상태 수정]");

    showFacilityList();

    List<Facility> list = service.getAllFacilities();
    if (list.isEmpty()) return;

    System.out.print("수정할 시설 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Facility target = list.get(index - 1);

    System.out.println("새로운 상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    String msg = service.updateFacilityStatus(target.getFacilityId(), status);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 비품 상태 수정 흐름
  // ─────────────────────────────────────────────────────
  private void updateEquipmentStatusFlow() {
    System.out.println("\n[비품 상태 수정]");

    showEquipmentList();

    List<Equipment> list = service.getAllEquipments();
    if (list.isEmpty()) return;

    System.out.print("수정할 비품 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Equipment target = list.get(index - 1);

    System.out.println("새로운 상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    String msg = service.updateEquipmentStatus(target.getEquipmentId(), status);
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 시설 삭제 흐름
  // ─────────────────────────────────────────────────────
  private void deleteFacilityFlow() {
    System.out.println("\n[시설 삭제]");

    showFacilityList();

    List<Facility> list = service.getAllFacilities();
    if (list.isEmpty()) return;

    System.out.print("삭제할 시설 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Facility target = list.get(index - 1);

    System.out.print("정말 [" + target.getName() + "] 을 삭제하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("삭제가 취소되었습니다.");
      return;
    }

    String msg = service.deleteFacility(target.getFacilityId());
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 비품 삭제 흐름
  // ─────────────────────────────────────────────────────
  private void deleteEquipmentFlow() {
    System.out.println("\n[비품 삭제]");

    showEquipmentList();

    List<Equipment> list = service.getAllEquipments();
    if (list.isEmpty()) return;

    System.out.print("삭제할 비품 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      return;
    }

    Equipment target = list.get(index - 1);

    System.out.print("정말 [" + target.getName() + "] 을 삭제하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("삭제가 취소되었습니다.");
      return;
    }

    String msg = service.deleteEquipment(target.getEquipmentId());
    System.out.println(">> " + msg);
  }

  // ─────────────────────────────────────────────────────
  // 상태 선택 헬퍼
  // ─────────────────────────────────────────────────────
  private String selectStatus() {
    for (int i = 0; i < STATUS_LIST.length; i++) {
      System.out.printf(" %d. %s%n", i + 1, STATUS_LIST[i]);
    }
    System.out.println(" 0. 뒤로");
    System.out.print("상태 선택: ");

    int choice = readInt();
    if (choice == 0 || choice < 1 || choice > STATUS_LIST.length) return null;
    return STATUS_LIST[choice - 1];
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