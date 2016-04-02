package se2aa4.morris.enums;

/**
 * Enumerator of Nodes (and Pieces)
 */
public enum Location {
    NONE,
	iR0, iR1, iR2, iR3, iR4, iR5,
	iB0, iB1, iB2, iB3, iB4, iB5,
	nONW, nON, nONE, nOE, nOSE, nOS, nOSW, nOW,
	nINW, nIN, nINE, nIE, nISE, nIS, nISW, nIW;

    /**
     * get location by string
     * @param s string representation of location
     */
    public static Location getByString(String s) {
        for (Location l: values()) {
            if (l.toString().equals(s)) return l;
        }
        return null;
    }

    /**
     * get inventory locations for a player
     * @param p which player
     */
    public static Location[] getInventory(Player p) {
        Location[] filtered = new Location[6];
        int c = 0;
        if (p == Player.BLUE) {
            for (Location l: Location.values()) {
                if (l.toString().contains("B")) {
                    filtered[c++] = l;
                }
            }
        } else {
            for (Location l: Location.values()) {
                if (l.toString().contains("R")) {
                    filtered[c++] = l;
                }
            }
        }
        return filtered;
    }
}
