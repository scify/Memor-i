package org.scify.memori.screens;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import org.scify.memori.card.CardDBHandlerJSON;
import org.scify.memori.enums.GameType;
import org.scify.memori.fx.FXSceneHandler;
import org.scify.memori.game_flavor.GameFlavor;
import org.scify.memori.game_flavor.GameFlavorService;

import java.util.ArrayList;
import java.util.List;

public class GameFlavorSelectionScreenController extends MemoriScreenController implements EventHandler<Event> {

    @FXML
    GridPane gamesContainer;
    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox vBox;
    @FXML
    ImageView loaderImage;
    GameFlavorService gameFlavorService;
    List<GameFlavor> gameFlavors = new ArrayList<>();

    @Override
    public void setParameters(FXSceneHandler sceneHandler, Scene scene) {
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Thread thread = new Thread(this::getGameFlavorsAndInitUIElements);
        thread.setDaemon(true);
        thread.start();
        super.setParameters(sceneHandler, scene);
    }

    private void getGameFlavorsAndInitUIElements() {
        gameFlavorService = GameFlavorService.getInstance();
        gameFlavors = gameFlavorService.getGameFlavors();
        Platform.runLater(this::initUIElements);
    }

    private void initUIElements() {
        if (gameFlavors.isEmpty())
            return;
        int cardFontSize = 23;
        double cardHeight = (Screen.getPrimary().getBounds().getHeight() * 0.33);
        double cardWidth = (Screen.getPrimary().getBounds().getWidth() * 0.33) - 200;
        int x = 0;
        int y = 0;
        final int idOfLastButton = gameFlavors.get(gameFlavors.size() - 1).id;
        for (GameFlavor gameFlavor : gameFlavors) {
            if (y > 2) {
                x++;
                y = 0;
            }
            Button btn = new Button();
            btn.getStyleClass().add("btn");
            btn.getStyleClass().add("btn-border");
            btn.setStyle("-fx-font-size:" + cardFontSize);
            btn.setText(gameFlavor.name);
            btn.setMaxWidth(cardWidth);
            btn.setMaxHeight(cardHeight);
            btn.setId(String.valueOf(gameFlavor.id));
            btn.setContentDisplay(ContentDisplay.BOTTOM);
            btn.setOnAction(this::handle);
            Thread thread = new Thread(() -> {
                ImageView imageView = new ImageView(gameFlavor.coverImgFilePath);
                imageView.setFitWidth(cardWidth);
                imageView.setFitHeight(cardHeight - cardFontSize);

                Platform.runLater(() -> {
                    btn.setGraphic(imageView);
                    if (Integer.parseInt(btn.getId()) == idOfLastButton)
                        vBox.getChildren().remove(loaderImage);
                });
            });
            thread.setDaemon(true);
            thread.start();
            gamesContainer.add(btn, y, x);
            y++;
        }
    }

    @Override
    public void handle(Event event) {
        if (event.getClass() == KeyEvent.class) {
            handleKeyEvent((KeyEvent) event);
        } else {
            loadLevelsScreenForGameFlavor(Integer.parseInt(((Control) event.getSource()).getId()));
        }
    }

    private void handleKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE:
                exitScreen();
                break;
            case ENTER:
                loadLevelsScreenForGameFlavor(Integer.parseInt(((Control) event.getSource()).getId()));
                break;
        }
    }

    private void loadLevelsScreenForGameFlavor(int gameFlavorId) {
        GameFlavor gameFlavor = gameFlavorService.getGameFlavor(gameFlavorId);
        CardDBHandlerJSON.setDbFilePath(gameFlavor.equivalenceSetFilePath);
        new LevelsScreen(sceneHandler, GameType.VS_CPU);
    }
}
