package core;

import processing.core.PApplet;
import processing.core.PVector;

public class LinearApplet extends PApplet {
	
	// main and initialization of applet
	public static void main(String args[]) {
		// Handle command line arguments
		try {
			if (args[0] != null) {
				pointScatter = Integer.parseInt(args[0]);
			}
			if (args[1] != null) {
				cyclesPerSecond = Integer.parseInt(args[1]);
			}
			if (args[2] != null) {
				slope = (float) Double.parseDouble(args[2]);
			}
			if (args[3] != null) {
				numberOfPoints = Integer.parseInt(args[3]);
			}
			if (args[4] != null) {
				windowSize = Integer.parseInt(args[4]);
			}
			// Catch all problems and literally ignore them
		} catch (Exception e) {
			System.out.println("This is the error: " + e.getMessage());
			System.out.println("Format is:\n[point scatter] [frame rate] [slope] [number of points]");
		}
		
		System.out.println("\nRunning app with these variables...\n"
				+ "pointScatter: " + pointScatter + "\n"
				+ "frameRate: " + cyclesPerSecond + "\n"
				+ "slope: " + slope + "\n"
				+ "numberOfPoints: " + numberOfPoints + "\n");
		
		PApplet.main("core.LinearApplet");
	}
	// Settings of applet
	public void settings() {
		size(windowSize, windowSize);
	}
	
	// Processing Variables
	private static int cyclesPerSecond = 60;				// Default fps 60
	private static int windowSize = 900;
	private boolean pause = true;
	
	// Graph variables
	private static int numberOfPoints = 100;				// Default 100 points
	private static float slope = 1.1f;						// Default 1 for slope
	private static int scale = 8;							// Default 10 for size
	private static int pointScatter = 120;					// Default 50 for deviance
	private static boolean drawGrid = false;
	private PVector[] points;
	
	// Guess Variables
	// Try not to mess with these as they change while its running.
	private float guessSlope = 4;
	private float minimumDistance = 9999999;
	private float minimumSlope = 1;
	private boolean toggle = true;
	
	// Setup and initial variable assigning.
	public void setup() {
		surface.setTitle("Visual Linear R");
		frameRate(cyclesPerSecond);
		
		scale = width / numberOfPoints;
		int pointsOffScreen = 0;
		
		// Initialize points
		points = new PVector[numberOfPoints];
		int x = 0, y = 0;
		for (int i = 0; i < points.length; i ++) {
			points[i] = new PVector(x + (int) random(-pointScatter, pointScatter),
									y + (int) random(-pointScatter, pointScatter));
			
			// Count points off screen during creation of the points
			if ((points[i].x > width) | (points[i].y > height)) {
				pointsOffScreen ++;
			}
			x += scale;
			y = (int) (x * slope);
		}
		println("points off screen: " + pointsOffScreen);
		println("scale: " + scale + "\nFinished Setup...");
	}
	
	// Processing main draw loop override.
	public void draw() {
		background(40);
		noFill();
		
		// We push the matrix to perform translations without affecting the camera
		pushMatrix();
		rotate(PI / -2);
		translate(-height, 0);
		
		// Draw Grid Lines
		stroke(100);
		if (drawGrid) {
			strokeWeight(0.75f);
			for (int x = 0; x < width; x += scale) {
				for (int y = 0; y < height; y += scale) {
					rect(x, y, scale, scale);
				}
			}
		}
		
		// Draws borders
		strokeWeight(4);
		line(0, 0, width, 0);
		line(1, 0, 1, height);

		// Draw points
		stroke(255);
		strokeWeight(4);
		beginShape(POINTS);
		for (PVector point : points) {
			vertex(point.x, point.y);
		}
		endShape();
		
		// Runs other methods of drawing
		drawGuessData();
		drawMinSlope();
		
		// We pop the camera back into place to allow for
		// correct UI placement on the screen
		popMatrix();
		
		drawUI();
	}
	
	// Draws the guess in the current line
	private void drawGuessData() {
		float sumOfDistance = 0;
		if (guessSlope >= 0.001) {
			stroke(0, 200, 255);
			line(0, 0, width, height * guessSlope);
			strokeWeight(0.5f);
			for (PVector point : points) {
				float minDisToLine = 9999;
				float minX = 0;
				for (int x = 0; x < width; x += scale) {
					float dis = getDistance(point, new PVector(x, (x * guessSlope)));
					if (dis < minDisToLine) {
						minDisToLine = dis;
						minX = x;
					}
				}
				line(point.x, point.y, minX, (minX * guessSlope));
				sumOfDistance += minDisToLine;
			}
			if (sumOfDistance < minimumDistance) {
				minimumDistance = sumOfDistance;
				minimumSlope = guessSlope;
			}
			if (!pause)
				guessSlope -= 0.01f;
		} else {
			guessSlope = 0;
			if (toggle) {
				println("Finished drawing...");
				println("\nMinimum slope: " + minimumSlope);
				toggle = false;
			}
		}
	}
	
	// Draws the min slope line
	private void drawMinSlope() {
		strokeWeight(2);
		stroke(200, 0, 255);
		line(0, 0, width, height * minimumSlope);
	}
	
	// Distance formula
	private float getDistance(PVector p1, PVector p2) {
		return sqrt(pow((p2.x - p1.x), 2) + pow((p2.y - p1.y), 2));
	}
	
	// Draws the text on screen
	private void drawUI() {
		noStroke();
		textSize(20);
		textAlign(RIGHT);
		fill(255);

		text("FPS: " + (int) frameRate, width - 20, 20);
		
		if (guessSlope >= 0.001) {	
			text("Slope: " + guessSlope, width - 20, height - 45);
		} else {
			text("Initial slope: " + slope, width - 20, height - 45);
			fill(0, 200, 255);
		}
		text("Min: " + minimumSlope, width - 20, height - 20);
		
		if (pause) {
			stroke(255,255,0);
			line(0, 1, width, 1);
			fill(255,255,0);
			textAlign(CENTER);
			text("PAUSED \t\t Press Space to Unpause", width / 2, 20);
		}
	}
	
	// Reset slope calculation
	private void reset() {
		guessSlope = 4;
		minimumDistance = 9999999;
		minimumSlope = 1;
		toggle = true;
	}
	
	// Processing keypressed method override
	public void keyPressed() {
		// Spacebar
		if (keyCode == 32) {
			pause = !pause;
		}
		// Down arrow
		if (keyCode == 40) {
			if (cyclesPerSecond - 2 >= 2)
				cyclesPerSecond -= 2;
		}
		// Up arrow
		if (keyCode == 38) {
			if (cyclesPerSecond + 2 <= 100)
				cyclesPerSecond += 2;
		}
		// R key
		if (keyCode == 82) {
			reset();
		}
		// Set frameRate limit again
		frameRate(cyclesPerSecond);
	}
}
