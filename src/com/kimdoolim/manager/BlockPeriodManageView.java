package com.kimdoolim.manager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BlockPeriodManageView {
    private final BlockPeriodController controller = BlockPeriodController.getBlockPeriodController();
    private final Scanner scanner = new Scanner(System.in);

    public void blockPeriodManageView() {
        while (true) {
            System.out.println("\n=============================");
            System.out.println("       제한 기간 관리 메뉴     ");
            System.out.println("=============================");
            System.out.println(" 1. 제한 일정 목록 조회");
            System.out.println(" 2. 신규 제한 일정 생성 (마스터)");
            System.out.println(" 3. 제한 대상 적용 (디테일 추가)");
            System.out.println(" 4. 제한 일정 수정 (사유 기준)");
            System.out.println(" 5. 제한 일정 삭제");
            System.out.println(" 0. 뒤로 가기");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            String input = scanner.nextLine();
            int choice = input.isEmpty() ? -1 : Integer.parseInt(input);

            switch (choice) {
                case 1: searchAllBlockPeriods(); break;
                case 2: enrollBlockMasterView(); break;
                case 3: enrollBlockDetailView(); break;
                case 4: updateBlockPeriodView(); break;
                case 5: deleteBlockPeriodView(); break;
                case 0: return;
                default: System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // 1. 조회
    private void searchAllBlockPeriods() {
        System.out.println("\n[현재 등록된 제한 일정]");
        System.out.println("------------------------------------------------------------");
        System.out.println("ID | 시작일      | 종료일      | 사유(명칭)");
        System.out.println("------------------------------------------------------------");
        List<Map<String, Object>> list = controller.getAllBlockMasters();
        if (list.isEmpty()) {
            System.out.println("등록된 일정이 없습니다.");
        } else {
            for (Map<String, Object> map : list) {
                System.out.printf("%-2s | %-10s | %-10s | %s\n",
                        map.get("id"), map.get("start"), map.get("end"), map.get("desc"));
            }
        }
    }

    // 2. 등록 (마스터만)
    private void enrollBlockMasterView() {
        System.out.println("\n[신규 제한 일정 생성]");
        try {
            System.out.print("시작 날짜 (yyyy-MM-dd) : ");
            LocalDate start = LocalDate.parse(scanner.nextLine());
            System.out.print("종료 날짜 (yyyy-MM-dd) : ");
            LocalDate end = LocalDate.parse(scanner.nextLine());
            System.out.print("제한 명칭 : ");
            String desc = scanner.nextLine();

            System.out.println("\n[등록 정보 확인]");
            System.out.println("> 기간: " + start + " ~ " + end);
            System.out.println("> 명칭: " + desc);
            System.out.print("이대로 생성하시겠습니까? (1.예 / 0.아니오) : ");

            if (scanner.nextLine().equals("1")) {
                if (controller.enrollBlockMaster(start, end, desc) > 0)
                    System.out.println(">> 마스터 정보 등록 완료!");
            }
        } catch (Exception e) {
            System.out.println(">> 입력 형식이 잘못되었습니다.");
        }
    }

    // 3. 디테일 적용 (실제 차단 실행)
    private void enrollBlockDetailView() {
        System.out.print("\n차단을 적용할 제한 명칭 입력 : ");
        String desc = scanner.nextLine();
        Map<String, Object> master = controller.getBlockPeriodByDescription(desc);

        if (master == null) {
            System.out.println(">> 일정을 찾을 수 없습니다.");
            return;
        }

        System.out.println("1. 모든 시설/비품 일괄 차단");
        System.out.println("2. 특정 시설 차단");
        System.out.println("3. 특정 비품 차단");
        System.out.print("선택 : ");
        int type = Integer.parseInt(scanner.nextLine());

        long masterId = (long)master.get("id");
        int res = 0;

        if (type == 1) {
            res = controller.applyBlockToAll(masterId);
        } else if (type == 2) {
            System.out.print("시설 PK 입력 : ");
            res = controller.enrollBlockDetail(masterId, "F", Long.parseLong(scanner.nextLine()));
        } else if (type == 3) {
            System.out.print("비품 PK 입력 : ");
            res = controller.enrollBlockDetail(masterId, "E", Long.parseLong(scanner.nextLine()));
        }

        if (res > 0) System.out.println(">> 차단 대상이 성공적으로 적용되었습니다.");
    }

    // 4. 수정 (전후 비교 포함)
    private void updateBlockPeriodView() {
        System.out.print("\n수정할 제한 명칭 입력 : ");
        String oldDesc = scanner.nextLine();
        Map<String, Object> current = controller.getBlockPeriodByDescription(oldDesc);

        if (current == null) {
            System.out.println(">> 존재하지 않는 일정입니다.");
            return;
        }

        System.out.println("\n[현재 정보] " + current.get("start") + " ~ " + current.get("end") + " / " + oldDesc);
        System.out.println("(변경하지 않을 항목은 엔터)");

        System.out.print("새 시작일 : ");
        String sStr = scanner.nextLine();
        LocalDate nStart = sStr.isBlank() ? (LocalDate)current.get("start") : LocalDate.parse(sStr);

        System.out.print("새 종료일 : ");
        String eStr = scanner.nextLine();
        LocalDate nEnd = eStr.isBlank() ? (LocalDate)current.get("end") : LocalDate.parse(eStr);

        System.out.print("새 명칭 : ");
        String nDesc = scanner.nextLine();
        if (nDesc.isBlank()) nDesc = oldDesc;

        System.out.println("\n[수정 예정 정보]");
        System.out.println("> " + nStart + " ~ " + nEnd + " / " + nDesc);
        System.out.print("정말 수정하시겠습니까? (1.예) : ");

        if (scanner.nextLine().equals("1")) {
            if (controller.updateBlockMaster(oldDesc, nStart, nEnd, nDesc) > 0)
                System.out.println(">> 수정 완료!");
        }
    }

    // 5. 삭제
    private void deleteBlockPeriodView() {
        System.out.print("\n삭제할 명칭 입력 : ");
        String desc = scanner.nextLine();
        System.out.print("이 일정을 삭제하면 관련 차단 상세 정보도 모두 삭제됩니다. 계속하시겠습니까? (1.예) : ");
        if (scanner.nextLine().equals("1")) {
            if (controller.deleteBlockMasterByDesc(desc) > 0)
                System.out.println(">> 삭제 완료!");
        }
    }
}