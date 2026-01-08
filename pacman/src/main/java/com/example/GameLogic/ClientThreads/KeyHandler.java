package com.example.GameLogic.ClientThreads;

import com.example.model.Constants;
import com.example.model.Direction;
import com.example.model.Player;
import com.example.model.Position;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class KeyHandler extends Application implements Runnable {

    // Client -> Space: ("input", playerId:int, direction:String, seq:Long)
    // Space  -> Client: ("state", playerId:int, x:Double, y:Double, direction:String, seq:Long)

    private final Set<KeyCode> pressed = new HashSet<>();

    private volatile boolean running = true;

    private Space space;

    // Movement-related state
    private Player player;
    private long inputSeq = 0;
    private long lastStateSeq = 0;

    // Optional visual representation for movement testing. Waiting for UI boys
    private Circle playerNode;

    @Override
    public void start(Stage stage) {
        // Think it works like this
        player = new Player(1);
        Position startPos = new Position();
        startPos.x = 200;
        startPos.y = 200;
        player.setPosition(startPos);
        player.setDirection(Direction.EAST);

        playerNode = new Circle(20, Color.YELLOW);
        playerNode.setCenterX(player.getPosition().x);
        playerNode.setCenterY(player.getPosition().y);

        Pane root = new Pane(playerNode);
        Scene scene = new Scene(root, 800, 600);

        scene.setOnKeyPressed(e -> pressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> pressed.remove(e.getCode()));

        // jSpace connection
        try {
            space = new RemoteSpace(Constants.REMOTE_URI);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to connect to jSpace at: " + Constants.REMOTE_URI, ex);
        }

        // Background listener for authoritative movement updates
        Thread listener = new Thread(this, "movement-state-listener");
        listener.setDaemon(true);
        listener.start();

        // Input publisher loop: only emits when direction changes
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Direction desired = directionFromKeys();
                if (desired == null) return;

                if (desired != player.getDirection()) {
                    player.setDirection(desired);

                    try {
                        space.put("input", player.getId(), desired.name(), ++inputSeq);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        running = false;
                    }
                }
            }
        };
        timer.start();
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
        stage.setOnCloseRequest(e -> running = false);
    }

    
    //  This keeps movement rules (walls/speed/tile stepping) out of the client file. I THink
    @Override
    public void run() {
        while (running) {
            try {
                Object[] msg = space.get(
                        new ActualField("state"),
                        new ActualField(player.getId()),
                        new FormalField(Double.class),
                        new FormalField(Double.class),
                        new FormalField(String.class),
                        new FormalField(Long.class)
                );

                double x = (Double) msg[2];
                double y = (Double) msg[3];
                Direction dir = Direction.valueOf((String) msg[4]);
                long seq = (Long) msg[5];

                if (seq <= lastStateSeq) {
                    continue;
                }
                lastStateSeq = seq;

                Position p = player.getPosition();
                p.x = x;
                p.y = y;
                player.setPosition(p);
                player.setDirection(dir);

                // Mirror to JavaFX node (movement-only visualization)
                Platform.runLater(() -> {
                    playerNode.setCenterX(x);
                    playerNode.setCenterY(y);
                });

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    private Direction directionFromKeys() {
        // Mapping: arrow keys -> your enum (NORTH/EAST/SOUTH/WEST)
        if (pressed.contains(KeyCode.UP)) return Direction.NORTH;
        if (pressed.contains(KeyCode.DOWN)) return Direction.SOUTH;
        if (pressed.contains(KeyCode.LEFT)) return Direction.WEST;
        if (pressed.contains(KeyCode.RIGHT)) return Direction.EAST;
        return null;
    }
}
