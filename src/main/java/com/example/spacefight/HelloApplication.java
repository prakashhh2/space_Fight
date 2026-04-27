package com.example.spacefight;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    public static final int WIDTH  = 800;
    public static final int HEIGHT = 900;

    @Override
    public void start(Stage stage) {
        GamePane gamePane = new GamePane(WIDTH, HEIGHT);
        Scene scene = new Scene(gamePane, WIDTH, HEIGHT);

        // Forward key events to the game pane
        scene.setOnKeyPressed(e  -> gamePane.handleKeyPressed(e.getCode()));
        scene.setOnKeyReleased(e -> gamePane.handleKeyReleased(e.getCode()));

        stage.setTitle("Space Fight");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        gamePane.startLoop();
    }
}
