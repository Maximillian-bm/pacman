package com.example.GUI;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class SoundEngine {

    private MediaPlayer creditMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/01. Credit Sound.mp3").toURI().toString()));
    private MediaPlayer startMusicMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/02. Start Music.mp3").toURI().toString()));
    private MediaPlayer eatDotMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/03 PAC-MAN - Eating The Pac-dots.mp3").toURI().toString()));
    private MediaPlayer turningCornerMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/04. PAC-MAN - Turning The Corner While Eating The Pac-dots.mp3").toURI().toString()));
    private MediaPlayer extendSoundMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/05. Extend Sound.mp3").toURI().toString()));
    private MediaPlayer ghostNormalMoveMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/06. Ghost - Normal Move.mp3").toURI().toString()));
    private MediaPlayer ghostSpurtMove1MediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/07. Ghost - Spurt Move #1.mp3").toURI().toString()));
    private MediaPlayer ghostSpurtMove2MediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/08. Ghost - Spurt Move #2.mp3").toURI().toString()));
    private MediaPlayer ghostSpurtMove3MediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/09. Ghost - Spurt Move #3.mp3").toURI().toString()));
    private MediaPlayer ghostSpurtMove4MediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/10. Ghost - Spurt Move #4.mp3").toURI().toString()));
    private MediaPlayer eatFruitMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/11. PAC-MAN - Eating The Fruit.mp3").toURI().toString()));
    private MediaPlayer ghostTurnBlueMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/12. Ghost - Turn to Blue.mp3").toURI().toString()));
    private MediaPlayer eatGhostMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/13. PAC-MAN - Eating The Ghost.mp3").toURI().toString()));
    private MediaPlayer ghostReturnHomeMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/14. Ghost - Return to Home.mp3").toURI().toString()));
    private MediaPlayer failMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/15. Fail.mp3").toURI().toString()));
    private MediaPlayer coffeeBreakMusicMediaPlayer = new MediaPlayer(new Media(new File("src/main/resources/sounds/16. Coffee Break Music.mp3").toURI().toString()));

    public void credit() {
        creditMediaPlayer.play();
    }

    public void startMusic() {
        startMusicMediaPlayer.play();
    }

    public void eatDot(){
        eatDotMediaPlayer.play();
    }

    public void turningCorner() {
        turningCornerMediaPlayer.play();
    }

    public void extendSound() {
        extendSoundMediaPlayer.play();
    }

    public void ghostNormalMove() {
        ghostNormalMoveMediaPlayer.play();
    }

    public void ghostSpurtMove1() {
        ghostSpurtMove1MediaPlayer.play();
    }

    public void ghostSpurtMove2() {
        ghostSpurtMove2MediaPlayer.play();
    }

    public void ghostSpurtMove3() {
        ghostSpurtMove3MediaPlayer.play();
    }

    public void ghostSpurtMove4() {
        ghostSpurtMove4MediaPlayer.play();
    }

    public void eatFruit() {
        eatFruitMediaPlayer.play();
    }

    public void ghostTurnBlue() {
        ghostTurnBlueMediaPlayer.play();
    }

    public void eatGhost() {
        eatGhostMediaPlayer.play();
    }

    public void ghostReturnHome() {
        ghostReturnHomeMediaPlayer.play();
    }

    public void fail() {
        failMediaPlayer.play();
    }

    public void coffeeBreakMusic() {
        coffeeBreakMusicMediaPlayer.play();
    }
}

