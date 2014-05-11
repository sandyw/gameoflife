import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Stack;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;


public class CellGrid extends Canvas {
	boolean[][] living;
	int x = 3, y = 3;
	Hashtable<Integer, Cell> cells = new Hashtable<Integer, Cell>();
	int hash = 104729;
	
	long generations = 0;
	int alive = 0;
	int alivePrev = 0;
	
	// Display variables
	int cellPx = 5; // cells are squares of this size
	int xSize, ySize;
	boolean killMode = false;
	boolean showNumbers = false;
	boolean drawGrid = true;
	
	// Rules Options
	boolean countDiagonals = true;
	int gen = 3;
	int live1 = 2;
	int live2 = 3;
	
	Stack<Cell> lifeRow = new Stack<Cell>();
	Stack<Cell> deathRow = new Stack<Cell>();
	
	public CellGrid (int x, int y) {
		this.x = (x > 2) ? x : 3;
		this.y = (y > 2) ? y : 3;
		living = new boolean[x][y];
		xSize = (cellPx * x + (x + 1));
		ySize = (cellPx * y + (y + 1));
		setSize(xSize, ySize);
		
		registerListeners();
	}
	
	public void changeSize(int x, int y, int px) {
		this.x = (x > 2) ? x : 3;
		this.y = (y > 2) ? y : 3;
		cellPx = px;
		living = new boolean[x][y];
		xSize = (cellPx * x + (x + 1));
		ySize = (cellPx * y + (y + 1));
		setSize(xSize, ySize);

		unregisterListeners();
		registerListeners();
	}
	
	public void unregisterListeners() {
		this.removeMouseListener(this.getMouseListeners()[0]);
		this.removeMouseMotionListener(this.getMouseMotionListeners()[0]);
	}
	
	public void registerListeners() {
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {	}
			public void mouseExited(MouseEvent arg0) {}

			public void mousePressed(MouseEvent arg0) {
				int x = arg0.getX() / (cellPx + 1);
				int y = arg0.getY() / (cellPx + 1);
				try {
					killMode = (living[x][y]) ? true : false;
				}
				catch (ArrayIndexOutOfBoundsException ex) {}
			}

			public void mouseReleased(MouseEvent arg0) {
				int x = arg0.getX() / (cellPx + 1);
				int y = arg0.getY() / (cellPx + 1);
				try {
					if (killMode) {
						killCell(x, y);
					}
					else {
						addCell(x, y);
					}
					repaint();
				}
				catch (ArrayIndexOutOfBoundsException ex) {}

			}
			
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				int x = e.getX() / (cellPx + 1);
				int y = e.getY() / (cellPx + 1);
				
				try {
					if (killMode) {
						killCell(x, y);
					}
					else {
						addCell(x, y);
					}
					repaint();
				}
				catch (ArrayIndexOutOfBoundsException ex) {
					
				}
			}
		});
	}
	
	public void paint(Graphics g) {
		// Draw grid
		drawGrid = true;
		if (cellPx <= 4) { 
			drawGrid = false;
		}
		if (((double)alive / (double)(x * y) * 100.0) >= (double)80.0) {
			drawGrid = false;
		}
		if (drawGrid) {
			g.setColor(new Color(200, 200, 200));
			for (int i = cellPx + 1; i < xSize; i += (cellPx + 1)) {
				g.drawLine(i, 0, i, ySize);
			}
			for (int i = cellPx + 1; i < ySize; i += (cellPx + 1)) {
				g.drawLine(0, i, xSize, i);
			}
			g.drawLine(xSize, 0, xSize, ySize);
		}

		
		Enumeration<Integer> e;
		Cell c;
		
		// Iterate over existing living cells
	    e = cells.keys();
	    while (e.hasMoreElements()) {
	    	c = cells.get(e.nextElement());
	    	// only process living cells, not dummy/dormant cell objects
	    	try {
		    	if (living[c.x][c.y]) {
		    		g.setColor(new Color(0, 100, 100, 130));
		    		g.fillRect((c.x * (cellPx + 1)), (c.y * (cellPx + 1)), cellPx + 2, cellPx + 2);
		    	}
	    	}
	    	catch (ArrayIndexOutOfBoundsException ex) {
	    		
	    	}
	    	if (showNumbers) {
		    	g.setColor(Color.black);
		    	g.drawString(String.valueOf(c.numNeighbors), (c.x * (cellPx + 1)) + 1, (c.y * (cellPx + 1)) + cellPx + 1);
	    	}
	    }
	}
	
	public boolean update() {
		Enumeration<Integer> e;
		Cell c, working;
		boolean processed[][] = new boolean[x][y];

		
		if (cells.isEmpty()) {
			return false;
		}
		
		generations++;
		
		// Iterate over existing living cells
	    e = cells.keys();
	    while (e.hasMoreElements()) {
	    	c = cells.get(e.nextElement());
	    	// only process living cells, not dummy/dormant cell objects
	    	try {
		    	if (living[c.x][c.y]) {
			    	// Bring new cells to life
			    	try {
			    		if (countDiagonals) {
				    		// Go in a square around this cell
				    		for (int i = (c.x - 1); i <= c.x + 1; i++) {
					    		for (int j = (c.y - 1); j <= c.y + 1; j++) {
					    			working = cells.get((hash * i) + j); // get cell from hashtable
					    			// if this cell is not alive, check to see if it should be
					    			if ((!living[i][j]) && (!processed[i][j])) {
					    				processed[i][j] = true;
					    				try {
						    				if (working.numNeighbors == gen) {
						    					lifeRow.push(working);
						    				}
					    				}
					    				catch (NullPointerException ex) {}
					    			}	
					    		}
				    		}
			    		}
			    		else {
							for (int i = (c.x - 1); i <= c.x + 1; i += 2) {
								working = cells.get((hash * i) + c.y); // get cell from hashtable
				    			// if this cell is not alive, check to see if it should be
				    			if ((!living[i][c.y]) && (!processed[i][c.y])) {
				    				processed[i][c.y] = true;
				    				try {
					    				if (working.numNeighbors == gen) {
					    					lifeRow.push(working);
					    				}
				    				}
				    				catch (NullPointerException ex) {}
				    			}
							}
					    	for (int j = (c.y - 1); j <= c.y + 1; j += 2) {
								working = cells.get((hash * c.x) + j); // get cell from hashtable
				    			// if this cell is not alive, check to see if it should be
				    			if ((!living[c.x][j]) && (!processed[c.x][j])) {
				    				processed[c.x][j] = true;
				    				try {
					    				if (working.numNeighbors == gen) {
					    					lifeRow.push(working);
					    				}
				    				}
				    				catch (NullPointerException ex) {}
				    			}
					    	}
			    		}

			    	}
			    	catch (ArrayIndexOutOfBoundsException ex) {
			    		
			    	}
			    	// Mark this cell for death if it needs to die
			    	if ((c.numNeighbors < live1) || (c.numNeighbors > live2)) {
			    		deathRow.push(c);
			    	}
		    	}
	    	}
	    	catch (ArrayIndexOutOfBoundsException ex) {
	    		
	    	}

	    }
	    
	    processDeathStack();
	    processLifeStack();
	    clearDormant();

		
		
		return true;
	}
	
	public void clear() {
		lifeRow.clear();
		deathRow.clear();
		cells.clear();
		living = new boolean[x][y];
		generations = 0;
		alive = 0;
	}
	
	public int countNeighbors(int x, int y) {
		int count = 0;
		
		if (countDiagonals) {
			for (int i = (x - 1); i <= x + 1; i++) {
	    		for (int j = (y - 1); j <= y + 1; j++) {
	    			try {
	    				count += (living[i][j]) ? 1 : 0;
	    			}
	    			catch (ArrayIndexOutOfBoundsException e) {}
	    		}
			}
			try {
				if (living[x][y]) count--;
			}
			catch (ArrayIndexOutOfBoundsException e) {}
		}
		else {
			try {
				count += (living[x][y - 1]) ? 1 : 0;
				count += (living[x - 1][y]) ? 1 : 0;
				count += (living[x][y + 1]) ? 1 : 0;
				count += (living[x + 1][y]) ? 1 : 0;

			}
			catch (ArrayIndexOutOfBoundsException e) {}
		}
		return count;
	}
	
	public void recountNeighbors() {
		Enumeration e = cells.keys();
	    Cell c;
	    while (e.hasMoreElements()) {
	    	c = cells.get(e.nextElement());
	    	c.numNeighbors = countNeighbors(c.x, c.y);
	    	if (countDiagonals && (living[c.x][c.y])) {
				for (int i = (c.x - 1); i <= c.x + 1; i++) {
		    		for (int j = (c.y - 1); j <= c.y + 1; j++) {
		    			if (cells.get((hash * i) + j) == null) {
		    				cells.put((hash * i) + j, new Cell(i, j, countNeighbors(i, j)));
		    			}
		    		}
				}
	    	}

	    }
	    clearDormant();
	}
	
	public void processDeathStack() {
		while (!deathRow.isEmpty()) {
			Cell c = deathRow.pop();
			// update neighbor counts
			if (countDiagonals) {
				for (int i = (c.x - 1); i <= c.x + 1; i++) {
		    		for (int j = (c.y - 1); j <= c.y + 1; j++) {
		    			try {
		    				Cell w = cells.get((hash * i) + j);
		    				w.numNeighbors--;
		    			}
		    			catch (NullPointerException e) {}
		    		}
				}
				c.numNeighbors++;
			}
			else {
    			try {
    				Cell w = cells.get((hash * (c.x - 1)) + c.y);
    				w.numNeighbors--;
    				w = cells.get((hash * c.x) + (c.y - 1));
    				w.numNeighbors--;
    				w = cells.get((hash * (c.x + 1)) + c.y);
    				w.numNeighbors--;
    				w = cells.get((hash * c.x) + (c.y + 1));
    				w.numNeighbors--;
    			}
    			catch (NullPointerException e) {}
			}
			// mark cell as dead
			living[c.x][c.y] = false;
			alivePrev = alive;
			alive--;
			
		}
	}
	
	public void processLifeStack() {
		while (!lifeRow.isEmpty()) {
			Cell c = lifeRow.pop();
			// update neighbor counts
			if (countDiagonals) {
				for (int i = (c.x - 1); i <= c.x + 1; i++) {
		    		for (int j = (c.y - 1); j <= c.y + 1; j++) {
		    			Cell w = cells.get((hash * i) + j);
		    			if (w == null) {
		    				cells.put(((hash * i) + j), new Cell(i, j, countNeighbors(i, j) + 1));
		    			}
		    			else {
		    				w.numNeighbors++;
		    			}
		    		}
				}
				c.numNeighbors--;
			}
			else {
				for (int i = (c.x - 1); i <= c.x + 1; i += 2) {
	    			Cell w = cells.get((hash * i) + c.y);
	    			if (w == null) {
	    				cells.put((hash * i) + c.y, new Cell(i, c.y, countNeighbors(i, c.y) + 1));
	    			}
	    			else {
	    				w.numNeighbors++;
	    			}
				}
		    	for (int j = (c.y - 1); j <= c.y + 1; j += 2) {
	    			Cell w = cells.get((hash * c.x) + j);
	    			if (w == null) {
	    				cells.put((hash * c.x) + j, new Cell(c.x, j, countNeighbors(c.x, j) + 1));
	    			}
	    			else {
	    				w.numNeighbors++;
	    			}
		    	}

			}
			// mark cell as alive
			try {
				living[c.x][c.y] = true;
				alivePrev = alive;
				alive++;
			}
			catch (ArrayIndexOutOfBoundsException e) {}
		}
	}
	
	public void clearDormant() {
		Enumeration<Integer> e;
		Cell c;
		
	    e = cells.keys();
	    while (e.hasMoreElements()) {
	    	c = cells.get(e.nextElement());
	    	try {
		    	if ((c.numNeighbors == 0) && (!living[c.x][c.y])) {
		    		cells.remove((hash * c.x) + c.y);
		    	}
	    	}
	    	catch (ArrayIndexOutOfBoundsException ex) {
	    		if (c.numNeighbors == 0) {
	    			cells.remove((hash * c.x) + c.y);
	    		}
	    	}
	    }
	}
	
	public void addCell(int x, int y) {
		Cell c = new Cell(x, y, countNeighbors(x, y));
		if (!living[x][y]) {
			// No dormant cell here
			if (cells.get((hash * x) + y) == null) {
				cells.put((hash * x) + y, c);
			}
			living[x][y] = true;
			alive++;
			
			// update neighbor counts
			if (countDiagonals) {
				for (int i = (x - 1); i <= x + 1; i++) {
		    		for (int j = (y - 1); j <= y + 1; j++) {
		    			Cell w = cells.get((hash * i) + j);
		    			if (w == null) {
		    				cells.put((hash * i) + j, new Cell(i, j, countNeighbors(i, j)));
		    			}
		    			else {
		    				w.numNeighbors++;
		    			}
		    		}
				}
				cells.get((hash * x) + y).numNeighbors--;
			}
			else {
				for (int i = (x - 1); i <= x + 1; i += 2) {
	    			Cell w = cells.get((hash * i) + y);
	    			if (w == null) {
	    				cells.put((hash * i) + y, new Cell(i, y, countNeighbors(i, y)));
	    			}
	    			else {
	    				w.numNeighbors++;
	    			}
				}
		    	for (int j = (y - 1); j <= y + 1; j += 2) {
	    			Cell w = cells.get((hash * x) + j);
	    			if (w == null) {
	    				cells.put((hash * x) + j, new Cell(x, j, countNeighbors(x, j)));
	    			}
	    			else {
	    				w.numNeighbors++;
	    			}
		    	}

			}
			
		}

	}
	
	public void killCell(int x, int y) {
		if (living[x][y]) {
			// update neighbor counts

			
			if (countDiagonals) {
				for (int i = (x - 1); i <= x + 1; i++) {
		    		for (int j = (y - 1); j <= y + 1; j++) {
		    			try {
		    				Cell w = cells.get((hash * i) + j);
		    				w.numNeighbors--;
		    			}
		    			catch (NullPointerException e) {
		    				
		    			}
		    		}
				}
				cells.get((hash * x) + y).numNeighbors++;
			}
			else {
    			try {
    				Cell w = cells.get((hash * (x - 1)) + y);
    				w.numNeighbors--;
    				w = cells.get((hash * x) + (y - 1));
    				w.numNeighbors--;
    				w = cells.get((hash * (x + 1)) + y);
    				w.numNeighbors--;
    				w = cells.get((hash * x) + (y + 1));
    				w.numNeighbors--;
    			}
    			catch (NullPointerException e) {}
			}
			// mark cell as dead
			living[x][y] = false;
			alive--;
		}
		clearDormant();
	}
	
	public void fill(long percent) {
		clear();
		Random r = new Random();
		int n = (int) ((percent / 100.0) * (x * y));
		int newX, newY;
		if (percent == (long)100) {
			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					living[i][j] = true;
					System.out.print("x");
					if ((i == 0) || (i == (x - 1)) || (j == 0) || (j == (y - 1))) {
						cells.put((hash * i) + j, new Cell(i, j, 5));
					}
					else {
						cells.put((hash * i) + j, new Cell(i, j, 8));
					}
				}
				System.out.println("dfsfsa");

			}
			cells.get((hash * 0) + 0).numNeighbors = 3;
			cells.get((hash * 0) + y - 1).numNeighbors = 3;
			cells.get((hash * (x - 1)) + 0).numNeighbors = 3;
			cells.get((hash * (x - 1)) + y - 1).numNeighbors = 3;
			alive = x * y;
		}
		else {
			for (int i = 0; i <= n; i++) {
				newX = r.nextInt(x);
				newY = r.nextInt(y);
				if (!living[newX][newY]) {
					addCell(newX, newY);
				}
				else {
					i--;
				}
			}
		}

	}
	
}
