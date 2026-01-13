package com.example.UI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import com.example.GUI.UI;
import com.example.common.BaseTest;
import com.example.model.Constants;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
@DisplayName("User Interface and Frontend Interaction Tests")
public class UITest extends BaseTest {

    @BeforeAll
    public static void setupHeadless() {
    }

    @Start
    public void start(Stage stage) {
        new UI().start(stage);
    }

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
        
        robot.clickOn(lobbyIdInput).eraseText(10).write("not-a-number");
        robot.clickOn("Join Lobby");

        // Wait for potential async UI updates
        WaitForAsyncUtils.waitForFxEvents();

        Node errorNode = robot.lookup(".text").match(n -> n instanceof Text && ((Text) n).getText().contains("Invalid"))
            .query();
        assertNotNull(errorNode, "Should display error message for invalid Lobby ID");
    }

    @Test
    @DisplayName("Game clock should advance smoothly via the AnimationTimer after starting")
    public void testSmoothAnimationLoop(FxRobot robot) throws java.util.concurrent.TimeoutException {
        int initialClock = Constants.clock;

        robot.clickOn("Create Lobby");

        // Wait for the view to switch and the "Start Game" button to appear
        org.testfx.util.WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS, () -> 
            !robot.lookup("Start Game").queryAll().isEmpty()
        );

        robot.clickOn("Start Game");

        // Wait for game to actually start and clock to tick
        org.testfx.util.WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS, () -> 
            Constants.clock > initialClock
        );

        assertTrue(Constants.clock > initialClock, "Game clock should be advancing in the AnimationTimer");
    }

    @Test
    @DisplayName("Lobby should handle player disconnection gracefully")
    public void testPlayerDisconnectionHandling(FxRobot robot) {
        robot.clickOn("Create Lobby");
        
        // Simulate a disconnection event (requires implementation in UI/LobbyHandler)
        // verifyThat("A player has disconnected", isVisible());
        assertTrue(false, "TDD: Implement disconnection notification in UI.");
    }
}
