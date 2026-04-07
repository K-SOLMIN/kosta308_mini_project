package com.kimdoolim.manager;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.util.Scanner;

public class FacilityManageView {
    FacilityController controller = FacilityController.getFacilityController();

    public void facilityManageView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("         시설 관리 메뉴       ");
            System.out.println("=============================");
            System.out.println(" 1. 시설 등록");
            System.out.println(" 2. 시설 수정");
            System.out.println(" 3. 시설 삭제");
            System.out.println(" 4. 시설 조회");
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
                    facilityUpdateView();
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

        System.out.print("최대 예약 단위 (WEEK / DAY / MONTH / YEAR) : ");
        String maxReservationUnit = scanner.nextLine().toUpperCase();

        if (!maxReservationUnit.equals("WEEK") && !maxReservationUnit.equals("DAY")
                && !maxReservationUnit.equals("MONTH") && !maxReservationUnit.equals("YEAR")) {
            System.out.println(">> 올바른 예약 단위를 입력해주세요. (WEEK / DAY / MONTH / YEAR)");
            return;
        }

        System.out.print("최대 예약 가능 값 : ");
        int maxReservationValue = scanner.nextInt();
        scanner.nextLine();

        System.out.print("상태 (정상 / 수리 / 점검) : ");
        String status = scanner.nextLine();

        // 중간관리자 배정
        System.out.println("=============================");
        System.out.print("담당 중간관리자 아이디 : ");
        String managerId = scanner.nextLine();

        // 중간관리자 존재 여부 조회
        User manager = controller.getUserById(managerId);

        if (manager == null) {
            System.out.println(">> 존재하지 않는 사용자입니다.");
            return;
        }

        if (manager.getPermission() != Permission.MIDDLEADMIN && manager.getPermission() != Permission.USER) {
            System.out.println(">> 해당 사용자는 관리자로 배정할 수 없습니다.");
            return;
        }

        System.out.println("=============================");
        System.out.println("배정할 중간관리자 정보 확인");
        System.out.println("=============================");
        System.out.println(" 아이디  : " + manager.getId());
        System.out.println(" 이름    : " + manager.getName());
        System.out.println(" 현재 권한 : " + manager.getPermission());
        System.out.println("=============================");
        System.out.println("입력 정보 확인");
        System.out.println("=============================");
        System.out.println(" 시설 이름    : " + name);
        System.out.println(" 위치         : " + location);
        System.out.println(" 최대 수용    : " + maxCapacity + "명");
        System.out.println(" 예약 단위    : " + maxReservationUnit);
        System.out.println(" 최대 예약    : " + maxReservationValue);
        System.out.println(" 상태         : " + status);
        System.out.println(" 담당 관리자  : " + manager.getName() + " (" + manager.getId() + ")");
        System.out.println("=============================");
        System.out.print("등록하시겠습니까? (1. 예 / 0. 아니오) : ");

        int confirm = scanner.nextInt();

        if (confirm == 1) {
            // 중간관리자 권한 변경
            int updateResult = controller.updatePermission(manager.getUserId(), Permission.MIDDLEADMIN);

            if (updateResult == 0) {
                System.out.println(">> 권한 변경 실패. 시설 등록을 중단합니다.");
                return;
            }

            Facility facility = Facility.builder()
                    .user(manager)          // 로그인한 사람이 아닌 배정된 중간관리자
                    .name(name)
                    .location(location)
                    .maxCapacity(maxCapacity)
                    .maxReservationUnit(maxReservationUnit.toUpperCase())
                    .maxReservationValue(maxReservationValue)
                    .status(status)
                    .isDelete(false)
                    .build();

            int result = controller.enrollFacility(facility);

            if (result > 0) {
                System.out.println(">> 시설 등록 및 중간관리자 배정 완료!");
            } else {
                System.out.println(">> 시설 등록 실패.");
            }
        } else {
            System.out.println(">> 등록을 취소합니다.");
        }
    }

    public void facilityUpdateView() {
        Scanner scanner = new Scanner(System.in);
        User loginUser = Auth.getUserInfo();

        System.out.println("=============================");
        System.out.println("         시설 수정            ");
        System.out.println("=============================");
        System.out.print("수정할 시설 이름 : ");
        String searchName = scanner.nextLine();

        Facility facility = controller.getFacilityByName(searchName);

        if (facility == null) {
            System.out.println(">> 해당 시설을 찾을 수 없습니다.");
            return;
        }

        // 권한 체크
        if (loginUser.getPermission() == Permission.MIDDLEADMIN) {
            if (facility.getUser().getUserId() != loginUser.getUserId()) {
                System.out.println(">> 해당 시설에 대한 수정 권한이 없습니다.");
                return;
            }
        }

        System.out.println("=============================");
        System.out.println("현재 시설 정보");
        System.out.println("=============================");
        System.out.println(" 시설 이름    : " + facility.getName());
        System.out.println(" 위치         : " + facility.getLocation());
        System.out.println(" 최대 수용    : " + facility.getMaxCapacity() + "명");
        System.out.println(" 예약 단위    : " + facility.getMaxReservationUnit());
        System.out.println(" 최대 예약    : " + facility.getMaxReservationValue());
        System.out.println(" 상태         : " + facility.getStatus());
        System.out.println("=============================");

        System.out.println("수정할 정보를 입력하세요. (변경하지않을 값은 엔터)");

        System.out.print("시설 이름 [" + facility.getName() + "] : ");
        String name = scanner.nextLine();
        if (name.isBlank()) name = facility.getName();

        System.out.print("위치 [" + facility.getLocation() + "] : ");
        String location = scanner.nextLine();
        if (location.isBlank()) location = facility.getLocation();

        System.out.print("최대 수용 인원 [" + facility.getMaxCapacity() + "] : ");
        String capacityInput = scanner.nextLine();
        int maxCapacity = capacityInput.isBlank() ? facility.getMaxCapacity() : Integer.parseInt(capacityInput);

        System.out.print("최대 예약 단위 (WEEK / DAY / MONTH / YEAR) : ");
        String maxReservationUnit = scanner.nextLine().toUpperCase();

        if (!maxReservationUnit.equals("WEEK") && !maxReservationUnit.equals("DAY")
                && !maxReservationUnit.equals("MONTH") && !maxReservationUnit.equals("YEAR")) {
            System.out.println(">> 올바른 예약 단위를 입력해주세요. (WEEK / DAY / MONTH / YEAR)");
            return;
        }

        System.out.print("최대 예약 가능 값 [" + facility.getMaxReservationValue() + "] : ");
        String valueInput = scanner.nextLine();
        int maxReservationValue = valueInput.isBlank() ? facility.getMaxReservationValue() : Integer.parseInt(valueInput);

        System.out.print("상태 [" + facility.getStatus() + "] : ");
        String status = scanner.nextLine();
        if (status.isBlank()) status = facility.getStatus();

        System.out.println("=============================");
        System.out.println("수정 정보 확인");
        System.out.println("=============================");
        System.out.println(" 시설 이름    : " + name);
        System.out.println(" 위치         : " + location);
        System.out.println(" 최대 수용    : " + maxCapacity + "명");
        System.out.println(" 예약 단위    : " + maxReservationUnit);
        System.out.println(" 최대 예약    : " + maxReservationValue);
        System.out.println(" 상태         : " + status);
        System.out.println("=============================");
        System.out.print("수정하시겠습니까? (1. 예 / 0. 아니오) : ");

        int confirm = scanner.nextInt();

        if (confirm == 1) {
            Facility updateFacility = Facility.builder()
                    .facilityId(facility.getFacilityId())
                    .user(Auth.getUserInfo())
                    .name(name)
                    .location(location)
                    .maxCapacity(maxCapacity)
                    .maxReservationUnit(maxReservationUnit)
                    .maxReservationValue(maxReservationValue)
                    .status(status)
                    .build();

            controller.updateFacility(updateFacility);
        } else {
            System.out.println(">> 수정을 취소합니다.");
        }
    }

    public void facilityDeleteView() {
        Scanner scanner = new Scanner(System.in);
        User loginUser = Auth.getUserInfo();

        System.out.println("=============================");
        System.out.println("         시설 삭제            ");
        System.out.println("=============================");
        System.out.print("삭제할 시설 이름 : ");
        String searchName = scanner.nextLine();

        Facility facility = controller.getFacilityByName(searchName);

        if (facility == null) {
            System.out.println(">> 해당 시설을 찾을 수 없습니다.");
            return;
        }

        // 권한 체크
        if (loginUser.getPermission() == Permission.MIDDLEADMIN) {
            if (facility.getUser().getUserId() != loginUser.getUserId()) {
                System.out.println(">> 해당 시설에 대한 삭제 권한이 없습니다.");
                return;
            }
        }

        // 삭제할 시설 정보 출력
        System.out.println("=============================");
        System.out.println("삭제할 시설 정보");
        System.out.println("=============================");
        System.out.println(" 시설 이름    : " + facility.getName());
        System.out.println(" 위치         : " + facility.getLocation());
        System.out.println(" 최대 수용    : " + facility.getMaxCapacity() + "명");
        System.out.println(" 예약 단위    : " + facility.getMaxReservationUnit());
        System.out.println(" 최대 예약    : " + facility.getMaxReservationValue());
        System.out.println(" 상태         : " + facility.getStatus());
        System.out.println("=============================");
        System.out.print("정말 삭제하시겠습니까? (1. 예 / 0. 아니오) : ");

        int confirm = scanner.nextInt();

        if (confirm == 1) {
            int result = controller.deleteFacility(facility.getFacilityId());
            if (result > 0) {
                System.out.println(">> 시설이 삭제되었습니다.");
            } else {
                System.out.println(">> 시설 삭제 실패.");
            }
        } else {
            System.out.println(">> 삭제를 취소합니다.");
        }
    }
}
