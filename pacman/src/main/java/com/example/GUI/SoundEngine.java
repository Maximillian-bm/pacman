package com.example.GUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class SoundEngine {

    String soundFile = "path/to/your/sound.wav";
    Media media = new Media(new File(soundFile).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(media);

    public void startMusic() {
        mediaPlayer.play();
    }

    public void stopMusic() {
        mediaPlayer.stop();
    }
}

