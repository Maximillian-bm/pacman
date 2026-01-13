package com.example.UI;

import com.example.GameLogic.ClientMain;
import com.example.model.Constants;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class UITest extends ApplicationTest {

    @BeforeClass
    public static void setupHeadless() {
        // Support running in CI/Headless environments
        if (Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        new UI().start(stage);
    }

    @Test
    public void testLobbyUIComponentsVisible() {
        verifyThat("Join Lobby", isVisible());
        verifyThat("Create Lobby", isVisible());
        verifyThat("Pacman", isVisible());
    }

    @Test
    public void testUIResponsivenessDuringJoin() throws InterruptedException {
        // This test checks if the UI thread is blocked when clicking "Join Lobby".
        // In a well-behaved UI, network calls should be async.
        
        TextField lobbyIdInput = lookup(".text-field").queryAs(TextField.class);
        Platform.runLater(() -> lobbyIdInput.setText("123"));
        
        Button joinButton = lookup("Join Lobby").queryButton();

        // We'll track "heartbeat" on the UI thread.
        AtomicLong heartbeatCount = new AtomicLong(0);
        
        // Start a heartbeat that increments every 10ms on the UI thread
        Thread heartbeatThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10);
                    Platform.runLater(heartbeatCount::incrementAndGet);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

        long startHeartbeat = heartbeatCount.get();
        
        // Click the button. This will trigger the blocking lobbyHandler.joinLobby()
        clickOn(joinButton);

        // Wait a bit for the operation (which will likely fail or timeout in this test env)
        Thread.sleep(200);

        long endHeartbeat = heartbeatCount.get();
        
        // If the UI was responsive, the heartbeat should have incremented significantly.
        // If it blocked for 200ms, the heartbeat wouldn't have increased.
        assertTrue("UI Thread was BLOCKED during Join Lobby! Heartbeat didn't increment enough.", 
                   (endHeartbeat - startHeartbeat) > 10);
        
        heartbeatThread.interrupt();
    }

    @Test
    public void testButtonScaling() {
        // Test that buttons are sized according to TILE_SIZE as requested in UI design
        Button joinButton = lookup("Join Lobby").queryButton();
        assertEquals("Button width should be 6 tiles", 6 * Constants.TILE_SIZE, joinButton.getPrefWidth(), 0.1);
        assertEquals("Button height should be 2 tiles", 2 * Constants.TILE_SIZE, joinButton.getPrefHeight(), 0.1);
    }

    @Test
    public void testInvalidLobbyIdHandling() {
        // TDD: Entering non-numeric lobby ID should show an error instead of crashing.
        TextField lobbyIdInput = lookup(".text-field").queryAs(TextField.class);
        clickOn(lobbyIdInput).write("not-a-number");
        
        clickOn("Join Lobby");
        
        // Expect some error text to appear (currently it just throws RuntimeException)
        Node errorNode = lookup(".text").match(n -> n instanceof Text && ((Text) n).getText().contains("Invalid")).query();
        assertNotNull("Should display error message for invalid Lobby ID", errorNode);
    }

    @Test
    public void testSmoothAnimationLoop() {
        // Verify that the GameAnimator is actually running and updating the clock
        int initialClock = ClientMain.clock;
        
        // Start the game by clicking "Create Lobby" then "Start Game"
        clickOn("Create Lobby");
        
        // Give it a moment to update UI
        WaitForAsyncUtils.waitForFxEvents();
        
        clickOn("Start Game");
        
        // Wait for a few frames
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        assertTrue("Game clock should be advancing in the AnimationTimer", ClientMain.clock > initialClock);
    }
}
