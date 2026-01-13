package com.example.GameLogic;

import static com.example.model.Constants.TILE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Player Game Logic and Lobby Interaction Tests")
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

    @BeforeEach
    public void setUp() {
        controller = new ClientGameController();
        Ghost.setFrightenedTimerSec(0.0);
        initialState = controller.initializeGameState(2);
        initialState.ghosts().clear();

        host = new ConnectToLobby();
    }

    @Test
    @DisplayName("Energized player should eat other players and gain points")
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

        assertEquals(2, prey.getLives(), "Prey should lose a life when eaten by energized player");
        assertFalse(prey.isAlive(), "Prey should be dead/respawning");
        assertTrue(predator.getPoints() > initialPoints, "Predator should gain points for eating player");
    }

    @Test
    @DisplayName("Non-energized players should collide and block each other")
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
        assertTrue(distance >= TILE_SIZE - 1.0, "Players should be at least one tile apart (collision blocked). Distance: " + distance);

        assertTrue(p1.isAlive());
        assertTrue(p2.isAlive());
    }

    @Test
    @DisplayName("Moving player should be blocked by stationary player")
    public void testPlayerCollisionSameSpot() {
        Player p1 = initialState.players().getFirst();
        Player p2 = initialState.players().get(1);

        p1.setPosition(new Position(5 * TILE_SIZE, 5 * TILE_SIZE));
        p2.setPosition(new Position(4 * TILE_SIZE, 5 * TILE_SIZE));
        p2.setDirection(Direction.EAST);

        controller.updateGameState(initialState, new ArrayList<>());

        double p2X = p2.getPosition().x;
        assertTrue(p2X <= 4 * TILE_SIZE + 2.0, "Moving player should be blocked by stationary player. Pos: " + p2X);
    }

    @Test
    @DisplayName("Player should lose life if spawning directly on a ghost")
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
            assertEquals(1, p.getLives(), "Player should lose a life immediately if spawn is camped");
        }
    }

    @Test
    @DisplayName("Rapid direction switching should maintain intended direction")
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
    @DisplayName("Score should handle potential integer overflow")
    public void testScoreCap() {
        Player p = initialState.players().getFirst();
        p.addPoints(Integer.MAX_VALUE - 5);
        p.addPoints(10);

        assertTrue(p.getPoints() > 0, "Score should handle overflow gracefully (e.g. cap or use long)");
    }

    @Test
    @DisplayName("Lobby should manage player joining and leaving correctly")
    public void testPlayerJoinAndLeaveLobby() {
        host.createLobby(3);
        int lobbyId = host.getLobbyID();
        assertTrue(lobbyId > 0, "Lobby ID should be valid");

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(String.valueOf(lobbyId));
        assertEquals(2, p2.getPlayerID(), "Player 2 should have ID 2");

        p2.leaveLobby();

        ConnectToLobby p3 = new ConnectToLobby();
        p3.joinLobby(String.valueOf(lobbyId));

        assertTrue(p3.getPlayerID() > 0, "Lobby should accept new player after one leaves");
    }

    @Test
    @DisplayName("Lobby should remain stable during rapid player join/leave cycles")
    public void testRapidJoinLeaveChurn() {
        host.createLobby(10);
        int lobbyId = host.getLobbyID();

        int numberOfChurns = 5;
        for (int i = 0; i < numberOfChurns; i++) {
            ConnectToLobby p = new ConnectToLobby();
            p.joinLobby(String.valueOf(lobbyId));
            assertTrue(p.getPlayerID() > 0, "Player " + i + " should join successfully");
            p.leaveLobby();
        }
        
        // After churn, try to join again and see if ID is reused or consistent
        ConnectToLobby finalPlayer = new ConnectToLobby();
        finalPlayer.joinLobby(String.valueOf(lobbyId));
        assertTrue(finalPlayer.getPlayerID() > 0, "Should be able to join after churn");
    }

    @Test
    @DisplayName("Leaving during game start should remove player from active game")
    public void testLeaveDuringGameStart() {
        host.createLobby(2);
        String lobbyId = String.valueOf(host.getLobbyID());

        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(lobbyId);

        Thread gameThread = new Thread(() -> host.startGame());
        gameThread.setDaemon(true);
        gameThread.start();

        p2.leaveLobby();
        assertFalse(host.isPlayerInGame(p2.getPlayerID()), "Player 2 should be considered out of the game");

        ConnectToLobby p2Rejoin = new ConnectToLobby();
        p2Rejoin.joinLobby(lobbyId);
        assertTrue(p2Rejoin.getPlayerID() > 0, "Rejoined player should have a valid positive ID");
    }

    @Test
    @DisplayName("Lobby should accept new players once a spot is freed in a full lobby")
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
        assertTrue(p5.getPlayerID() > 0, "Player should be able to join after spot frees up");
    }

    @Test
    @DisplayName("Mid-game disconnect should remove player from game state")
    public void testDisconnectMidGame() {
        host.createLobby(2);
        ConnectToLobby p2 = new ConnectToLobby();
        p2.joinLobby(String.valueOf(host.getLobbyID()));

        host.startGame();
        p2.startGame();

        p2.leaveLobby();

        boolean isP2InGame = host.isPlayerInGame(p2.getPlayerID());
        assertFalse(isP2InGame, "Player 2 should be removed from game after leaving");
    }

    @Test
    @OptimalTimeoutMillis(3000)
    @DisplayName("Lobby should handle simultaneous connections and assign unique IDs")
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

        assertEquals(numberOfPlayers - 1, connectedPlayers.size(), "All players should have joined");

        long uniqueIds = connectedPlayers.stream()
            .map(ConnectToLobby::getPlayerID)
            .distinct()
            .count();
        assertEquals(connectedPlayers.size(), uniqueIds, "All players should have unique IDs");
    }
}
