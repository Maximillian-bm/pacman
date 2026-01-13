package com.example.GameLogic;

import com.example.common.BaseTest;
import com.example.model.Action;
import com.example.model.ActionList;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GameCommunicationTest extends BaseTest {

    @Override
    protected long getTimeoutSeconds() {
        return 1;
    }

    @Override
    protected long getOptimalTimeoutMillis() {
        return 50;
    }

    @Test
    public void testActionUtilConversionValid() {
        Object[] input = new Object[]{1, 100, 2, 5}; // playerId, clock, move, index
        Action action = ActionUtil.convertObjToAction(input);
        
        assertEquals(1, action.getPlayerId());
        assertEquals(100, action.getClock());
        assertEquals(2, action.getMove());
        assertEquals(5, action.getIndex());
    }

    @Test(expected = AssertionError.class)
    public void testActionUtilConversionInvalidLength() {
        Object[] input = new Object[]{1, 100};
        ActionUtil.convertObjToAction(input);
    }

    @Test
    public void testURIUtilGetLobbyID() {
        String uri = "tcp://127.0.0.1:50000/123sync?keep";
        int id = URIUtil.getLobbyID(uri);
        assertEquals(123, id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testURIUtilInvalidURI() {
        URIUtil.getLobbyID("tcp://127.0.0.1:50000/?keep");
    }

    @Test
    public void testActionListLogic() {
        ActionList list = new ActionList();
        Action a1 = new Action(1, 10, 2, 1);
        list.addAction(a1);
        
        List<Action> actions = list.getActions(10);
        assertFalse(actions.isEmpty());
        assertEquals(1, actions.size());
        assertEquals(10, actions.get(0).getClock());
    }
    
    @Test
    public void testActionListMissedActionLogic() {
        ActionList list = new ActionList();
        // Add action with index 5, but we haven't processed 0-4
        Action a1 = new Action(1, 10, 2, 5);
        list.addAction(a1);
        
        // This should trigger missedAction because index 5 > nrOfActionsCalled (0)
        list.getActions(10);
        assertTrue("Should have missed action", list.missedAction());
        
        list.fixedMissedAction();
        assertFalse(list.missedAction());
    }
}