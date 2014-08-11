import java.awt.Point;

public class DrawnBox
{
	private Point src;
	public int x, y, w, h;

    public DrawnBox(Point a, Point b)
    {
	    src = a;
	    x = Math.min(src.x, b.x);
	    y = Math.min(src.y, b.y);
	    w = Math.max(src.x, b.x) - Math.min(src.x, b.x);
	    h = Math.max(src.y, b.y) - Math.min(src.y, b.y);
    }

	public void newDest(Point a)
	{
		x = Math.min(src.x, a.x);
		y = Math.min(src.y, a.y);
		w = Math.max(src.x, a.x) - Math.min(src.x, a.x);
		h = Math.max(src.y, a.y) - Math.min(src.y, a.y);
	}
}