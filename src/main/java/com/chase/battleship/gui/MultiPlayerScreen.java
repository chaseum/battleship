package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MultiPlayerScreen extends BaseScreen {

    private final StackPane root;
    private final RetroMenu menu;
    private final MenuSettingsOverlay settingsOverlay;

    public MultiPlayerScreen(ScreenManager screenManager) {
        super(screenManager);

        Label label = new Label("Multiplayer");
        label.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px; -fx-font-family: 'Consolas';");

        menu = new RetroMenu(java.util.List.of(
                new RetroMenu.Option("Local 2P - Classic", () -> {
                    screenManager.setPlannedMode(GuiGameSession.Mode.CLASSIC_LOCAL_2P);
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.SETUP);
                }),
                new RetroMenu.Option("Local 2P - Neo-Retro", () -> {
                    screenManager.setPlannedMode(GuiGameSession.Mode.NEORETRO_LOCAL_2P);
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.SETUP);
                }),
                new RetroMenu.Option("Host Online Game", () -> {
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.HOST_MODE_SELECT);
                }),
                new RetroMenu.Option("Join Online Game", () -> {
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.JOIN_CODE);
                })
        ));

        Label hint = new Label("Arrow keys / Enter or mouse. Esc for settings/back.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 12px;");

        VBox box = new VBox(18, label, menu, hint);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 40;");

        settingsOverlay = new MenuSettingsOverlay(screenManager::goBack);
        StackPane wrapped = settingsOverlay.wrap(box);
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
