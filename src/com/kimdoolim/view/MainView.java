package com.kimdoolim.view;

import com.kimdoolim.alarm.AlarmService;
import com.kimdoolim.common.AppScanner;
import com.kimdoolim.common.Auth;
import com.kimdoolim.main.ClientMain;
import com.kimdoolim.manager.BlockPeriodManageView;
import com.kimdoolim.manager.BlockScheduleView;
import com.kimdoolim.manager.FacilityEquipmentView;
import com.kimdoolim.manager.ManagerReservationView;
import com.kimdoolim.view.MainView;

import java.io.IOException;
import java.util.Scanner;

public class MainView {

    private final Scanner scanner = AppScanner.getScanner();
    private final AlarmService alarmService = AlarmService.getAlarmService();

    private static final int W = 84;
    private static final String BORDER = "=".repeat(W);

    /** 한글(2칸) / 영문(1칸) 기준으로 가운데 정렬 */
    private static String center(String text) {
        int dw = 0;
        for (char c : text.toCharArray())
            dw += (c >= '\uAC00' && c <= '\uD7A3') ? 2 : 1;
        int pad = Math.max(0, (W - dw) / 2);
        return " ".repeat(pad) + text;
    }

    // ─────────────────────────────────────────────────────
    // 비활성 사용자 메뉴 (휴직/전근)
    // ─────────────────────────────────────────────────────
    public void restrictedUserView() {
        while (true) {
            AppScanner.cls();
            System.out.println(BORDER);
            System.out.println(center("제한된 메뉴 (비활성 계정)"));
            System.out.println(BORDER);
            System.out.println(" 1.예약 내역 확인 || 2.마이페이지 || 0.종료");
            System.out.println(BORDER);
            System.out.print("메뉴 선택: ");

            switch (readInt()) {
                case 1: new ReservationView().reservationHistoryMenu(); break;
                case 2: new com.kimdoolim.main.view.MyPageView().myPageMenu(); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 일반 사용자 메뉴  (1~4번 공통)
    // ─────────────────────────────────────────────────────
    public void userMainView() {
        while (true) {
            AppScanner.cls();
            int unread = alarmService.getUnreadAlarmCount(Auth.getUserInfo().getUserId());

            System.out.println(BORDER);
            System.out.println(center("사용자 메인 메뉴"));
            System.out.println(BORDER);
            System.out.printf(" 1.예약하기 || 2.예약 내역 확인 || 3.마이페이지 || 4.알림 조회(%d건) || 0.로그아웃%n", unread);
            System.out.println(BORDER);
            System.out.print("메뉴 선택: ");

            switch (readInt()) {
                case 1: new ReservationView().reservationMenu(); break;
                case 2: new ReservationView().reservationHistoryMenu(); break;
                case 3: new com.kimdoolim.main.view.MyPageView().myPageMenu(); break;
                case 4: new AlarmView().alarmMenu(); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    closeSocket(); return;
                default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 중간 관리자 메뉴  (1~4번 공통 + 5~6번 관리자 전용)
    // ─────────────────────────────────────────────────────
    public void middleAdminMainView() {
        while (true) {
            AppScanner.cls();
            int unread = alarmService.getUnreadAlarmCount(Auth.getUserInfo().getUserId());

            System.out.println(BORDER);
            System.out.println(center("중간 관리자 메인 메뉴"));
            System.out.println(BORDER);
            System.out.printf(" 1.예약하기  || 2.예약 내역 확인 || 3.마이페이지 || 4.알림 조회(%d건) || 0.로그아웃%n", unread);
            System.out.println(" 5.예약 관리 || 6.시설/비품 관리 || 7.제한기간 관리");
            System.out.println(BORDER);
            System.out.print("메뉴 선택: ");

            switch (readInt()) {
                case 1: new ReservationView().reservationMenu(); break;
                case 2: new ReservationView().reservationHistoryMenu(); break;
                case 3: new com.kimdoolim.main.view.MyPageView().myPageMenu(); break;
                case 4: new AlarmView().alarmMenu(); break;
                case 5: new ManagerReservationView().managerReservationMenu(); break;
                case 6: new FacilityEquipmentView().facilityEquipmentMenu(); break;
                case 7: new BlockScheduleView().blockScheduleMenu(); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    closeSocket(); return;
                default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 상위 관리자 메뉴  (1~4번 공통 + 5~8번 관리자 전용)
    // ─────────────────────────────────────────────────────
    public void adminMainView() {
        while (true) {
            AppScanner.cls();
            int unread = alarmService.getUnreadAlarmCount(Auth.getUserInfo().getUserId());

            System.out.println(BORDER);
            System.out.println(center("상위 관리자 메인 메뉴"));
            System.out.println(BORDER);
            System.out.printf(" 1.예약하기  || 2.예약 내역 확인 || 3.마이페이지  || 4.알림 조회(%d건) || 0.로그아웃%n", unread);
            System.out.println(" 5.예약 관리 || 6.시설/비품 관리 || 7.사용자 관리 || 8.제한기간 관리");
            System.out.println(BORDER);
            System.out.print("메뉴 선택: ");

            switch (readInt()) {
                case 1: new ReservationView().reservationMenu(); break;
                case 2: new ReservationView().reservationHistoryMenu(); break;
                case 3: new com.kimdoolim.main.view.MyPageView().myPageMenu(); break;
                case 4: new AlarmView().alarmMenu(); break;
                case 5: new ManagerReservationView().managerReservationMenu(); break;
                case 6: new FacilityEquipmentView().facilityEquipmentMenu(); break;
                case 7: new UserManageView().userManageMenu(); break;
                case 8: new BlockPeriodManageView().blockPeriodManageView(); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    closeSocket(); return;
                default: System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    private void closeSocket() {
        try {
            if (ClientMain.socket != null && !ClientMain.socket.isClosed())
                ClientMain.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
