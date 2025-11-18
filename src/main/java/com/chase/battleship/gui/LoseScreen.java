package com.chase.battleship.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class LoseScreen extends BaseScreen {

    private final VBox root = new VBox(15);

    public LoseScreen(ScreenManager screenManager) {
        super(screenManager);

        Label label = new Label("You Lost");
        Button playAgain = new Button("Play Again");
        Button title = new Button("Back to Title");

        playAgain.setOnAction(e -> screenManager.show(ScreenId.SINGLE_PLAYER_SELECT));
        title.setOnAction(e -> screenManager.show(ScreenId.TITLE));

        root.getChildren().addAll(label, playAgain, title);
        root.setAlignment(Pos.CENTER);
    }

    @Override
    public Region getRoot() {
        return root;
    }
}
