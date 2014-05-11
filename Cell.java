/* 
 * Each cell in the grid is considered to be "alive". If a block in the grid is dead
 * there is no cell object corresponding to those coordinates. 
 * The cell class contains methods for counting neighbors according to several rulesets:
 * diagonals counted, diagonals not counted, with wrapping and without wrapping (wrapping
 * over the edges of the grid).
 * 
 */

public class Cell {
	int x;
	int y;
	int numNeighbors = 0;
	int numNeighborsNew = 0;
	
	boolean processed = false;
	
	public Cell (int x, int y, int n) {
		this.x = x;
		this.y = y;
		this.numNeighbors = n;
	}
	
	// Implements the hashCode() and equals() functions so the cells can be used in the Hashtable
	public int hashCode() {
		return (104729 * x) + y;
	}
	
	public boolean equals(Object o) {
		if ((o instanceof Cell) && ((x == ((Cell)o).x) && (y == ((Cell)o).y))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String toString() {
		return x + " " + y;
	}
}
