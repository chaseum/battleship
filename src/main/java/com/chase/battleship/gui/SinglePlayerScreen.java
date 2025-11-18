package com.chase.battleship.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SinglePlayerScreen extends BaseScreen {

    private final VBox root;

    public SinglePlayerScreen(ScreenManager manager) {
        super(manager);

        root = new VBox(15);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #001b29;");

        Label title = new Label("Single Player");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px;");

        Button classicVsAi = new Button("Classic vs AI");
        Button neoVsAi = new Button("Neo-Retro vs AI");
        Button classicLocal = new Button("Classic Local 2P");
        Button neoLocal = new Button("Neo-Retro Local 2P");
        Button back = new Button("Back");

        classicVsAi.setOnAction(e -> {
            manager.setPlannedMode(GuiGameSession.Mode.CLASSIC_VS_AI);
            manager.clearCurrentSession();
            manager.show(ScreenId.SETUP);
        });

        neoVsAi.setOnAction(e -> {
            manager.setPlannedMode(GuiGameSession.Mode.NEORETRO_VS_AI);
            manager.clearCurrentSession();
            manager.show(ScreenId.SETUP);
        });

        classicLocal.setOnAction(e -> {
            manager.setPlannedMode(GuiGameSession.Mode.CLASSIC_LOCAL_2P);
            manager.clearCurrentSession();
            manager.show(ScreenId.SETUP);
        });

        neoLocal.setOnAction(e -> {
            manager.setPlannedMode(GuiGameSession.Mode.NEORETRO_LOCAL_2P);
            manager.clearCurrentSession();
            manager.show(ScreenId.SETUP);
        });

        back.setOnAction(e -> manager.goBack());

        root.getChildren().addAll(title, classicVsAi, neoVsAi, classicLocal, neoLocal, back);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
