package com.chase.battleship.gui;

import com.chase.battleship.core.Board;
import com.chase.battleship.core.CellState;
import com.chase.battleship.core.Coordinate;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BoardView extends GridPane {

    public interface CellClickListener {
        void onCellClicked(int row, int col);
    }

    private static final double CELL_SIZE = 32;

    private Board board;
    private boolean showShips;
    private CellClickListener clickListener;

    public BoardView(Board board, boolean showShips) {
        this.board = board;
        this.showShips = showShips;

        setHgap(2);
        setVgap(2);
        setPadding(new Insets(10));

        buildCells();
        refresh();
    }

    public void setBoard(Board board) {
        this.board = board;
        refresh();
    }

    public void setShowShips(boolean showShips) {
        this.showShips = showShips;
        refresh();
    }

    public void setCellClickListener(CellClickListener listener) {
        this.clickListener = listener;
    }

    private void buildCells() {
        getChildren().clear();
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.setArcWidth(4);
                rect.setArcHeight(4);

                final int rr = r;
                final int cc = c;
                rect.setOnMouseClicked(e -> {
                    if (clickListener != null) {
                        clickListener.onCellClicked(rr, cc);
                    }
                });

                GridPane.setRowIndex(rect, r);
                GridPane.setColumnIndex(rect, c);
                getChildren().add(rect);
            }
        }
    }

    public void refresh() {
        for (Node n : getChildren()) {
            if (!(n instanceof Rectangle rect)) continue;
            Integer r = GridPane.getRowIndex(rect);
            Integer c = GridPane.getColumnIndex(rect);
            if (r == null || c == null) continue;

            CellState state = board.getCellState(new Coordinate(r, c));
            rect.setFill(colorFor(state));
            rect.setStroke(Color.web("#001f2b"));
        }
    }

    private Color colorFor(CellState state) {
        // base colors
        Color water = Color.web("#007b80");
        Color ship  = Color.web("#c2c2c2");
        Color hit   = Color.web("#ff5555");
        Color miss  = Color.web("#004050");

        switch (state) {
            case SHIP:
                return showShips ? ship : water;
            case HIT:
				return hit;
            case MISS:
                return miss;
            case EMPTY:
            default:
                return water;
        }
    }
}
