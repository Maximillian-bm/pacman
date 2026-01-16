package com.example.ServerLogic;

import java.util.Scanner;

public class ServerIO implements Runnable{

    LobbyCleaner lobbyCleaner;

    public ServerIO(LobbyCleaner lobbyCleaner){
        this.lobbyCleaner = lobbyCleaner;
    }
    
    @Override
    public void run(){
        help();
        Scanner scanner = new Scanner (System.in);
     
      while (scanner.hasNextLine()) {
        String msg = scanner.nextLine();
        switch (msg) {
            case "help":
                help();
                break;
            case "h":
                help();
                break;
            case "show":
                lobbyCleaner.showActiveLobbys();
                break;
            case "s":
                lobbyCleaner.showActiveLobbys();
                break;
            case "close":
                lobbyCleaner.closeAllLobbys();
                break;
            case "c":
                lobbyCleaner.closeAllLobbys();
                break;
            default:
                invalidIndput();
                break;
        }

      }
    }

    private void help(){
        System.out.println("Command list\nhelp | h : Show command list\nshow | s : Show all active lobbys\nclose | c : Close all active lobbys");
    }

    private void invalidIndput(){
        System.out.println("Seems like you suck at following directions so let me help");
        help();
    }
}
