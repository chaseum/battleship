package com.chase.battleship.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class GameApp extends Application {

	private ScreenManager screenManager;

	@Override
	public void start(Stage primaryStage) {
		this.screenManager = new ScreenManager(primaryStage);

		primaryStage.setTitle("Neo-Retro Battleship");
		// primaryStage.getIcons().add(new Image("/assets/icon.png"));

		screenManager.show(ScreenId.TITLE);

		primaryStage.setMinWidth(1024);
		primaryStage.setMinHeight(576);
		
		primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

		primaryStage.getScene().getStylesheets().add(
    getClass().getResource("/battleship.css").toExternalForm()
		);


		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

