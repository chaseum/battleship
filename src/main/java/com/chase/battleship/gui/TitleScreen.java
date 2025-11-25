package com.chase.battleship.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
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
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 46px; -fx-font-family: 'Press Start 2P'; -fx-effect: dropshadow(one-pass-box, #00151f, 6, 0, 0, 2);");

        menu = new RetroMenu(java.util.List.of(
                new RetroMenu.Option("Singleplayer", () -> screenManager.show(ScreenId.SINGLE_PLAYER_SELECT)),
                new RetroMenu.Option("Multiplayer", () -> screenManager.show(ScreenId.MULTI_PLAYER_SELECT)),
                new RetroMenu.Option("Controls", () -> screenManager.show(ScreenId.CONTROLS)),
                new RetroMenu.Option("Quit", Platform::exit)
        ));
        menu.setStyle("-fx-font-size: 26px;");

        Label hint = new Label("Arrow keys / Enter or mouse. Esc for settings.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 9px; -fx-font-family: 'Press Start 2P';");

        VBox menuBox = new VBox(22, menu);
        menuBox.setAlignment(Pos.CENTER_LEFT);
        menuBox.setPadding(new javafx.geometry.Insets(0, 0, 0, 40));

        StackPane titlePane = new StackPane(title);
        titlePane.setPadding(new javafx.geometry.Insets(40, 0, 0, 0));
        titlePane.setAlignment(Pos.TOP_CENTER);

        StackPane hintPane = new StackPane(hint);
        hintPane.setAlignment(Pos.BOTTOM_RIGHT);
        hintPane.setPadding(new javafx.geometry.Insets(0, 30, 30, 0));

        BorderPane layout = new BorderPane();
        layout.setTop(titlePane);
        layout.setLeft(menuBox);
        layout.setCenter(new StackPane()); // spacer
        layout.setBottom(hintPane);

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
