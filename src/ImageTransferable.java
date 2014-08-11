import java.awt.Image;
import java.awt.datatransfer.*;
import java.io.IOException;

public class ImageTransferable implements Transferable
{
	private Image im;

	public ImageTransferable(Image i)
	{
		im = i;
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[]{DataFlavor.imageFlavor};
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor == DataFlavor.imageFlavor;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (isDataFlavorSupported(flavor)) return im;
		throw new UnsupportedFlavorException(flavor);
	}
}
