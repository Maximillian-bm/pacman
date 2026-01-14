package com.example.GUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class SoundEngine {

    String soundFile = "src/main/resources/sounds/02. Start Music.mp3";
    Media media = new Media(new File(soundFile).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(media);

    public void startMusic() {
        mediaPlayer.play();
    }
}

