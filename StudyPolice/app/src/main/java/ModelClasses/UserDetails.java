package ModelClasses;

public class UserDetails {
    public String name;
    public String email;
    public String password1;
    public String password2;
    public String gender;

    public UserDetails() {
    }

    public UserDetails(String name, String email, String password1, String password2, String gender) {
        this.name = name;
        this.email = email;
        this.password1 = password1;
        this.password2 = password2;
        this.gender = gender;
    }
}
