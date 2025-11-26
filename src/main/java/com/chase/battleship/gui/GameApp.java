package com.chase.battleship.gui;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class GameApp extends Application {

	private ScreenManager screenManager;

	@Override
	public void start(Stage primaryStage) {
		this.screenManager = new ScreenManager(primaryStage);

		primaryStage.setTitle("Neo-Retro Battleship");
		primaryStage.getIcons().add(new Image("/assets/images/stupidicon.png"));

		// preload pixel font for consistent styling
		try (var fontStream = getClass().getResourceAsStream("/assets/images/PressStart2P.ttf")) {
			if (fontStream != null) {
				Font.loadFont(fontStream, 18);
			}
		} catch (Exception ignored) {}

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

