package com.example.GameLogic;

import com.example.GUI.UI;
import com.example.GameLogic.ClientComs.Reader;
import com.example.model.*;
import javafx.application.Application;

public class ClientMain {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e.getClass().getName().contains("MediaException") || 
               (e.getCause() != null && e.getCause().getClass().getName().contains("MediaException"))) {
                if (!com.example.GUI.SoundEngine.disabled) {
                    com.example.GUI.SoundEngine.disabled = true;
                    System.err.println("Audio system error detected. Disabling audio to prevent crashes.");
                }
            } else {
                System.err.print("Exception in thread \"" + t.getName() + "\" ");
                e.printStackTrace();
            }
        });
        Application.launch(UI.class, args);
    }
}
