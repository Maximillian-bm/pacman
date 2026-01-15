package com.example.GUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.EnumMap;
import java.util.Map;

import com.example.model.Sound;

public class SoundEngine {

    private final Map<Sound, MediaPlayer> players = new EnumMap<>(Sound.class);

    public SoundEngine() {
        for (Sound sound : Sound.values()) {
            Media media = new Media(
                getClass().getResource("/" + sound.getPath()).toExternalForm()
            );
            MediaPlayer player = new MediaPlayer(media);
            players.put(sound, player);
        }
    }

    public void play(Sound sound) {
        MediaPlayer player = players.get(sound);
        player.stop();   // IMPORTANT: restart sound if already playing
        player.play();
    }

    public void stop(Sound sound) {
        players.get(sound).stop();
    }

    public void setVolume(double volume) {
        players.values().forEach(p -> p.setVolume(volume));
    }
}

