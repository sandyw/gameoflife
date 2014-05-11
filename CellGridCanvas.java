import java.awt.Canvas;
import java.awt.Graphics;


public class CellGridCanvas extends Canvas {
	public CellGridCanvas() { 
		setSize(380, 340); 
	} 

	public void paint(Graphics g) { 
		g.drawRect(0, 0, 90, 50);  
		g.drawString("A Canvas", 15,15); 
	}
}
