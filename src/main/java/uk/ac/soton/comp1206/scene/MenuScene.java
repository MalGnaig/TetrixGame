package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.audio.Multimedia;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoresList;

import java.io.StreamCorruptedException;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {
    private ImageView title;

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);


        title = new ImageView(new Image(this.getClass().getResource("/images/TetrECS.png").toExternalForm()));
        title.setPreserveRatio(true);
        title.setFitWidth(700);

        mainPane.setCenter(title);

        //For now, let us just add a challengeButton that starts the game. .
        var challengeButton = new Button("Play");
        challengeButton.setWrapText(true);
        var instructionsButton = new Button("instructions");
        instructionsButton.setWrapText(true);
        var scoreBoardButton = new Button("scoreboard");
        scoreBoardButton.setWrapText(true);

        VBox buttons = new VBox(challengeButton, scoreBoardButton, instructionsButton);
        buttons.setMaxWidth(300);
        buttons.setMaxHeight(100);
        mainPane.setBottom(buttons);
        buttons.setSpacing(15);

        //Bind the challengeButton action to the startGame method in the menu
        challengeButton.setOnAction(this::startGame);
        scoreBoardButton.setOnAction((evt) -> gameWindow.startScores(new Game(5, 5)));
        instructionsButton.setOnAction(this::showInstructions);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        Multimedia.playMusic("menu.mp3");
        TranslateTransition translate = new TranslateTransition(Duration.seconds(4), title);
        translate.setCycleCount(TranslateTransition.INDEFINITE);
        translate.setByX(200);
        translate.setAutoReverse(true);
        translate.play();

        scene.setOnKeyPressed((e) -> {
            if (e.getCode() != KeyCode.ESCAPE) return;
            gameWindow.shutdown();
        });
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        Multimedia.playAudio("transition.wav");
    }

    /**
     * Handle when the instructions button is pressed
     * @param event event
     */
    private void showInstructions(ActionEvent event) {
        gameWindow.startInstructions();
    }
}
