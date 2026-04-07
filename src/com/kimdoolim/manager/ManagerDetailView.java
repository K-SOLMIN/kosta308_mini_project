package com.kimdoolim.manager;

import java.util.Scanner;

public class ManagerDetailView {
    FacilityManageView facilityManageView = new FacilityManageView();

    public void manageChoiceView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("      시설&비품 관리          ");
            System.out.println("=============================");
            System.out.println(" 1. 시설 관리");
            System.out.println(" 2. 비품 관리");
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
