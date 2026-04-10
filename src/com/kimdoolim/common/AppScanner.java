package com.kimdoolim.common;

import java.util.Scanner;

/**
 * 프로그램 전체에서 Scanner를 딱 하나만 사용하기 위한 클래스
 * Scanner를 여러 개 만들면 System.in 충돌이 나서
 * 이 클래스에서 하나만 만들고 어디서든 가져다 씀
 *
 * 사용법: AppScanner.getScanner()
 */
public class AppScanner {

  private static final Scanner scanner = new Scanner(System.in);

  private AppScanner() {}

  public static Scanner getScanner() {
    return scanner;
  }

  /** 메시지 확인 후 Enter 대기 */
  public static void pause() {
    System.out.print("\n  [ Enter 를 누르면 계속합니다 ] ");
    System.out.flush();
    try {
      // Windows/PowerShell 에서 이전 nextLine() 후 \r\n 잔여분을 제거
      try { scanner.skip("[\\r\\n]*"); } catch (Exception ignored) {}
      scanner.nextLine();
    } catch (Exception ignored) {}
  }

  /** 콘솔 화면 클리어 (보이는 화면 + 스크롤 버퍼 전부 삭제) */
  public static void cls() {
    System.out.print("\033[H\033[2J\033[3J");
    System.out.flush();
  }

  // ── ANSI 색상 코드 ──────────────────────────────────────
  public static final String RESET   = "\033[0m";
  public static final String RED     = "\033[91m";
  public static final String GREEN   = "\033[92m";
  public static final String YELLOW  = "\033[93m";
  public static final String BLUE    = "\033[94m";
  public static final String MAGENTA = "\033[95m";
  public static final String CYAN    = "\033[96m";
  public static final String WHITE   = "\033[97m";
  public static final String BOLD    = "\033[1m";

  /** 한글 2칸, ASCII 1칸으로 계산해 width 칸에 맞게 padding/truncate */
  public static String fit(String s, int width) {
    int dw = 0, i = 0;
    for (; i < s.length(); i++) {
      int cw = (s.charAt(i) >= '\uAC00' && s.charAt(i) <= '\uD7A3') ? 2 : 1;
      if (dw + cw > width) break;
      dw += cw;
    }
    return s.substring(0, i) + " ".repeat(width - dw);
  }
}