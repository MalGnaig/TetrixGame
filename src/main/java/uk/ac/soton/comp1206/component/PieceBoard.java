package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.RotateBlockListener;
import uk.ac.soton.comp1206.event.SwapPiecesListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

public class PieceBoard extends GameBoard{

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    private RotateBlockListener rotateBlockListener;
    private SwapPiecesListener swapPiecesListener;

    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * new Piece is added and old Piece gets deleted
     * @param gamePiece the current Piece in the game
     */
    public void nextPiece(GamePiece gamePiece) {
        logger.info("displaying new Piece " + gamePiece);
        // each gamePiece e.g. the L game piece has a value & a block[][]. The value is L's value &
        // the block[][] is the gamepiece 'L' 2d grid array which represents it's shape so we need to get BOTH
        int value = gamePiece.getValue();
        int [][] blocks = gamePiece.getBlocks();

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                //put in piece
                if (blocks[i][j] > 0)
                    this.grid.set(i, j, value);
                //clear otherwise
                else
                    this.grid.set(i, j, 0);
    }
    

    /**
     * set a rotateListener so the piece can be rotated
     * @param rotateBlockListener the rotateListener provided in the ChallengeScene
     */
    public void setOnRotate(RotateBlockListener rotateBlockListener) {
        this.rotateBlockListener = rotateBlockListener;
    }

    /**
     * set a swapPiecesListener so the piece can be rotated
     * @param swapPiecesListener the swapPiecesListener provided in the ChallengeScene
     */
    public void setOnSwap(SwapPiecesListener swapPiecesListener) {
        this.swapPiecesListener = swapPiecesListener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * which calls the rotateListener which actually only does something
     * if we clicked the middle tile
     * @param event mouse event
     * @param block block clicked on
     */
    @Override
    void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if (rotateBlockListener != null)
            rotateBlockListener.rotateBlock(block);
        if (swapPiecesListener != null)
            swapPiecesListener.swapPieces();
    }
}
