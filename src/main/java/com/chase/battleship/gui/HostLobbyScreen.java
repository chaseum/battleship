package com.chase.battleship.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Waiting room for online hosts. Displays the lobby code and transitions to setup
 * once a client connects.
 */
public class HostLobbyScreen extends BaseScreen {

    private final StackPane root;
    private final VBox contentBox;
    private final MenuSettingsOverlay settingsOverlay;
    private final Label codeLabel;
    private final Label statusLabel;
    private final Button waitButton;
    private final Button retryButton;

    private GuiGameSession session;
    private volatile boolean waiting;

    public HostLobbyScreen(ScreenManager manager) {
        super(manager);

        contentBox = new VBox(18);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(40));
        contentBox.setStyle("-fx-background-color: #001b29;");

        Label title = new Label("Host Lobby");
        title.setStyle("-fx-text-fill: #f0f0f0; -fx-font-size: 32px;");

        Label info = new Label("Share this code with your friend, then start searching when they're ready to join.");
        info.setStyle("-fx-text-fill: #cccccc;");
        info.setWrapText(true);
        info.setMaxWidth(420);

        codeLabel = new Label("Generating code...");
        codeLabel.setStyle("-fx-text-fill: #00ffcc; -fx-font-size: 28px; -fx-font-weight: bold;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #cccccc;");

        waitButton = new Button("Start searching for player");
        waitButton.setDisable(true);
        waitButton.setOnAction(e -> {
            statusLabel.setText("Waiting for a player to join...");
            waitButton.setText("Waiting...");
            waitButton.setDisable(true);
            startWaitingForClient();
        });

        retryButton = new Button("Retry host setup");
        retryButton.setManaged(false);
        retryButton.setVisible(false);
        retryButton.setOnAction(e -> beginSessionSetup());

        contentBox.getChildren().addAll(title, info, codeLabel, statusLabel, waitButton, retryButton);

        settingsOverlay = new MenuSettingsOverlay(() -> {
            stopWaiting();
            if (session != null) {
                session.close();
            }
            manager.clearCurrentSession();
            manager.goBack();
        });
        StackPane wrapped = settingsOverlay.wrap(contentBox);
        wrapped.setStyle("-fx-background-color: #001b29;");
        wrapped.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                settingsOverlay.toggle();
                e.consume();
            }
        });
        root = wrapped;
    }

    @Override
    public void onShow() {
        settingsOverlay.hide();
        beginSessionSetup();
    }

    private void beginSessionSetup() {
        stopWaiting();
        if (session != null) {
            session.close();
        }
        manager.clearCurrentSession();
        codeLabel.setText("Generating code...");
        statusLabel.setText("Connecting to rendezvous...");
        waitButton.setText("Start searching for player");
        waitButton.setDisable(true);
        showRetry(false);
        session = null;

        Thread setupThread = new Thread(() -> {
            try {
                GuiGameSession existingSession = manager.getCurrentSession();
                GuiGameSession preparedSession = existingSession != null
                        ? existingSession
                        : new GuiGameSession(manager.getPlannedMode(), manager.getPendingJoinCode());

                if (existingSession == null) {
                    manager.setCurrentSession(preparedSession);
                }

                Platform.runLater(() -> {
                    session = preparedSession;
                    String code = session.getLobbyCode();
                    codeLabel.setText("Lobby Code: " + (code == null ? "--" : code));
                    statusLabel.setText("Ready. Click start when your friend is ready to join.");
                    waitButton.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    session = null;
                    codeLabel.setText("Lobby Code: --");
                    statusLabel.setText("Failed to host: " + ex.getMessage()
                            + ". Is the rendezvous server running?");
                    waitButton.setDisable(true);
                    showRetry(true);
                });
            }
        }, "host-setup");

        setupThread.setDaemon(true);
        setupThread.start();
    }

    private void startWaitingForClient() {
        if (session == null) {
            return;
        }
        stopWaiting();
        waiting = true;

        Thread t = new Thread(() -> {
            try {
                session.waitForPeerReady();
                if (!waiting) {
                    return;
                }
                Platform.runLater(() -> {
                    if (!waiting) return;
                    statusLabel.setText("Player joined! Moving to setup...");
                    manager.show(ScreenId.SETUP);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    if (!waiting) return;
                    statusLabel.setText("Connection failed: " + ex.getMessage());
                    waiting = false;
                    waitButton.setDisable(true);
                    showRetry(true);
                    manager.show(ScreenId.DISCONNECTED);
                });
            }
        }, "host-lobby-wait");
        t.setDaemon(true);
        t.start();
    }

    private void stopWaiting() {
        waiting = false;
    }

    @Override
    public Region getRoot() {
        return root;
    }

    private void showRetry(boolean show) {
        retryButton.setManaged(show);
        retryButton.setVisible(show);
    }
}
