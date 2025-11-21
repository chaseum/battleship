package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class WinScreen extends BaseScreen {

    private final VBox root = new VBox(15);

    public WinScreen(ScreenManager screenManager) {
        super(screenManager);

        Label label = new Label("You Win!");
        Button playAgain = new Button("Play Again");
        Button title = new Button("Back to Title");

        Runnable clear = () -> {
            if (screenManager.getCurrentSession() != null) {
                screenManager.getCurrentSession().close();
            }
            screenManager.clearCurrentSession();
        };

        playAgain.setOnAction(e -> {
            clear.run();
            screenManager.show(ScreenId.SINGLE_PLAYER_SELECT);
        });
        title.setOnAction(e -> {
            clear.run();
            screenManager.show(ScreenId.TITLE);
        });

        root.getChildren().addAll(label, playAgain, title);
        root.setAlignment(Pos.CENTER);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
