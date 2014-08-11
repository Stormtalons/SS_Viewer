import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
	public static File ssFolder;
	public static File ini = new File("SSViewer.ini");
	public static int monitor = 0;
	public static final byte MAX = 20;
	private static final ArrayList<SSFrame> ssfs = new ArrayList<>();
	public static boolean suspended = false;
    public static void main(String[] args) throws Exception
    {
	    PopupMenu pop = new PopupMenu();
		final MenuItem suspendResume = new MenuItem("Suspend");
		suspendResume.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				suspended = !suspended;
				suspendResume.setLabel(suspended ? "Resume" : "Suspend");
			}
		});
		pop.add(suspendResume);
	    MenuItem save = new MenuItem("Save All");
	    save.addActionListener(new ActionListener()
	    {
		    public void actionPerformed(ActionEvent e)
		    {
			    saveAll();
		    }
	    });
	    pop.add(save);
	    MenuItem hist = new MenuItem("Show Last " + MAX);
	    hist.addActionListener(new ActionListener()
	    {
		    public void actionPerformed(ActionEvent e)
		    {
			    activateAll();
		    }
	    });
	    pop.add(hist);
		MenuItem openIni = new MenuItem("Open INI");
		openIni.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (ini.exists()) try {Desktop.getDesktop().open(ini);} catch (Exception e0) {}
			}
		});
		pop.add(openIni);
	    MenuItem iniLoad = new MenuItem("Reload INI");
	    iniLoad.addActionListener(new ActionListener()
	    {
		    public void actionPerformed(ActionEvent e)
		    {
			    loadIni();
		    }
	    });
	    pop.add(iniLoad);
	    MenuItem exit = new MenuItem("Exit");
	    exit.addActionListener(new ActionListener()
	    {
		    public void actionPerformed(ActionEvent e)
		    {
			    System.exit(0);
		    }
	    });
	    pop.add(exit);
	    TrayIcon ico;
	    if (new File("eye.png").exists()) ico = new TrayIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage("eye.png")).getImage(), "", pop);
	    else ico = new TrayIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("eye.png"))).getImage(), "", pop);
	    ico.addMouseListener(new MouseAdapter()
	    {
		    public void mousePressed(MouseEvent e)
		    {
				if (e.getButton() == MouseEvent.BUTTON1) toggle();
		    }
	    });
	    SystemTray.getSystemTray().add(ico);
	    loadIni();
        while (true)
        {
	        if (ssFolder == null) return;
	        if (!ssFolder.exists()) return;
	        for (File ss : ssFolder.listFiles())
	        {
		        if (!ss.getName().endsWith(".jpg")) continue;
		        try
		        {
			        RandomAccessFile raf = new RandomAccessFile(ss.getAbsolutePath(), "rw");
			        FileLock fl = raf.getChannel().tryLock();
			        if (fl != null)
			        {
				        raf.close();
						if (!suspended)
						{
				        	BufferedImage im = ImageIO.read(ss);
				        	if (im != null)
								add(new SSFrame(im));
						}
				        ss.delete();
			        }
		        } catch (Exception e) {break;}
	        }
	        Thread.sleep(50);
        }
    }

	public static void toggle()
	{
		boolean anyHidden = false;
		for (SSFrame ssf : ssfs) if (ssf.isActive())
		{
			if (!ssf.isVisible())
			{
				ssf.setVisible(true);
				anyHidden = true;
			}
			else ssf.toFront();
		}
		if (!anyHidden) for (SSFrame ssf : ssfs) ssf.setVisible(false);
	}

	public static void saveAll()
	{
		for (SSFrame ssf : ssfs) if (ssf.isActive()) ssf.saveImage();
	}

	public static void activateAll()
	{
		for (SSFrame ssf : ssfs) ssf.activate();
	}

	public static void add(SSFrame ssf)
	{
		MAIN: while (ssfs.size() >= MAX)
		{
			for (int i = ssfs.size() - 1; i >= 0; ++i) if (ssfs.get(i) != null && !ssfs.get(i).isActive())
			{
				ssfs.remove(i);
				continue MAIN;
			}
			break;
		}

		ssfs.add(ssf);
	}

	public static void rem(SSFrame ssf)
	{
		if (ssfs.size() > MAX) ssfs.remove(ssf);
	}

	public static void loadIni()
	{
		try
		{
			if (!ini.exists()) ini.createNewFile();
			Scanner in = new Scanner(ini);
			boolean foundFolder = false;
			while (in.hasNextLine())
			{
				String[] s = in.nextLine().split("=");
				if (s.length != 2) continue;
				if (s[0].equalsIgnoreCase("folder"))
				{
					foundFolder = true;
					ssFolder = new File(s[1]);
					if (!ssFolder.exists())
					{
						if (JOptionPane.showConfirmDialog(null, "Directory at '" + s[1] + "' does not exist. Would you like to create it?") == JOptionPane.OK_OPTION)
							ssFolder.mkdirs();
						else
							ssFolder = null;
					}
					else if (!ssFolder.isDirectory())
						ssFolder = null;
				}
				else if (s[0].equalsIgnoreCase("monitor"))
				{
					try
					{
						monitor = Integer.parseInt(s[1]);
					} catch (Exception e) {}
				}
			}
			in.close();
			if (!foundFolder) JOptionPane.showMessageDialog(null, "No screenshot folder was found in SSViewer.ini. Please add a 'folder=' record.");
		} catch (Exception e) {}
	}
}