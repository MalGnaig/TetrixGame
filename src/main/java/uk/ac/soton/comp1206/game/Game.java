package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.audio.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The current GamePiece which will be placed next
     */
    private GamePiece gamePiece;
    private GamePiece followingPiece;

    private NextPieceListener nextPieceListener;
    private NextPieceListener followingPieceListener;
    private GameLoopListener gameLoopListener;

    private LineClearedListener lineClearedListener;

    private final IntegerProperty score = new SimpleIntegerProperty(this, "score", 0);
    private final IntegerProperty level = new SimpleIntegerProperty(this, "level", 0);
    private final IntegerProperty lives = new SimpleIntegerProperty(this, "lives", 3);
    private final IntegerProperty multiplier = new SimpleIntegerProperty(this, "multiplier", 1);

    private int timerDelay = 12_000;
    private Timer timer = new Timer(true);
    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        buildTimer();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //check whether this block can be placed
        if (!grid.canPlayPiece(this.gamePiece, x, y)) {
            Multimedia.playAudio("fail.wav");
            return;
        }

        //place the gamePiece, get a new one, and check whether columns should get deleted
        grid.playPiece(this.gamePiece, x, y);
        Multimedia.playAudio("place.wav");
        this.nextPiece();
        this.afterPiece();
        this.buildTimer();
    }

    /**
     * Get a new GamePiece from the GamePiece class
     * @return new random GamePiece
     */
    public GamePiece spawnPiece() {
        return GamePiece.createPiece((int) (Math.random()*15));
    }

    /**
     * overrides the current gamePiece with a new one
     */
    public void nextPiece() {
        if (followingPiece != null)
            this.gamePiece = followingPiece;
        else
            this.gamePiece = spawnPiece();
        this.followingPiece = spawnPiece();
        if(nextPieceListener != null)
            nextPieceListener.nextPiece(this.gamePiece);
        if(followingPieceListener != null)
            followingPieceListener.nextPiece(this.followingPiece);
    }

    /**
     * check whether a whole line/row is finished by checking whether we have the same amount of
     * blocks as rows/columns if true increment the clearedLines,array for the whole line/row and
     * at the end clear the grid at every point where the clearedLines array is bigger than 0
     */
    public void afterPiece() {
        int [][] clearedLines = new int [getCols()][getRows()];
        int blocksPerCol = 0;
        int lines = 0;
        int blocks = 0;

        //check for columns
        for (int i = 0; i < getCols(); i++) {
            for (int j = 0; j < getRows(); j++){
                if (grid.get(i, j) > 0)
                    blocksPerCol++;
            }
            if (blocksPerCol == getCols()) {
                for (int j = 0; j < getRows(); j++)
                    clearedLines[i][j]++;
                lines++;
            }
            blocksPerCol = 0;
        }

        //check for rows
        for (int j = 0; j < getCols(); j++) {
            for (int i = 0; i < getRows(); i++){
                if (grid.get(i, j) > 0)
                    blocksPerCol++;
            }
            if (blocksPerCol == getCols()) {
                for (int i = 0; i < getRows(); i++)
                    clearedLines[i][j]++;
                lines++;
            }
            blocksPerCol = 0;
        }

        //clear the grid
        Set<GameBlockCoordinate> blockCoordinates = new HashSet<>();
        for (int i = 0; i < getCols(); i++)
            for (int j = 0; j < getRows(); j++)
                if (clearedLines[j][i] > 0) {
                    blockCoordinates.add(new GameBlockCoordinate(j, i));
                    blocks++;
                }

        lineClearedListener.clearLines(blockCoordinates);

        score(lines, blocks);
        changeMultiplier(lines > 0);
        changeLevel();
    }

    /**
     * @param lines amount of lines / rows cleared
     * @param blocks amount of blocks cleared
     */
    private void score(int lines, int blocks) {
        score.set(score.get() + lines * blocks * 10 * multiplier.get());
    }

    /**
     * if a line is cleared increment and play clear sound, else reset
     * @param isLinesCleared whether lines where cleared
     */
    private void changeMultiplier(boolean isLinesCleared) {
        if (isLinesCleared) {
            multiplier.set(multiplier.get() + 1);
            Multimedia.playAudio("explode.wav");
            Multimedia.playAudio("clear.wav");
        }
        else
            multiplier.set(1);
    }

    /**
     * is called every single round because was just easy this way.
     * If it ever decreases something went wrong.
     */
    private void changeLevel() {
        if (level.get() == score.get()/1000)
            return;
        level.set(score.get()/1000);
        timerDelay = Math.max(12_000 - (500 * level.get()), 2_500);
        Multimedia.playAudio("level.wav");
    }

    /**
     * rotate the piece by button input to the right/left
     * @param amountOfRotations 1 -> right, 3 -> left
     */
    public void rotateCurrentPiece(int amountOfRotations) {
        gamePiece.rotate(amountOfRotations);
        Multimedia.playAudio("rotate.wav");
        nextPieceListener.nextPiece(this.gamePiece);
    }

    /**
     * swap the Pieces by making use of a placeHolder and afterwards alarming the listeners
     */
    public void swapCurrentPiece() {
        logger.info("swapping Pieces");

        GamePiece gamePiecePlaceholder = GamePiece.createPiece(gamePiece.getValue() - 1);
        this.gamePiece = followingPiece;
        followingPiece = gamePiecePlaceholder;
        Multimedia.playAudio("rotate.wav");

        if(nextPieceListener != null)
            nextPieceListener.nextPiece(this.gamePiece);
        if(followingPieceListener != null)
            followingPieceListener.nextPiece(this.followingPiece);
    }

    /**
     * if this method is called the old timer gets discarded, and we get a new one
     * startGameLoop in ChallengeScene is added
     * if this timer executes, it calls gameLoop.
     */
    public void buildTimer() {
        gameLoopListener.startGameLoop(timerDelay, lives.get());
        timer.cancel();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> gameLoop());
            }
        }, timerDelay);
    }

    /**
     * decreases the lives, spawns a new piece, resets the multiplier and calls buildTimer again
     */
    private void gameLoop() {
        lives.set(lives.get() - 1);
        Multimedia.playAudio("lifelose.wav");
        nextPiece();
        setMultiplier(1);
        if (lives.get() != 0)
            buildTimer();
        else
            gameLoopListener.startGameLoop(timerDelay, lives.get());
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener){
        this.nextPieceListener = nextPieceListener;
    }

    public void setFollowingPieceListener(NextPieceListener nextPieceListener){
        this.followingPieceListener = nextPieceListener;
    }

    public int getScore() {
        return score.get();
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public int getLevel() {
        return level.get();
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public void setLevel(int level) {
        this.level.set(level);
    }

    public int getLives() {
        return lives.get();
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives.set(lives);
    }

    public double getMultiplier() {
        return multiplier.get();
    }

    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier.set(multiplier);
    }

    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    public int getTimerDelay() {
        return timerDelay;
    }

    public void setTimerDelay(int timerDelay) {
        this.timerDelay = timerDelay;
    }

    public void setGameLoopListener(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }
}
