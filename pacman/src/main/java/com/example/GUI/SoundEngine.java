package com.example.GUI;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.EnumMap;
import java.util.Map;

import com.example.model.Sound;

public class SoundEngine {

    private final Map<Sound, AudioClip> soundPlayers = new EnumMap<>(Sound.class);
    private final Map<Sound, AudioClip> soundLoopers = new EnumMap<>(Sound.class);

    public SoundEngine() {
        try {
            for (Sound sound : Sound.values()) {
            String url = getClass().getResource("/" + sound.getPath()).toExternalForm();
            AudioClip player = new AudioClip(url);
            AudioClip looper = new AudioClip(url);
            looper.setCycleCount(AudioClip.INDEFINITE);
            soundPlayers.put(sound, player);
            soundLoopers.put(sound, looper);
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(Sound sound) {
        try {
            AudioClip player = soundPlayers.get(sound);
            player.stop();   // IMPORTANT: restart sound if already playing
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loop(Sound sound) {
        try {
            AudioClip looper = soundLoopers.get(sound);
            looper.stop();   // IMPORTANT: restart sound if already playing
            looper.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop(Sound sound) {
        try {
            soundPlayers.get(sound).stop();
            soundPlayers.get(sound).stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVolume(double volume) {
        try {
            soundPlayers.values().forEach(p -> p.setVolume(volume));
            soundLoopers.values().forEach(p -> p.setVolume(volume));    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

