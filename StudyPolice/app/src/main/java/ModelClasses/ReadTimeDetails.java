package ModelClasses;

public class ReadTimeDetails {
    public int id;
    public String name;
    public long duration;

    public ReadTimeDetails() {
    }

    public ReadTimeDetails(int id, String name, long duration) {
        this.id = id;
        this.name = name;
        this.duration = duration;
    }
}
