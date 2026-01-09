package com.example.GameLogic;

import com.example.GameLogic.ClientComs.Reader;
import com.example.UI.UI;
import com.example.model.*;
import javafx.application.Application;

public class ClientMain {
    public static int clock = 0;
    
    public static void main(String[] args) {
        Application.launch(UI.class, args);
    }
}
