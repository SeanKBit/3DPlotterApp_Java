/*
 * 3D Plotter App
 * © 2020 Sean Bittner
 * 
 * 
 * Completely dependent on the StdDraw3D.java library written by Hayk Martirosyan. 
 * See StdDraw3D Class header for more info.
 * Introductory Tutorial:
 * http://introcs.cs.princeton.edu/java/stddraw3d
 *
 * Reference manual:
 * http://introcs.cs.princeton.edu/java/stddraw3d-manual.html
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class Main {

	private static double dia = 0.3;
	private static String filePath = "C:\Users\\PlotPoints.csv"; // REMOVED *proprietary*
	private static JFrame frame;
	private static JPanel contentPane;
	private static JTextArea textArea;
	private static JScrollPane scrollPane;
	private static JButton create3DButton;
	public boolean wait;

	public Main() {
		int i;
		String row = "";
		int rowCounter = 0;
		double minDev;
		double maxDev;
		double upperScale;
		double lowerScale;
		String delimiter = null;

		
		createSplash();
		
		// Opens window for user to input csv data. Then waits for them to hit button
		wait = true;
		makeWindow();
		while (wait) {
			System.out.println("wating...");
		}

		// Initial check of delimiter type, and row count of data file used to initiate
		// array sizes and such later on
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(filePath));
			while ((row = csvReader.readLine()) != null) {
				if (row.contains(",")) {
					delimiter = ",";
				} else {
					delimiter = "\\s";
				}
				rowCounter++;
			}
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Parse data file to get all deviation data into array. Used later to mark min and max data points, as well as for dynamically calculating "scale".
		i = 0;
		double deviationArr[] = new double[rowCounter];
		double nomXArr[] = new double[rowCounter];
		double nomYArr[] = new double[rowCounter];
		double nomZArr[] = new double[rowCounter];
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(filePath));
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(delimiter);
				nomXArr[i] = Double.parseDouble(data[0]);
				nomYArr[i] = Double.parseDouble(data[1]);
				nomZArr[i] = Double.parseDouble(data[2]);
				deviationArr[i] = Double.parseDouble(data[6]);
				i++;
			}
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get min and max deviating values
		minDev = getMin(deviationArr);
		maxDev = getMax(deviationArr);
		upperScale = getSetScaleValue(nomXArr, nomYArr, nomZArr);
		lowerScale = -upperScale;
		//System.out.println(upperScale);
		//System.out.println(lowerScale);
		//System.out.println(maxDev);
		
		// Begin drawing stuff to fullscreen
		StdDraw3D.fullscreen();
		// Set the scale of the window
		StdDraw3D.setScale(0, 60); // 60 worked well. 
		// Draws overlay text
		StdDraw3D.setPenRadius(0.5);
		StdDraw3D.setPenColor(StdDraw3D.BLUE);
		StdDraw3D.overlayTextLeft(3, 10, "Minimum Deviation = BLUE");
		StdDraw3D.setPenColor(StdDraw3D.RED);
		StdDraw3D.overlayTextLeft(3, 8, "Maximum Deviation = RED");
		StdDraw3D.setPenColor(StdDraw3D.WHITE);
		StdDraw3D.overlayTextLeft(3, 6, "Starting Point = WHITE");
		StdDraw3D.setPenColor(StdDraw3D.LIGHT_GRAY);
		StdDraw3D.overlayTextLeft(3, 4, "Nominal = LIGHT_GRAY");
		StdDraw3D.setPenColor(StdDraw3D.GREEN);
		StdDraw3D.overlayTextLeft(3, 2, "Actual = GREEN");
		// Reset scale for object size
		StdDraw3D.setScale(lowerScale, upperScale);

		// Initiate 3D object arrays
		StdDraw3D objNom[] = new StdDraw3D[rowCounter];
		StdDraw3D objAct[] = new StdDraw3D[rowCounter];

		// Begin parsing data again to create objects
		i = 0;
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(filePath));
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(delimiter);

				// Set camera relative to mid-point of scan
				if (i == (int) rowCounter / 2) {
					StdDraw3D.setOrbitCenter(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]));
					StdDraw3D.setCameraPosition(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Math.abs(Double.parseDouble(data[2]) * 2));
					StdDraw3D.setCameraOrientation(0, 0, 1);
				}

				// Draw the sphere(s) using parsed data
				if (i == 0) {
					StdDraw3D.setPenColor(StdDraw3D.WHITE, 210); // Set first-scanned point a different color to show scan direction (starting point)
																	
				} else {
					StdDraw3D.setPenColor(StdDraw3D.LIGHT_GRAY, 130);
				}
				objNom[i].sphere(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), dia); // "nominal" point data

				// Set colors relative to min/max deviation conditions
				if (Double.parseDouble(data[6]) == minDev) {
					StdDraw3D.setPenColor(StdDraw3D.BLUE, 250);
				} else if (Double.parseDouble(data[6]) == maxDev) {
					StdDraw3D.setPenColor(StdDraw3D.RED, 250);
				} else {
					StdDraw3D.setPenColor(StdDraw3D.GREEN, 220);
				}
				objAct[i].sphere(Double.parseDouble(data[3]), Double.parseDouble(data[4]), Double.parseDouble(data[5]), dia); // "actual" point data
				i++;
			}
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Finish rendering
		StdDraw3D.finished();
	}

	public static double getMin(double[] inputArr) {
		double minValue = inputArr[0];
		for (int i = 0; i < inputArr.length; i++) {
			if (inputArr[i] < minValue) {
				minValue = inputArr[i];
			}
		}
		return minValue;
	}

	public static double getMax(double[] inputArr) {
		double maxValue = inputArr[0];
		for (int i = 0; i < inputArr.length; i++) {
			if (inputArr[i] > maxValue) {
				maxValue = inputArr[i];
			}
		}
		return maxValue;
	}
	
	public static double getSetScaleValue(double[] xArr, double[] yArr, double[] zArr) {
		double returnUpperScale = Math.abs(xArr[0]);
		for (int i = 0; i < xArr.length; i++) {
			if (Math.abs(xArr[i]) > returnUpperScale) {
				returnUpperScale = Math.abs(xArr[i]);
			}
			if (Math.abs(yArr[i]) > returnUpperScale) {
				returnUpperScale = Math.abs(yArr[i]);
			}
			if (Math.abs(zArr[i]) > returnUpperScale) {
				returnUpperScale = Math.abs(zArr[i]);
			}
			if (returnUpperScale < 40.0) {
				returnUpperScale = 40.0;
			}
		}
		return returnUpperScale;
	}

	public void createSplash() {
		
		JWindow window = new JWindow();
		//window.setLayout(new BorderLayout());
		window.getContentPane().add(new JLabel("", new ImageIcon("\\\\filer1\\Public\\SBittner\\3D Plotter App\\Resources\\SpaceX Splash 3D Resized.png"), SwingConstants.CENTER));
		window.setShape(new Ellipse2D.Double(0, 0, 390, 300));
		//window.setUndecorated(true);
		window.setSize(390, 300);
		window.setLocationRelativeTo(null);
		window.getContentPane().setBackground(new Color(225, 240, 252));
		
		window.setVisible(true);
		try {
		    Thread.sleep(5000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		window.dispose();
	}
	
	public void makeWindow() {
		Color backGround = new Color(61, 73, 74);
		frame = new JFrame("CSV Input");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		makeMenu();
		contentPane = (JPanel) frame.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
		contentPane.setBackground(backGround);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(3, 3));
		panel.setPreferredSize(new Dimension(600, 720));
		panel.setBackground(backGround);

		textArea = new JTextArea(
				"*** Delete this text and the labels below, then paste CSV Data into this area. See \"Help\" for formatting. *** \n"
						+ "*** Xnom   |   Ynom   |   Znom   |   Xact   |   Yact   |   Zact   |   Dev ***");
		// textArea.setText(" \n");
		textArea.setMinimumSize(new Dimension(600, 500));

		scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane);

		JPanel panelSouth = new JPanel();
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
		panelSouth.setPreferredSize(new Dimension(100, 70));
		panelSouth.setBackground(backGround);

		create3DButton = new JButton(" < C R E A T E - 3D > ");
		create3DButton.setToolTipText("Click Me...");
		create3DButton.setRolloverEnabled(true);
		create3DButton.setSize(50, 10);
		create3DButton.setBackground(new Color(127, 173, 200));
		create3DButton.setFont(new Font("Baskerville", Font.ITALIC, 18));
		create3DButton.setBorder(BorderFactory.createRaisedBevelBorder());
		create3DButton.addActionListener(new ButtonListener());
		panelSouth.add(create3DButton);

		contentPane.add(panel);
		contentPane.add(panelSouth);

		//frame.setBackground(new Color(61, 73, 74));
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("\\\\filer1\\Public\\SBittner\\3D Plotter App\\Resources\\SpaceX-X-White 3D.png"));
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
	
	private void makeMenu() {
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu menu;
		JMenuItem helpItem;
		JMenuItem aboutItem;
		
		menu = new JMenu("Help");
		helpItem = new JMenuItem("How To Info...");
		helpItem.addActionListener(new HelpListener());
		menu.add(helpItem);
		menuBar.add(menu);
		
		menu = new JMenu("About");
		aboutItem = new JMenuItem("About...");
		aboutItem.addActionListener(new AboutListener());
		menu.add(aboutItem);
		menuBar.add(menu);
	}
	

	public class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// String formatString = textArea.getText();
			// formatString.replaceAll("\\s+", ",");

			try {
				BufferedWriter userInput = new BufferedWriter(new FileWriter(filePath));
				userInput.write(textArea.getText());
				userInput.close();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(frame, "An error occurred.", "Output Error", JOptionPane.ERROR_MESSAGE);
			}

			wait = false;
			frame.dispose();
		}
	}
	

	public class HelpListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ImageIcon icon = new ImageIcon("\\\\filer1\\Public\\SBittner\\3D Plotter App\\Resources\\help.png");
			JOptionPane.showMessageDialog(null, new JLabel("", icon, JLabel.CENTER), "Info", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	
	public class AboutListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(null, "3D Data Plotter \n" 
					+ "Copyright 2020 Sean Bittner \n"
					+ "Credit to Hayk Martirosyan for creating the StdDraw3D.java library", "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}


	public static void main(String[] args) {
		Main m = new Main();
	}

}
