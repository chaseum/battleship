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
        label.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px; -fx-font-family: 'Press Start 2P';");

        Label desc = new Label("Select a mode to see details.");
        desc.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 11px; -fx-font-family: 'Press Start 2P';");
        desc.setWrapText(true);
        desc.setMaxWidth(520);
        desc.setAlignment(Pos.CENTER_LEFT);

        menu = new RetroMenu(java.util.List.of(
                new RetroMenu.Option("Local 2P - Classic", "Pass-and-play classic rules. No abilities.", () -> {
                    screenManager.setPlannedMode(GuiGameSession.Mode.CLASSIC_LOCAL_2P);
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.SETUP);
                }),
                new RetroMenu.Option("Local 2P - Neo-Retro", "Pass-and-play with abilities (Sonar, Multishot, EMP).", () -> {
                    screenManager.setPlannedMode(GuiGameSession.Mode.NEORETRO_LOCAL_2P);
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.SETUP);
                }),
                new RetroMenu.Option("Host Online Game", "Create a lobby others can join.", () -> {
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.HOST_MODE_SELECT);
                }),
                new RetroMenu.Option("Join Online Game", "Enter a lobby code to join.", () -> {
                    screenManager.clearCurrentSession();
                    screenManager.show(ScreenId.JOIN_CODE);
                })
        ));
        menu.setSelectionListener(opt -> desc.setText(opt.description().isBlank()
                ? "Select a mode to see details."
                : opt.description()));

        Label hint = new Label("Arrow keys / Enter or mouse. Esc for settings/back.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 8px; -fx-font-family: 'Press Start 2P';");

        VBox box = new VBox(16, label, menu, desc, hint);
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
