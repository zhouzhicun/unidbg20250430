package zz.app.adjust;

public class Sensor {

    String name;
    String vendor;
    int version;
    long type;
    double maxRange;

    double resolution;
    double power;
    long minDelay;


    public Sensor(String name, String vendor, int version, long type, double maxRange, double resolution, double power, long minDelay) {
        this.name = name;
        this.vendor = vendor;
        this.version = version;
        this.type = type;
        this.maxRange = maxRange;
        this.resolution = resolution;
        this.power = power;
        this.minDelay = minDelay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(double maxRange) {
        this.maxRange = maxRange;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public long getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(long minDelay) {
        this.minDelay = minDelay;
    }
}
