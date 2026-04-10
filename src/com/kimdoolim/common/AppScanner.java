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

  /** 콘솔 화면 클리어 */
  public static void cls() {
    System.out.print("\033[H\033[2J");
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
}