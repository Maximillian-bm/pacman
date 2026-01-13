package com.example.GameLogic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.GameLogic.ClientComs.ConnectToLobby;
import com.example.GameLogic.ClientComs.Reader;
import com.example.common.BaseTest;
import com.example.common.OptimalTimeoutMillis;
import com.example.model.Action;
import com.example.model.ActionList;
import com.example.model.Constants;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

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
    public void testActionUtilConversionValid() {
        Object[] input = new Object[]{1, 100, 2, 5};
        Action action = ActionUtil.convertObjToAction(input);

        assertEquals(1, action.getPlayerId());
        assertEquals(100, action.getClock());
        assertEquals(2, action.getMove());
        assertEquals(5, action.getIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testActionUtilConversionInvalidLength() {
        Object[] input = new Object[]{1, 100};
        Action action = ActionUtil.convertObjToAction(input);
        assertNotNull("Action should not be null if it somehow succeeded", action);
    }

    @Test
    public void testURIUtilGetLobbyID() {
        String baseUri = Constants.REMOTE_PUBLIC_URI;
        String uri = baseUri.replace("/?", "/123sync?");
        int id = URIUtil.getLobbyID(uri);
        assertEquals(123, id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testURIUtilInvalidURI() {
        URIUtil.getLobbyID(Constants.REMOTE_PUBLIC_URI);
    }

    @Test
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
    public void testActionListMissedActionLogic() {
        ActionList list = new ActionList();
        Action a1 = new Action(1, 10, 2, 5);
        list.addAction(a1);

        list.getActions(10);
        assertTrue("Should have missed action", list.missedAction());

        list.fixedMissedAction();
        assertFalse(list.missedAction());
    }

    @Test
    public void testCreateLobbySuccess() {
        ConnectToLobby client = new ConnectToLobby();
        client.createLobby(2);
        assertTrue("Lobby ID should be positive", client.getLobbyID() > 0);
        assertTrue("Player ID should be set (usually 0 for creator)", client.getPlayerID() >= 0);
    }

    @Test
    public void testJoinLobbySuccess() {
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(2);
        int lobbyId = creator.getLobbyID();
        assertTrue("Failed to create lobby for join test", lobbyId > 0);

        ConnectToLobby joiner = new ConnectToLobby();
        joiner.joinLobby(String.valueOf(lobbyId));
        assertEquals("Joined lobby ID should match", lobbyId, joiner.getLobbyID());
        assertTrue("Player ID should be valid", joiner.getPlayerID() >= 0);
        assertNotEquals("Players should have different IDs", creator.getPlayerID(), joiner.getPlayerID());
    }

    @Test(expected = NumberFormatException.class)
    public void testJoinLobbyInvalidIdFormat() {
        ConnectToLobby client = new ConnectToLobby();
        client.joinLobby("invalid-id");
    }

    @Test
    public void testJoinNonExistentLobby() {
        ConnectToLobby client = new ConnectToLobby();
        client.joinLobby("999999");
        assertNotEquals("Should not be able to join non-existent lobby", 999999, client.getLobbyID());
    }

    @Test
    @OptimalTimeoutMillis(3000)
    public void testStartGameWait() throws InterruptedException {
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(1);

        Thread gameThread = new Thread(creator::startGame);
        gameThread.start();
        gameThread.join(2000);

        assertTrue(gameThread.isAlive());
    }

    @Test
    public void testMultipleLobbyCreation() {
        ConnectToLobby client1 = new ConnectToLobby();
        client1.createLobby(2);

        ConnectToLobby client2 = new ConnectToLobby();
        client2.createLobby(2);

        assertNotEquals("Lobby IDs should be unique", client1.getLobbyID(), client2.getLobbyID());
    }

    @Test
    public void testLobbyCapacityLimit() {
        ConnectToLobby creator = new ConnectToLobby();
        creator.createLobby(1);

        ConnectToLobby joiner = new ConnectToLobby();
        joiner.joinLobby(String.valueOf(creator.getLobbyID()));

        assertNotEquals("Should not be able to join full lobby", creator.getLobbyID(), joiner.getLobbyID());
    }

    @Test(expected = TimeoutException.class)
    public void testJoinLobbyTimeout() throws Throwable {
        ConnectToLobby client = new ConnectToLobby();
        client.joinLobby("12345", 500);
    }

    @Test
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
