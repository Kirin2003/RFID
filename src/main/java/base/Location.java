package base;

/**
 * A class which records the virtual locations of tags and readers
 */
public class Location {

    private double x;
    private double y;

    public Location(){}
    public Location(double x , double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    @Override
    public String toString() {
        return "Location [x=" + x + ", y=" + y + "]";
    }
}
