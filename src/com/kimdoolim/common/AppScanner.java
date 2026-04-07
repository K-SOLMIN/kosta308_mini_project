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

  // 외부에서 new AppScanner() 못 하게 막기
  private AppScanner() {}

  public static Scanner getScanner() {
    return scanner;
  }
}