package ModelClasses;

public class User {
    public int id;
    public String email;
    public String name;
    public int gender;
    public String image;

    public User() {
    }

    public User(int id, String email, String name, int gender, String image) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.image = image;
    }
}
