package uk.ac.soton.comp1206.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Objects;

public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;

    /**
     * provided the right address in memory it will play background music indefinitely
     * @param music memory address for music
     */
    public static void playMusic(String music) {
        if (musicPlayer != null)
            musicPlayer.stop();
        logger.info("start background music: " + music);
        String toPlay =	Objects.requireNonNull(Multimedia.class.getResource("/music/" + music)).toExternalForm();
        musicPlayer = new MediaPlayer(new Media(toPlay));
        musicPlayer.play();
        musicPlayer.setAutoPlay(true);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    }

    /**
     * provided the right address in memory it will play a sound once
     * @param audio memory address for audio
     */
    public static void playAudio(String audio){
        logger.info("start sound effect: " + audio);
        String toPlay =	Multimedia.class.getResource("/sounds/"+audio).toExternalForm();
        audioPlayer = new MediaPlayer(new Media(toPlay));
        audioPlayer.play();
    }
}
