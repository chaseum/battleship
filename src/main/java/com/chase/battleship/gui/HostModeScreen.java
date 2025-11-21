package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * GUI placeholder for hosting a multiplayer game.
 * For now this simply starts a local 2-player session in the chosen mode.
 */
public class HostModeScreen extends BaseScreen {

    private final StackPane root;
    private final RetroMenu menu;
    private final MenuSettingsOverlay settingsOverlay;

    public HostModeScreen(ScreenManager manager) {
        super(manager);

        Label title = new Label("Host Game");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px; -fx-font-family: 'Consolas';");

        Label note = new Label("Share the generated code after choosing a mode.");
        note.setStyle("-fx-text-fill: #cccccc;");

        menu = new RetroMenu(java.util.List.of(
                new RetroMenu.Option("Classic Online Host", () -> {
                    manager.setPlannedMode(GuiGameSession.Mode.CLASSIC_ONLINE_HOST);
                    manager.clearCurrentSession();
                    manager.show(ScreenId.HOST_LOBBY);
                }),
                new RetroMenu.Option("Neo-Retro Online Host", () -> {
                    manager.setPlannedMode(GuiGameSession.Mode.NEORETRO_ONLINE_HOST);
                    manager.clearCurrentSession();
                    manager.show(ScreenId.HOST_LOBBY);
                })
        ));

        Label hint = new Label("Arrow keys / Enter or mouse. Esc for settings/back.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 12px;");

        VBox layout = new VBox(14, title, note, menu, hint);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40;");

        settingsOverlay = new MenuSettingsOverlay(manager::goBack);
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
