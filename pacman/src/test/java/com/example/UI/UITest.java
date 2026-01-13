package com.example.UI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import com.example.common.BaseTest;
import com.example.model.Constants;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
@DisplayName("User Interface and Frontend Interaction Tests")
public class UITest extends BaseTest {

    // ... setupHeadless and start stay the same ...

    @Test
    @DisplayName("Main menu buttons and title should be visible on startup")
    public void testLobbyUIComponentsVisible() {
        verifyThat("Join Lobby", isVisible());
        verifyThat("Create Lobby", isVisible());
        verifyThat("Pacman", isVisible());
    }

    @Test
    @DisplayName("UI thread must remain responsive and unblocked during network join requests")
    public void testUIResponsivenessDuringJoin(FxRobot robot) {

        TextField lobbyIdInput = robot.lookup(".text-field").queryAs(TextField.class);
        Platform.runLater(() -> lobbyIdInput.setText("123"));

        Button joinButton = robot.lookup("Join Lobby").queryButton();

        AtomicLong heartbeatCount = new AtomicLong(0);
        javafx.animation.AnimationTimer heartbeatTimer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                heartbeatCount.incrementAndGet();
            }
        };
        heartbeatTimer.start();

        long startHeartbeat = heartbeatCount.get();

        robot.clickOn(joinButton);

        WaitForAsyncUtils.sleep(200, java.util.concurrent.TimeUnit.MILLISECONDS);

        long endHeartbeat = heartbeatCount.get();

        assertTrue((endHeartbeat - startHeartbeat) > 5, "UI Thread was BLOCKED during Join Lobby! Heartbeat didn't increment within JavaFX loop.");

        heartbeatTimer.stop();
    }

    @Test
    @DisplayName("Buttons should be scaled proportionally to the TILE_SIZE")
    public void testButtonScaling(FxRobot robot) {

        Button joinButton = robot.lookup("Join Lobby").queryButton();
        assertEquals(6 * Constants.TILE_SIZE, joinButton.getPrefWidth(), 0.1, "Button width should be 6 tiles");
        assertEquals(2 * Constants.TILE_SIZE, joinButton.getPrefHeight(), 0.1, "Button height should be 2 tiles");
    }

    @Test
    @DisplayName("Invalid lobby ID input should display a clear error message")
    public void testInvalidLobbyIdHandling(FxRobot robot) {

        TextField lobbyIdInput = robot.lookup(".text-field").queryAs(TextField.class);
        robot.clickOn(lobbyIdInput).write("not-a-number");

        robot.clickOn("Join Lobby");

        Node errorNode = robot.lookup(".text").match(n -> n instanceof Text && ((Text) n).getText().contains("Invalid"))
            .query();
        assertNotNull(errorNode, "Should display error message for invalid Lobby ID");
    }

    @Test
    @DisplayName("Game clock should advance smoothly via the AnimationTimer after starting")
    public void testSmoothAnimationLoop(FxRobot robot) {

        int initialClock = Constants.clock;

        robot.clickOn("Create Lobby");

        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn("Start Game");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for game to start. Error: " + e.getMessage());
        }

        assertTrue(Constants.clock > initialClock, "Game clock should be advancing in the AnimationTimer");
    }
}
