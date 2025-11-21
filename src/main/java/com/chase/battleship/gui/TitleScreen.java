package com.chase.battleship.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TitleScreen extends BaseScreen {

    private final StackPane root;
    private final RetroMenu menu;
    private final MenuSettingsOverlay settingsOverlay;

    public TitleScreen(ScreenManager screenManager) {
        super(screenManager);

        Label title = new Label("NEO-RETRO BATTLESHIP");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 42px; -fx-font-family: 'Consolas'; -fx-effect: dropshadow(one-pass-box, #00151f, 6, 0, 0, 2);");

        menu = new RetroMenu(java.util.List.of(
                new RetroMenu.Option("Singleplayer", () -> screenManager.show(ScreenId.SINGLE_PLAYER_SELECT)),
                new RetroMenu.Option("Multiplayer", () -> screenManager.show(ScreenId.MULTI_PLAYER_SELECT)),
                new RetroMenu.Option("Quit", Platform::exit)
        ));

        Label hint = new Label("Arrow keys / Enter or mouse. Esc for settings.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 12px;");

        VBox layout = new VBox(18, title, menu, hint);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        settingsOverlay = new MenuSettingsOverlay(null);
        StackPane wrapped = settingsOverlay.wrap(layout);
        wrapped.setStyle("-fx-background-color: linear-gradient(#0b2a42, #03121f);");

        wrapped.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                settingsOverlay.toggle();
                e.consume();
                return;
            }
            if (settingsOverlay.isVisible()) return;
            if (menu.handleKey(e.getCode())) {
                e.consume();
            }
        });
        wrapped.setOnMouseClicked(e -> wrapped.requestFocus());
        root = wrapped;
    }

    @Override
    public Region getRoot() {
        return root;
    }

    @Override
    public void onShow() {
        settingsOverlay.hide();
        menu.focusFirst();
        root.requestFocus();
    }
}
