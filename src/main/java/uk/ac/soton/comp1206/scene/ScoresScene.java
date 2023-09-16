package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.audio.Multimedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScoresScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    Game game;
    SimpleListProperty<Pair<String, Integer>> localScores;
    TextField playerName;

    static File scoresDataBase = new File("newscores.txt");
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */

    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        logger.info("Creating Scores Scene");
        List<Pair<String, Integer>> burger = new ArrayList<>();
        localScores = new SimpleListProperty<>(FXCollections.observableArrayList(burger));
    }

    @Override
    public void initialise() {
        logger.info("Building " + this.getClass().getName());

        this.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER -> {
                    localScores.get().add(new Pair<>(playerName.getText(), game.getScore()));
                    writeScores();
                    gameWindow.startMenu();
                }
                case ESCAPE -> {
                    gameWindow.startMenu();
                    Multimedia.playAudio("transition.wav");
                }
            }
        });
    }




    @Override
    public void build() {
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new BorderPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        ScoresList scoresList = new ScoresList();
        scoresList.scoresProperty().bind(localScores);
        ListView<Pair<String, Integer>> showingList = scoresList.reveal();
        showingList.itemsProperty().bind(scoresList.scoresProperty());
        menuPane.setCenter(showingList);


        loadScores();

        playerName = new TextField();
        menuPane.setBottom(playerName);
    }

    /**
     * reading the Scores from the newscores.txt File
     */
    private void loadScores(){
        Scanner myReader = null;
        try {
            myReader = new Scanner(scoresDataBase);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert myReader != null;
        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            String [] splitLine = line.split("-");
            String name = splitLine[0];
            Integer score = Integer.valueOf(splitLine[1]);

            int i = 0;
            for (; i < localScores.size(); i++) {
                if (localScores.get(i).getValue() < score)
                    break;
            }

            localScores.get().add(i, new Pair<>(name, score));
        }
    }
    /**
     * writing the scores to the newscores.txt File
     */
    private void writeScores() {
        BufferedWriter fileWriter;
        try {
            fileWriter = new BufferedWriter(new FileWriter(scoresDataBase));
            for (Pair<String, Integer> localScore : localScores) {
                fileWriter.write(localScore.getKey() + "-" + localScore.getValue());
                fileWriter.newLine();
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the highest Score from the newscores.txt File for the Challenge scene
     */
    public static int getHighScore() {
        int returnValue = 0;
        Scanner myReader = null;
        try {
            myReader = new Scanner(scoresDataBase);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert myReader != null;
        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            String [] splitLine = line.split("-");
            int score = Integer.parseInt(splitLine[1]);
            returnValue = Math.max(returnValue, score);
        }
        return returnValue;
    }
}
