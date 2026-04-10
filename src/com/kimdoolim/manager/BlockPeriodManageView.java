package com.kimdoolim.manager;

import com.kimdoolim.common.AppScanner;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class BlockPeriodManageView {
    private final BlockPeriodController controller = BlockPeriodController.getBlockPeriodController();
    private final Scanner scanner = new Scanner(System.in);

    // ? 입력 시 현재 입력 흐름을 즉시 중단하기 위한 예외
    private static class CancelInputException extends RuntimeException {
        CancelInputException() { super(null, null, true, false); }
    }

    // ─── 입력 헬퍼 ───────────────────────────────────────────

    private static final String CANCEL_NOTICE = "  (언제든 ?를 입력하면 취소됩니다)";

    /** 일반 텍스트 입력. ? 입력 시 취소 */
    private String ask(String prompt) {
        System.out.print(prompt + " : ");
        String input = scanner.nextLine().trim();
        if ("?".equals(input)) throw new CancelInputException();
        return input;
    }

    /** 날짜 입력. 형식 오류 시 재입력, ? 입력 시 취소 */
    private LocalDate askDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-MM-dd) : ");
            String input = scanner.nextLine().trim();
            if ("?".equals(input)) throw new CancelInputException();
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("  날짜 형식이 잘못됐습니다 (예: 2025-06-01). 다시 입력해주세요.");
            }
        }
    }

    /** 수정용 날짜 입력. 엔터 = 기존값 유지, 형식 오류 시 재입력, ? 입력 시 취소 */
    private LocalDate askDateUpdate(String prompt, LocalDate current) {
        while (true) {
            System.out.print(prompt + " (유지: 엔터, yyyy-MM-dd) : ");
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

    /** 숫자(long) 입력. 형식 오류 시 재입력, ? 입력 시 취소 */
    private long askLong(String prompt) {
        while (true) {
            System.out.print(prompt + " : ");
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
            System.out.println("=============================");
            System.out.println("       제한 기간 관리 메뉴     ");
            System.out.println("=============================");
            System.out.println(" 1. 제한 일정 목록 조회");
            System.out.println(" 2. 신규 제한 일정 생성");
            System.out.println(" 3. 제한 대상 적용");
            System.out.println(" 4. 제한 일정 수정");
            System.out.println(" 5. 제한 일정 삭제");
            System.out.println(" 0. 뒤로 가기");
            System.out.println("=============================");
            System.out.print("메뉴 선택 : ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = input.isEmpty() ? -1 : Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 입력입니다.");
                continue;
            }

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

    // ─────────────────────────────────────────────────────
    // 1. 조회
    // ─────────────────────────────────────────────────────
    private void searchAllBlockPeriods() {
        List<Map<String, Object>> list = controller.getAllBlockMasters();
        System.out.println("\n[현재 등록된 제한 일정]");
        System.out.println("============================================================");
        if (list.isEmpty()) {
            System.out.println("  등록된 제한 일정이 없습니다.");
        } else {
            for (Map<String, Object> map : list) {
                String period = map.get("periodName") != null ? (String) map.get("periodName") : "종일";
                System.out.println("  ID    : " + map.get("id"));
                System.out.println("  명칭  : " + map.get("desc"));
                System.out.println("  기간  : " + map.get("start") + "  ~  " + map.get("end"));
                System.out.println("  교시  : " + period);
                System.out.println("------------------------------------------------------------");
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // 2. 등록
    // ─────────────────────────────────────────────────────
    private void enrollBlockMasterView() {
        System.out.println("\n[신규 제한 일정 생성]");
        System.out.println(CANCEL_NOTICE);
        try {
            LocalDate start = askDate("시작 날짜");
            LocalDate end   = askDate("종료 날짜");
            String desc     = ask("제한 명칭");
            Integer periodId = selectPeriod();

            System.out.println("\n[등록 정보 확인]");
            System.out.println("> 기간   : " + start + " ~ " + end);
            System.out.println("> 교시   : " + (periodId != null ? "교시 ID " + periodId : "종일"));
            System.out.println("> 명칭   : " + desc);
            System.out.print("이대로 생성하시겠습니까? (1.예 / 0.아니오) : ");

            if (scanner.nextLine().trim().equals("1")) {
                if (controller.enrollBlockMaster(start, end, desc, periodId) > 0)
                    System.out.println(">> 제한 일정 등록 완료!");
                else
                    System.out.println(">> 등록 실패. 다시 시도해주세요.");
            }
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
        }
    }

    // ─────────────────────────────────────────────────────
    // 3. 디테일 적용
    // ─────────────────────────────────────────────────────
    private void enrollBlockDetailView() {
        System.out.println("\n[제한 대상 적용]");
        System.out.println(CANCEL_NOTICE);
        searchAllBlockPeriods();

        try {
            long masterId = askLong("적용할 제한 일정 ID");
            Map<String, Object> master = controller.getBlockPeriodById(masterId);

            if (master == null) {
                System.out.println(">> 존재하지 않는 제한 일정 ID입니다.");
                return;
            }

            System.out.println("  선택된 일정 : [" + masterId + "] " + master.get("desc")
                + "  (" + master.get("start") + " ~ " + master.get("end") + ")");

            System.out.println("\n  1. 모든 시설/비품 일괄 차단");
            System.out.println("  2. 특정 시설 차단");
            System.out.println("  3. 특정 비품 차단");
            System.out.print("선택 : ");
            String typeInput = scanner.nextLine().trim();
            if ("?".equals(typeInput)) throw new CancelInputException();

            int type;
            try {
                type = Integer.parseInt(typeInput);
            } catch (NumberFormatException e) {
                System.out.println(">> 잘못된 입력입니다.");
                return;
            }

            int res = 0;

            if (type == 1) {
                res = controller.applyBlockToAll(masterId);
            } else if (type == 2) {
                List<Map<String, Object>> facilities = controller.getAllFacilities();
                System.out.println("\n[시설 목록]");
                System.out.println("============================================================");
                for (Map<String, Object> f : facilities) {
                    System.out.printf("  ID %-4s | %s  (%s)%n", f.get("id"), f.get("name"), f.get("location"));
                }
                System.out.println("============================================================");
                long facilityId = askLong("차단할 시설 ID");
                res = controller.enrollBlockDetail(masterId, "F", facilityId);
            } else if (type == 3) {
                List<Map<String, Object>> equipments = controller.getAllEquipments();
                System.out.println("\n[비품 목록]");
                System.out.println("============================================================");
                for (Map<String, Object> e : equipments) {
                    System.out.printf("  ID %-4s | %s  (%s)%n", e.get("id"), e.get("name"), e.get("location"));
                }
                System.out.println("============================================================");
                long equipmentId = askLong("차단할 비품 ID");
                res = controller.enrollBlockDetail(masterId, "E", equipmentId);
            }

            if (res > 0) System.out.println(">> 차단 대상이 성공적으로 적용되었습니다.");
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
        }
    }

    // ─────────────────────────────────────────────────────
    // 4. 수정
    // ─────────────────────────────────────────────────────
    private void updateBlockPeriodView() {
        System.out.println("\n[제한 일정 수정]");
        System.out.println(CANCEL_NOTICE);
        searchAllBlockPeriods();

        try {
            long id = askLong("수정할 일정 ID");
            Map<String, Object> current = controller.getBlockPeriodById(id);

            if (current == null) {
                System.out.println(">> 존재하지 않는 일정 ID입니다.");
                return;
            }

            String currentDesc        = (String) current.get("desc");
            String currentPeriodDisplay = current.get("periodName") != null
                ? (String) current.get("periodName") : "종일";

            System.out.println("\n--- 현재 정보 ---");
            System.out.println("  ID    : " + id);
            System.out.println("  명칭  : " + currentDesc);
            System.out.println("  기간  : " + current.get("start") + "  ~  " + current.get("end"));
            System.out.println("  교시  : " + currentPeriodDisplay);
            System.out.println("-----------------");
            System.out.println("  (변경하지 않을 항목은 엔터)");

            LocalDate nStart = askDateUpdate("새 시작일", (LocalDate) current.get("start"));
            LocalDate nEnd   = askDateUpdate("새 종료일", (LocalDate) current.get("end"));

            System.out.print("새 명칭 (유지: 엔터, 취소: ?) : ");
            String nDescInput = scanner.nextLine().trim();
            if ("?".equals(nDescInput)) throw new CancelInputException();
            String nDesc = nDescInput.isBlank() ? currentDesc : nDescInput;

            System.out.print("교시를 변경하시겠습니까? (1.예 / 0.유지) : ");
            Integer nPeriodId;
            if (scanner.nextLine().trim().equals("1")) {
                nPeriodId = selectPeriod();
            } else {
                nPeriodId = (Integer) current.get("periodId");
            }

            String nPeriodDisplay = nPeriodId != null ? "교시 ID " + nPeriodId : "종일";

            System.out.println("\n--- 수정 예정 정보 ---");
            System.out.println("  명칭  : " + nDesc);
            System.out.println("  기간  : " + nStart + "  ~  " + nEnd);
            System.out.println("  교시  : " + nPeriodDisplay);
            System.out.println("---------------------");
            System.out.print("정말 수정하시겠습니까? (1.예 / 0.아니오) : ");

            if (scanner.nextLine().trim().equals("1")) {
                if (controller.updateBlockMasterById(id, nStart, nEnd, nDesc, nPeriodId) > 0)
                    System.out.println(">> 수정 완료!");
                else
                    System.out.println(">> 수정 실패. 다시 시도해주세요.");
            } else {
                System.out.println("수정이 취소되었습니다.");
            }
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
        }
    }

    // ─────────────────────────────────────────────────────
    // 5. 삭제
    // ─────────────────────────────────────────────────────
    private void deleteBlockPeriodView() {
        System.out.println("\n[제한 일정 삭제]");
        System.out.println(CANCEL_NOTICE);
        searchAllBlockPeriods();

        try {
            String desc = ask("\n삭제할 명칭 입력");
            Map<String, Object> target = controller.getBlockPeriodByDescription(desc);
            if (target == null) {
                System.out.println(">> 존재하지 않는 일정입니다.");
                return;
            }

            System.out.println("\n[삭제 대상]");
            System.out.println("> " + target.get("start") + " ~ " + target.get("end") + " / " + desc);
            System.out.println("※ 관련 차단 상세 정보도 모두 삭제됩니다.");
            System.out.print("정말 삭제하시겠습니까? (1.예 / 0.아니오) : ");

            if (scanner.nextLine().trim().equals("1")) {
                if (controller.deleteBlockMasterByDesc(desc) > 0)
                    System.out.println(">> 삭제 완료!");
                else
                    System.out.println(">> 삭제 실패. 다시 시도해주세요.");
            } else {
                System.out.println("삭제가 취소되었습니다.");
            }
        } catch (CancelInputException e) {
            System.out.println(">> 입력이 취소됐습니다.");
        }
    }

    // ─────────────────────────────────────────────────────
    // 교시 선택 공통 메서드
    // null 반환 = 종일 제한, CancelInputException = 취소
    // ─────────────────────────────────────────────────────
    private Integer selectPeriod() {
        List<Map<String, Object>> periods = controller.getAllPeriods();

        System.out.println("\n── 제한 교시 선택 ──────────────────");
        System.out.println(" 0. 종일 제한 (교시 구분 없음)");
        for (int i = 0; i < periods.size(); i++) {
            Map<String, Object> p = periods.get(i);
            System.out.printf(" %d. %s  (%s ~ %s)%n",
                i + 1, p.get("name"), p.get("startTime"), p.get("endTime"));
        }
        System.out.print("선택 : ");
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
}
