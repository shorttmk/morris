package se2aa4.morris;

import se2aa4.morris.enums.*;

import java.io.Serializable;
import java.util.AbstractMap;

/**
 * Game logic and data
 */
public class Game implements Serializable {

    // game board object
    private Frame frame;

    // game fields
    private State state, stateBak;
    private Player turn, turnBak;
    private Location sel, selBak;
    private boolean moved, movedBak;
    private boolean multipleMoves, multipleMovesBak;
    private boolean blueMillExists, redMillExists;
    private boolean removed, removedBak;
    private boolean redInventory, redInventoryBak,
                    blueInventory, blueInventoryBak;

    /**
     * Game constructor
     */
    public Game() {
        state = State.UNSTARTED;
    }

    /**
     * start a new game
     */
    public void newGame() {
        frame = new Frame();
        state = State.IN_PROGRESS;
        sel = Location.NONE;
        blueMillExists = false;
        redMillExists = false;
        redInventory = true;
        blueInventory = true;
        moved = false;
        removed = false;
        randTurn();
        createRestorePoint();
    }

    /**
     * set to random players turn
     */
    private void randTurn() {
        turn = Math.random() > 0.5 ? Player.RED : Player.BLUE;
    }

    /**
     * set to next player's turn
     */
    private void nextTurn() {
        if (turn == Player.RED) turn = Player.BLUE;
        else turn = Player.RED;
    }

    /**
     * end a turn
     * @return
     */
    public Detail endTurn() {
        if (multipleMoves) {
            if (turn == Player.BLUE) {
                blueMillExists = false;
            } else {
                redMillExists = false;
            }
            return Detail.MULTIPLE_MOVES;
        } else if (!moved) {
            return Detail.NO_MOVE;
        } else if (redMillExists && turn == Player.RED && !removed) {
                return Detail.MILL;
        } else if (blueMillExists && turn == Player.BLUE && !removed) {
                return Detail.MILL;
        } else {
            nextTurn();
            moved = false;
            removed = false;
            redMillExists = false;
            blueMillExists = false;
            sel = Location.NONE;
            createRestorePoint();
            return Detail.END_TURN;
        }
    }

    /**
     * handle piece move logic
     * @param l location on board
     */
    public void handleMove(Location l) {
        if (sel != Location.NONE && l.toString().contains("n")) {
            if (Piece.isPlayers(turn, frame.getPieceByLocation(l))) {
                // reselect piece
                sel = l;
            }
            // piece is selected
            if (!l.toString().contains("n")) {
                // didn't click no board location
            } else {
                // clicked board location
                if (isInventoryEmpty(turn)) {
                    // allow moving of placed pieces
                    if (sel.toString().contains("n")) {
                        // selected piece is on board
                        if (l.toString().contains("n")) {
                            if (!frame.isMoveFly(sel, l))
                                move(l);
                                updateMillInfo();
                        }
                    }
                } else {
                    // move inventory pieces
                    if (sel.toString().contains("i")) {
                        move(l);
                        updateMillInfo();
                    }
                }
            }
        } else {
            if (!Piece.isPlayers(turn, frame.getPieceByLocation(l))) {
                // clicked other players piece
                if (turn == Player.BLUE) {
                    if (blueMillExists && !removed) {
                        // allow removal of players piece
                        frame.remove(l);
                        redMillExists = false;
                        removed = true;
                        return;
                    } else {
                        return;
                    }
                } else {
                    if (redMillExists && !removed) {
                        // allow removal of players piece
                        frame.remove(l);
                        redMillExists = false;
                        removed = true;
                        return;
                    } else {
                        return;
                    }
                }
            }
            // select piece
            if (Piece.isPlayers(turn, frame.getPieceByLocation(l))) {
                // player selected own piece
                sel = l;
            } else {
                // player selected opponent's piece
                if (turn == Player.BLUE && blueMillExists) {
                    sel = l;
                } else if (turn == Player.RED && redMillExists) {
                    sel = l;
                }
            }
        }
    }

    /**
     * move a piece
     * @param l location to move (the selected piece)
     */
    public void move(Location l) {
        frame.move(sel, l);
        sel = Location.NONE;
        if (moved) multipleMoves = true;
        moved = true;
        redInventory = frame.isInventoryEmpty(Player.RED);
        blueInventory = frame.isInventoryEmpty(Player.BLUE);
    }

    /**
     * create a restore point
     */
    public void createRestorePoint() {
        stateBak = state;
        turnBak = turn;
        selBak = sel;
        movedBak = moved;
        removedBak = removed;
        multipleMovesBak = multipleMoves;
        frame.createRestorePoint();
        redInventoryBak = redInventory;
        blueInventoryBak = blueInventory;
    }

    /**
     * restore game state
     */
    public void restore() {
        state = stateBak;
        turn = turnBak;
        sel = selBak;
        moved = movedBak;
        removed = removedBak;
        multipleMoves = multipleMovesBak;
        redInventory = redInventoryBak;
        blueInventory = blueInventoryBak;
        frame.restore();
        createRestorePoint();
    }

    /**
     * get who has a mill
     * @return which player has a mill
     */
    public Player whoseMill() {
        if (blueMillExists) return Player.BLUE;
        else if (redMillExists) return Player.RED;
        else return Player.NONE;
    }

    /**
     * update mill information
     */
    public void updateMillInfo() {
        Player whoseMill = frame.whoseMill();
        if (whoseMill == Player.BLUE) {
            if (blueMillExists) {
            } else {
                blueMillExists = true;
            }
        } else if (whoseMill == Player.RED) {
            if (redMillExists) {
            } else {
                redMillExists = true;
            }
        } else if (whoseMill == Player.BOTH) {
            if (redMillExists) {
            } else if (!redMillExists) {
                redMillExists = true;
            }

            if (blueMillExists) {
            } else if (!blueMillExists) {
                blueMillExists = true;
            }
        } else {
            blueMillExists = false;
            redMillExists = false;
        }
    }

    /**
     * get game state
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * get whose turn it is
     * @return
     */
    public Player getTurn() {
        return turn;
    }

    /**
     * get selected pieces' location
     * @return
     */
    public Location getSel() {
        return sel;
    }

    /**
     * get board location's data
     * @return
     */
    public AbstractMap.SimpleEntry<Location, Piece>[] getBoard() {
        return frame.getFrame();
    }

    /**
     * get if player's inventory is empty
     * @param player which player
     * @return is inventory empty
     */
    public boolean isInventoryEmpty(Player player) {
        return frame.isInventoryEmpty(player);
    }

}