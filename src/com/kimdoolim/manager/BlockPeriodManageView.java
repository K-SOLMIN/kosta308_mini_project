package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import static com.kimdoolim.common.AppScanner.fit;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BlockPeriodManageView {
    private final BlockPeriodController controller = BlockPeriodController.getBlockPeriodController();
    private final Scanner scanner = AppScanner.getScanner();

    // 입력 흐름을 즉시 중단하기 위한 예외 (?를 입력하면 취소)
    private static class CancelInputException extends RuntimeException {
        CancelInputException() { super(null, null, true, false); }
    }

    // ─────────────────────────────────────────────────────
    // 입력 헬퍼
    // ─────────────────────────────────────────────────────

    private static final String CANCEL_NOTICE = "  ※ 언제든 ?를 입력하면 취소됩니다.";

    private String ask(String prompt) {
        System.out.print(" " + prompt + " : ");
        String input = scanner.nextLine().trim();
        if ("?".equals(input)) throw new CancelInputException();
        return input;
    }

    private LocalDate askDate(String prompt) {
        while (true) {
            System.out.print(" " + prompt + " (yyyy-MM-dd) : ");
            String input = scanner.nextLine().trim();
            if ("?".equals(input)) throw new CancelInputException();
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("  날짜 형식이 잘못됐습니다 (예: 2025-06-01). 다시 입력해주세요.");
            }
        }
    }

    private LocalDate askDateUpdate(String prompt, LocalDate current) {
        while (true) {
            System.out.print(" " + prompt + " (유지: 엔터, yyyy-MM-dd) : ");
            String input = scanner.nextLine().trim();
            if ("?".equals(input)) throw new CancelInputException();
            if (input.isBlank()) return current;
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("  날짜 형식이 잘못됐습니다 (예: 2025-06-01). 다시 입력해주세요.");
            }
        }
    }

    private long askLong(String prompt) {
        while (true) {
            System.out.print(" " + prompt + " : ");
            String input = scanner.nextLine().trim();
            if ("?".equals(input)) throw new CancelInputException();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("  숫자를 입력해주세요.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 메인 메뉴
    // ─────────────────────────────────────────────────────
    public void blockPeriodManageView() {
        while (true) {
            AppScanner.cls();
            System.out.println("──────────────────────────────────────────────────────────────────");
            System.out.println("                       [ 제한 기간 관리 ]");
            System.out.println(" 1.목록조회  2.신규생성  3.대상적용  4.일정수정  5.일정삭제  0.뒤로");
            System.out.println("──────────────────────────────────────────────────────────────────");
            System.out.print("메뉴 선택: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = input.isEmpty() ? -1 : Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 입력입니다.");
                continue;
            }

            switch (choice) {
                case 1: searchAllBlockPeriods(); waitForBack(); break;
                case 2: enrollBlockMasterView(); break;
                case 3: enrollBlockDetailView(); break;
                case 4: updateBlockPeriodView(); break;
                case 5: deleteBlockPeriodView(); break;
                case 0: return;
                default: System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 1. 목록 조회
    // ─────────────────────────────────────────────────────
    private void searchAllBlockPeriods() {
        AppScanner.cls();
        System.out.println("\n[제한 일정 목록]");

        List<Map<String, Object>> list = controller.getAllBlockMasters();

        String sep = "─".repeat(62);
        System.out.println(sep);

        if (list.isEmpty()) {
            System.out.println("  등록된 제한 일정이 없습니다.");
            System.out.println(sep);
            return;
        }

        System.out.println(
            fit("ID", 4) + "  " +
            fit("명칭", 16) + "  " +
            fit("기간", 26) + "  " +
            fit("교시", 10)
        );
        System.out.println(sep);

        for (Map<String, Object> map : list) {
            String period  = map.get("periodName") != null ? (String) map.get("periodName") : "종일";
            String range   = map.get("start") + " ~ " + map.get("end");
            System.out.println(
                fit(String.valueOf(map.get("id")), 4) + "  " +
                fit(String.valueOf(map.get("desc")), 16) + "  " +
                fit(range, 26) + "  " +
                fit(period, 10)
            );
        }
        System.out.println(sep);
    }

    // ─────────────────────────────────────────────────────
    // 2. 신규 제한 일정 생성
    // ─────────────────────────────────────────────────────
    private void enrollBlockMasterView() {
        AppScanner.cls();
        System.out.println("\n[신규 제한 일정 생성]");
        System.out.println(CANCEL_NOTICE);
        try {
            System.out.println("\n── 일정 입력 ──────────────────────");
            LocalDate start  = askDate("시작 날짜");
            LocalDate end    = askDate("종료 날짜");
            String desc      = ask("제한 명칭");
            Integer periodId = selectPeriod();

            String periodDisplay = periodId != null ? "교시 ID " + periodId : "종일";

            System.out.println("\n── 등록 정보 확인 ──────────────────");
            System.out.println(" 기간  : " + start + " ~ " + end);
            System.out.println(" 교시  : " + periodDisplay);
            System.out.println(" 명칭  : " + desc);
            System.out.println("────────────────────────────────────");
            System.out.print("등록하시겠습니까? (Y/N): ");

            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                if (controller.enrollBlockMaster(start, end, desc, periodId) > 0)
                    System.out.println(">> 제한 일정이 등록되었습니다.");
                else
                    System.out.println(">> 등록 실패. 다시 시도해주세요.");
            } else {
                System.out.println(">> 등록이 취소되었습니다.");
            }
            waitForBack();
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
            waitForBack();
        }
    }

    // ─────────────────────────────────────────────────────
    // 3. 제한 대상 적용
    // ─────────────────────────────────────────────────────
    private void enrollBlockDetailView() {
        AppScanner.cls();
        System.out.println("\n[제한 대상 적용]");
        System.out.println(CANCEL_NOTICE);
        searchAllBlockPeriods();

        try {
            long masterId = askLong("\n적용할 제한 일정 ID");
            Map<String, Object> master = controller.getBlockPeriodById(masterId);

            if (master == null) {
                System.out.println(">> 존재하지 않는 제한 일정 ID입니다.");
                waitForBack();
                return;
            }

            System.out.println("\n── 선택된 일정 ──────────────────────");
            System.out.println(" ID   : " + masterId);
            System.out.println(" 명칭 : " + master.get("desc"));
            System.out.println(" 기간 : " + master.get("start") + " ~ " + master.get("end"));
            System.out.println("────────────────────────────────────");

            System.out.println("\n── 차단 적용 방식 선택 ──────────────");
            System.out.println(" 1. 모든 시설/비품 일괄 차단");
            System.out.println(" 2. 특정 시설 차단");
            System.out.println(" 3. 특정 비품 차단");
            System.out.println(" 0. 뒤로");
            System.out.print("선택: ");
            String typeInput = scanner.nextLine().trim();
            if ("?".equals(typeInput)) throw new CancelInputException();

            int type;
            try {
                type = Integer.parseInt(typeInput);
            } catch (NumberFormatException e) {
                System.out.println(">> 잘못된 입력입니다.");
                waitForBack();
                return;
            }

            int res = 0;

            if (type == 0) {
                return;
            } else if (type == 1) {
                res = controller.applyBlockToAll(masterId);
            } else if (type == 2) {
                List<Map<String, Object>> facilities = controller.getAllFacilities();
                System.out.println("\n── 시설 목록 ──────────────────────");
                String fsep = "─".repeat(40);
                System.out.println(fsep);
                for (Map<String, Object> f : facilities) {
                    System.out.printf("  ID %-4s | %s  (%s)%n", f.get("id"), f.get("name"), f.get("location"));
                }
                System.out.println(fsep);
                long facilityId = askLong("차단할 시설 ID");
                res = controller.enrollBlockDetail(masterId, "F", facilityId);
            } else if (type == 3) {
                List<Map<String, Object>> equipments = controller.getAllEquipments();
                System.out.println("\n── 비품 목록 ──────────────────────");
                String esep = "─".repeat(40);
                System.out.println(esep);
                for (Map<String, Object> e : equipments) {
                    System.out.printf("  ID %-4s | %s  (%s)%n", e.get("id"), e.get("name"), e.get("location"));
                }
                System.out.println(esep);
                long equipmentId = askLong("차단할 비품 ID");
                res = controller.enrollBlockDetail(masterId, "E", equipmentId);
            } else {
                System.out.println(">> 잘못된 입력입니다.");
                waitForBack();
                return;
            }

            System.out.println(res > 0
                ? ">> 차단 대상이 성공적으로 적용되었습니다."
                : ">> 적용 실패. 이미 등록된 대상이거나 오류가 발생했습니다.");
            waitForBack();
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
            waitForBack();
        }
    }

    // ─────────────────────────────────────────────────────
    // 4. 제한 일정 수정
    // ─────────────────────────────────────────────────────
    private void updateBlockPeriodView() {
        AppScanner.cls();
        System.out.println("\n[제한 일정 수정]");
        System.out.println(CANCEL_NOTICE);
        searchAllBlockPeriods();

        try {
            long id = askLong("\n수정할 일정 ID");
            Map<String, Object> current = controller.getBlockPeriodById(id);

            if (current == null) {
                System.out.println(">> 존재하지 않는 일정 ID입니다.");
                waitForBack();
                return;
            }

            String currentDesc          = (String) current.get("desc");
            String currentPeriodDisplay = current.get("periodName") != null
                ? (String) current.get("periodName") : "종일";

            System.out.println("\n── 현재 정보 ──────────────────────");
            System.out.println(" ID   : " + id);
            System.out.println(" 명칭 : " + currentDesc);
            System.out.println(" 기간 : " + current.get("start") + " ~ " + current.get("end"));
            System.out.println(" 교시 : " + currentPeriodDisplay);
            System.out.println("────────────────────────────────────");
            System.out.println("  ※ 변경하지 않을 항목은 엔터를 누르세요.");

            LocalDate nStart = askDateUpdate("새 시작일", (LocalDate) current.get("start"));
            LocalDate nEnd   = askDateUpdate("새 종료일", (LocalDate) current.get("end"));

            System.out.print(" 새 명칭 (유지: 엔터, 취소: ?) : ");
            String nDescInput = scanner.nextLine().trim();
            if ("?".equals(nDescInput)) throw new CancelInputException();
            String nDesc = nDescInput.isBlank() ? currentDesc : nDescInput;

            System.out.print(" 교시를 변경하시겠습니까? (Y/N): ");
            Integer nPeriodId;
            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                nPeriodId = selectPeriod();
            } else {
                nPeriodId = (Integer) current.get("periodId");
            }

            String nPeriodDisplay = nPeriodId != null ? "교시 ID " + nPeriodId : "종일";

            System.out.println("\n── 수정 예정 정보 ──────────────────");
            System.out.println(" 명칭 : " + nDesc);
            System.out.println(" 기간 : " + nStart + " ~ " + nEnd);
            System.out.println(" 교시 : " + nPeriodDisplay);
            System.out.println("────────────────────────────────────");
            System.out.print("수정하시겠습니까? (Y/N): ");

            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                if (controller.updateBlockMasterById(id, nStart, nEnd, nDesc, nPeriodId) > 0)
                    System.out.println(">> 수정이 완료되었습니다.");
                else
                    System.out.println(">> 수정 실패. 다시 시도해주세요.");
            } else {
                System.out.println(">> 수정이 취소되었습니다.");
            }
            waitForBack();
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
            waitForBack();
        }
    }

    // ─────────────────────────────────────────────────────
    // 5. 제한 일정 삭제
    // ─────────────────────────────────────────────────────
    private void deleteBlockPeriodView() {
        AppScanner.cls();
        System.out.println("\n[제한 일정 삭제]");
        System.out.println(CANCEL_NOTICE);
        searchAllBlockPeriods();

        try {
            String desc = ask("\n삭제할 명칭 입력");
            Map<String, Object> target = controller.getBlockPeriodByDescription(desc);

            if (target == null) {
                System.out.println(">> 존재하지 않는 일정입니다.");
                waitForBack();
                return;
            }

            System.out.println("\n── 삭제 대상 ──────────────────────");
            System.out.println(" 명칭 : " + desc);
            System.out.println(" 기간 : " + target.get("start") + " ~ " + target.get("end"));
            System.out.println("  ※ 관련 차단 상세 정보도 모두 삭제됩니다.");
            System.out.println("────────────────────────────────────");
            System.out.print("정말 삭제하시겠습니까? (Y/N): ");

            if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                if (controller.deleteBlockMasterByDesc(desc) > 0)
                    System.out.println(">> 삭제가 완료되었습니다.");
                else
                    System.out.println(">> 삭제 실패. 다시 시도해주세요.");
            } else {
                System.out.println(">> 삭제가 취소되었습니다.");
            }
            waitForBack();
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
            waitForBack();
        }
    }

    // ─────────────────────────────────────────────────────
    // 교시 선택
    // null 반환 = 종일 제한, CancelInputException = 취소
    // ─────────────────────────────────────────────────────
    private Integer selectPeriod() {
        List<Map<String, Object>> periods = controller.getAllPeriods();

        System.out.println("\n── 교시 선택 ──────────────────────");
        System.out.println(" 0. 종일 제한 (교시 구분 없음)");
        for (int i = 0; i < periods.size(); i++) {
            Map<String, Object> p = periods.get(i);
            System.out.printf(" %d. %s  (%s ~ %s)%n",
                i + 1, p.get("name"), p.get("startTime"), p.get("endTime"));
        }
        System.out.print("선택: ");
        String input = scanner.nextLine().trim();
        if ("?".equals(input)) throw new CancelInputException();

        try {
            int choice = Integer.parseInt(input);
            if (choice == 0 || choice < 0 || choice > periods.size()) return null;
            return (Integer) periods.get(choice - 1).get("id");
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ─────────────────────────────────────────────────────
    // 엔터 대기
    // ─────────────────────────────────────────────────────
    private void waitForBack() {
        System.out.println(" 0. 뒤로가기");
        System.out.print("선택: ");
        scanner.nextLine();
    }
}
