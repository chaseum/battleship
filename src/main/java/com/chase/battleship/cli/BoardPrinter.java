package com.chase.battleship.cli;

import com.chase.battleship.core.*;

import java.util.ArrayList;
import java.util.List;

public class BoardPrinter {
	public static void printBoard(Board board, boolean showShips) {
        renderBoard(board, showShips).forEach(System.out::println);
    }

    public static List<String> renderBoard(Board board, boolean showShips) {
        List<String> lines = new ArrayList<>();

        // header row
        StringBuilder header = new StringBuilder("   ");
        for (int c = 0; c < board.getCols(); c++) {
            header.append(c).append(' ');
        }
        lines.add(header.toString());

        for (int r = 0; r < board.getRows(); r++) {
            StringBuilder row = new StringBuilder();
            row.append(' ').append(r).append(' ');

            for (int c = 0; c < board.getCols(); c++) {
                Coordinate coord = new Coordinate(r, c);
                CellState state = board.getCellState(coord);
                char ch = '.';

                switch (state) {
                    case EMPTY -> ch = '.';
                    case SHIP  -> ch = showShips ? 'S' : '.';
                    case HIT   -> ch = 'X';
                    case MISS  -> ch = 'o';
                    default    -> ch = '?';
                }
                row.append(ch).append(' ');
            }
            lines.add(row.toString());
        }

        return lines;
    }
}