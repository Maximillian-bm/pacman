package com.example.GameLogic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.Reader;
import com.example.common.BaseTest;
import com.example.common.OptimalTimeoutMillis;
import com.example.model.Action;
import com.example.model.ActionList;
import com.example.model.Constants;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Game Systems Integration and Utility Tests")
public class GameIntegrationTest extends BaseTest {

    @Override
    protected long getTimeoutSeconds() {
        return 5;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 2000;
    }

    @Test
    @DisplayName("ActionUtil should correctly convert valid object arrays to Action objects")
    public void testActionUtilConversionValid() {
        Object[] input = new Object[]{1, 100, 2, 5};
        Action action = ActionUtil.convertObjToAction(input);

        assertEquals(1, action.getPlayerId());
        assertEquals(100, action.getClock());
        assertEquals(2, action.getMove());
        assertEquals(5, action.getIndex());
    }

    @Test
    @DisplayName("ActionUtil should throw exception for invalid input length")
    public void testActionUtilConversionInvalidLength() {
        Object[] input = new Object[]{1, 100};
        assertThrows(IllegalArgumentException.class, () -> {
            ActionUtil.convertObjToAction(input);
        });
    }

    @Test
    @DisplayName("URIUtil should extract lobby ID from valid synchronization URIs")
    public void testURIUtilGetLobbyID() {
        String baseUri = Constants.REMOTE_PUBLIC_URI;
        String uri = baseUri.replace("/?", "/123sync?");
        int id = URIUtil.getLobbyID(uri);
        assertEquals(123, id);
    }

    @Test
    @DisplayName("URIUtil should throw exception for URIs without lobby IDs")
    public void testURIUtilInvalidURI() {
        assertThrows(IllegalArgumentException.class, () -> {
            URIUtil.getLobbyID(Constants.REMOTE_PUBLIC_URI);
        });
    }

    @Test
    @DisplayName("ActionList should store and retrieve actions by clock tick")
    public void testActionListLogic() {
        ActionList list = new ActionList();
        Action a1 = new Action(1, 10, 2, 1);
        list.addAction(a1);

        List<Action> actions = list.getActions(10);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());
        assertEquals(10, actions.getFirst().getClock());
    }

    @Test
    @DisplayName("ActionList should detect missing intermediate actions")
    public void testActionListMissedActionLogic() {
        ActionList list = new ActionList();
        Action a1 = new Action(1, 10, 2, 5);
        list.addAction(a1);

        list.getActions(10);
        assertTrue(list.missedAction(), "Should have missed action");

        list.fixedMissedAction();
        assertFalse(list.missedAction());
    }

    @Test
    @DisplayName("Client should successfully create a new lobby on the server")
    public void testCreateLobbySuccess() {
        ConnectToLobby client = new ConnectToLobby();
        client.createLobby(2);
        assertTrue(client.getLobbyID() > 0, "Lobby ID should be positive");
        assertTrue(client.getPlayerID() >= 0, "Player ID should be set (usually 0 for creator)");
    }

    @Test
    @DisplayName("Client should successfully join an existing lobby")
    public void testJoinLobbySuccess() {
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(2);
        int lobbyId = creator.getLobbyID();
        assertTrue(lobbyId > 0, "Failed to create lobby for join test");

        ConnectToLobby joiner = new ConnectToLobby();
        joiner.joinLobby(String.valueOf(lobbyId));
        assertEquals(lobbyId, joiner.getLobbyID(), "Joined lobby ID should match");
        assertTrue(joiner.getPlayerID() >= 0, "Player ID should be valid");
        assertNotEquals(creator.getPlayerID(), joiner.getPlayerID(), "Players should have different IDs");
    }

    @Test
    @DisplayName("Joining a lobby with non-numeric ID should throw exception")
    public void testJoinLobbyInvalidIdFormat() {
        ConnectToLobby client = new ConnectToLobby();
        assertThrows(NumberFormatException.class, () -> {
            client.joinLobby("invalid-id");
        });
    }

    @Test
    @DisplayName("Joining a non-existent lobby should fail gracefully")
    public void testJoinNonExistentLobby() {
        ConnectToLobby client = new ConnectToLobby();
        client.joinLobby("999999");
        assertNotEquals(999999, client.getLobbyID(), "Should not be able to join non-existent lobby");
    }

    @Test
    @OptimalTimeoutMillis(3000)
    @DisplayName("Lobby creator should wait for other players or start game correctly")
    public void testStartGameWait() throws InterruptedException {
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(1);

        Thread gameThread = new Thread(creator::startGame);
        gameThread.start();
        gameThread.join(2000);

        assertTrue(gameThread.isAlive());
    }

    @Test
    @DisplayName("Multiple clients should be able to create unique lobbies simultaneously")
    public void testMultipleLobbyCreation() {
        ConnectToLobby client1 = new ConnectToLobby();
        client1.createLobby(2);

        ConnectToLobby client2 = new ConnectToLobby();
        client2.createLobby(2);

        assertNotEquals(client1.getLobbyID(), client2.getLobbyID(), "Lobby IDs should be unique");
    }

    @Test
    @DisplayName("Joining a full lobby should be prevented")
    public void testLobbyCapacityLimit() {
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(1);

        ConnectToLobby joiner = new ConnectToLobby();
        joiner.joinLobby(String.valueOf(creator.getLobbyID()));

        assertNotEquals(creator.getLobbyID(), joiner.getLobbyID(), "Should not be able to join full lobby");
    }

    @Test
    @DisplayName("Joining a lobby should timeout if the server does not respond")
    public void testJoinLobbyTimeout() throws Throwable {
        ConnectToLobby client = new ConnectToLobby();
        assertThrows(TimeoutException.class, () -> {
            client.joinLobby("12345", 500);
        });
    }

    @Test
    @DisplayName("Reader should correctly identify and handle socket disconnections")
    public void testReaderHandleDisconnect() throws InterruptedException {
        Reader reader = new Reader(999);
        Thread t = new Thread(reader);
        t.start();

        t.join(100);

        if (reader.isConnected()) {
            throw new RuntimeException("Reader should report disconnected");
        }
    }
}
