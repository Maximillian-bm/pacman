package com.example.GameLogic;

import java.io.IOException;

import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import com.example.model.Constants;

public class test {
    public static void main(String[] arg){
        try {
            String space1URI = URIUtil.getSpace1URI(Constants.REMOTE_PUBLIC_URI);
            System.out.println("connecting to "+space1URI);
            Space space1 = new RemoteSpace(space1URI);
            int lobbyID = (int) space1.get(new FormalField(Integer.class))[0];
            System.out.println("Asked for lobby id and got "+lobbyID);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
