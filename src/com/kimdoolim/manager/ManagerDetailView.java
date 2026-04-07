package com.kimdoolim.manager;

import java.util.Scanner;

public class ManagerDetailView {
    FacilityManageView facilityManageView = new FacilityManageView();

    public void topManagerView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("      최상위 관리자 메뉴      ");
            System.out.println("=============================");
            System.out.println(" 1. 시설 관리");
            System.out.println(" 2. 비품 관리");
            System.out.println(" 3. 사용자 등록");
            System.out.println(" 4. 예약 요청 조회");
            System.out.println(" 5. 제한 시간 관리");
            System.out.println(" 0. 뒤로 가기");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println(">> 시설 관리로 이동합니다.");
                    facilityManageView.facilityManageView();
                    break;
                case 2:
                    System.out.println(">> 비품 관리로 이동합니다.");
                    //equipmentView();
                    break;
                case 3:
                    System.out.println(">> 사용자 등록으로 이동합니다.");
                    //userRegisterView();
                    break;
                case 4:
                    System.out.println(">> 예약 요청 조회로 이동합니다.");
                    //reservationRequestView();
                    break;
                case 5:
                    System.out.println(">> 제한 시간 관리로 이동합니다.");
                    //timeLimitView();
                    break;
                case 0:
                    System.out.println(">> 이전 메뉴로 돌아갑니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

}
