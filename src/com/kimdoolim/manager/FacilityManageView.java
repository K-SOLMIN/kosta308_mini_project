package com.kimdoolim.manager;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Facility;

import java.util.Scanner;

public class FacilityManageView {

    public void facilityManageView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("         시설 관리 메뉴       ");
            System.out.println("=============================");
            System.out.println(" 1. 시설 등록");
            System.out.println(" 2. 시설 수정");
            System.out.println(" 3. 시설 삭제");
            System.out.println(" 0. 뒤로 가기");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println(">> 시설 등록으로 이동합니다.");
                    facilityEnrollView();
                    break;
                case 2:
                    System.out.println(">> 시설 수정으로 이동합니다.");
                    //facilityUpdateView();
                    break;
                case 3:
                    System.out.println(">> 시설 삭제로 이동합니다.");
                    //facilityDeleteView();
                    break;
                case 0:
                    System.out.println(">> 이전 메뉴로 돌아갑니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    public void facilityEnrollView() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=============================");
        System.out.println("         시설 등록            ");
        System.out.println("=============================");

        System.out.print("시설 이름 : ");
        String name = scanner.nextLine();

        System.out.print("위치 : ");
        String location = scanner.nextLine();

        System.out.print("최대 수용 인원 : ");
        int maxCapacity = scanner.nextInt();
        scanner.nextLine();

        System.out.println("최대 예약 단위 (예: 시간, 분) : ");
        String maxReservationUnit = scanner.nextLine();

        System.out.print("최대 예약 가능 값 : ");
        int maxReservationValue = scanner.nextInt();
        scanner.nextLine();

        System.out.print("상태 (정상 / 수리 / 점검) : ");
        String status = scanner.nextLine();

        System.out.println("=============================");
        System.out.println("입력 정보 확인");
        System.out.println("=============================");
        System.out.println(" 시설 이름    : " + name);
        System.out.println(" 위치         : " + location);
        System.out.println(" 최대 수용    : " + maxCapacity + "명");
        System.out.println(" 예약 단위    : " + maxReservationUnit);
        System.out.println(" 최대 예약    : " + maxReservationValue);
        System.out.println(" 상태         : " + status);
        System.out.println("=============================");
        System.out.print("등록하시겠습니까? (1. 예 / 0. 아니오) : ");

        int confirm = scanner.nextInt();

        if (confirm == 1) {
            Facility facility = Facility.builder()
                    .user(Auth.getUserInfo())
                    .name(name)
                    .location(location)
                    .maxCapacity(maxCapacity)
                    .maxReservationUnit(maxReservationUnit)
                    .maxReservationValue(maxReservationValue)
                    .status(status)
                    .isDelete(false)
                    .build();

            // facilityService.insert(facility);
            System.out.println(">> 시설이 등록되었습니다.");
        } else {
            System.out.println(">> 등록을 취소합니다.");
        }
    }
}
