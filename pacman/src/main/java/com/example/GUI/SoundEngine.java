package com.example.GUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.EnumMap;
import java.util.Map;

import com.example.model.Sound;

public class SoundEngine {

    private final Map<Sound, MediaPlayer> players = new EnumMap<>(Sound.class);

    public SoundEngine() {
        try {
            for (Sound sound : Sound.values()) {
                Media media = new Media(
                    getClass().getResource("/" + sound.getPath()).toExternalForm()
                );
                MediaPlayer player = new MediaPlayer(media);
                players.put(sound, player);
            }
        } catch (Exception e) {
            System.out.println("WARNING: Sound does not work");
        }
    }

    public void play(Sound sound) {
        try {
            MediaPlayer player = players.get(sound);
            player.stop();   // IMPORTANT: restart sound if already playing
            player.play();
        } catch (Exception e) {
            System.out.println("WARNING: failed to play sound");
        }
    }

    public void stop(Sound sound) {
        players.get(sound).stop();
    }

    public void setVolume(double volume) {
        players.values().forEach(p -> p.setVolume(volume));
    }
}

