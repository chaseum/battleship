package com.chase.battleship.cli;

import com.chase.battleship.core.*;
import com.chase.battleship.ai.PlayerAgent;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class TerminalHumanAgent implements PlayerAgent {
    private final Scanner scanner;

    public TerminalHumanAgent(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public TurnAction chooseAction(GameState gameState, boolean ignored) {
        PlayerState me = gameState.getCurrentPlayer();
        GameConfig cfg = gameState.getConfig();

        if (cfg.getGameMode() == GameMode.NEO_RETRO &&
            me.getAbilities() != null &&
            !me.abilitiesLocked()) {

            while (true) {
                System.out.print("Choose action: (F)ire or (A)bility? ");
                String line = scanner.nextLine().trim().toUpperCase();
                switch (line) {
                    case "F" -> {
                        return chooseFire();
                    }
                    case "A" -> {
                        return chooseAbilityAction(gameState);
                    }
                    default -> System.out.println("Invalid choice, enter F or A.");
                }
            }
        }
        return chooseFire();
    }

    private TurnAction chooseFire() {
        while (true) {
            System.out.print("Enter target (row col): ");
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Please enter exactly two integers: row col.");
                continue;
            }
            try {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                return new FireAction(new Coordinate(r, c));
            } catch (NumberFormatException e) {
                System.out.println("Invalid numbers, try again.");
            }
        }
    }

    private TurnAction chooseAbilityAction(GameState gameState) {
        PlayerState me = gameState.getCurrentPlayer();

        while (true) {
            System.out.print("Which ability (EMP, MULTISHOT, SHIELD, SONAR): ");
            String abilStr = scanner.nextLine().trim().toUpperCase();

            AbilityType type;
            try {
                type = AbilityType.valueOf(abilStr);
            } catch (IllegalArgumentException ex) {
                System.out.println("Unknown ability, try again.");
                continue;
            }

            PlayerAbilities abilities = me.getAbilities();
            AbilityStatus status = (abilities != null) ? abilities.getStatus(type) : null;
            if (status == null) {
                System.out.println("You do not have that ability configured.");
                continue;
            }

            AbilityTarget target;

            switch (type) {
                case SHIELD -> {
                    Coordinate coord = readSingleCoord("Shield coordinate (row col) on your board: ");
                    target = new AbilityTarget(coord, 0);
                }
                case SONAR -> {
                    Coordinate coord = readSingleCoord("Sonar center coordinate on enemy board (row col): ");
                    target = new AbilityTarget(coord, 0);
                }
                case MULTISHOT -> {
                    int shots = 3;
                    List<Coordinate> coords = readMultishotTargets(shots);
                    target = new AbilityTarget(coords);
                }
                case EMP -> {
                    target = new AbilityTarget((Coordinate) null, 0);
                }
                default -> {
                    target = new AbilityTarget((Coordinate) null, 0);
                }
            }

            return new UseAbilityAction(type, target);
        }
    }

    private Coordinate readSingleCoord(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Please enter exactly two integers: row col.");
                continue;
            }
            try {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                return new Coordinate(r, c);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numbers, try again.");
            }
        }
    }

    private List<Coordinate> readMultishotTargets(int shots) {
        System.out.println("Enter " + shots + " targets (row col row col ...):");
        List<Coordinate> coords = new ArrayList<>();

        while (coords.size() < shots) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] nums = line.split("\\s+");
            if (nums.length % 2 != 0) {
                System.out.println("Please enter pairs: row col row col ...");
                continue;
            }

            coords.clear();
            boolean ok = true;
            for (int i = 0; i + 1 < nums.length && coords.size() < shots; i += 2) {
                try {
                    int r = Integer.parseInt(nums[i]);
                    int c = Integer.parseInt(nums[i + 1]);
                    coords.add(new Coordinate(r, c));
                } catch (NumberFormatException e) {
                    System.out.println("Bad numbers, try again.");
                    ok = false;
                    break;
                }
            }

            if (!ok || coords.size() != shots) {
                System.out.println("Need exactly " + shots + " valid coordinates total, re-enter:");
                coords.clear();
            }
        }

        return coords;
    }
}
