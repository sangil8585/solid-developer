package com.book.soliddeveloper.codeTest;

import java.util.Scanner;

public class ChristmasTree {
    static char[][] map;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.close();

        // 전체 맵 초기화 (공백으로 채움)
        map = new char[n][2 * n - 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 2 * n - 1; j++) {
                map[i][j] = ' ';
            }
        }

        // 재귀적으로 삼각형 그리기
        drawTriangle(0, n - 1, n);

        // 결과 출력
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 2 * n - 1; j++) {
                sb.append(map[i][j]);
            }
            sb.append('\n');
        }
        System.out.print(sb.toString());
    }

    // 재귀 함수: (row, col) 위치에서 높이 height인 삼각형 그리기
    // col은 삼각형의 꼭짓점(가장 위쪽 별)의 열 위치
    static void drawTriangle(int row, int col, int height) {
        // 기본 케이스: 높이가 3일 때
        if (height == 3) {
            map[row][col] = '*';                    // 꼭짓점
            map[row + 1][col - 1] = '*';            // 왼쪽
            map[row + 1][col + 1] = '*';            // 오른쪽
            map[row + 2][col - 2] = '*';            // 왼쪽 끝
            map[row + 2][col - 1] = '*';
            map[row + 2][col] = '*';
            map[row + 2][col + 1] = '*';
            map[row + 2][col + 2] = '*';            // 오른쪽 끝
            return;
        }

        // 재귀 케이스: 높이를 반으로 나눠서 3개의 작은 삼각형 그리기
        int half = height / 2;

        // 1. 위쪽 삼각형 (중앙)
        drawTriangle(row, col, half);

        // 2. 아래쪽 왼쪽 삼각형
        drawTriangle(row + half, col - half, half);

        // 3. 아래쪽 오른쪽 삼각형
        drawTriangle(row + half, col + half, half);
    }
}


