package com.kimdoolim.main.view;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.manager.BlockPeriodManageView;
import com.kimdoolim.manager.FacilityEquipmentView;
import com.kimdoolim.manager.ManagerReservationView;

import java.util.Scanner;

public class MainView {

    private final Scanner scanner = AppScanner.getScanner();

    // ─────────────────────────────────────────────────────
    // 일반 사용자 메뉴
    // ─────────────────────────────────────────────────────
    public void userMainView() {
        while (true) {
            System.out.println("=================================================================");
            System.out.println("                          사용자 메인 메뉴                          ");
            System.out.println("=================================================================");
            System.out.println("  1. 예약하기 || 2. 마이페이지 || 3. 예약 내역 확인 || 0. 종료 ");
            System.out.println("=================================================================");
            System.out.print("메뉴 선택 : ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    System.out.println(">> [예약 하기]로 이동합니다.");
                    new ReservationView().reservationMenu();
                    break;
                case 2:
                    System.out.println(">> [마이페이지]로 이동합니다.");
                    break;
                case 3:
                    System.out.println(">> [예약 내역 확인]으로 이동합니다.");
                    new ReservationView().reservationHistoryMenu();
                    break;
                case 0:
                    System.out.println("[프로그램을 종료합니다.]");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 중간 관리자 메뉴
    // - 예약 관리 (담당 시설/비품만)
    // - 시설/비품 관리
    // - 마이페이지
    // - 예약하기
    // ─────────────────────────────────────────────────────
    public void middleAdminMainView() {
        while (true) {
            System.out.println("========================================================================");
            System.out.println("                           중간 관리자 메인 메뉴                          ");
            System.out.println("========================================================================");
            System.out.println(" 1. 예약 관리 || 2. 시설/비품 관리 || 3. 마이페이지 || 4. 예약하기 || 0. 종료");
            System.out.println("========================================================================");
            System.out.print("메뉴 선택 : ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    System.out.println(">> [예약 관리] 메뉴로 이동합니다.");
                    new ManagerReservationView().managerReservationMenu();
                    break;
                case 2:
                    System.out.println(">> [시설/비품 관리] 메뉴로 이동합니다.");
                    new FacilityEquipmentView().facilityEquipmentMenu();
                    break;
                case 3:
                    System.out.println(">> [마이페이지]로 이동합니다.");
                    break;
                case 4:
                    System.out.println(">> [예약 하기]로 이동합니다.");
                    new ReservationView().reservationMenu();
                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 상위 관리자 메뉴
    // - 예약 관리 (전체)
    // - 시설/비품 관리
    // - 사용자 관리
    // - 마이페이지
    // - 예약하기
    // ─────────────────────────────────────────────────────
    public void adminMainView() {
        while (true) {
            System.out.println("========================================================================");
            System.out.println("                           상위 관리자 메인 메뉴                          ");
            System.out.println("========================================================================");
            System.out.println(" 1. 예약 관리 || 2. 시설/비품 관리 || 3. 사용자 관리 || 4. 마이페이지 || 5. 예약하기  || 6. 제한기간 관리 || 0. 종료");
            System.out.println("========================================================================");
            System.out.print("메뉴 선택 : ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    System.out.println(">> [예약 관리] 메뉴로 이동합니다.");
                    new ManagerReservationView().managerReservationMenu();
                    break;
                case 2:
                    System.out.println(">> [시설/비품 관리] 메뉴로 이동합니다.");
                    new FacilityEquipmentView().facilityEquipmentMenu();
                    break;
                case 3:
                    System.out.println(">> [사용자 관리] 메뉴로 이동합니다.");
                    break;
                case 4:
                    System.out.println(">> [마이페이지]로 이동합니다.");
                    break;
                case 5:
                    System.out.println(">> [예약 하기]로 이동합니다.");
                    new ReservationView().reservationMenu();
                    break;
                case 6:
                    System.out.println(">> [제한기간 관리]로 이동합니다.");
                    new BlockPeriodManageView().blockPeriodManageView();
                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
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