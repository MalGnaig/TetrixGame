package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Block Clicked listener is used to handle the event when a block in a GameBoard is clicked. It passes the
 * GameBlock that was clicked in the message
 */
public interface NextPieceListener {
    /**
     * everytime the current Block in the game changes the UI should show a new gamePiece
     * @param gamePiece the new gamePiece
     */
    void nextPiece(GamePiece gamePiece);
}
