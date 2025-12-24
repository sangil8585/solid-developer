package com.book.soliddeveloper.codeTest;

import java.util.Scanner;

public class ChristmasTree {
    static char[][] map;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.close();

        map = new char[n][2 * n - 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 2 * n - 1; j++) {
                map[i][j] = ' ';
            }
        }

        drawTriangle(0, n - 1, n);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 2 * n - 1; j++) {
                sb.append(map[i][j]);
            }
            sb.append('\n');
        }
        System.out.print(sb.toString());
    }

    static void drawTriangle(int row, int col, int height) {
        if (height == 3) {
            map[row][col] = '*';
            map[row + 1][col - 1] = '*';
            map[row + 1][col + 1] = '*';
            map[row + 2][col - 2] = '*';
            map[row + 2][col - 1] = '*';
            map[row + 2][col] = '*';
            map[row + 2][col + 1] = '*';
            map[row + 2][col + 2] = '*';
            return;
        }

        int half = height / 2;

        drawTriangle(row, col, half);

        drawTriangle(row + half, col - half, half);

        drawTriangle(row + half, col + half, half);
    }
}


