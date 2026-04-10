package com.kimdoolim.main.view;

import com.kimdoolim.alarm.AlarmService;
import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import com.kimdoolim.main.ClientMain;
import com.kimdoolim.manager.BlockPeriodManageView;
import com.kimdoolim.manager.FacilityEquipmentView;
import com.kimdoolim.manager.ManagerReservationView;

import java.io.IOException;
import java.util.Scanner;

public class MainView {

    private final Scanner scanner = AppScanner.getScanner();
    private final AlarmService alarmService = AlarmService.getAlarmService();

    // ─────────────────────────────────────────────────────
    // 비활성 사용자 메뉴 (휴직/전근)
    // 예약 내역 조회 + 마이페이지만 허용
    // ─────────────────────────────────────────────────────
    public void restrictedUserView() {
        while (true) {
            System.out.println("=================================================================");
            System.out.println("                     제한된 메뉴 (비활성 계정)                     ");
            System.out.println("=================================================================");
            System.out.println("  1. 예약 내역 확인 || 2. 마이페이지 || 0. 종료 ");
            System.out.println("=================================================================");
            System.out.print("메뉴 선택 : ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    System.out.println(">> [예약 내역 확인]으로 이동합니다.");
                    new ReservationView().reservationHistoryMenu();
                    break;
                case 2:
                    System.out.println(">> [마이페이지]로 이동합니다.");
                    new MyPageView().myPageMenu();
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
    // 일반 사용자 메뉴
    // ─────────────────────────────────────────────────────
    public void userMainView() {
        while (true) {
            int unread = alarmService.getUnreadAlarmCount(Auth.getUserInfo().getUserId());
            String alarmLabel = "알림조회(" + unread + ")";
            System.out.println("=================================================================");
            System.out.println("                          사용자 메인 메뉴                          ");
            System.out.println("=================================================================");
            System.out.println("  1. 예약하기 || 2. 마이페이지 || 3. 예약 내역 확인 || 4. " + alarmLabel + " || 0. 종료 ");
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
                    new MyPageView().myPageMenu();
                    break;
                case 3:
                    System.out.println(">> [예약 내역 확인]으로 이동합니다.");
                    new ReservationView().reservationHistoryMenu();
                    break;
                case 4:
                    System.out.println(">> [알림조회]로 이동합니다.");
                    new AlarmView().alarmMenu();
                    break;
                case 0:
                    System.out.println("[프로그램을 종료합니다.]");
                    try {
                        if (ClientMain.socket != null && !ClientMain.socket.isClosed()) {
                            ClientMain.socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            int unread = alarmService.getUnreadAlarmCount(Auth.getUserInfo().getUserId());
            String alarmLabel = "알림조회(" + unread + ")";
            System.out.println("========================================================================");
            System.out.println("                           중간 관리자 메인 메뉴                          ");
            System.out.println("========================================================================");
            System.out.println(" 1. 예약 관리 || 2. 시설/비품 관리 || 3. 마이페이지 || 4. 예약하기 || 5. 예약 내역 확인 || 6. " + alarmLabel + " || 0. 종료");
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
                    new MyPageView().myPageMenu();
                    break;
                case 4:
                    System.out.println(">> [예약 하기]로 이동합니다.");
                    new ReservationView().reservationMenu();
                    break;
                case 5:
                    System.out.println(">> [예약 내역 확인]으로 이동합니다.");
                    new ReservationView().reservationHistoryMenu();
                    break;
                case 6:
                    System.out.println(">> [알림조회]로 이동합니다.");
                    new AlarmView().alarmMenu();
                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    try {
                        if (ClientMain.socket != null && !ClientMain.socket.isClosed()) {
                            ClientMain.socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
            int unread = alarmService.getUnreadAlarmCount(Auth.getUserInfo().getUserId());
            String alarmLabel = "알림조회(" + unread + ")";
            System.out.println("============================================================================================");
            System.out.println("                                     상위 관리자 메인 메뉴                                     ");
            System.out.println("============================================================================================");
            System.out.println(" 1. 예약 관리 || 2. 시설/비품 관리 || 3. 사용자 관리 || 4. 마이페이지 || 5. 예약하기 || 6. 제한기간 관리 || 7. " + alarmLabel + " || 0. 로그아웃");
            System.out.println("============================================================================================");
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
                    new UserManageView().userManageMenu();
                    break;
                case 4:
                    System.out.println(">> [마이페이지]로 이동합니다.");
                    new MyPageView().myPageMenu();
                    break;
                case 5:
                    System.out.println(">> [예약 하기]로 이동합니다.");
                    new ReservationView().reservationMenu();
                    break;
                case 6:
                    System.out.println(">> [제한기간 관리]로 이동합니다.");
                    new BlockPeriodManageView().blockPeriodManageView();
                    break;
                case 7:
                    System.out.println(">> [알림조회]로 이동합니다.");
                    new AlarmView().alarmMenu();
                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    try {
                        if (ClientMain.socket != null && !ClientMain.socket.isClosed()) {
                            ClientMain.socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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