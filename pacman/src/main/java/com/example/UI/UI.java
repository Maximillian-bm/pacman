package com.example.UI;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.Group;
import javafx.animation.AnimationTimer;

import com.example.model.Player;
import com.example.model.Position;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Constants;

public class UI extends Application {
    private GameState gameState;
    private Player player;
    private GraphicsContext gc;
    private Image playerImage;

     @Override
    public void start(Stage stage) {
        player = new Player(0);
        Position pos = new Position();
        pos.x = 100;
        pos.y = 100;
        player.setPosition(pos);
        player.setDirection(Direction.SOUTH);

        playerImage = new Image("./tilesets/pacman-sprite-sheet.png");

        final Group root = new Group();

        final Scene scene = new Scene(root, Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);
        stage.setScene(scene);

        final Canvas canvas = new Canvas(Constants.INIT_SCREEN_WIDTH, Constants.INIT_SCREEN_HEIGHT);
        root.getChildren().add(canvas);

        final AnimationTimer tm = new TimerMethod();
        tm.start();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
              if(key.getCode()==KeyCode.A || key.getCode()==KeyCode.LEFT) {
                  player.setDirection(Direction.WEST);
                  System.out.println("Move left(west)");
              }
              if(key.getCode()==KeyCode.D || key.getCode()==KeyCode.RIGHT) {
                  player.setDirection(Direction.EAST);
                  System.out.println("Move right(east)");
              }
              if(key.getCode()==KeyCode.W || key.getCode()==KeyCode.UP) {
                  player.setDirection(Direction.NORTH);
                  System.out.println("Move up(north)");
              }
              if(key.getCode()==KeyCode.S || key.getCode()==KeyCode.DOWN) {
                  player.setDirection(Direction.SOUTH);
                  System.out.println("Move down(south)");
              }
        });

        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(playerImage, 757, 42, 32, 32, pos.x, pos.y, 100, 100);

        stage.show();
    }

    private class TimerMethod extends AnimationTimer {

        @Override
        public void handle(long now) {
            switch(player.getDirection()) {
                case WEST:
                    drawPlayerPosition(-1, 0);
                    break;
                case EAST:
                    drawPlayerPosition(1, 0);
                    break;
                case NORTH:
                    drawPlayerPosition(0, -1);
                    break;
                case SOUTH:
                    drawPlayerPosition(0, 1);
                    break;
            }
        }

        private void drawPlayerPosition(int x, int y) {
            Position pos = player.getPosition();
            pos.x += x;
            pos.y += y;
            player.setPosition(pos);
            gc.setFill(Color.WHITE);
            gc.drawImage(playerImage, 757, 42, 32, 32, pos.x, pos.y, 100, 100);
        }
    }
}
