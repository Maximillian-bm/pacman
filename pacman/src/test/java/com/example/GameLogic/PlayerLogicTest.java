package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.common.BaseTest;
import com.example.common.OptimalTimeoutMillis;
import com.example.model.Action;
import com.example.model.Direction;
import com.example.model.GameState;
import com.example.model.Ghost;
import com.example.model.GhostType;
import com.example.model.Player;
import com.example.model.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PlayerLogicTest extends BaseTest {

    private ClientGameController controller;
    private GameState initialState;
    private ConnectToLobby host;

    @Override
    protected long getTimeoutSeconds() {
        return 5;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 200;
    }

    @Before
    public void setUp() {
        controller = new ClientGameController();
        Ghost.setFrightenedTimerSec(0.0);
        initialState = controller.initializeGameState(2);
        initialState.ghosts().clear();

        host = new ConnectToLobby();
    }

    @Test
    public void testPlayerEatsPlayerWithEnergizer() {
        Player predator = initialState.players().getFirst();
        Player prey = initialState.players().get(1);

        predator.setEnergized(true);
        prey.setEnergized(false);

        predator.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        prey.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));

        prey.setLives(3);
        prey.setAlive(true);
        int initialPoints = predator.getPoints();

        controller.updateGameState(initialState, new ArrayList<>());

        assertEquals("Prey should lose a life when eaten by energized player", 2, prey.getLives());
        assertFalse("Prey should be dead/respawning", prey.isAlive());
        assertTrue("Predator should gain points for eating player", predator.getPoints() > initialPoints);
    }

    @Test
    public void testPlayerCollisionNoEnergizer() {
        Player p1 = initialState.players().getFirst();
        Player p2 = initialState.players().get(1);

        p1.setEnergized(false);
        p2.setEnergized(false);

        p1.setPosition(new Position(3 * TILE_SIZE, 3 * TILE_SIZE));
        p1.setDirection(Direction.EAST);

        p2.setPosition(new Position(4 * TILE_SIZE, 3 * TILE_SIZE));
        p2.setDirection(Direction.WEST);

        controller.updateGameState(initialState, new ArrayList<>());

        double distance = Math.abs(p1.getPosition().x - p2.getPosition().x);
        assertTrue("Players should be at least one tile apart (collision blocked). Distance: " + distance,
            distance >= TILE_SIZE - 1.0);

        assertTrue(p1.isAlive());
        assertTrue(p2.isAlive());
    }

    @Test
    public void testPlayerCollisionSameSpot() {
        Player p1 = initialState.players().getFirst();
        Player p2 = initialState.players().get(1);

        p1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        p2.setPosition(new Position(4 * TILE_SIZE, 5 * TILE_SIZE));
        p2.setDirection(Direction.EAST);

        controller.updateGameState(initialState, new ArrayList<>());

        double p2X = p2.getPosition().x;
        assertTrue("Moving player should be blocked by stationary player. Pos: " + p2X,
            p2X <= 4 * TILE_SIZE + 2.0);
    }

    @Test
    public void testSimultaneousPlayerGhostSpawnCollision() {
        Player p = initialState.players().getFirst();
        Ghost g = new Ghost(GhostType.RED);
        initialState.ghosts().add(g);

        Position deathSpot = new Position(5 * TILE_SIZE, 5 * TILE_SIZE);
        p.setSpawnPosition(deathSpot);
        g.setPosition(deathSpot);

        p.setAlive(false);
        p.setRespawnTimer(0.01);
        p.setLives(2);

        controller.updateGameState(initialState, new ArrayList<>());

        if (!p.isAlive()) {
            assertEquals("Player should lose a life immediately if spawn is camped", 1, p.getLives());
        }
    }

    @Test
    public void testRapidDirectionSwitching() {
        Player p = initialState.players().getFirst();
        p.setPosition(new Position(TILE_SIZE, TILE_SIZE));

        List<Action> spamActions = new ArrayList<>();
        spamActions.add(new Action(p.getId(), 0, 1));
        spamActions.add(new Action(p.getId(), 0, 2));
        spamActions.add(new Action(p.getId(), 0, 3));
        spamActions.add(new Action(p.getId(), 0, 4));

        controller.updateGameState(initialState, spamActions);

        assertNotNull(p.getIntendedDirection());
    }

    @Test
    public void testScoreCap() {
        Player p = initialState.players().getFirst();
        p.addPoints(Integer.MAX_VALUE - 5);
        p.addPoints(10);

        assertTrue("Score should handle overflow gracefully (e.g. cap or use long)", p.getPoints() > 0);
    }

    @Test
    public void testPlayerJoinAndLeaveLobby() {
        host.createLobby(3);
        int lobbyId = host.getLobbyID();
        assertTrue("Lobby ID should be valid", lobbyId > 0);

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(String.valueOf(lobbyId));
        assertEquals("Player 2 should have ID 2", 2, p2.getPlayerID());

        p2.leaveLobby();

        ConnectToLobby p3 = new ConnectToLobby();
        p3.joinLobby(String.valueOf(lobbyId));

        assertTrue("Lobby should accept new player after one leaves", p3.getPlayerID() > 0);
    }

    @Test
    public void testRapidJoinLeaveChurn() {
        host.createLobby(10);
        int lobbyId = host.getLobbyID();

        int numberOfChurns = 5;
        for (int i = 0; i < numberOfChurns; i++) {
            ConnectToLobby p = new ConnectToLobby();
            p.joinLobby(String.valueOf(lobbyId));
            assertTrue("Player " + i + " should join successfully", p.getPlayerID() > 0);
            p.leaveLobby();
        }
        
        // After churn, try to join again and see if ID is reused or consistent
        ConnectToLobby finalPlayer = new ConnectToLobby();
        finalPlayer.joinLobby(String.valueOf(lobbyId));
        assertTrue("Should be able to join after churn", finalPlayer.getPlayerID() > 0);
    }

    @Test
    public void testLeaveDuringGameStart() {
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);

        Thread gameThread = new Thread(() -> host.startGame());
        gameThread.setDaemon(true);
        gameThread.start();

        p2.leaveLobby();
        assertFalse("Player 2 should be considered out of the game", host.isPlayerInGame(p2.getPlayerID()));

        ConnectToLobby p2Rejoin = new ConnectToLobby();
        p2Rejoin.joinLobby(lobbyId);
        assertTrue("Rejoined player should have a valid positive ID", p2Rejoin.getPlayerID() > 0);
    }

    @Test
    public void testFullLobbyJoinLeaveCycle() {
        int maxPlayers = 3;
        host.createLobby(maxPlayers);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);
        ConnectToLobby p3 = new ConnectToLobby();
        p3.joinLobby(lobbyId);

        Thread t = new Thread(() -> {
            ConnectToLobby p4 = new ConnectToLobby();
            p4.joinLobby(lobbyId);
        });
        t.setDaemon(true);
        t.start();

        try {
            t.join(1000);
        } catch (InterruptedException _) { }

        p2.leaveLobby();

        ConnectToLobby p5 = new ConnectToLobby();
        p5.joinLobby(lobbyId);
        assertTrue("Player should be able to join after spot frees up", p5.getPlayerID() > 0);
    }

    @Test
    public void testDisconnectMidGame() {
        host.createLobby(2);
        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(String.valueOf(host.getLobbyID()));

        host.startGame();
        p2.startGame();

        p2.leaveLobby();

        boolean isP2InGame = host.isPlayerInGame(p2.getPlayerID());
        assertFalse("Player 2 should be removed from game after leaving", isP2InGame);
    }

    @Test
    @OptimalTimeoutMillis(3000)
    public void testSimultaneousConnections() throws InterruptedException {
        int numberOfPlayers = 10;
        host.createLobby(numberOfPlayers);
        String lobbyId = String.valueOf(host.getLobbyID());

        List<ConnectToLobby> connectedPlayers = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfPlayers - 1; i++) {
            Thread t = new Thread(() -> {
                try {
                    ConnectToLobby player = new ConnectToLobby();
                    player.joinLobby(lobbyId);
                    connectedPlayers.add(player);
                } catch (Throwable e) {
                    exceptions.add(e);
                }
            });
            t.setDaemon(true);
            threads.add(t);
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join(2000);
        }

        if (!exceptions.isEmpty()) {
            fail("Exceptions occurred during simultaneous connection: " + exceptions.getFirst().getMessage());
        }

        assertEquals("All players should have joined", numberOfPlayers - 1, connectedPlayers.size());

        long uniqueIds = connectedPlayers.stream()
            .map(ConnectToLobby::getPlayerID)
            .distinct()
            .count();
        assertEquals("All players should have unique IDs", connectedPlayers.size(), uniqueIds);
    }
}
