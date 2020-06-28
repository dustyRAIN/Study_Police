package ModelClasses;

public class ContentFrequencyDetails {
    public int id;
    public int content_id;
    public int content_type;
    public int user;
    public int frequency;
    public String name;

    public ContentFrequencyDetails() {
    }

    public ContentFrequencyDetails(int id, int content_id, int content_type, int user, int frequency, String name) {
        this.id = id;
        this.content_id = content_id;
        this.content_type = content_type;
        this.user = user;
        this.frequency = frequency;
        this.name = name;
    }
}
