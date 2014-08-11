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
	private static final ArrayList<KFrame> kfs = new ArrayList<>();
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
								add(new KFrame(im));
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
		for (KFrame kf : kfs) if (kf.isActive())
		{
			if (!kf.isVisible())
			{
				kf.setVisible(true);
				anyHidden = true;
			}
			else kf.toFront();
		}
		if (!anyHidden) for (KFrame kf : kfs) kf.setVisible(false);
	}

	public static void saveAll()
	{
		for (KFrame kf : kfs) if (kf.isActive()) kf.saveImage();
	}

	public static void activateAll()
	{
		for (KFrame kf : kfs) kf.activate();
	}

	public static void add(KFrame kf)
	{
		MAIN: while (kfs.size() >= MAX)
		{
			for (int i = kfs.size() - 1; i >= 0; ++i) if (kfs.get(i) != null && !kfs.get(i).isActive())
			{
				kfs.remove(i);
				continue MAIN;
			}
			break;
		}

		kfs.add(kf);
	}

	public static void rem(KFrame kf)
	{
		if (kfs.size() > MAX) kfs.remove(kf);
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