package com.example.UI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import com.example.GameLogic.ClientMain;
import com.example.model.Constants;
import java.util.concurrent.atomic.AtomicLong;
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

public class UITest extends ApplicationTest {

    @BeforeClass
    public static void setupHeadless() {

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
    public void start(Stage stage) {
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

        TextField lobbyIdInput = lookup(".text-field").queryAs(TextField.class);
        Platform.runLater(() -> lobbyIdInput.setText("123"));

        Button joinButton = lookup("Join Lobby").queryButton();

        AtomicLong heartbeatCount = new AtomicLong(0);

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

        clickOn(joinButton);

        Thread.sleep(200);

        long endHeartbeat = heartbeatCount.get();

        assertTrue("UI Thread was BLOCKED during Join Lobby! Heartbeat didn't increment enough.",
            (endHeartbeat - startHeartbeat) > 10);

        heartbeatThread.interrupt();
    }

    @Test
    public void testButtonScaling() {

        Button joinButton = lookup("Join Lobby").queryButton();
        assertEquals("Button width should be 6 tiles", 6 * Constants.TILE_SIZE, joinButton.getPrefWidth(), 0.1);
        assertEquals("Button height should be 2 tiles", 2 * Constants.TILE_SIZE, joinButton.getPrefHeight(), 0.1);
    }

    @Test
    public void testInvalidLobbyIdHandling() {

        TextField lobbyIdInput = lookup(".text-field").queryAs(TextField.class);
        clickOn(lobbyIdInput).write("not-a-number");

        clickOn("Join Lobby");

        Node errorNode = lookup(".text").match(n -> n instanceof Text && ((Text) n).getText().contains("Invalid"))
            .query();
        assertNotNull("Should display error message for invalid Lobby ID", errorNode);
    }

    @Test
    public void testSmoothAnimationLoop() {

        int initialClock = ClientMain.clock;

        clickOn("Create Lobby");

        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Start Game");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for game to start. Error: " + e.getMessage());
        }

        assertTrue("Game clock should be advancing in the AnimationTimer", ClientMain.clock > initialClock);
    }
}
