package uk.ac.soton.comp1206.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.audio.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {

        super(gameWindow);
        logger.info("Creating Instruction Scene");
    }

    @Override
    public void initialise() {
        logger.info("Initialising " + this.getClass().getName());
        this.getScene().setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                Multimedia.playAudio("transition.wav");
                gameWindow.startMenu();
            }
        });
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //setting the instructionPane to fit the gamewindow
        var instructionsPane = new BorderPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionsPane);

        //setting the instruction image to fit the gamewindow
        String toShow =	Multimedia.class.getResource("/images/Instructions.png").toExternalForm();
        ImageView instructionsImage = new ImageView(new Image(toShow));
        instructionsImage.setFitHeight(gameWindow.getHeight());
        instructionsImage.setFitWidth(gameWindow.getWidth());
        instructionsPane.setCenter(instructionsImage);

    }

}
