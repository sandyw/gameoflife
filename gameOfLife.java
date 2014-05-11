import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class gameOfLife {
//	Timer timer;
//	ActionListener timerListener;
//	CellGrid grid;
//	
//	public gameOfLife() {
//
//	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame window = new JFrame();
		GridPanel p = new GridPanel(window);



		window.setLocation(500, 300);
		window.add(p);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
		p.timer.start();
	}

}
