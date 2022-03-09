// https://www.youtube.com/watch?v=I1qTZaUcFX0
// Features added: call jump() after the space key is pressed instead of released, high score displayed,
// smoother transition to another round after game over (mouse click to start a new round, space key to move the bird)

package flappybird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class FlappyBird implements ActionListener, MouseListener, KeyListener
{
	public static FlappyBird flappyBird;
	public final int WIDTH = 800, HEIGHT = 800;
	private Renderer renderer;
	private Rectangle bird;
	private int ticks, yMotion, score, highScore;
	private ArrayList<Rectangle> columns;
	private boolean gameOver, started;
	private Random rand;
	
	public FlappyBird()
	{
		JFrame jframe = new JFrame();
		Timer timer = new Timer(20, this);
		renderer = new Renderer();
		rand = new Random();
		ticks = 0; yMotion = 0; score = 0; highScore = 0; gameOver = false; started = false;
		
		jframe.add(renderer);
		jframe.setTitle("Flappy Bird");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(WIDTH, HEIGHT);
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		jframe.setResizable(false);
		jframe.setVisible(true);
		
		bird = new Rectangle(WIDTH/2 - 10, HEIGHT/2 - 10, 20, 20);
		columns = new ArrayList<Rectangle>();
		
		addColumn(true);
		addColumn(true);
		addColumn(true);
		addColumn(true);
		
		timer.start();
	}
	
	public void addColumn(boolean start)
	{
		int space = 300;
		int width = 100;
		int height = 50 + rand.nextInt(300); // minimum height = 50, maximum height = 299
		
		if (start)
		{
			columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height - 120, width, height)); // bottom columns
			columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * 300, 0, width, HEIGHT - height - space)); // top columns
		}
		else
		{
			columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - height - 120, width, height)); // bottom columns
			columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, width, HEIGHT - height - space)); // top columns
		}
	}
	
	public void paintColumn(Graphics g, Rectangle column)
	{
		g.setColor(Color.green.darker());
		g.fillRect(column.x, column.y, column.width, column.height);
	}
	
	public void jump()
	{
		// This block of code is called if a mouse button is clicked (should work with trackpads too).
		// The game must not have been started.
		if (!started)
		{
			started = true;
		}
		// This block of code is called if the space key is pressed and the game has been started but is not over at
		// the time of running this code.
		else if (!gameOver)
		{
			if (yMotion > 0)
				yMotion = 0;
			
			yMotion -= 10;
		}
		// This block of code is called if a mouse button is clicked (should work with trackpads too) and the game
		// has been started but is now over.
		else if (gameOver)
		{
			bird = new Rectangle(WIDTH/2 - 10, HEIGHT/2 - 10, 20, 20);
			columns.clear();
			yMotion = 0;
			if (score > highScore)
				highScore = score;
			score = 0;
			
			addColumn(true);
			addColumn(true);
			addColumn(true);
			addColumn(true);
			
			gameOver = false;
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		int speed = 10;
		ticks++;
		
		if (started)
		{
			for (int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);
				column.x -= speed;
			}

			if (ticks % 2 == 0 && yMotion < 15)
			{
				yMotion += 2;
			}

			for (int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);
				if (column.x + column.width < 0)
				{
					columns.remove(column);
					if (column.y == 0)
						addColumn(false);
				}
			}

			bird.y += yMotion;

			for (Rectangle column : columns)
			{
				// Increment the score by one if the bird passes through the space between columns, when the bird's
				// center x value is at the center x value of a column (the top column).
				if (column.y == 0 && bird.x + bird.width/2 > column.x + column.width/2 - 10 && bird.x + bird.width/2 < column.x + column.width/2 + 10)
					score++;
				
				// If the bird touches any part of a column, the game is over.
				if (column.intersects(bird))
				{
					gameOver = true;
					
					// If the bird touches the left side of a column, the bird experiences a force to the left.
					if (bird.x <= column.x)
						bird.x = column.x - bird.width;
					else
					{
						// Ensures that if the bird touches the top part of a bottom column, it cannot pass through the column.
						if (column.y != 0)
						{
							bird.y = column.y - bird.height;
						}
						// Ensures that if the bird touches the bottom part of a top column, it cannot pass through the column.
						else if (bird.y < column.height)
						{
							bird.y = column.height;
						}
					}
				}
			}

			// If the bird touches the grass or the sky, then the game is over.
			// The original code (from the youtube video) contained "if (bird.y > HEIGHT - 120 || bird.y < 0)".
			if (bird.y + bird.height >= HEIGHT - 120 || bird.y <= 0)
				gameOver = true;
			
			// Ensures that the bird cannot go through the grass.
			// The bird's y position cannot be higher (visually lower) than the grass. If the bird reaches the grass,
			// it will simply rest on top of it.
			if (bird.y + yMotion >= HEIGHT - 120)
				bird.y = HEIGHT - 120 - bird.height;
		}
		
		renderer.repaint();
	}
	
	public void repaint(Graphics g)
	{
		// Background color that fills the program's entire window, unless otherwise specified
		g.setColor(Color.cyan);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		// Paints the dirt at the very bottom of the window, covering the width of the window
		g.setColor(Color.orange);
		g.fillRect(0, HEIGHT - 120, WIDTH, 120);
		
		// Paints the grass on top of the dirt, covering the width of the window
		g.setColor(Color.green);
		g.fillRect(0, HEIGHT - 120, WIDTH, 20);
		
		// Paints the bird according to the specifications in its Rectangle constructor
		g.setColor(Color.red);
		g.fillRect(bird.x, bird.y, bird.width, bird.height);
		
		for (Rectangle column : columns)
		{
			paintColumn(g, column);
		}
		
		g.setColor(Color.white);
		g.setFont(new Font("Arial", 1, 100));
		
		// Only displayed once upon starting the application
		if (!started)
		{
			g.drawString("Click to start!", 75, HEIGHT / 2 - 50);
		}
		// The user's score and high score (both initially at zero) are displayed after the user initiates the start
		// of the game for the first time until the program is terminated.
		else
		{
			g.setFont(new Font("Arial", 1, 50));
			g.drawString("HIGH SCORE: " + String.valueOf(highScore), WIDTH / 2 - 400, 50);
			g.drawString("SCORE: " + String.valueOf(score), WIDTH / 2 - 400, 100);
		}
		
		if (gameOver)
		{
			g.setFont(new Font("Arial", 1, 45));
			g.drawString("Game Over! Click to play again.", 75, HEIGHT / 2 - 50);
		}
		
		/*
		if (!gameOver && started)
		{
			g.drawString(String.valueOf(score), WIDTH / 2 - 25, 100);
		}
		*/
	}
	
	public static void main(String[] args)
	{
		flappyBird = new FlappyBird();
	}
	
	public void mouseClicked(MouseEvent e)
	{
		// jump() is called only if either (1) the game has not yet started (which is only true once as soon as the
		// game's window is opened until the user clicks their mouse within this window) or (2) the game is over due
		// to the bird having touched the top edge of the game's window, the grass on the bottom, or a column.
		// This ensures that the user must clearly indicate wanting to play, either for the first time or after a game is over.
		if (!started || gameOver)
			jump();
	}
	
	public void mouseEntered(MouseEvent e)
	{
		
	}
	
	public void mouseExited(MouseEvent e)
	{
		
	}
	
	public void mousePressed(MouseEvent e)
	{
		
	}
	
	public void mouseReleased(MouseEvent e)
	{
		
	}
	
	public void keyPressed(KeyEvent e)
	{
		// jump() is called here if the space key is pressed. The game has to have started and cannot be over.
		// This ensures that the user can only move the bird when the game has started and is not in a "Game Over" state.
		if (e.getKeyCode() == KeyEvent.VK_SPACE && started && !gameOver)
			jump();
	}
	
	public void keyReleased(KeyEvent e)
	{
		
	}
	
	public void keyTyped(KeyEvent e)
	{
		
	}
}