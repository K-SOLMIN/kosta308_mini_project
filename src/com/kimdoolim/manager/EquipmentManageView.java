package com.kimdoolim.manager;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.EquipmentDetail;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EquipmentManageView {
    EquipmentController controller = EquipmentController.getEquipmentController();

    public void equipmentManageView() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("=============================");
            System.out.println("         비품 관리 메뉴       ");
            System.out.println("=============================");
            System.out.println(" 1. 비품 등록");
            System.out.println(" 2. 비품 수정");
            System.out.println(" 3. 비품 삭제");
            System.out.println(" 0. 뒤로 가기");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println(">> 비품 등록으로 이동합니다.");
                    equipmentEnrollView();
                    break;
                case 2:
                    System.out.println(">> 비품 수정으로 이동합니다.");
                    equipmentUpdateView();
                    break;
                case 3:
                    System.out.println(">> 비품 삭제로 이동합니다.");
                    equipmentDeleteView();
                    break;
                case 0:
                    System.out.println(">> 이전 메뉴로 돌아갑니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    public void equipmentEnrollView() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=============================");
        System.out.println("         비품 등록            ");
        System.out.println("=============================");

        System.out.print("비품 이름 : ");
        String name = scanner.nextLine();

        System.out.print("위치 : ");
        String location = scanner.nextLine();

        // 중간관리자 배정
        System.out.println("=============================");
        System.out.print("담당 중간관리자 아이디 : ");
        String managerId = scanner.nextLine();

        User manager = controller.getUserById(managerId);

        if (manager == null) {
            System.out.println(">> 존재하지 않는 사용자입니다.");
            return;
        }

        if (manager.getPermission() == Permission.ADMIN) {
            System.out.println(">> 최상위 관리자는 담당자로 배정할 수 없습니다.");
            return;
        }

        System.out.println("=============================");
        System.out.println(" 아이디    : " + manager.getId());
        System.out.println(" 이름      : " + manager.getName());
        System.out.println(" 현재 권한 : " + manager.getPermission());
        System.out.println("=============================");

        // 세트 여부 선택
        System.out.println(" 1. 세트 (낱개 여러 개)");
        System.out.println(" 2. 낱개 (1개)");
        System.out.print("등록 방식 선택 : ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        if (typeChoice != 1 && typeChoice != 2) {
            System.out.println(">> 잘못된 입력입니다.");
            return;
        }

        int quantity = 1;
        if (typeChoice == 1) {
            System.out.print("낱개 수량 : ");
            quantity = scanner.nextInt();
            scanner.nextLine();
        }

        // 낱개 정보 미리 입력받기
        List<EquipmentDetail> details = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            System.out.println("=============================");
            System.out.println(" [" + i + "번 낱개 정보 입력]");
            System.out.print("시리얼 번호 : ");
            String serialNo = scanner.nextLine();

            details.add(EquipmentDetail.builder()
                    .serialNo(serialNo)
                    .status("정상")
                    .checkDelete(false)
                    .build());
        }

        System.out.println("=============================");
        System.out.println("입력 정보 확인");
        System.out.println("=============================");
        System.out.println(" 비품 이름    : " + name);
        System.out.println(" 위치         : " + location);
        System.out.println(" 담당 관리자  : " + manager.getName() + " (" + manager.getId() + ")");
        System.out.println(" 등록 방식    : " + (typeChoice == 1 ? "세트 (" + quantity + "개)" : "낱개"));
        System.out.println("-----------------------------");
        for (int i = 0; i < details.size(); i++) {
            System.out.println(" [" + (i + 1) + "번 낱개] 시리얼: " + details.get(i).getSerialNo() + " / 상태: " + details.get(i).getStatus());
        }
        System.out.println("=============================");
        System.out.print("등록하시겠습니까? (1. 예 / 0. 아니오) : ");

        int confirm = scanner.nextInt();

        if (confirm == 1) {
            int updateResult = controller.updatePermission(manager.getUserId(), Permission.MIDDLEADMIN);
            if (updateResult == 0) {
                System.out.println(">> 권한 변경 실패. 비품 등록을 중단합니다.");
                return;
            }

            Equipment equipment = Equipment.builder()
                    .user(manager)
                    .name(name)
                    .location(location)
                    .checkDelete(false)
                    .build();

            int result = controller.enrollEquipment(equipment, details);

            if (result > 0) {
                System.out.println(">> 비품 등록 완료!");
            } else {
                System.out.println(">> 비품 등록 실패.");
            }
        } else {
            System.out.println(">> 등록을 취소합니다.");
        }
    }

    public void equipmentUpdateView() {
        Scanner scanner = new Scanner(System.in);
        User loginUser = Auth.getUserInfo();

        System.out.println("=============================");
        System.out.println("         비품 수정            ");
        System.out.println("=============================");
        System.out.print("수정할 비품 이름 : ");
        String searchName = scanner.nextLine();

        Equipment equipment = controller.getEquipmentByName(searchName);

        if (equipment == null) {
            System.out.println(">> 해당 비품을 찾을 수 없습니다.");
            return;
        }

        if (loginUser.getPermission() == Permission.MIDDLEADMIN) {
            if (equipment.getUser().getUserId() != loginUser.getUserId()) {
                System.out.println(">> 해당 비품에 대한 수정 권한이 없습니다.");
                return;
            }
        }

        // 낱개 목록 조회
        List<EquipmentDetail> details = controller.getEquipmentDetails(equipment.getEquipmentId());

        System.out.println("=============================");
        System.out.println("현재 비품 정보");
        System.out.println("=============================");
        System.out.println(" 비품 이름  : " + equipment.getName());
        System.out.println(" 위치       : " + equipment.getLocation());
        System.out.println(" 낱개 수량  : " + details.size() + "개");
        System.out.println("-----------------------------");
        for (int i = 0; i < details.size(); i++) {
            System.out.println(" [" + (i + 1) + "번 낱개] 시리얼: " + details.get(i).getSerialNo() + " / 상태: " + details.get(i).getStatus());
        }
        System.out.println("=============================");

        if (details.size() > 1) {
            System.out.println(" 1. 세트 전체 수정 (비품 이름 / 위치)");
            System.out.println(" 2. 낱개 개별 수정 (시리얼번호 / 상태)");
            System.out.print("수정 방식 선택 : ");
            int updateType = scanner.nextInt();
            scanner.nextLine();

            if (updateType == 1) {
                // 세트 전체 수정
                System.out.print("비품 이름 [" + equipment.getName() + "] : ");
                String name = scanner.nextLine();
                if (name.isBlank()) name = equipment.getName();

                System.out.print("위치 [" + equipment.getLocation() + "] : ");
                String location = scanner.nextLine();
                if (location.isBlank()) location = equipment.getLocation();

                System.out.println("=============================");
                System.out.println(" 비품 이름  : " + name);
                System.out.println(" 위치       : " + location);
                System.out.println("=============================");
                System.out.print("수정하시겠습니까? (1. 예 / 0. 아니오) : ");
                int confirm = scanner.nextInt();

                if (confirm == 1) {
                    Equipment updateEquipment = Equipment.builder()
                            .equipmentId(equipment.getEquipmentId())
                            .user(equipment.getUser())
                            .name(name)
                            .location(location)
                            .build();
                    controller.updateEquipment(updateEquipment);
                } else {
                    System.out.println(">> 수정을 취소합니다.");
                }

            } else if (updateType == 2) {
                // 낱개 개별 수정
                System.out.print("수정할 낱개 번호 선택 (1 ~ " + details.size() + ") : ");
                int detailIndex = scanner.nextInt() - 1;
                scanner.nextLine();

                if (detailIndex < 0 || detailIndex >= details.size()) {
                    System.out.println(">> 잘못된 번호입니다.");
                    return;
                }

                EquipmentDetail target = details.get(detailIndex);

                System.out.print("시리얼 번호 [" + target.getSerialNo() + "] : ");
                String serialNo = scanner.nextLine();
                if (serialNo.isBlank()) serialNo = target.getSerialNo();

                System.out.print("상태 [" + target.getStatus() + "] : ");
                String status = scanner.nextLine();
                if (status.isBlank()) status = target.getStatus();

                System.out.println("=============================");
                System.out.println(" 시리얼 번호  : " + serialNo);
                System.out.println(" 상태         : " + status);
                System.out.println("=============================");
                System.out.print("수정하시겠습니까? (1. 예 / 0. 아니오) : ");
                int confirm = scanner.nextInt();

                if (confirm == 1) {
                    EquipmentDetail updateDetail = EquipmentDetail.builder()
                            .equipmentDetailId(target.getEquipmentDetailId())
                            .serialNo(serialNo)
                            .status(status)
                            .build();
                    controller.updateEquipmentDetail(updateDetail);
                } else {
                    System.out.println(">> 수정을 취소합니다.");
                }
            } else {
                System.out.println(">> 잘못된 입력입니다.");
            }

        } else {
            // 낱개 1개짜리 수정
            System.out.print("비품 이름 [" + equipment.getName() + "] : ");
            String name = scanner.nextLine();
            if (name.isBlank()) name = equipment.getName();

            System.out.print("위치 [" + equipment.getLocation() + "] : ");
            String location = scanner.nextLine();
            if (location.isBlank()) location = equipment.getLocation();

            EquipmentDetail target = details.get(0);

            System.out.print("시리얼 번호 [" + target.getSerialNo() + "] : ");
            String serialNo = scanner.nextLine();
            if (serialNo.isBlank()) serialNo = target.getSerialNo();

            System.out.print("상태 [" + target.getStatus() + "] : ");
            String status = scanner.nextLine();
            if (status.isBlank()) status = target.getStatus();

            System.out.println("=============================");
            System.out.println(" 비품 이름    : " + name);
            System.out.println(" 위치         : " + location);
            System.out.println(" 시리얼 번호  : " + serialNo);
            System.out.println(" 상태         : " + status);
            System.out.println("=============================");
            System.out.print("수정하시겠습니까? (1. 예 / 0. 아니오) : ");
            int confirm = scanner.nextInt();

            if (confirm == 1) {
                Equipment updateEquipment = Equipment.builder()
                        .equipmentId(equipment.getEquipmentId())
                        .user(equipment.getUser())
                        .name(name)
                        .location(location)
                        .build();

                EquipmentDetail updateDetail = EquipmentDetail.builder()
                        .equipmentDetailId(target.getEquipmentDetailId())
                        .serialNo(serialNo)
                        .status(status)
                        .build();

                controller.updateEquipment(updateEquipment);
                controller.updateEquipmentDetail(updateDetail);
            } else {
                System.out.println(">> 수정을 취소합니다.");
            }
        }
    }

    public void equipmentDeleteView() {
        Scanner scanner = new Scanner(System.in);
        User loginUser = Auth.getUserInfo();

        System.out.println("=============================");
        System.out.println("         비품 삭제            ");
        System.out.println("=============================");
        System.out.print("삭제할 비품 이름 : ");
        String searchName = scanner.nextLine();

        Equipment equipment = controller.getEquipmentByName(searchName);

        if (equipment == null) {
            System.out.println(">> 해당 비품을 찾을 수 없습니다.");
            return;
        }

        if (loginUser.getPermission() == Permission.MIDDLEADMIN) {
            if (equipment.getUser().getUserId() != loginUser.getUserId()) {
                System.out.println(">> 해당 비품에 대한 삭제 권한이 없습니다.");
                return;
            }
        }

        List<EquipmentDetail> details = controller.getEquipmentDetails(equipment.getEquipmentId());

        System.out.println("=============================");
        System.out.println("삭제할 비품 정보");
        System.out.println("=============================");
        System.out.println(" 비품 이름  : " + equipment.getName());
        System.out.println(" 위치       : " + equipment.getLocation());
        System.out.println(" 낱개 수량  : " + details.size() + "개");
        System.out.println("-----------------------------");
        for (int i = 0; i < details.size(); i++) {
            System.out.println(" [" + (i + 1) + "번 낱개] 시리얼: " + details.get(i).getSerialNo() + " / 상태: " + details.get(i).getStatus());
        }
        System.out.println("=============================");
        System.out.print("정말 삭제하시겠습니까? (1. 예 / 0. 아니오) : ");

        int confirm = scanner.nextInt();

        if (confirm == 1) {
            int result = controller.deleteEquipment(equipment.getEquipmentId());
            if (result > 0) {
                System.out.println(">> 비품 및 낱개 전체 삭제 완료!");
            } else {
                System.out.println(">> 비품 삭제 실패.");
            }
        } else {
            System.out.println(">> 삭제를 취소합니다.");
        }
    }
}