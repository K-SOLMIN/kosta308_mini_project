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

    // 선택된 교시의 표시 이름 (교시 ID 대신 이름을 확인 화면에 보여주기 위함)
    private String lastSelectedPeriodName = "종일";

    private static final String SEP  = "─".repeat(70);
    private static final String SEP2 = "────────────────────────────────────";
    private static final String CANCEL_NOTICE = "  ※ 입력 중 ?를 누르면 언제든 취소됩니다.";

    // ? 입력 시 현재 입력 흐름을 즉시 중단하기 위한 예외
    private static class CancelInputException extends RuntimeException {
        CancelInputException() { super(null, null, true, false); }
    }

    // ─────────────────────────────────────────────────────
    // 입력 헬퍼
    // ─────────────────────────────────────────────────────

    private String ask(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine().trim();
        if ("?".equals(input)) throw new CancelInputException();
        return input;
    }

    private LocalDate askDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();
            if ("?".equals(input)) throw new CancelInputException();
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("  날짜 형식이 올바르지 않습니다. (예: 2026-04-01)");
            }
        }
    }

    private LocalDate askDateUpdate(String prompt, LocalDate current) {
        while (true) {
            System.out.print(prompt + " (유지: 엔터, yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();
            if ("?".equals(input)) throw new CancelInputException();
            if (input.isBlank()) return current;
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("  날짜 형식이 올바르지 않습니다. (예: 2026-04-01)");
            }
        }
    }

    private long askLong(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
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
            System.out.println(SEP);
            System.out.println("                      [ 제한기간 관리 ]");
            System.out.println(SEP);
            System.out.println(" 1.목록 조회 || 2.일정 생성 || 3.대상 적용 || 4.일정 수정 || 5.일정 삭제");
            System.out.println(" 6.적용 대상 조회 || 7.교시별 예약 차단 관리 || 0.뒤로 가기");
            System.out.println(SEP);
            System.out.print("메뉴 선택: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1: searchAllBlockPeriods(); waitForBack(); break;
                case 2: enrollBlockMasterView(); break;
                case 3: enrollBlockDetailView(); break;
                case 4: updateBlockPeriodView(); break;
                case 5: deleteBlockPeriodView(); break;
                case 6: showBlockAppliedView(); break;
                case 7: new BlockScheduleView().blockScheduleMenu(); break;
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

        System.out.println(SEP);
        if (list.isEmpty()) {
            System.out.println("  등록된 제한 일정이 없습니다.");
            System.out.println(SEP);
            return;
        }

        System.out.println(
            fit("ID", 4) + "  " +
            fit("명칭", 22) + "  " +
            fit("시작일", 12) + "  " +
            fit("종료일", 12) + "  " +
            "교시"
        );
        System.out.println(SEP);
        for (Map<String, Object> m : list) {
            String periodName = m.get("periodName") != null ? (String) m.get("periodName") : "종일";
            System.out.println(
                fit(m.get("id").toString(), 4) + "  " +
                fit((String) m.get("desc"), 22) + "  " +
                fit(m.get("start").toString(), 12) + "  " +
                fit(m.get("end").toString(), 12) + "  " +
                periodName
            );
        }
        System.out.println(SEP);
    }

    // ─────────────────────────────────────────────────────
    // 2. 신규 제한 일정 생성
    // ─────────────────────────────────────────────────────
    private void enrollBlockMasterView() {
        AppScanner.cls();
        System.out.println("\n[신규 제한 일정 생성]");
        System.out.println(CANCEL_NOTICE);
        System.out.println(SEP);

        try {
            LocalDate start  = askDate("시작 날짜");
            LocalDate end    = askDate("종료 날짜");
            String desc      = ask("제한 명칭");
            Integer periodId = selectPeriod();

            System.out.println("\n── 등록 정보 확인 " + SEP2.substring(9));
            System.out.println(" 명칭  : " + desc);
            System.out.println(" 기간  : " + start + " ~ " + end);
            System.out.println(" 교시  : " + lastSelectedPeriodName);
            System.out.println(SEP2);
            System.out.print("등록하시겠습니까? (Y/N): ");

            if (scanner.nextLine().trim().toUpperCase().equals("Y")) {
                if (controller.enrollBlockMaster(start, end, desc, periodId) > 0)
                    System.out.println(">> 제한 일정이 등록되었습니다.");
                else
                    System.out.println(">> 등록에 실패했습니다. 다시 시도해주세요.");
            } else {
                System.out.println("등록이 취소되었습니다.");
            }
            waitForBack();
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소되었습니다.");
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
            long masterId = askLong("적용할 제한 일정 ID");
            Map<String, Object> master = controller.getBlockPeriodById(masterId);

            if (master == null) {
                System.out.println("존재하지 않는 제한 일정 ID입니다.");
                waitForBack();
                return;
            }

            System.out.println("  선택된 일정: [" + masterId + "] " + master.get("desc")
                + "  (" + master.get("start") + " ~ " + master.get("end") + ")");
            System.out.println(SEP);
            System.out.println(" 1. 모든 시설/비품 일괄 차단");
            System.out.println(" 2. 특정 시설 차단");
            System.out.println(" 3. 특정 비품 차단");
            System.out.println(SEP);
            System.out.print("선택: ");
            String typeInput = scanner.nextLine().trim();
            if ("?".equals(typeInput)) throw new CancelInputException();

            int type;
            try {
                type = Integer.parseInt(typeInput);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 입력입니다.");
                waitForBack();
                return;
            }

            int res = 0;

            if (type == 1) {
                System.out.print("모든 시설/비품에 일괄 적용하시겠습니까? (Y/N): ");
                if (!scanner.nextLine().trim().toUpperCase().equals("Y")) {
                    System.out.println("취소되었습니다.");
                    waitForBack();
                    return;
                }
                res = controller.applyBlockToAll(masterId);

            } else if (type == 2) {
                List<Map<String, Object>> facilities = controller.getAllFacilities();
                System.out.println("\n[시설 목록]");
                System.out.println(SEP);
                System.out.println(fit("ID", 4) + "  " + fit("시설명", 20) + "  " + "위치");
                System.out.println(SEP);
                for (Map<String, Object> f : facilities) {
                    System.out.println(fit(f.get("id").toString(), 4) + "  " +
                        fit((String) f.get("name"), 20) + "  " + f.get("location"));
                }
                System.out.println(SEP);
                long facilityId = askLong("차단할 시설 ID");
                res = controller.enrollBlockDetail(masterId, "F", facilityId);

            } else if (type == 3) {
                List<Map<String, Object>> equipments = controller.getAllEquipments();
                System.out.println("\n[비품 목록]");
                System.out.println(SEP);
                System.out.println(fit("ID", 4) + "  " + fit("비품명", 20) + "  " + "위치");
                System.out.println(SEP);
                for (Map<String, Object> e : equipments) {
                    System.out.println(fit(e.get("id").toString(), 4) + "  " +
                        fit((String) e.get("name"), 20) + "  " + e.get("location"));
                }
                System.out.println(SEP);
                long equipmentId = askLong("차단할 비품 ID");
                res = controller.enrollBlockDetail(masterId, "E", equipmentId);

            } else {
                System.out.println("잘못된 입력입니다.");
                waitForBack();
                return;
            }

            if (res > 0) System.out.println(">> 차단 대상이 적용되었습니다.");
            else         System.out.println(">> 적용에 실패했습니다. 이미 등록된 대상이거나 오류가 발생했습니다.");
            waitForBack();

        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소되었습니다.");
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
            long id = askLong("수정할 일정 ID");
            Map<String, Object> current = controller.getBlockPeriodById(id);

            if (current == null) {
                System.out.println("존재하지 않는 일정 ID입니다.");
                waitForBack();
                return;
            }

            String currentDesc          = (String) current.get("desc");
            String currentPeriodDisplay = current.get("periodName") != null
                ? (String) current.get("periodName") : "종일";

            System.out.println("\n── 현재 정보 " + SEP2.substring(5));
            System.out.println(" ID    : " + id);
            System.out.println(" 명칭  : " + currentDesc);
            System.out.println(" 기간  : " + current.get("start") + " ~ " + current.get("end"));
            System.out.println(" 교시  : " + currentPeriodDisplay);
            System.out.println("  (변경하지 않을 항목은 엔터로 넘어가세요)");
            System.out.println(SEP2);

            LocalDate nStart = askDateUpdate("새 시작일", (LocalDate) current.get("start"));
            LocalDate nEnd   = askDateUpdate("새 종료일", (LocalDate) current.get("end"));

            System.out.print("새 명칭 (유지: 엔터): ");
            String nDescInput = scanner.nextLine().trim();
            if ("?".equals(nDescInput)) throw new CancelInputException();
            String nDesc = nDescInput.isBlank() ? currentDesc : nDescInput;

            System.out.print("교시를 변경하시겠습니까? (Y/N): ");
            Integer nPeriodId;
            if (scanner.nextLine().trim().toUpperCase().equals("Y")) {
                nPeriodId = selectPeriod();
            } else {
                nPeriodId = (Integer) current.get("periodId");
                lastSelectedPeriodName = currentPeriodDisplay;
            }

            System.out.println("\n── 수정 내용 확인 " + SEP2.substring(9));
            System.out.println(" 명칭  : " + nDesc);
            System.out.println(" 기간  : " + nStart + " ~ " + nEnd);
            System.out.println(" 교시  : " + lastSelectedPeriodName);
            System.out.println(SEP2);
            System.out.print("수정하시겠습니까? (Y/N): ");

            if (scanner.nextLine().trim().toUpperCase().equals("Y")) {
                if (controller.updateBlockMasterById(id, nStart, nEnd, nDesc, nPeriodId) > 0)
                    System.out.println(">> 수정이 완료되었습니다.");
                else
                    System.out.println(">> 수정에 실패했습니다. 다시 시도해주세요.");
            } else {
                System.out.println("수정이 취소되었습니다.");
            }
            waitForBack();

        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소되었습니다.");
            waitForBack();
        }
    }

    // ─────────────────────────────────────────────────────
    // 6. 적용 대상 조회
    // ─────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void showBlockAppliedView() {
        AppScanner.cls();
        System.out.println("\n[제한 일정 적용 대상 조회]");
        List<Map<String, Object>> list = controller.getAllBlockMasters();

        System.out.println(SEP);
        if (list.isEmpty()) {
            System.out.println("  등록된 제한 일정이 없습니다.");
            System.out.println(SEP);
            waitForBack();
            return;
        }

        System.out.println(
            fit("ID", 4) + "  " +
            fit("명칭", 22) + "  " +
            fit("시작일", 12) + "  " +
            fit("종료일", 12) + "  " +
            "교시"
        );
        System.out.println(SEP);

        for (Map<String, Object> m : list) {
            String periodName = m.get("periodName") != null ? (String) m.get("periodName") : "종일";
            System.out.println(
                fit(m.get("id").toString(), 4) + "  " +
                fit((String) m.get("desc"), 22) + "  " +
                fit(m.get("start").toString(), 12) + "  " +
                fit(m.get("end").toString(), 12) + "  " +
                periodName
            );

            long masterId = (long) m.get("id");
            Map<String, Object> details = controller.getBlockDetailsForDisplay(masterId);
            boolean isAll = (boolean) details.get("isAll");
            List<String> facilities = (List<String>) details.get("facilities");
            List<String> equipments = (List<String>) details.get("equipments");

            if (isAll) {
                System.out.println("      └ 전체 시설/비품 적용");
            } else if (facilities.isEmpty() && equipments.isEmpty()) {
                System.out.println("      └ 적용된 시설/비품 없음");
            } else {
                boolean hasBoth = !facilities.isEmpty() && !equipments.isEmpty();
                if (!facilities.isEmpty()) {
                    String prefix = hasBoth ? "├" : "└";
                    System.out.println("      " + prefix + " [시설] " + String.join(" / ", facilities));
                }
                if (!equipments.isEmpty()) {
                    System.out.println("      └ [비품] " + String.join(" / ", equipments));
                }
            }
            System.out.println(SEP);
        }

        waitForBack();
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
            long id = askLong("삭제할 일정 ID");
            Map<String, Object> target = controller.getBlockPeriodById(id);
            if (target == null) {
                System.out.println("존재하지 않는 일정 ID입니다.");
                waitForBack();
                return;
            }

            System.out.println("\n── 삭제 대상 " + SEP2.substring(5));
            System.out.println(" ID    : " + id);
            System.out.println(" 명칭  : " + target.get("desc"));
            System.out.println(" 기간  : " + target.get("start") + " ~ " + target.get("end"));
            System.out.println("  ※ 관련 차단 상세 정보도 모두 삭제됩니다.");
            System.out.println(SEP2);
            System.out.print("삭제하시겠습니까? (Y/N): ");

            if (scanner.nextLine().trim().toUpperCase().equals("Y")) {
                if (controller.deleteBlockMasterById(id) > 0)
                    System.out.println(">> 삭제가 완료되었습니다.");
                else
                    System.out.println(">> 삭제에 실패했습니다. 다시 시도해주세요.");
            } else {
                System.out.println("삭제가 취소되었습니다.");
            }
            waitForBack();

        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소되었습니다.");
            waitForBack();
        }
    }

    // ─────────────────────────────────────────────────────
    // 뒤로가기 대기
    // ─────────────────────────────────────────────────────
    private void waitForBack() {
        while (true) {
            System.out.println(" 0. 뒤로가기");
            System.out.print("선택: ");
            if ("0".equals(scanner.nextLine().trim())) return;
        }
    }

    // ─────────────────────────────────────────────────────
    // 교시 선택 헬퍼
    // null 반환 = 종일 제한 / lastSelectedPeriodName 갱신
    // ─────────────────────────────────────────────────────
    private Integer selectPeriod() {
        List<Map<String, Object>> periods = controller.getAllPeriods();

        System.out.println("\n── 교시 선택 ──────────────────────");
        System.out.println(" 0. 종일 제한 (교시 구분 없음)");
        for (int i = 0; i < periods.size(); i++) {
            Map<String, Object> p = periods.get(i);
            System.out.printf(" %d. %s  (%s ~ %s)%n", i + 1, p.get("name"), p.get("startTime"), p.get("endTime"));
        }
        System.out.println(SEP2);
        System.out.print("선택: ");
        String input = scanner.nextLine().trim();
        if ("?".equals(input)) throw new CancelInputException();

        try {
            int choice = Integer.parseInt(input);
            if (choice <= 0 || choice > periods.size()) {
                lastSelectedPeriodName = "종일";
                return null;
            }
            Map<String, Object> selected = periods.get(choice - 1);
            lastSelectedPeriodName = (String) selected.get("name");
            return (Integer) selected.get("id");
        } catch (NumberFormatException e) {
            lastSelectedPeriodName = "종일";
            return null;
        }
    }
}
