package se2aa4.morris;

import se2aa4.morris.enums.Location;
import se2aa4.morris.enums.Piece;
import se2aa4.morris.enums.Player;

import java.io.Serializable;
import java.util.*;

/**
 * Frame data and methods
 */
public class Frame implements Serializable {

    // board and piece data
    private Map<Location, Piece> frame, frameBak;

    // board fields
	private boolean valid, validBak;
	private Location invalidLocation, invalidLocationBak;

    private boolean redMill, blueMill;
    private Location[] blueMillLocation, redMillLocation;

	/**
	 * Frame object constructor
	 */
	public Frame() {
        valid = true;
        frame = new HashMap<>(28);

        for (Location l: Location.values()) {
            frame.put(l, Piece.NONE);
        }

        frame.put(Location.iB0, Piece.B0);
        frame.put(Location.iB1, Piece.B1);
        frame.put(Location.iB2, Piece.B2);
        frame.put(Location.iB3, Piece.B3);
        frame.put(Location.iB4, Piece.B4);
        frame.put(Location.iB5, Piece.B5);

        frame.put(Location.iR0, Piece.R0);
        frame.put(Location.iR1, Piece.R1);
        frame.put(Location.iR2, Piece.R2);
        frame.put(Location.iR3, Piece.R3);
        frame.put(Location.iR4, Piece.R4);
        frame.put(Location.iR5, Piece.R5);
	}

    /**
     * Get board data
     * @return board data
     */
    public AbstractMap.SimpleEntry<Location, Piece>[] getFrame() {
        AbstractMap.SimpleEntry<Location, Piece>[] pairs;
        pairs = new AbstractMap.SimpleEntry[28];
        int i = 0;
        for (Location loc: frame.keySet()) {
            if (loc != Location.NONE) {
                pairs[i++] = new AbstractMap.SimpleEntry<>(loc, frame.get(loc));
            }
        }
        return pairs;
    }

	/**
	 * Check if frame is valid
	 * @return is frame valid
     */
	public boolean getValid() {
		return valid;
	}

	/**
	 * Move piece to another location on the frame
	 * @param here location to move
	 * @param there destination location
     */
	public void move(Location here, Location there) {
        if (frame.get(there) != Piece.NONE) {
            // overlapping piece
            valid = false;
            invalidLocation = there;
        } else {
            frame.replace(there, frame.get(here));
            frame.replace(here, Piece.NONE);
        }
	}

    /**
     * remove a piece from a location
     * @param there which location
     */
    public void remove(Location there) {
        frame.put(there, Piece.NONE);
    }

	/**
	 * Create restore point of game state
	 */
	public void createRestorePoint() {
        validBak = valid;
        invalidLocationBak = invalidLocation;
        frameBak = new HashMap<Location, Piece>(frame);
        for (Location loc: frameBak.keySet()) {
        }
	}

	/**
	 * Restore game state
	 */
	public void restore() {
        valid = validBak;
        invalidLocation = invalidLocationBak;
        frame = frameBak;
	}

    /**
     * get piece based on board location
     * @param l location on board
     * @return piece at the location
     */
    public Piece getPieceByLocation(Location l) {
        return frame.get(l);
    }

    /**
     * get which player currently has a mill
     * @return player with a mill
     */
    public Player whoseMill() {
        boolean redMill = false,
                blueMill = false;

        // handle mill in between turns
        if (this.redMill || this.blueMill) {
            if (redMill) this.redMill = false;
            else this.blueMill = false;
            return Player.NONE;
        }

        // possible mill locations
        Location[][] triplets = {
                {Location.nONW, Location.nON, Location.nONE},
                {Location.nONE, Location.nOE, Location.nOSE},
                {Location.nOSE, Location.nOS, Location.nOSW},
                {Location.nOSW, Location.nOW, Location.nONW},
                {Location.nINW, Location.nIN, Location.nINE},
                {Location.nINE, Location.nIE, Location.nISE},
                {Location.nISE, Location.nIS, Location.nISW},
                {Location.nISW, Location.nIW, Location.nINW},
        };

        // check if mill exists
        for (Location[] triplet: triplets) {
            if (Piece.isSamePlayer(frame.get(triplet[0]), frame.get(triplet[1]),
                    frame.get(triplet[2]))) {
                if (Piece.isPlayers(Player.BLUE, frame.get(triplet[0]))) {
                    if (!Arrays.equals(triplet, blueMillLocation)) {
                        blueMill = true;
                        blueMillLocation = triplet;
                    } else {
                        blueMillLocation = null;
                    }
                } else {
                    if (!Arrays.equals(triplet, redMillLocation)) {
                        redMill = true;
                        redMillLocation = triplet;
                    } else {
                        blueMillLocation = null;
                    }
                }
            }
        }

        this.redMill = redMill;
        this.blueMill = blueMill;
        if (blueMill && redMill) {
            return Player.BOTH;
        } else if (blueMill ^ redMill) {
            return (blueMill) ? Player.BLUE : Player.RED;
        } else {
            return Player.NONE;
        }
    }

    /**
     * get if inventory is empty for a given player
     * @param player which player
     * @return is inventory empty
     */
    public boolean isInventoryEmpty(Player player) {
        for (Location l: Location.getInventory(player))
            if (frame.get(l) != Piece.NONE) return false;

        return true;
    }

    /**
     * is a move a fly move
     * @param l1 first location
     * @param l2 second location
     * @return is move between l1 and l2 a fly move
     */
    public static boolean isMoveFly(Location l1, Location l2) {
        // valid non-fly moves
        if (either(l1, l2, Location.nONW, Location.nON)) return false;
        else if (either(l1, l2, Location.nON, Location.nIN)) return false;
        else if (either(l1, l2, Location.nON, Location.nONE)) return false;
        else if (either(l1, l2, Location.nONE, Location.nOE)) return false;
        else if (either(l1, l2, Location.nOE, Location.nIE)) return false;
        else if (either(l1, l2, Location.nOE, Location.nOSE)) return false;
        else if (either(l1, l2, Location.nOSE, Location.nOS)) return false;
        else if (either(l1, l2, Location.nOS, Location.nIS)) return false;
        else if (either(l1, l2, Location.nOS, Location.nOSW)) return false;
        else if (either(l1, l2, Location.nOSW, Location.nOW)) return false;
        else if (either(l1, l2, Location.nOW, Location.nIW)) return false;
        else if (either(l1, l2, Location.nOW, Location.nONW)) return false;
        else if (either(l1, l2, Location.nINW, Location.nIN)) return false;
        else if (either(l1, l2, Location.nIN, Location.nINE)) return false;
        else if (either(l1, l2, Location.nINE, Location.nIE)) return false;
        else if (either(l1, l2, Location.nIE, Location.nISE)) return false;
        else if (either(l1, l2, Location.nISE, Location.nIS)) return false;
        else if (either(l1, l2, Location.nIS, Location.nISW)) return false;
        else if (either(l1, l2, Location.nISW, Location.nIW)) return false;
        else if (either(l1, l2, Location.nIW, Location.nINW)) return false;
        else return true;
    }

    /**
     * are either of the two parameters equal to other two parameters
     * @param l1 first parameter
     * @param l2 second parameter
     * @param c1 first comparison parameter
     * @param c2 second comparison parameter
     * @return are first two equal to second two in any combination
     */
    public static boolean either(Location l1, Location l2, Location c1, Location c2) {
        if (l1 == c1 || l1 == c2)
            if (l2 == c1 || l2 == c2)
                if (l1 != l2)
                    return true;
        return false;
    }

    
    public boolean movePossible(Player p){
    	
    	boolean move = false;
    	
    	
    	for (Map.Entry<Location, Piece> entry : frame.entrySet()){
    		
    		
    		boolean playersColour = (frame.get(entry.getKey()).toString().charAt(0) == p.toString().charAt(0));
    		
    		
    		/*
    		if(p == Player.RED){
    			playersColour = frame.get(entry.getKey()).toString().contains("R");
    		}
    		else{
    			playersColour = frame.get(entry.getKey()).toString().contains("B");
    		}
    		*/
    		
    		
    		if(!entry.getKey().toString().contains("i") && playersColour){
    			
    			ArrayList<Location> adjacentLocations = getAdjacent(entry.getKey()); //find adjacent location of this piece
    			
    			for(int i = 0; i < adjacentLocations.size(); i++){
    				if(frame.get(adjacentLocations.get(i)) == Piece.NONE ){
    					move = true;
    				}
    				
    			}	
    		}
    	}
    	
    	return move;
    	
    }
    
    
    private ArrayList<Location> getAdjacent(Location loc){
    	
    	ArrayList<Location> adjLoc = new ArrayList<Location>();
    	
    	switch(loc){
    	
    	case nONW : adjLoc.add(Location.nON);
    				adjLoc.add(Location.nOW);
    				break;
    	
    	case nON : adjLoc.add(Location.nONE);
    			   adjLoc.add(Location.nONW);
    			   adjLoc.add(Location.nIN);
    			   break;
    			   
    	case nONE : adjLoc.add(Location.nOE);   
    			    adjLoc.add(Location.nON);
    			    break;
    			    
    	case nOE :  adjLoc.add(Location.nONE);
    				adjLoc.add(Location.nOSE);
    				adjLoc.add(Location.nIE);
    				break;
    		
    	case nOSE : adjLoc.add(Location.nOE);
    				adjLoc.add(Location.nOS);
    				break;
    				
    	case nOS : adjLoc.add(Location.nOSE);
    			   adjLoc.add(Location.nOSW);
    			   adjLoc.add(Location.nIS);
    			   break;
    	
    	case nOSW : adjLoc.add(Location.nOS);
    				adjLoc.add(Location.nOW);
    				break;
    				
    	case nOW : adjLoc.add(Location.nONW);
    			   adjLoc.add(Location.nOSW);
    			   adjLoc.add(Location.nIW);
    			   break;
    			   
    	case nINW : adjLoc.add(Location.nIN);
    				adjLoc.add(Location.nIW);
    				break;
    				
    	case nIN : adjLoc.add(Location.nINW);
    			   adjLoc.add(Location.nINE);
    			   adjLoc.add(Location.nON);
    			   break;
    			   
    	case nINE : adjLoc.add(Location.nIN);
    				adjLoc.add(Location.nIE);
    				break;
    				
    	case nIE : adjLoc.add(Location.nINE);
    			   adjLoc.add(Location.nISE);
    			   adjLoc.add(Location.nOE);
    			   break;
    	
    	case nISE : adjLoc.add(Location.nIS);
    			   adjLoc.add(Location.nIE);
    			   break;
    			   
    	case nIS : adjLoc.add(Location.nISE);
    			   adjLoc.add(Location.nISW);
    			   adjLoc.add(Location.nOS);
    			   break;
    	
    	case nISW : adjLoc.add(Location.nIS);
    				adjLoc.add(Location.nIW);
    				break;
    				
    	case nIW : adjLoc.add(Location.nINW);
    			   adjLoc.add(Location.nISW);
    			   adjLoc.add(Location.nOW);
    			   break;
    	
    	default : break;
    	
    	
    	}
    
    	return adjLoc;
    
    
    }    
    
    
    
    
    
    
    
    
    
}
