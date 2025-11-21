package com.chase.battleship.gui;

import com.chase.battleship.core.ShipType;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple image cache for GUI assets (ships, player avatars, fonts).
 */
public final class AssetLibrary {

    private static final Map<String, Image> CACHE = new HashMap<>();

    private AssetLibrary() {
        // utility
    }

    public static Image shipIcon(ShipType type) {
        return load("/assets/images/" + assetKey(type) + "_icon.png");
    }

    public static Image shipTopDown(ShipType type) {
        return switch (type) {
            case SUBMARINE -> loadWithFallback(
                    "/assets/images/submarine_topdown.png",
                    "/assets/images/destroyer_topdown.png");
            case DESTROYER -> load("/assets/images/patrol_topdown.png");
            case CRUISER -> load("/assets/images/destroyer_topdown.png");
            default -> load("/assets/images/" + assetKey(type) + "_topdown.png");
        };
    }

    public static Image shipTopDownVertical(ShipType type) {
        return switch (type) {
            case SUBMARINE -> loadWithFallback(
                    "/assets/images/submarine_topdown_rotated.png",
                    "/assets/images/destroyer_topdown_rotated.png");
            case DESTROYER -> load("/assets/images/patrol_topdown_rotated.png");
            case CRUISER -> load("/assets/images/destroyer_topdown_rotated.png");
            default -> load("/assets/images/" + assetKey(type) + "_topdown_rotated.png");
        };
    }

    public static Image playerOne() {
        return load("/assets/images/player1.png");
    }

    public static Image playerTwo() {
        return load("/assets/images/player2.png");
    }

    private static String assetKey(ShipType type) {
        return switch (type) {
            case CARRIER -> "carrier";
            case BATTLESHIP -> "battleship";
            case CRUISER -> "destroyer"; // reuse destroyer art for cruiser length
            case SUBMARINE -> "submarine";
            case DESTROYER -> "patrol";
        };
    }

    private static Image load(String path) {
        return CACHE.computeIfAbsent(path, key -> {
            InputStream stream = AssetLibrary.class.getResourceAsStream(key);
            if (stream == null) {
                return new WritableImage(1, 1);
            }
            return new Image(stream, 0, 0, true, false);
        });
    }

    private static Image loadWithFallback(String primary, String fallback) {
        Image img = load(primary);
        if (img.getWidth() <= 1 && fallback != null) {
            return load(fallback);
        }
        return img;
    }
}
