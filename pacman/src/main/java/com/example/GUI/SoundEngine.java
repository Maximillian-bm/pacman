package com.example.GUI;

import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundEngine {

    private String musicFile = "example.mp3";
    private Media sound = new Media(new File(musicFile).toURI().toString());
    private MediaPlayer mediaPlayer = new MediaPlayer(sound);

    public void startMusic() {
        mediaPlayer.play();
    }

    public void stopMusic() {
        mediaPlayer.stop();
    }
}

