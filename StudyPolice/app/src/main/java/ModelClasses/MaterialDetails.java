package ModelClasses;

public class MaterialDetails {
    public int id;
    public String name;
    public int classroom;
    public String the_file;
    public String date_uploaded;

    public MaterialDetails() {
    }

    public MaterialDetails(int id, String name, int classroom, String the_file, String date_uploaded) {
        this.id = id;
        this.name = name;
        this.classroom = classroom;
        this.the_file = the_file;
        this.date_uploaded = date_uploaded;
    }
}
