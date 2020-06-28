package ModelClasses;

public class JoinedClassDetails {
    public ClassAndCreatorDetails classroom;
    public int student;
    public String date_joined;

    public JoinedClassDetails() {
    }

    public JoinedClassDetails(ClassAndCreatorDetails classroom, int student, String date_joined) {
        this.classroom = classroom;
        this.student = student;
        this.date_joined = date_joined;
    }
}
