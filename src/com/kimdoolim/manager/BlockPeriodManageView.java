package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import com.kimdoolim.dto.BlockPeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class BlockPeriodManageView {

    private final BlockPeriodController controller = BlockPeriodController.getBlockPeriodController();
    private final Scanner scanner = AppScanner.getScanner();

    public void blockPeriodManageView() {
        while (true) {
            System.out.println("\n=============================");
            System.out.println("      시설/비품 제한 기간 관리    ");
            System.out.println("=============================");
            System.out.println(" 1. 제한기간 조회");
            System.out.println(" 2. 신규 제한일정 등록");
            System.out.println(" 3. 제한기간 수정");
            System.out.println(" 4. 제한기간 전체적용");
            System.out.println(" 5. 제한기간 지정적용");
            System.out.println(" 0. 뒤로 가기");
            System.out.println("=============================");
            System.out.print("메뉴 선택: ");

            int choice = readInt();
            switch (choice) {
                case 1: showAllBlockPeriods(); break;
                case 2: enrollNewBlockPeriod(); break;
                case 3: updateBlockPeriodFlow(); break;
                case 4: applyDetailByDescription(); break;
                case 5: applySpecificResourceFlow(); break; // 새로 만든 지정 적용
                case 0: return;
                default: System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // [1] 목록 조회
    private void showAllBlockPeriods() {
        System.out.println("\n[제한 일정 전체 목록]");
        List<BlockPeriod> list = controller.getAllBlockPeriods();
        if (list.isEmpty()) {
            System.out.println("등록된 일정이 없습니다.");
            return;
        }
        printBlockTable(list);
    }

    // [2] 등록
    private void enrollNewBlockPeriod() {
        System.out.println("\n[신규 제한 일정 등록]");
        System.out.print("제한 명칭 (Unique): ");
        String description = scanner.nextLine().trim();
        System.out.print("시작 날짜 (yyyy-MM-dd): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("종료 날짜 (yyyy-MM-dd): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());

        BlockPeriod newBlock = BlockPeriod.builder()
                .startDate(startDate).endDate(endDate).description(description).build();

        int generatedId = controller.enrollBlockPeriod(newBlock);
        if (generatedId > 0) {
            System.out.println(">> '" + description + "' 일정이 등록되었습니다.");
            System.out.print("모든 시설/비품에 대해 제한을 적용하시겠습니까? (Y/N): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                int count = controller.applyBlockToAllResources(generatedId);
                System.out.println(">> 총 " + count + "건의 자원이 제한되었습니다.");
            }
        }
    }

    // [3] 수정 (비교 뷰 포함)
    private void updateBlockPeriodFlow() {
        System.out.print("\n수정할 제한일정의 이름을 입력하세요: ");
        String targetDesc = scanner.nextLine().trim();
        BlockPeriod origin = controller.getBlockByDescription(targetDesc);

        if (origin == null) {
            System.out.println(">> 해당 명칭의 일정을 찾을 수 없습니다.");
            return;
        }

        System.out.println("\n── 기존 정보 ──────────────────────");
        System.out.println(" 이름: " + origin.getDescription());
        System.out.println(" 기간: " + origin.getStartDate() + " ~ " + origin.getEndDate());
        System.out.println("───────────────────────────────────");

        System.out.print("새 제한일정 이름 (미입력 시 유지): ");
        String nextDesc = scanner.nextLine().trim();
        if (nextDesc.isEmpty()) nextDesc = origin.getDescription();

        System.out.print("수정할 시작일 (yyyy-MM-dd / 미입력 시 유지): ");
        String nextStartStr = scanner.nextLine().trim();
        LocalDate nextStart = nextStartStr.isEmpty() ? origin.getStartDate() : LocalDate.parse(nextStartStr);

        System.out.print("수정할 종료일 (yyyy-MM-dd / 미입력 시 유지): ");
        String nextEndStr = scanner.nextLine().trim();
        LocalDate nextEnd = nextEndStr.isEmpty() ? origin.getEndDate() : LocalDate.parse(nextEndStr);

        System.out.println("\n── 수정 후 적용 정보 ────────────────");
        System.out.println(" 이름: " + nextDesc);
        System.out.println(" 기간: " + nextStart + " ~ " + nextEnd);
        System.out.println("───────────────────────────────────");
        System.out.print("정말 위 내용으로 수정하시겠습니까? (Y/N): ");

        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            BlockPeriod updateData = BlockPeriod.builder()
                    .startDate(nextStart).endDate(nextEnd).description(nextDesc).build();

            if (controller.updateBlockPeriod(targetDesc, updateData) > 0) {
                System.out.println(">> 수정이 완료되었습니다.");
            }
        }
    }

    // [4] 명칭으로 상세 적용
    private void applyDetailByDescription() {
        System.out.print("\n제한 대상을 적용할 일정이름 입력: ");
        String desc = scanner.nextLine().trim();
        BlockPeriod target = controller.getBlockByDescription(desc);

        if (target == null) {
            System.out.println(">> 해당 이름의 일정이 존재하지 않습니다.");
            return;
        }

        System.out.print("'" + desc + "' 모든 시설/비품에 제한을 적용하시겠습니까? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            int count = controller.applyBlockToAllResources(target.getBlockPeriodId());
            System.out.println(">> 총 " + count + "건 적용 완료.");
        }
    }

    // ─────────────────────────────────────────────────────
    private void printBlockTable(List<BlockPeriod> list) {
        System.out.println("──────────────────────────────────────────────────────────────");
        System.out.printf("%-6s %-20s %-12s %-12s%n", "ID", "제한 명칭", "시작일", "종료일");
        System.out.println("──────────────────────────────────────────────────────────────");
        for (BlockPeriod bp : list) {
            System.out.printf("%-6d %-20s %-12s %-12s%n",
                    bp.getBlockPeriodId(), bp.getDescription(), bp.getStartDate(), bp.getEndDate());
        }
        System.out.println("──────────────────────────────────────────────────────────────");
    }

    /**
     * [추가] 특정 자원 지정 차단 설정 흐름 (명칭 기반)
     */
    private void applySpecificResourceFlow() {
        System.out.println("\n[특정 시설/비품 지정제한 설정]");

        System.out.print("제한일정 이름입력: ");
        String blockDescription = scanner.nextLine().trim();
        BlockPeriod targetBlock = controller.getBlockByDescription(blockDescription);

        if (targetBlock == null) {
            System.out.println(">> 해당 명칭의 제한 일정을 찾을 수 없습니다.");
            return;
        }

        System.out.print("시설/비품 을 선택하세요 (1.시설 / 2.비품): ");
        int typeChoice = readInt();
        String resourceType = (typeChoice == 1) ? "F" : "E";
        String typeLabel = (typeChoice == 1) ? "시설" : "비품";

        System.out.print("제한할 " + typeLabel + " 명칭 입력: ");
        String resourceName = scanner.nextLine().trim();

        System.out.println("\n── 설정 정보 확인 ──────────────────");
        System.out.println(" 제한 일정: " + blockDescription);
        System.out.println(" 제한 대상: [" + typeLabel + "] " + resourceName);
        System.out.print("정말 적용하시겠습니까? (Y/N): ");

        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            int result = controller.applyBlockToSpecificResource(targetBlock.getBlockPeriodId(), resourceType, resourceName);

            if (result > 0) {
                System.out.println(">> [" + resourceName + "] 지정제한 설정이 완료되었습니다.");
            } else {
                System.out.println(">> 설정 실패. 자원 이름을 다시 확인해주세요.");
            }
        }
    }

    private int readInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); } catch (Exception e) { return -1; }
    }
}