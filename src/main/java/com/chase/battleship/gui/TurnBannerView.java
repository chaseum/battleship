package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Small wrapper for the top banner: avatar + turn text + action text.
 */
public class TurnBannerView {
    private final HBox root;
    private final ImageView avatar;
    private final Label turnLabel;
    private final Label actionLabel;

    public TurnBannerView() {
        turnLabel = new Label("Your turn");
        turnLabel.getStyleClass().add("turn-label");

        avatar = new ImageView(AssetLibrary.playerOne());
        avatar.setFitHeight(34);
        avatar.setFitWidth(34);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(false);
        avatar.setEffect(new DropShadow(8, Color.web("#061826")));

        actionLabel = new Label("");
        actionLabel.getStyleClass().add("action-label");
        actionLabel.setWrapText(false);

        root = new HBox(18, avatar, turnLabel, actionLabel);
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("turn-banner");
    }

    public HBox getRoot() {
        return root;
    }

    public Label getTurnLabel() {
        return turnLabel;
    }

    public Label getActionLabel() {
        return actionLabel;
    }

    public ImageView getAvatar() {
        return avatar;
    }

    public void setAvatar(Image image) {
        if (image != null) {
            avatar.setImage(image);
        }
    }

    public void setActionColor(Color color) {
        actionLabel.setTextFill(color);
    }

    public void typeAction(String text) {
        FxAnimations.typewriter(actionLabel, text, Duration.millis(900));
    }
}
