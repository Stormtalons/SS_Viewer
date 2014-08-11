import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class SSFrame extends JFrame
{
	boolean active = true, resize = false, suspend = false;
	JPanel imgPane;
    BufferedImage original, current;
	Point click;
	ArrayList<DrawnBox> boxes;

	public SSFrame(BufferedImage a)
	{
		boxes = new ArrayList<>();
		original = a;
		current = a;
		
		Container c = getContentPane();
		imgPane = new JPanel()
		{
			public void paint(Graphics g)
			{
				g.drawImage(SSFrame.this.current, 0, 0, null);
				g.setColor(Color.RED);
				for (DrawnBox db : SSFrame.this.boxes)
				{
					g.drawRect(db.x, db.y, db.w, db.h);
					g.drawRect(db.x + 1, db.y + 1, db.w - 2, db.h - 2);
				}
			}
		};
		imgPane.setBackground(Color.green);
		SpringLayout sl = new SpringLayout();
		sl.putConstraint(SpringLayout.NORTH, imgPane, 2, SpringLayout.NORTH, c);
		sl.putConstraint(SpringLayout.WEST, imgPane, 2, SpringLayout.WEST, c);
		sl.putConstraint(SpringLayout.EAST, imgPane, -2, SpringLayout.EAST, c);
		sl.putConstraint(SpringLayout.SOUTH, imgPane, -2, SpringLayout.SOUTH, c);
		c.setLayout(sl);
		c.add(imgPane);

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown())
				{
					Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
					if (t.isDataFlavorSupported(DataFlavor.imageFlavor))
					{
						try
						{
							current = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
							setSize(new Dimension(current.getWidth() + 4, current.getHeight() + 4));
							repaint();
						} catch (Exception e1) {e1.printStackTrace();}
					}
				}
			}
		});
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3) inactivate();
				else if (e.getButton() == MouseEvent.BUTTON1)
				{
					if (inResizeBlock(e))
					{
						moveTo(SSFrame.this.getX() + SSFrame.this.getWidth(), SSFrame.this.getY() + SSFrame.this.getHeight(), true);
						click = new Point(SSFrame.this.getWidth(), SSFrame.this.getHeight());
					}
					else click = e.getPoint();
					if (e.isControlDown() && e.isAltDown()) saveImage();
					else if (e.isAltDown()) boxes.clear();
					else if (e.isShiftDown())
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(getScreen()), null);
					repaint();
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1 && resize)
				{
					double imw = (double) current.getWidth();
					double imh = (double) current.getHeight();
					double xdif = e.getX() - click.getX();
					double ydif = e.getY() - click.getY();
					if (xdif == 0 && ydif == 0) current = original;
					else
					{
						if (Math.abs(xdif) < Math.abs(ydif)) ydif = xdif / (imw / imh);
						else xdif = ydif * (imw / imh);
						BufferedImage temp = new BufferedImage((int) (imw + xdif), (int) (imh + ydif), BufferedImage.TYPE_INT_RGB);
						Graphics2D g = temp.createGraphics();
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						g.drawImage(original, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, original.getWidth(), original.getHeight(), null);
						g.dispose();
						current = temp;
					}
					SSFrame.this.setSize(current.getWidth() + 4, current.getHeight() + 4);
					repaint();
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseMoved(MouseEvent e)
			{
				if (suspend) return;
				if (inResizeBlock(e))
				{
					resize = true;
					SSFrame.this.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
				}
				else
				{
					resize = false;
					SSFrame.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void mouseDragged(MouseEvent e)
			{
				if (suspend) return;
				if (e.isControlDown())
				{
					if (click != null)
					{
						boxes.add(new DrawnBox(click, e.getPoint()));
						click = null;
					}
					else boxes.get(boxes.size() - 1).newDest(e.getPoint());
					repaint();
				}
				else if (click != null)
				{
					if (resize) SSFrame.this.setSize(e.getX(), e.getY());
					else setLocation(e.getXOnScreen() - click.x, e.getYOnScreen() - click.y);
				}
			}
		});
		setSize(new Dimension(current.getWidth() + 4, current.getHeight() + 4));
		Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[Main.monitor].getDefaultConfiguration().getBounds();
		setLocation(r.x, r.y);
		setUndecorated(true);
		setVisible(true);
		setAlwaysOnTop(true);
		setAlwaysOnTop(false);
	}

	public void inactivate()
	{
		active = false;
		setVisible(false);
		Main.rem(this);
	}

	public void activate()
	{
		active = true;
		setVisible(true);
	}

	public boolean isActive()
	{
		return active;
	}

	public void setVisible(boolean v)
	{
		if (active || !v) super.setVisible(v);
	}

	private BufferedImage getScreen()
	{
		BufferedImage tr = new BufferedImage(current.getWidth(), current.getHeight(), BufferedImage.TYPE_INT_RGB);
		imgPane.paint(tr.getGraphics());
		return tr;
	}

	public void paint(Graphics g)
	{
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g.setColor(Color.WHITE);
		g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
		imgPane.repaint();
	}

	public void saveImage()
	{
		File tw;
		int i = 0;
		do tw = new File(System.getenv("USERPROFILE") + "\\Desktop\\" + (++i) + ".png");
		while (tw.exists());
		try
		{
			tw.createNewFile();
			ImageIO.write(getScreen(), "png", tw);
		} catch (Exception e) {}
	}

	private boolean inResizeBlock(MouseEvent e)
	{
		int xmax = getX() + getWidth();
		int ymax = getY() + getHeight();
		return e.getXOnScreen() >= xmax - 10 && e.getXOnScreen() <= xmax && e.getYOnScreen() >= ymax - 10 && e.getYOnScreen() <= ymax;
	}

	private void moveTo(int x, int y, boolean s)
	{
		if (s) suspend = true;
		try {new Robot().mouseMove(x, y);} catch (Exception e) {}
		suspend = false;
	}
}