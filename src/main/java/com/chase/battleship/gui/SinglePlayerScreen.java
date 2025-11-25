package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SinglePlayerScreen extends BaseScreen {

    private final StackPane root;
    private final RetroMenu menu;
    private final MenuSettingsOverlay settingsOverlay;

    public SinglePlayerScreen(ScreenManager manager) {
        super(manager);

        Label title = new Label("Single Player");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px; -fx-font-family: 'Press Start 2P';");

        menu = new RetroMenu(java.util.List.of(
                new RetroMenu.Option("Classic vs AI", () -> {
                    manager.setPlannedMode(GuiGameSession.Mode.CLASSIC_VS_AI);
                    manager.clearCurrentSession();
                    manager.show(ScreenId.SETUP);
                }),
                new RetroMenu.Option("Neo-Retro vs AI", () -> {
                    manager.setPlannedMode(GuiGameSession.Mode.NEORETRO_VS_AI);
                    manager.clearCurrentSession();
                    manager.show(ScreenId.SETUP);
                })
        ));

        Label hint = new Label("Arrow keys / Enter or mouse. Esc for settings/back.");
        hint.setStyle("-fx-text-fill: #b0d8f0; -fx-font-size: 11px; -fx-font-family: 'Press Start 2P';");

        VBox layout = new VBox(18, title, menu, hint);
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
