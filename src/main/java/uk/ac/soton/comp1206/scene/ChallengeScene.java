package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.audio.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;
    protected GameBoard gameBoard;
    private GameBlock aim;
    private Rectangle gameLoopTimer;
    Label highScore;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());


        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        gameLoopTimer = new Rectangle (gameWindow.getWidth(), 20);
        mainPane.setTop(gameLoopTimer);

        gameBoard = new GameBoard(game.getGrid(),gameWindow.getWidth()/2.0,gameWindow.getWidth()/2.0);
        aim = gameBoard.getBlock(game.getCols()/2, game.getRows()/2);
        setIndicatorForAim(true);
        mainPane.setCenter(gameBoard);

        // adding the small pieceBoard to the scene which shows the next upcoming piece into gamewindow of challengescene
        PieceBoard pieceBoard = new PieceBoard(new Grid(3, 3), gameWindow.getWidth()/4.0,gameWindow.getWidth()/4.0);
        PieceBoard followingPieceBoard = new PieceBoard(new Grid(3, 3), gameWindow.getWidth()/6.0,gameWindow.getWidth()/6.0);


        //display all the UI values
        var scoreText = new Text("score:");
        scoreText.getStyleClass().add("text");
        var score = new Text();
        score.textProperty().bind(game.scoreProperty().asString());
        score.getStyleClass().add("value");

        var highScoreText = new Text("highscore:");
        highScoreText.getStyleClass().add("highscore");
        highScore = new Label(String.valueOf(ScoresScene.getHighScore()));
        highScore.getStyleClass().add("highscore");

        var levelText = new Text("level:");
        levelText.getStyleClass().add("text");
        var level = new Text();
        level.textProperty().bind(game.levelProperty().asString());
        level.getStyleClass().add("value");

        var livesText = new Text("lives:");
        livesText.getStyleClass().add("text");
        var lives = new Text();
        lives.textProperty().bind(game.livesProperty().asString());
        lives.getStyleClass().add("value");

        var multiplierText = new Text("multiplier:");
        multiplierText.getStyleClass().add("text");
        var multiplier = new Text();
        multiplier.textProperty().bind(game.multiplierProperty().asString());
        multiplier.getStyleClass().add("value");

        VBox pieceBoardsAndScore = new VBox(highScoreText, highScore, scoreText, score, levelText,  level, pieceBoard, followingPieceBoard);
        VBox uiValues = new VBox(livesText,  lives, multiplierText, multiplier);
        mainPane.setLeft(uiValues);
        mainPane.setRight(pieceBoardsAndScore);

        //Handle block on gameboard grid being clicked
        gameBoard.setOnBlockClick(this::blockClicked);
        pieceBoard.setOnRotate(this::rotateBlock);
        followingPieceBoard.setOnSwap(this::swapPieces);

        //Handle new Piece in game
        game.setNextPieceListener(pieceBoard::nextPiece);
        game.setFollowingPieceListener(followingPieceBoard::nextPiece);
        game.setLineClearedListener(this::clearLines);
        game.setGameLoopListener(this::startGameLoop);

        //getting a new Piece now, that a NextPieceListener is attached
        game.nextPiece();

        pieceBoard.getBlock(1, 1).setHasIndicator(true);

    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        if (gameBlock.getGameBoard().equals(gameBoard)) {
            setIndicatorForAim(false);
            aim = gameBlock;
        }
        game.blockClicked(gameBlock);
    }

    /**
     * if the middle Block was hit, rotate the piece
     * @param gameBlock the Block which was hit
     */
    private void rotateBlock(GameBlock gameBlock) {
        if (gameBlock.getX() == 1 && gameBlock.getY() == 1)
            game.rotateCurrentPiece(1);
    }

    /**
     * swap the Pieces in the game if the following PieceBoard was hit
     */
    private void swapPieces() {
        game.swapCurrentPiece();
    }

    /**
     * the main thread fades out the blocks while a new one waits this time and only puts in something afterwards
     * @param blockCoordinates all the blocks that must be erased
     */
    private void clearLines(Set<GameBlockCoordinate> blockCoordinates) {
        gameBoard.fadeOut(blockCoordinates);

        Thread gridReset = new Thread(() -> {
            try {
                Thread.sleep(250);
                for (GameBlockCoordinate blockCoordinate :
                        blockCoordinates) {
                    game.getGrid().set(blockCoordinate.getX(), blockCoordinate.getY(), 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        gridReset.start();
        if (game.getScore() > ScoresScene.getHighScore())
            highScore.textProperty().bind(game.scoreProperty().asString());
    }

    /**
     * two Transitions work parallel to shrink it and change color at the same time
     * @param timerDelay the Time this Timer should take
     */
    public void startGameLoop(int timerDelay, int lives) {
        if (lives == 0) {
            gameWindow.startScores(game);
            return;
        }
        final int CYCLE_COUNT = 100;

        FillTransition fillTransition = new FillTransition(Duration.millis(timerDelay), Color.GREEN, Color.RED);
        fillTransition.setCycleCount(CYCLE_COUNT);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(timerDelay));
        scaleTransition.setFromX(1);
        scaleTransition.setToX(0);
        scaleTransition.setCycleCount(CYCLE_COUNT);

        ParallelTransition parallelTransition = new ParallelTransition(gameLoopTimer, fillTransition, scaleTransition);
        parallelTransition.play();
    }

    /**
     * @param hasIndicator puts a new Indicator for the aim, whenever Keyboard controls are used and out otherwise
     */
    private void setIndicatorForAim(boolean hasIndicator) {
        this.aim.setHasIndicator(hasIndicator);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        Multimedia.playMusic("game.wav");
        game.start();

        //keyboard events
        this.getScene().setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case E, C, J -> game.rotateCurrentPiece(1);
                case Q, Z, I -> game.rotateCurrentPiece(3);
                case SPACE, R -> game.swapCurrentPiece();
                case W, UP -> {
                    if (aim.getY() == 0)
                        return;
                    aim.setHasIndicator(false);
                    aim = gameBoard.getBlock(aim.getX(), aim.getY() - 1);
                }
                case A, LEFT -> {
                    if (aim.getX() == 0)
                        return;
                    aim.setHasIndicator(false);
                    aim = gameBoard.getBlock(aim.getX() - 1, aim.getY());
                }
                case S, DOWN -> {
                    if (aim.getY() == 4)
                        return;
                    aim.setHasIndicator(false);
                    aim = gameBoard.getBlock(aim.getX(), aim.getY() + 1);
                }
                case D, RIGHT -> {
                    if (aim.getX() == 4)
                        return;
                    aim.setHasIndicator(false);
                    aim = gameBoard.getBlock(aim.getX() + 1, aim.getY());
                }
                case ENTER, X -> this.blockClicked(aim);
                case ESCAPE -> {
                    gameWindow.startMenu();
                    Multimedia.playAudio("transition.wav");
                }
            }
            aim.setHasIndicator(true);
        });
    }

}
