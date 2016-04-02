package se2aa4.morris;

import java.io.*;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import se2aa4.morris.enums.*;

/**
 * Handles UI events and communicates with Game
 */
public class Controller implements Initializable {

	// constants
    private static final String SAVE_FILENAME = "save/save.ser";
	private static final Paint
        COL_RED = Paint.valueOf("#ff0000"),
        COL_BLUE = Paint.valueOf("#0000ff"),
        COL_BLACK = Paint.valueOf("#000000"),
        COL_GREEN = Paint.valueOf("#00ff00"),
        COL_YELLOW = Paint.valueOf("#f4de00");
    private static final String
        MSG_PRESS_NEW = "Press New",
        MSG_OVERLAPPING = "Overlapping Pieces",
        MSG_MULTIPLE_MOVES = "Multiple moves made",
        MSG_NO_MOVE = "No move made",
        MSG_LOADED = "Game loaded",
        MSG_SAVED = "Game saved",
        MSG_LOAD_FAIL = "Failed to load",
        MSG_SAVE_FAIL = "Failed to save",
        MSG_DRAW = "Draw",
        MSG_WIN_POSTFIX = " wins",
        MSG_TURN_POSTFIX = "'s turn",
        MSG_RESTORED = "Game state restored",
        MSG_MILL = "Chose piece to remove";
    private static final int RADIUS_EMPTY = 8;
    private static final int RADIUS_PIECE = 10;

    // UI elements
    @FXML
    private Text msgLabelL, msgLabelR;

    @FXML
	private Shape
        iR0, iR1, iR2, iR3, iR4, iR5,
        iB0, iB1, iB2, iB3, iB4, iB5,
        nONW, nON, nONE, nOE, nOSE, nOS, nOSW, nOW,
        nINW, nIN, nINE, nIE, nISE, nIS, nISW, nIW;

    // game object
    private Game game;

	/**
	 * initialize UI
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
        game =  new Game();
        updateMsg(game.getState());
	}

    /**
     * Click new game button
     * @param event action event
     */
	@FXML
	private void processNewGame(ActionEvent event) {
        game.newGame();
        updateMsg(State.IN_PROGRESS);
        draw();
	}

    /**
     * Click load button
     * @param event action event
     */
    @FXML
    private void processLoad(ActionEvent event) {
        Game gameBak = game;
        game = load();
        if (game == null) {
            game = gameBak;
            updateMsg(game.getState(), Detail.LOAD_FAIL);
        } else {
            updateMsg(game.getState(), Detail.LOADED);
        }
        draw();
    }

    /**
     * Click save button
     * @param event action event
     */
    @FXML
    private void processSave(ActionEvent event) {
        // TODO new game saved
        if (save(game)) updateMsg(game.getState(), Detail.SAVED);
        else updateMsg(game.getState(), Detail.SAVE_FAIL);
    }

    /**
     * Click restore button
     * @param event action event
     */
    @FXML
    private void processRestore(ActionEvent event) {
        game.restore();
        updateMsg(game.getState(), Detail.RESTORED);
        draw();
    }

    /**
     * Click end turn
     * @param event action event
     */
	@FXML
	private void processEndTurn(ActionEvent event) {
        if (game.getState() == State.UNSTARTED) return;
        switch (game.endTurn()) {
            case MULTIPLE_MOVES:
                updateMsg(game.getState(), Detail.MULTIPLE_MOVES);
                break;
            case NO_MOVE:
                updateMsg(game.getState(), Detail.NO_MOVE);
                break;
            case OVERLAPPING:
                updateMsg(game.getState(), Detail.OVERLAPPING);
                break;
            case END_TURN:
                updateMsg(game.getState(), Detail.CLEAR);
                break;
            default:
                //
        }
	}

    /**
     * Click on a node
     * @param event action event
     */
	@FXML
	private void processNodeClick(MouseEvent event) {
        switch (game.getState()) {
            case UNSTARTED:
                // game isn't started
                break;
            case IN_PROGRESS:
                Location loc = Location.getByString(((Shape)event.getSource()).getId());
                game.handleMove(loc);
                if (game.whoseMill() == game.getTurn()) {
                    updateMsg(game.getState(), Detail.MILL);
                } else {
                    updateMsg(game.getState(), Detail.CLEAR);
                }
                draw();
                break;
            case DRAW:
                break;
            case WON:
                break;
        }

	}

    /**
     * Display message about game state and additional details
     * @param state action event
     */
    private void updateMsg(State state) {
        switch (state) {
            case UNSTARTED:
                msgLabelL.setText(MSG_PRESS_NEW);
                break;
            case IN_PROGRESS:
                msgLabelL.setText(game.getTurn() + MSG_TURN_POSTFIX);
                break;
            case DRAW:
                msgLabelL.setText(MSG_DRAW);
                break;
            case WON:
                // FIXME
                msgLabelL.setText(game.getTurn() + MSG_WIN_POSTFIX);
                break;
            default:
                //
        }
    }

    /**
     * Display message about game state and additional details
     * @param state state enum
     * @param detail detail enum
     */
    private void updateMsg(State state, Detail detail) {
        updateMsg(state);
        switch (detail) {
            case CLEAR:
                msgLabelR.setText("");
                break;
            case END_TURN:
                break;
            case MILL:
                msgLabelR.setText(MSG_MILL);
                break;
            case OVERLAPPING:
                msgLabelR.setText(MSG_OVERLAPPING);
                break;
            case MULTIPLE_MOVES:
                msgLabelR.setText(MSG_MULTIPLE_MOVES);
                break;
            case NO_MOVE:
                msgLabelR.setText(MSG_NO_MOVE);
                break;
            case LOADED:
                msgLabelR.setText(MSG_LOADED);
                break;
            case SAVED:
                msgLabelR.setText(MSG_SAVED);
                break;
            case LOAD_FAIL:
                msgLabelR.setText(MSG_LOAD_FAIL);
                break;
            case SAVE_FAIL:
                msgLabelR.setText(MSG_SAVE_FAIL);
                break;
            case RESTORED:
                msgLabelR.setText(MSG_RESTORED);
                break;
            default:
                //
        }
    }

    /**
     * Load a game save
     * @return game object
     */
    private static Game load() {
        Game game;
        try {
            FileInputStream filein = new FileInputStream(SAVE_FILENAME);
            ObjectInputStream in = new ObjectInputStream(filein);
            game = (Game) in.readObject();
            in.close();
            filein.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return game;
    }

    /**
     * Save a game
     * @param game game object
     * @return successful save
     */
    private static boolean save(Game game) {
        try {
            FileOutputStream fileout = new FileOutputStream(SAVE_FILENAME);
            ObjectOutputStream out = new ObjectOutputStream(fileout);
            out.writeObject(game);
            out.close();
            fileout.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Draw pieces
     */
    private void draw() {
        AbstractMap.SimpleEntry<Location, Piece>[] list = game.getBoard();

        for (AbstractMap.SimpleEntry<Location, Piece> pair: list) {

            Circle node = (Circle)getShape(pair.getKey());

            if (pair.getValue() == Piece.NONE) {
                if (pair.getKey().toString().contains("B") ||
                        pair.getKey().toString().contains("R")) {
                    node.setVisible(false);
                } else {
                    node.setRadius(RADIUS_EMPTY);
                    node.setFill(COL_BLACK);
                }
            } else {
                node.setRadius(RADIUS_PIECE);
                node.setVisible(true);
                if (pair.getValue().toString().contains("R")) {
                    node.setFill(COL_RED);
                } else {
                    node.setFill(COL_BLUE);
                }
            }
        }
        if (game.getSel() != Location.NONE) {
            Circle node = (Circle) getShape(game.getSel());
            node.setFill(COL_GREEN);
        }
    }

    /**
     * get javafx shape from a given location
     * @param loc location on the board
     * @return javafx shape
     */
    private Shape getShape(Location loc) {
        switch (loc) {
            case iR0:
                return iR0;
            case iR1:
                return iR1;
            case iR2:
                return iR2;
            case iR3:
                return iR3;
            case iR4:
                return iR4;
            case iR5:
                return iR5;
            case iB0:
                return iB0;
            case iB1:
                return iB1;
            case iB2:
                return iB2;
            case iB3:
                return iB3;
            case iB4:
                return iB4;
            case iB5:
                return iB5;
            case nONW:
                return nONW;
            case nON:
                return nON;
            case nONE:
                return nONE;
            case nOE:
                return nOE;
            case nOSE:
                return nOSE;
            case nOS:
                return nOS;
            case nOSW:
                return nOSW;
            case nOW:
                return nOW;
            case nINW:
                return nINW;
            case nIN:
                return nIN;
            case nINE:
                return nINE;
            case nIE:
                return nIE;
            case nISE:
                return nISE;
            case nIS:
                return nIS;
            case nISW:
                return nISW;
            case nIW:
                return nIW;
            default:
                return null;
        }
    }
}
