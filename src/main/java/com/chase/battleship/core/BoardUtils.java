package com.chase.battleship.core;

import com.chase.battleship.cli.BoardPrinter;
import java.util.Random;
import java.util.Scanner;

public final class BoardUtils {
	private static final ShipType[] DEFAULT_FLEET = {
		ShipType.CARRIER,
		ShipType.BATTLESHIP,
		ShipType.CRUISER,
		ShipType.SUBMARINE,
		ShipType.DESTROYER
	};

	private static final Random RANDOM = new Random();

	private BoardUtils() {}

	public static void randomFleetPlacement(Board board) {
		for(ShipType type : DEFAULT_FLEET) {
			placeRandomShip(board, type);
		}
	}

	private static void placeRandomShip(Board board, ShipType type) {
		while(true) {
			boolean horizontal = RANDOM.nextBoolean();
			int maxRow = horizontal ? board.getRows() : board.getRows() - type.getLength() + 1;
			int maxCol = horizontal ? board.getCols() - type.getLength() + 1 : board.getCols();

			int r = RANDOM.nextInt(maxRow);
			int c = RANDOM.nextInt(maxCol);

			Coordinate start = new Coordinate(r,c);
			if(board.canPlaceShip(type, start, horizontal)) {
				Ship ship = new Ship(type);
				board.placeShip(ship, start, horizontal);
				return;
			}
			// else loop again until we find a valid position
		}
	}

	public static void setupBoardInteractive(Board board) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Ship placement: enter row col orientation(H/V). Example: 2 3 H");
		for (ShipType type : new ShipType[] {
				ShipType.CARRIER,
				ShipType.BATTLESHIP,
				ShipType.CRUISER,
				ShipType.SUBMARINE,
				ShipType.DESTROYER
		}) {
			boolean placed = false;
			while (!placed) {
				System.out.println("Place " + type + " (length " + type.getLength() + "): ");
				String line = scanner.nextLine().trim();
				String[] parts = line.split("\\s+");
				if (parts.length != 3) {
					System.out.println("Format: row col H/V");
					continue;
				}
				try {
					int r = Integer.parseInt(parts[0]);
					int c = Integer.parseInt(parts[1]);
					char o = Character.toUpperCase(parts[2].charAt(0));
					boolean horizontal = (o == 'H');

					Coordinate start = new Coordinate(r, c);
					if (!board.canPlaceShip(type, start, horizontal)) {
						System.out.println("Invalid placement, try again.");
						continue;
					}

					Ship ship = new Ship(type);
					board.placeShip(ship, start, horizontal);
					placed = true;
				} catch (Exception ex) {
					System.out.println("Error: " + ex.getMessage());
				}
			}
			BoardPrinter.printBoard(board, true);
		}
	}

}