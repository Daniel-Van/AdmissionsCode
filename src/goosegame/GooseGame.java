/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package goosegame;

/**
 *
 * @author TheDa
 */
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 *
 * @author Daniel Van
 */
public class GooseGame extends JComponent implements ActionListener {

    // Height and Width of our game
    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    //Title of the window
    String title = "Line Game";
    // sets the framerate and delay for our game
    // this calculates the number of milliseconds per frame
    // you just need to select an approproate framerate
    int desiredFPS = 60;
    int desiredTime = Math.round((1000 / desiredFPS));
    // timer used to run the game loop
    // this is what keeps our time running smoothly :)
    Timer gameTimer;
    // YOUR GAME VARIABLES WOULD GO HERE

    // player specificaitions
    int playerHeight = 50;
    int playerWidth = 50;
    int playerX = 200;
    double playerY = 400;
    int holeSize = 100;
    int random = (int) (Math.random() * 8 + 1);
    int lineAdjust = random * 100;

    // line one specifications
    int line1X = 0;
    double line1Y = 400;
    int line1Height = 5;
    int line1Width = WIDTH - lineAdjust;

    // line two specifications
    int line2X = line1Width + holeSize;
    double line2Y = line1Y;
    int line2Height = line1Height;
    int line2Width = WIDTH - line2X;

    // player speeds
    int playerSpeed = 9;
    int playerFallSpeed = 4;

    // line Speed
    double lineSpeed = 0.75;
    // control variables
    boolean playerLeft = false;
    boolean playerRight = false;
    boolean playerFall = true;
    boolean lineRise = true;

    // store the values of the new lines that will keep repeating
    int[] newLine1X = new int[6];
    double[] newLine1Y = new double[6];
    int[] newLine1Height = new int[6];
    int[] newLine1Width = new int[6];

    int[] newLine2X = new int[6];
    double[] newLine2Y = new double[6];
    int[] newLine2Height = new int[6];
    int[] newLine2Width = new int[6];

    // used to create a timer
    long startTime = System.currentTimeMillis();

    // images used in the game
    BufferedImage playerImage = loadImage("cartoon-canadian-goose-png-clipart.png");
    BufferedImage gameOverImage = loadImage("WelcomeToUw.png");
    BufferedImage backgroundImage = loadImage("UW.png");

    // GAME VARIABLES END HERE    
    // Constructor to create the Frame and place the panel in
    // You will learn more about this in Grade 12 :)
    public GooseGame() {
        // creates a windows to show my game
        JFrame frame = new JFrame(title);

        // sets the size of my game
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        // adds the game to the window
        frame.add(this);

        // sets some options and size of the window automatically
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        // shows the window to the user
        frame.setVisible(true);

        // add listeners for keyboard and mouse
        frame.addKeyListener(new Keyboard());
        Mouse m = new Mouse();
        this.addMouseMotionListener(m);
        this.addMouseWheelListener(m);
        this.addMouseListener(m);

        // Set things up for the game at startup
        preSetup();

        // Start the game loop
        // start the timer when you start the game
        gameTimer = new Timer(desiredTime, this);
        gameTimer.setRepeats(true);
        gameTimer.start();
    }

    public BufferedImage loadImage(String filename) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return img;
    }

    // drawing of the game happens in here
    // we use the Graphics object, g, to perform the drawing
    // NOTE: This is already double buffered!(helps with framerate/speed)
    @Override
    public void paintComponent(Graphics g) {
        // always clear the screen first!
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, null);
        // GAME DRAWING GOES HERE
        
        // goose image
        g.drawImage(playerImage, playerX, (int) playerY, playerWidth, playerHeight, null);
        // color of the lines
        for (int i = 0; i < 6; i++) {
            g.setColor(Color.BLACK);
            g.fillRect(newLine1X[i], (int) newLine1Y[i], newLine1Width[i], newLine1Height[i]);
            g.fillRect(newLine2X[i], (int) newLine2Y[i], newLine2Width[i], newLine2Height[i]);
        }

        // output "GAME OVER" when the lost parameter is met
        if (playerY <= 0 || playerY + playerHeight >= HEIGHT) {
            g.setColor(Color.RED);
            // output an image of Feridun Hamdullahpur
            g.drawImage(gameOverImage, 0, 0, WIDTH, HEIGHT, null);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", WIDTH / 2 - 100, HEIGHT / 2 - 150);
        }
        // GAME DRAWING ENDS HERE
    }
    // This method is used to do any pre-setup you might need to do
    // This is run before the game loop begins!

    public void preSetup() {
        // generate all the lines before the game starts
        repeatLines();
        // Any of your pre setup before the loop starts should go here
    }

    // The main game loop
    // In here is where all the logic for my game will go
    public void gameLoop() {
        // if the player loses, exit the game loop
        if (playerY <= 0 || playerY + playerHeight >= HEIGHT) {
            return;
        }
        // how the time is kept in the game
        timer();
        // method to move the player
        movePlayer();
        // how the player collides with the lines
        collision();
        // method that moves the lines to the bottom
        moveLines();
        // method that continuously increases the line upwards
        lineMovement();
    }

    // collision method for lines
    public boolean collides(int aX, int aY, int aW, int aH, int bX, int bY, int bW, int bH) {
        if ((aX + aW < bX || aX > bX + bW || aY + aH < bY || aY > bY + bH)) {
            return false;
        } else {
            return true;
        }
    }

    // checks if the player collides with the lines or not
    public void collision() {
        for (int i = 0; i < 6; i++) {
            // does it collide with the left line
            if (collides(playerX, (int) playerY, playerWidth, playerHeight, newLine1X[i], (int) newLine1Y[i], newLine1Width[i], newLine1Height[i]) || collides(playerX, (int) playerY, playerWidth, playerHeight, newLine2X[i], (int) newLine2Y[i], newLine2Width[i], newLine2Height[i])) {
                if (collides(playerX, (int) playerY, playerWidth, playerHeight, newLine1X[i], (int) newLine1Y[i], newLine1Width[i], newLine1Height[i])) {
                    if (playerX >= newLine1X[i] + newLine1Width[i] - playerSpeed && playerX <= newLine1X[i] + newLine1Width[i] && playerY + playerHeight >= newLine1Y[i] + newLine1Height[i] / 4) {
                        playerX = newLine1X[i] + newLine1Width[i];
                        playerFall = true;
                    } else if (playerX < newLine1X[i] + newLine1Width[i]) {
                        playerFall = false;
                        playerY = playerY - lineSpeed;
                    }
                    if (playerY + playerHeight + newLine1Height[i] / 4 > line1Y + newLine1Height[i]) {
                        playerFall = true;
                    }
                }
                // does it collide with the right line
                if (collides(playerX, (int) playerY, playerWidth, playerHeight, newLine2X[i], (int) newLine2Y[i], newLine2Width[i], newLine2Height[i])) {
                    if (playerX + playerWidth <= newLine2X[i] + playerSpeed && playerX + playerWidth >= newLine2X[i] && playerY + playerHeight >= newLine2Y[i] + newLine2Height[i] / 4) {
                        playerX = newLine2X[i] - playerWidth;
                        playerFall = true;
                    } else if (playerX + playerWidth > newLine2X[i]) {
                        playerFall = false;
                        playerY = playerY - lineSpeed;
                    }
                    if (playerY + playerHeight + newLine2Height[i] / 4 > newLine2Y[i] + newLine2Height[i]) {
                        playerFall = true;
                    }
                }
            }
        }
    }

    // method to move the player for specific conditions
    public void movePlayer() {
        if (playerLeft && playerX > 0) {
            playerX = playerX - playerSpeed;
        }
        if (playerRight && playerX < WIDTH - playerWidth) {
            playerX = playerX + playerSpeed;
        }
        if (playerFall) {
            playerY = playerY + playerFallSpeed;
        }
    }

    // method that makes the line move upwards
    public void lineMovement() {
        if (lineRise) {
            for (int i = 0; i < 6; i++) {
                newLine1Y[i] = newLine1Y[i] - lineSpeed;
                newLine2Y[i] = newLine2Y[i] - lineSpeed;
            }
        }
    }

    // method that will keep outputing lines from the bottom
    public void repeatLines() {
        for (int i = 0; i < 6; i++) {
            random = (int) (Math.random() * 8 + 1);
            lineAdjust = random * 100;

            newLine1X[i] = line1X;
            newLine1Y[i] = line2Y + line2Height + 95;
            newLine1Height[i] = line1Height;
            newLine1Width[i] = WIDTH - lineAdjust;

            newLine2X[i] = newLine1Width[i] + holeSize;
            newLine2Y[i] = newLine1Y[i];
            newLine2Height[i] = line1Height;
            newLine2Width[i] = WIDTH - newLine2X[i];

            line1X = newLine1X[i];
            line1Y = newLine1Y[i];
            line1Height = newLine1Height[i];
            line1Width = newLine1Width[i];

            line2X = newLine2X[i];
            line2Y = newLine2Y[i];
            line2Height = newLine2Height[i];
            line2Width = newLine2Width[i];
        }
    }

    // method that generates the new lines after it reaches the top of the screen
    // lines will move to the bottom and repeat
    public void moveLines() {
        for (int j = 0; j < 6; j++) {
            if (newLine1Y[j] + newLine1Height[j] <= 0) {
                newLine1Y[j] = HEIGHT;
                newLine2Y[j] = HEIGHT;

                random = (int) (Math.random() * 8 + 1);
                lineAdjust = random * 100;

                newLine1X[j] = line1X;
                newLine1Height[j] = line1Height;
                newLine1Width[j] = WIDTH - lineAdjust;

                newLine2X[j] = newLine1Width[j] + holeSize;
                newLine2Height[j] = line1Height;
                newLine2Width[j] = WIDTH - newLine2X[j];
            }
        }
    }

    // timer used to increase the speed of the line
    // line of speed caps at 2.75
    public void timer() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime >= 20000) {
            lineSpeed = lineSpeed + 0.435;
            startTime = System.currentTimeMillis();
        }
        if (lineSpeed >= 2.75) {
            lineSpeed = 2.75;
        }
    }

    // Used to implement any of the Mouse Actions
    private class Mouse extends MouseAdapter {

        // if a mouse button has been pressed down
        @Override
        public void mousePressed(MouseEvent e) {
        }

        // if a mouse button has been released
        @Override
        public void mouseReleased(MouseEvent e) {
        }

        // if the scroll wheel has been moved
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
        }

        // if the mouse has moved positions
        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    // Used to implements any of the Keyboard Actions
    private class Keyboard extends KeyAdapter {

        // if a key has been pressed down
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_A) {
                playerLeft = true;
            } else if (key == KeyEvent.VK_D) {
                playerRight = true;
            }
        }

        // if a key has been released
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_A) {
                playerLeft = false;
            } else if (key == KeyEvent.VK_D) {
                playerRight = false;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        gameLoop();
        repaint();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // creates an instance of my game
        GooseGame game = new GooseGame();
    }
}
