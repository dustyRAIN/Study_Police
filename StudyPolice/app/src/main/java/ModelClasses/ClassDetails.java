package ModelClasses;

public class ClassDetails {
    public int id;
    public String name;
    public String access_code;
    public String date_created;
    public int creator;

    public ClassDetails() {
    }

    public ClassDetails(int id, String name, String access_code, String date_created, int creator) {
        this.id = id;
        this.name = name;
        this.access_code = access_code;
        this.date_created = date_created;
        this.creator = creator;
    }
}
