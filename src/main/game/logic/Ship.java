package src.main.game.logic;

import java.awt.image.*;

public class Ship {
	public int startX;
	public int startY;
	public int length;
	public int[] tiles;
	public BufferedImage im;
	public boolean horizontal;
	public int tile;

	public Ship(int x, int y, int length, String name) {
		startX = x;
		startY = y;
		this.length = length;
		horizontal = true;
		tiles = new int[length];
		tile = length;
	}

	public Ship(int length, String name) {
		this(-1, -1, length, name);

	}

	public void setX(int x) {
		startX = x;
	}

	public void setY(int y) {
		startY = y;
	}

	public int getX() {
		return startX;
	}

	public int getY() {
		return startY;
	}

	public int getLength() {
		return length;
	}

	public void setHorizontal(boolean h) {
		horizontal = h;
	}

	public boolean isHorizontal() {
		return horizontal;
	}

	public void updateShip(int x) {
		tiles[x] = 1;
		tile--;
	}

	public boolean isSunk() {
		return tile == 0;
	}

}
