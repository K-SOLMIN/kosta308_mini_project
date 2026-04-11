package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import static com.kimdoolim.common.AppScanner.fit;
import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.EquipmentDetail;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.service.FacilityEquipmentService;
import com.kimdoolim.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FacilityEquipmentView {

  private final FacilityEquipmentService service = new FacilityEquipmentService();
  private final UserService userService = new UserService();
  private final Scanner scanner = AppScanner.getScanner();

  // 상태 선택지
  private static final String[] STATUS_LIST = {"정상", "수리", "점검", "폐기", "분실"};

  // ─────────────────────────────────────────────────────
  // 시설/비품 관리 메인 메뉴
  // ─────────────────────────────────────────────────────
  public void facilityEquipmentMenu() {
    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    while (true) {
      AppScanner.cls();
      System.out.println("──────────────────────────────────────────────────────────────────");
      System.out.println("                       [ 시설/비품 관리 ]");
      System.out.println("──────────────────────────────────────────────────────────────────");
      if (isAdmin) {
        System.out.println(" 1.시설 관리 || 2.비품 관리 || 3.교시별 예약 차단 관리 || 0.뒤로 가기");
      } else {
        System.out.println(" 1.시설 관리 || 2.비품 관리 || 0.뒤로 가기");
      }
      System.out.println("──────────────────────────────────────────────────────────────────");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: facilityMenu(); break;
        case 2: equipmentMenu(); break;
        case 3:
          if (isAdmin) new BlockScheduleView().blockScheduleMenu();
          else System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
          break;
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
      AppScanner.cls();
      System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.println("                         [ 시설 관리 ]");
      System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
      if (Auth.getUserInfo().getPermission() == Permission.ADMIN) {
        System.out.println(" 1.시설 목록 조회 || 2.시설 등록 || 3.시설 상태 수정 || 4.시설 삭제 || 5.담당자 재배정 || 0.뒤로 가기");
      } else {
        System.out.println(" 1.시설 목록 조회 || 2.시설 등록 || 3.시설 상태 수정 || 4.시설 삭제 || 0.뒤로 가기");
      }
      System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: showFacilityList(); break;
        case 2: registerFacilityFlow(); break;
        case 3: updateFacilityStatusFlow(); break;
        case 4: deleteFacilityFlow(); break;
        case 5:
          if (Auth.getUserInfo().getPermission() == Permission.ADMIN)
            reassignFacilityManagerFlow();
          break;
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
      AppScanner.cls();
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.println("                         [ 비품 관리 ]");
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────");
      if (Auth.getUserInfo().getPermission() == Permission.ADMIN) {
        System.out.println(" 1.비품 목록 조회 || 2.비품 등록 || 3.비품 상태 수정 || 4.비품 삭제 || 5.담당자 재배정 || 0.뒤로 가기");
      } else {
        System.out.println(" 1.비품 목록 조회 || 2.비품 등록 || 3.비품 상태 수정 || 4.비품 삭제 || 0.뒤로 가기");
      }
      System.out.println("───────────────────────────────────────────────────────────────────────────────────────────────");
      System.out.print("메뉴 선택: ");

      int choice = readInt();

      switch (choice) {
        case 1: showEquipmentList(); break;
        case 2: registerEquipmentFlow(); break;
        case 3: updateEquipmentStatusFlow(); break;
        case 4: deleteEquipmentFlow(); break;
        case 5:
          if (Auth.getUserInfo().getPermission() == Permission.ADMIN)
            reassignEquipmentManagerFlow();
          break;
        case 0: return;
        default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
      }
    }
  }

  // ─────────────────────────────────────────────────────
  // 시설 목록 출력
  // 상위관리자 → 전체 + 담당자 표시
  // 중간관리자 → 본인 담당만
  // ─────────────────────────────────────────────────────
  private void showFacilityList() {
    AppScanner.cls();
    System.out.println("\n[시설 목록]");
    printFacilityTable();
    waitBack();
  }

  private void printFacilityTable() {
    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    List<Facility> list = isAdmin ? service.getAllFacilities() : service.getManagedFacilities();
    if (list.isEmpty()) { System.out.println("등록된 시설이 없습니다."); return; }

    String div = isAdmin ? "─".repeat(63) : "─".repeat(52);
    System.out.println(div);
    if (isAdmin) {
      System.out.println(fit("번호", 4) + " " + fit("시설명", 16) + " " +
          fit("위치", 12) + " " + fit("수용인원", 8) + " " + fit("상태", 8) + " " + fit("담당자", 10));
    } else {
      System.out.println(fit("번호", 4) + " " + fit("시설명", 16) + " " +
          fit("위치", 12) + " " + fit("수용인원", 8) + " " + fit("상태", 8));
    }
    System.out.println(div);
    for (int i = 0; i < list.size(); i++) {
      Facility f = list.get(i);
      if (isAdmin) {
        String managerName = (f.getUser() != null) ? f.getUser().getName() : "담당자 없음";
        System.out.println(fit(String.valueOf(i + 1), 4) + " " + fit(f.getName(), 16) + " " +
            fit(f.getLocation(), 12) + " " + fit(f.getMaxCapacity() + "명", 8) + " " +
            fit(f.getStatus(), 8) + " " + fit(managerName, 10));
      } else {
        System.out.println(fit(String.valueOf(i + 1), 4) + " " + fit(f.getName(), 16) + " " +
            fit(f.getLocation(), 12) + " " + fit(f.getMaxCapacity() + "명", 8) + " " +
            fit(f.getStatus(), 8));
      }
    }
    System.out.println(div);
  }

  // ─────────────────────────────────────────────────────
  // 비품 목록 출력
  // 상위관리자 → 전체 + 담당자 표시
  // 중간관리자 → 본인 담당만
  // ─────────────────────────────────────────────────────
  private void showEquipmentList() {
    AppScanner.cls();
    System.out.println("\n[비품 목록]");
    printEquipmentTable();
    waitBack();
  }

  private void printEquipmentTable() {
    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    List<Equipment> list = isAdmin ? service.getAllEquipments() : service.getManagedEquipments();
    if (list.isEmpty()) { System.out.println("등록된 비품이 없습니다."); return; }

    String div = isAdmin ? "─".repeat(93) : "─".repeat(82);
    System.out.println(div);
    if (isAdmin) {
      System.out.println(fit("번호", 4) + " " + fit("비품명", 14) + " " +
          fit("위치", 12) + " " + fit("수량", 6) + " " +
          fit("시리얼(기본)", 18) + " " + fit("상태", 8) + " " +
          fit("낱개현황", 16) + " " + fit("담당자", 10));
    } else {
      System.out.println(fit("번호", 4) + " " + fit("비품명", 14) + " " +
          fit("위치", 12) + " " + fit("수량", 6) + " " +
          fit("시리얼(기본)", 18) + " " + fit("상태", 8) + " " + fit("낱개현황", 16));
    }
    System.out.println(div);
    for (int i = 0; i < list.size(); i++) {
      Equipment e = list.get(i);
      String qtyStr     = e.getQuantity() > 0 ? e.getQuantity() + "개" : "-";
      String summaryStr = (e.getStatusSummary() != null && !e.getStatusSummary().isEmpty())
          ? e.getStatusSummary() : "-";
      if (isAdmin) {
        String managerName = (e.getUser() != null) ? e.getUser().getName() : "담당자 없음";
        System.out.println(fit(String.valueOf(i + 1), 4) + " " + fit(e.getName(), 14) + " " +
            fit(e.getLocation(), 12) + " " + fit(qtyStr, 6) + " " +
            fit(e.getSerialNo(), 18) + " " + fit(e.getStatus(), 8) + " " +
            fit(summaryStr, 16) + " " + fit(managerName, 10));
      } else {
        System.out.println(fit(String.valueOf(i + 1), 4) + " " + fit(e.getName(), 14) + " " +
            fit(e.getLocation(), 12) + " " + fit(qtyStr, 6) + " " +
            fit(e.getSerialNo(), 18) + " " + fit(e.getStatus(), 8) + " " +
            fit(summaryStr, 16));
      }
    }
    System.out.println(div);
  }

  // ─────────────────────────────────────────────────────
  // 시설 등록 흐름
  // ─────────────────────────────────────────────────────
  private void registerFacilityFlow() {
    AppScanner.cls();
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
        .maxReservationUnit("DAY")
        .maxReservationValue(1)
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
      waitBack();
      return;
    }

    String msg = service.registerFacility(facility);
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 비품 등록 흐름
  //  - 수량 1개    : 시리얼번호 직접 입력 → 상태 1회 선택
  //  - 수량 2개 이상 : 접두사 입력 후 자동 시리얼 생성,
  //                   낱개마다 상태 개별 선택
  // ─────────────────────────────────────────────────────
  private void registerEquipmentFlow() {
    AppScanner.cls();
    System.out.println("\n[비품 등록]");

    System.out.print("비품 이름: ");
    String name = scanner.nextLine().trim();

    System.out.print("위치: ");
    String location = scanner.nextLine().trim();

    System.out.print("수량: ");
    int quantity = readInt();
    if (quantity < 1) {
      System.out.println("수량은 1 이상이어야 합니다.");
      waitBack();
      return;
    }

    List<EquipmentDetail> details = new ArrayList<>();
    String serialInput;

    if (quantity == 1) {
      // ── 단일 품목: 시리얼 직접 입력 + 상태 1회 선택 ──
      do {
        System.out.print("시리얼 번호: ");
        serialInput = scanner.nextLine().trim();
        if (serialInput.isEmpty()) System.out.println("시리얼 번호를 입력해주세요.");
      } while (serialInput.isEmpty());

      System.out.println("상태 선택:");
      String status = selectStatus("취소하기");
      if (status == null) {
        System.out.println("등록이 취소되었습니다.");
        waitBack();
        return;
      }

      details.add(EquipmentDetail.builder()
          .serialNo(serialInput)
          .status(status)
          .build());

    } else {
      // ── 복수 품목: 접두사 입력 → 자동 생성 → 낱개별 상태 선택 ──
      do {
        System.out.print("시리얼 번호를 입력해주세요 (예: Seoul-Tablet): ");
        serialInput = scanner.nextLine().trim();
        if (serialInput.isEmpty()) System.out.println("시리얼 번호를 입력해주세요 (예: Seoul-Tablet): ");
      } while (serialInput.isEmpty());

      int digits = Math.max(String.valueOf(quantity).length(), 3);
      String fmt = "%s-%0" + digits + "d";
      System.out.println("  자동 생성 예시: "
          + String.format(fmt, serialInput, 1)
          + " ~ "
          + String.format(fmt, serialInput, quantity));

      System.out.println("\n── 낱개별 상태 입력 (" + quantity + "개) ──");
      for (int i = 1; i <= quantity; i++) {
        String serial = String.format(fmt, serialInput, i);
        System.out.println("[" + serial + "] 상태 선택:");
        String status = selectStatus("취소하기");
        if (status == null) {
          System.out.println("등록이 취소되었습니다.");
          waitBack();
          return;
        }
        details.add(EquipmentDetail.builder()
            .serialNo(serial)
            .status(status)
            .build());
      }
    }

    // 세트의 대표 상태 = 첫 번째 낱개 상태
    String representativeStatus = details.get(0).getStatus();

    Equipment equipment = Equipment.builder()
        .name(name)
        .location(location)
        .serialNo(serialInput)
        .status(representativeStatus)
        .build();

    // ── 등록 확인 요약 ──
    System.out.println("\n── 비품 정보 확인 ──────────────────");
    System.out.println(" 비품명    : " + name);
    System.out.println(" 위치      : " + location);
    System.out.println(" 수량      : " + quantity + "개");
    if (quantity == 1) {
      System.out.println(" 시리얼번호: " + details.get(0).getSerialNo());
      System.out.println(" 상태      : " + representativeStatus);
    } else {
      System.out.println(" 시리얼번호: "
          + details.get(0).getSerialNo()
          + " ~ "
          + details.get(details.size() - 1).getSerialNo());
      System.out.println("  ┌ 낱개 상태 목록:");
      for (EquipmentDetail d : details) {
        System.out.println("  │ " + d.getSerialNo() + "  →  " + d.getStatus());
      }
      System.out.println("  └──");
    }
    System.out.println("────────────────────────────────────");
    System.out.print("등록하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("등록이 취소되었습니다.");
      waitBack();
      return;
    }

    String msg = service.registerEquipmentWithDetails(equipment, details);
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 시설 상태 수정 흐름
  // ─────────────────────────────────────────────────────
  private void updateFacilityStatusFlow() {
    AppScanner.cls();
    System.out.println("\n[시설 상태 수정]");

    printFacilityTable();

    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    List<Facility> list = isAdmin ? service.getAllFacilities() : service.getManagedFacilities();
    if (list.isEmpty()) { waitBack(); return; }

    System.out.print("수정할 시설 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      waitBack();
      return;
    }

    Facility target = list.get(index - 1);

    System.out.println("새로운 상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    String msg = service.updateFacilityStatus(target.getFacilityId(), status);
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 비품 상태 수정 흐름
  //  - 낱개가 없는 비품(quantity==0) : 기존대로 세트 상태만 변경
  //  - 낱개가 있는 비품(quantity>0)  : 세트 전체 or 낱개 개별 선택
  // ─────────────────────────────────────────────────────
  private void updateEquipmentStatusFlow() {
    AppScanner.cls();
    System.out.println("\n[비품 상태 수정]");

    printEquipmentTable();

    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    List<Equipment> list = isAdmin ? service.getAllEquipments() : service.getManagedEquipments();
    if (list.isEmpty()) { waitBack(); return; }

    System.out.print("수정할 비품 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      waitBack();
      return;
    }

    Equipment target = list.get(index - 1);

    // 낱개가 있는 비품이면 세트 전체 vs 낱개 개별 선택
    if (target.getQuantity() > 0) {
      System.out.println("\n── 상태 수정 방식 선택 ──────────────");
      System.out.println(" 1. 세트 전체 상태 변경");
      System.out.println(" 2. 낱개 개별 상태 변경 (시리얼번호 선택)");
      System.out.println(" 0. 뒤로");
      System.out.println("────────────────────────────────────");
      System.out.print("선택: ");
      int mode = readInt();

      if (mode == 0) return;
      if (mode == 2) {
        updateEquipmentDetailStatusFlow(target);
        return;
      }
      if (mode != 1) {
        System.out.println("잘못된 입력입니다.");
        waitBack();
        return;
      }
    }

    // 세트 전체 상태 변경 (기존 로직 그대로)
    System.out.println("새로운 상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    String msg = service.updateEquipmentStatus(target.getEquipmentId(), status);
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 비품 낱개 개별 상태 수정 흐름
  // ─────────────────────────────────────────────────────
  private void updateEquipmentDetailStatusFlow(Equipment equipment) {
    List<EquipmentDetail> details = service.getEquipmentDetails(equipment.getEquipmentId());

    if (details.isEmpty()) {
      System.out.println("등록된 낱개 정보가 없습니다.");
      waitBack();
      return;
    }

    System.out.println("\n── [" + equipment.getName() + "] 낱개 목록 ──────────────────");
    String detailSep = "─".repeat(44);
    System.out.println(fit("번호", 4) + " " + fit("시리얼번호", 25) + " " + fit("현재상태", 8));
    System.out.println(detailSep);
    for (int i = 0; i < details.size(); i++) {
      EquipmentDetail d = details.get(i);
      System.out.println(fit(String.valueOf(i + 1), 4) + " " + fit(d.getSerialNo(), 25) + " " + fit(d.getStatus(), 8));
    }
    System.out.println(detailSep);

    System.out.print("상태를 변경할 낱개 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > details.size()) {
      System.out.println("잘못된 번호입니다.");
      waitBack();
      return;
    }

    EquipmentDetail target = details.get(index - 1);

    System.out.println("[" + target.getSerialNo() + "] 새로운 상태 선택:");
    String status = selectStatus();
    if (status == null) return;

    String msg = service.updateEquipmentDetailStatus(target.getEquipmentDetailId(), status);
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 시설 삭제 흐름
  // ─────────────────────────────────────────────────────
  private void deleteFacilityFlow() {
    AppScanner.cls();
    System.out.println("\n[시설 삭제]");

    printFacilityTable();

    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    List<Facility> list = isAdmin ? service.getAllFacilities() : service.getManagedFacilities();
    if (list.isEmpty()) { waitBack(); return; }

    System.out.print("삭제할 시설 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      waitBack();
      return;
    }

    Facility target = list.get(index - 1);

    System.out.print("정말 [" + target.getName() + "] 을 삭제하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("삭제가 취소되었습니다.");
      waitBack();
      return;
    }

    String msg = service.deleteFacility(target.getFacilityId());
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 비품 삭제 흐름
  // ─────────────────────────────────────────────────────
  private void deleteEquipmentFlow() {
    AppScanner.cls();
    System.out.println("\n[비품 삭제]");

    printEquipmentTable();

    boolean isAdmin = Auth.getUserInfo().getPermission() == Permission.ADMIN;
    List<Equipment> list = isAdmin ? service.getAllEquipments() : service.getManagedEquipments();
    if (list.isEmpty()) { waitBack(); return; }

    System.out.print("삭제할 비품 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > list.size()) {
      System.out.println("잘못된 번호입니다.");
      waitBack();
      return;
    }

    Equipment target = list.get(index - 1);

    System.out.print("정말 [" + target.getName() + "] 을 삭제하시겠습니까? (Y/N): ");
    String confirm = scanner.nextLine().trim().toUpperCase();

    if (!confirm.equals("Y")) {
      System.out.println("삭제가 취소되었습니다.");
      waitBack();
      return;
    }

    String msg = service.deleteEquipment(target.getEquipmentId());
    System.out.println(">> " + msg);
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 시설 담당자 재배정 흐름 (상위관리자 전용)
  // ─────────────────────────────────────────────────────
  private void reassignFacilityManagerFlow() {
    AppScanner.cls();
    System.out.println("\n[시설 담당자 재배정]");

    printFacilityTable();

    List<Facility> facilities = service.getAllFacilities();
    if (facilities.isEmpty()) { waitBack(); return; }

    System.out.print("재배정할 시설 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > facilities.size()) { System.out.println("잘못된 번호입니다."); waitBack(); return; }

    Facility target = facilities.get(index - 1);

    List<User> managers = userService.getMiddleAdmins();
    if (managers.isEmpty()) { System.out.println("배정 가능한 중간관리자가 없습니다."); waitBack(); return; }

    System.out.println("\n── 중간관리자 목록 ──");
    for (int i = 0; i < managers.size(); i++) {
      System.out.printf(" %d. %s (%s)%n", i + 1, managers.get(i).getName(), managers.get(i).getId());
    }
    System.out.println(" 0. 담당자 없음으로 변경");
    System.out.print("선택: ");
    int mIndex = readInt();

    Integer newManagerId = null;
    if (mIndex > 0 && mIndex <= managers.size()) {
      newManagerId = managers.get(mIndex - 1).getUserId();
    } else if (mIndex != 0) {
      System.out.println("잘못된 번호입니다."); waitBack(); return;
    }

    System.out.print("변경하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); waitBack(); return; }

    System.out.println(">> " + service.updateFacilityManager(target.getFacilityId(), newManagerId));
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 비품 담당자 재배정 흐름 (상위관리자 전용)
  // ─────────────────────────────────────────────────────
  private void reassignEquipmentManagerFlow() {
    AppScanner.cls();
    System.out.println("\n[비품 담당자 재배정]");

    printEquipmentTable();

    List<Equipment> equipments = service.getAllEquipments();
    if (equipments.isEmpty()) { waitBack(); return; }

    System.out.print("재배정할 비품 번호 선택 (0: 뒤로): ");
    int index = readInt();
    if (index == 0) return;
    if (index < 1 || index > equipments.size()) { System.out.println("잘못된 번호입니다."); waitBack(); return; }

    Equipment target = equipments.get(index - 1);

    List<User> managers = userService.getMiddleAdmins();
    if (managers.isEmpty()) { System.out.println("배정 가능한 중간관리자가 없습니다."); waitBack(); return; }

    System.out.println("\n── 중간관리자 목록 ──");
    for (int i = 0; i < managers.size(); i++) {
      System.out.printf(" %d. %s (%s)%n", i + 1, managers.get(i).getName(), managers.get(i).getId());
    }
    System.out.println(" 0. 담당자 없음으로 변경");
    System.out.print("선택: ");
    int mIndex = readInt();

    Integer newManagerId = null;
    if (mIndex > 0 && mIndex <= managers.size()) {
      newManagerId = managers.get(mIndex - 1).getUserId();
    } else if (mIndex != 0) {
      System.out.println("잘못된 번호입니다."); waitBack(); return;
    }

    System.out.print("변경하시겠습니까? (Y/N): ");
    if (!scanner.nextLine().trim().toUpperCase().equals("Y")) { System.out.println("취소되었습니다."); waitBack(); return; }

    System.out.println(">> " + service.updateEquipmentManager(target.getEquipmentId(), newManagerId));
    waitBack();
  }

  // ─────────────────────────────────────────────────────
  // 상태 선택 헬퍼
  // ─────────────────────────────────────────────────────
  private String selectStatus() {
    return selectStatus("뒤로");
  }

  private String selectStatus(String cancelLabel) {
    while (true) {
      for (int i = 0; i < STATUS_LIST.length; i++) {
        System.out.printf(" %d. %s%n", i + 1, STATUS_LIST[i]);
      }
      System.out.println(" 0. " + cancelLabel);
      System.out.print("상태 선택: ");

      int choice = readInt();
      if (choice == 0) return null;
      if (choice >= 1 && choice <= STATUS_LIST.length) return STATUS_LIST[choice - 1];
      System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
    }
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