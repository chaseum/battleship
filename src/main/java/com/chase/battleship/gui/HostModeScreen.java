package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * GUI placeholder for hosting a multiplayer game.
 * For now this simply starts a local 2-player session in the chosen mode.
 */
public class HostModeScreen extends BaseScreen {

    private final VBox root;

    public HostModeScreen(ScreenManager manager) {
        super(manager);

        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #001b29;");

        Label title = new Label("Host Game");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px;");

        Label note = new Label("Share the generated code after choosing a mode.");
        note.setStyle("-fx-text-fill: #cccccc;");

        Button classicBtn = new Button("Classic Online Host");
        Button neoBtn = new Button("Neo-Retro Online Host");
        Button backBtn = new Button("Back");

        classicBtn.setOnAction(e -> {
            manager.setPlannedMode(GuiGameSession.Mode.CLASSIC_ONLINE_HOST);
            manager.clearCurrentSession();
            manager.show(ScreenId.HOST_LOBBY);
        });

        neoBtn.setOnAction(e -> {
            manager.setPlannedMode(GuiGameSession.Mode.NEORETRO_ONLINE_HOST);
            manager.clearCurrentSession();
            manager.show(ScreenId.HOST_LOBBY);
        });

        backBtn.setOnAction(e -> manager.goBack());

        root.getChildren().addAll(title, note, classicBtn, neoBtn, backBtn);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
