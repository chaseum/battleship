package com.chase.battleship.gui;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.Map;
import java.util.Stack;

public class ScreenManager {

    private final Stage stage;
    private final Map<ScreenId, BaseScreen> screens = new EnumMap<>(ScreenId.class);
    private final Stack<ScreenId> navStack = new Stack<>();
    private StackPane stageRoot;
    private StackPane contentHost;
    private LoadingOverlay loadingOverlay;

    // GUI-level game state
    private GuiGameSession.Mode plannedMode = GuiGameSession.Mode.CLASSIC_VS_AI;
    private GuiGameSession currentSession;
    private String pendingJoinCode;

    public ScreenManager(Stage stage) {
        this.stage = stage;
    }

    // -------- session plumbing --------
    public GuiGameSession.Mode getPlannedMode() {
        return plannedMode;
    }

    public void setPlannedMode(GuiGameSession.Mode plannedMode) {
        this.plannedMode = plannedMode;
    }

    public GuiGameSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(GuiGameSession session) {
        this.currentSession = session;
    }

    public String getPendingJoinCode() {
        return pendingJoinCode;
    }

    public void setPendingJoinCode(String pendingJoinCode) {
        this.pendingJoinCode = pendingJoinCode;
    }

    public void clearCurrentSession() {
        if (this.currentSession != null) {
            this.currentSession.close();
        }
        this.currentSession = null;
    }

    // -------- screen navigation --------
    public void show(ScreenId id) {
        boolean sceneReady = stageRoot != null;
        if (!screens.containsKey(id)) {
            screens.put(id, createScreen(id));
        }

        if (id == ScreenId.TITLE && currentSession != null) {
            currentSession.close();
            clearCurrentSession();
        }

        BaseScreen screen = screens.get(id);
        Region root = screen.getRoot();
        ensureScene(root);
        if (loadingOverlay != null && sceneReady) {
            loadingOverlay.transition("Loading...");
        }

        if (!contentHost.getChildren().isEmpty()) {
            Region previous = (Region) contentHost.getChildren().get(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), previous);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                contentHost.getChildren().setAll(root);
                root.setOpacity(0);
                root.requestFocus();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(220), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            contentHost.getChildren().setAll(root);
            root.setOpacity(0);
            root.requestFocus();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }

        if (!navStack.isEmpty() && navStack.peek() != id) {
            navStack.push(id);
        } else if (navStack.isEmpty()) {
            navStack.push(id);
        }
        screen.onShow();
    }

    public void goBack() {
        if (navStack.size() <= 1) {
            show(ScreenId.TITLE);
            return;
        }
        navStack.pop();
        ScreenId prev = navStack.peek();
        show(prev);
    }

    public void showLoading(String message) {
        if (loadingOverlay != null) {
            loadingOverlay.show(message);
        }
    }

    public void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    LoadingOverlay getLoadingOverlay() {
        return loadingOverlay;
    }

    private void ensureScene(Region fallback) {
        if (stageRoot != null) {
            return;
        }
        contentHost = new StackPane(fallback);
        loadingOverlay = new LoadingOverlay();
        stageRoot = new StackPane(contentHost, loadingOverlay);

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(stageRoot, 1280, 720);
            scene.setCursor(javafx.scene.Cursor.CROSSHAIR);
            var css = ScreenManager.class.getResource("/gui.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
        } else {
            scene.setRoot(stageRoot);
        }
    }

    private BaseScreen createScreen(ScreenId id) {
        return switch (id) {
            case TITLE -> new TitleScreen(this);
            case SINGLE_PLAYER_SELECT -> new SinglePlayerScreen(this);
            case MULTI_PLAYER_SELECT -> new MultiPlayerScreen(this);
            case HOST_MODE_SELECT -> new HostModeScreen(this);
            case HOST_LOBBY -> new HostLobbyScreen(this);
            case JOIN_CODE -> new JoinCodeScreen(this);
            case SETUP -> new SetupScreen(this);
            case PLAYING -> new PlayingScreen(this);
            case WIN -> new WinScreen(this);
            case LOSE -> new LoseScreen(this);
            case DISCONNECTED -> new DisconnectedScreen(this);
        };
    }
}
