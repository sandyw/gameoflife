import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Scanner;

import javax.swing.*;


public class GridPanel extends JPanel implements ActionListener {
	CellGrid grid;
	JPanel buttonPanel;
	Canvas disp;
	JButton stopButton, stepButton, clearButton, fillButton, openButton, sizeButton, liveButton;
	JCheckBox diagToggle, numberToggle;
	JFormattedTextField fillField, placeX, placeY, sizeX, sizeY, cellSize;
	JFormattedTextField gen, live1, live2;
	JFileChooser fileChooser;
	Timer timer;
	
	JFrame window;
	boolean paused = true;

	public GridPanel(JFrame w) {
		this.window = w;
		grid = new CellGrid(100, 100);	
		try {
			File f = new File("halfmax.rle");
			openFile(f, grid.x / 2 - 32, grid.y / 2 - 40);
		} catch (IOException e1) {
		}
		
		timer = new Timer(100, this);
		
		stopButton = new JButton("Stop/Start");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paused = (paused) ? false : true;
				
			}
		});
		
		stepButton = new JButton("Step");
		stepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paused = true;
				grid.update();
				grid.repaint();
				disp.repaint();
				
			}
		});
		
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				grid.clear();
			}
		});
		
		diagToggle = new JCheckBox("Diagonals are neighbors");
		diagToggle.setSelected(true);
		diagToggle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				boolean tmp = paused;
				paused = true;
				grid.countDiagonals = (grid.countDiagonals) ? false : true;
				grid.recountNeighbors();
				grid.repaint();
				paused = tmp;
			}
		});
		
		numberToggle = new JCheckBox("Display neighbor counts");
		numberToggle.setSelected(false);
		numberToggle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				grid.showNumbers = (grid.showNumbers) ? false : true;
				grid.repaint();
			}
		});
		
		fillField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		fillField.setColumns(3);
		fillButton = new JButton("Fill");
		fillButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				grid.fill((long)(Long)fillField.getValue());
				grid.repaint();
			}
		});
		
		sizeX = new JFormattedTextField(NumberFormat.getIntegerInstance());
		sizeX.setColumns(3);
		sizeX.setText(Integer.toString(grid.x));
		sizeY = new JFormattedTextField(NumberFormat.getIntegerInstance());
		sizeY.setColumns(3);
		sizeY.setText(Integer.toString(grid.y));
		cellSize = new JFormattedTextField(NumberFormat.getIntegerInstance());
		cellSize.setColumns(3);
		cellSize.setText(Integer.toString(grid.cellPx));

		sizeButton = new JButton("Resize");
		sizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int x = (sizeX.getValue() == null) ? grid.x : ((Long) sizeX.getValue()).intValue();
				int y = (sizeY.getValue() == null) ? grid.y : ((Long) sizeY.getValue()).intValue();
				int px = (cellSize.getValue() == null) ? grid.cellPx : ((Long) cellSize.getValue()).intValue();
				
				paused = true;
				grid.clear();
				grid.changeSize(x, y, px);
				grid.repaint();
				window.pack();
			}
		});
		
		gen = new JFormattedTextField(NumberFormat.getIntegerInstance());
		gen.setColumns(3);
		gen.setText(Integer.toString(grid.gen));
		live1 = new JFormattedTextField(NumberFormat.getIntegerInstance());
		live1.setColumns(3);
		live1.setText(Integer.toString(grid.live1));
		live2 = new JFormattedTextField(NumberFormat.getIntegerInstance());
		live2.setColumns(3);
		live2.setText(Integer.toString(grid.live2));
		
		liveButton = new JButton("Change Rules");
		liveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int g = (gen.getValue() == null) ? grid.gen : ((Long) gen.getValue()).intValue();
				int l1 = (live1.getValue() == null) ? grid.live1 : ((Long) live1.getValue()).intValue();
				int l2 = (live2.getValue() == null) ? grid.live2 : ((Long) live2.getValue()).intValue();
				
				grid.gen = g;
				grid.live1 = l1;
				grid.live2 = l2;
			}
		});
		
		disp = new Canvas() {
			public void paint(Graphics g) {
				g.drawString("Generations: " + String.valueOf(grid.generations), 10, 12);
				g.drawString("Cells: " + String.valueOf(grid.alive), 10, 25);
				g.drawString("  (" + String.format("%4.2f", (double)grid.alive / (double)(grid.x * grid.y) * 100.0) + "%)", 80, 25);
				g.drawString("Cells last turn: " + String.valueOf(grid.alivePrev), 10, 37);
			}
		};
		disp.setSize(200, 100);
		disp.setBackground(Color.white);
		
		openButton = new JButton("Open .rle file...");
		fileChooser = new JFileChooser();
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fileChooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
			        try {
						openFile(fileChooser.getSelectedFile(), grid.x / 4, grid.y / 4);
					} catch (IOException ex) {
						System.out.println("Could not open file");
					}
				}
			}
		});
		
		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(200, 500));
		buttonPanel.add(stopButton);
		buttonPanel.add(stepButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(diagToggle);
		buttonPanel.add(numberToggle);
		buttonPanel.add(disp);
		buttonPanel.add(new JLabel("Fill to %: "));
		buttonPanel.add(fillField);
		buttonPanel.add(fillButton);
		
		buttonPanel.add(new JLabel("X: "));
		buttonPanel.add(sizeX);
		buttonPanel.add(new JLabel("Y: "));
		buttonPanel.add(sizeY);
		buttonPanel.add(new JLabel("CellSize: "));
		buttonPanel.add(cellSize);
		sizeButton.setPreferredSize(new Dimension(150, 30));
		buttonPanel.add(sizeButton);
		
		buttonPanel.add(new JLabel("# to come alive: "));
		buttonPanel.add(gen);
		buttonPanel.add(new JLabel("# to stay alive: "));
		buttonPanel.add(live1);
		buttonPanel.add(new JLabel("# to stay alive: "));
		buttonPanel.add(live2);
		buttonPanel.add(liveButton);

		buttonPanel.add(openButton);


		this.setLayout(new BorderLayout());
		add(grid, BorderLayout.WEST);
		add(buttonPanel, BorderLayout.EAST);

	}

	public void actionPerformed(ActionEvent e) {
		if (!paused) {
			grid.update();
		}
		grid.repaint();
		disp.repaint();
		
	}
	
	public boolean openFile(File f, int xPos, int yPos) throws IOException {
		String s;
		StringBuilder sb = new StringBuilder("");
		Scanner scanner = new Scanner(f).useDelimiter("\\$");
		s = scanner.nextLine();
		while (s.charAt(0) == '#') {
				s = scanner.nextLine();
		}
		
		int n, x; 
		int y = yPos;
		try {
			while(scanner.hasNext()) { 
				n = 1;
				x = xPos;
				s = scanner.next();
				sb = new StringBuilder();

				for (int i = 0; i < s.length(); i++) {
					if (Character.isDigit(s.charAt(i))) {
						sb.append(s.charAt(i));
						//System.out.print(String.valueOf(s.charAt(i)));
					}
					else {
						//System.out.print(String.valueOf(s.charAt(i)));

						n = (sb.length() == 0) ? 1 : Integer.parseInt(sb.toString());
						sb = new StringBuilder();
						
						if (s.charAt(i) == 'o') {
							for (int j = x; j < (x + n); j++) {
								if ((j < grid.x) && (y < grid.y)) {
									grid.addCell(j, y);
								}

							}
							x += n;
						}
						if (s.charAt(i) == 'b') {
							x += n;
						}
					}
				}
				n = (sb.length() == 0) ? 1 : Integer.parseInt(sb.toString());
				y += n;
			}
		} finally {
			scanner.close();
		}
		// catch error states
		if (sb.length() == 0) {
			throw new IOException();
		}
		if (sb.toString() == null) {
			throw new IOException();
		}

		return true;
	}
}
