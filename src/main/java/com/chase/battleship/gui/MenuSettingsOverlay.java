package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Lightweight settings/back overlay for menu screens.
 */
public class MenuSettingsOverlay {

    private final StackPane overlayPane;
    private final Button backButton;

    public MenuSettingsOverlay(Runnable onBack) {
        Label title = new Label("Settings");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 20px; -fx-font-family: 'Consolas';");

        backButton = new Button("Back");
        backButton.setVisible(onBack != null);
        backButton.setManaged(onBack != null);
        if (onBack != null) {
            backButton.setOnAction(e -> {
                hide();
                onBack.run();
            });
        }

        Button close = new Button("Close");
        close.setOnAction(e -> hide());

        VBox box = new VBox(10, title, backButton, close);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(20));
        box.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-text-fill: #f0f0f0; -fx-border-color: #00ffcc; -fx-border-width: 1;");

        overlayPane = new StackPane(box);
        overlayPane.setAlignment(Pos.CENTER);
        overlayPane.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);
    }

    public StackPane wrap(javafx.scene.Node content) {
        StackPane root = new StackPane(content, overlayPane);
        return root;
    }

    public void toggle() {
        boolean nowVisible = !overlayPane.isVisible();
        overlayPane.setVisible(nowVisible);
        overlayPane.setMouseTransparent(!nowVisible);
    }

    public void hide() {
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);
    }

    public boolean isVisible() {
        return overlayPane.isVisible();
    }
}
