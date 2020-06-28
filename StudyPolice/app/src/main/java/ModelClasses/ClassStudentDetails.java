package ModelClasses;

public class ClassStudentDetails {
    public int classroom;
    public UserShortDetails student;

    public ClassStudentDetails() {
    }

    public ClassStudentDetails(int classroom, UserShortDetails student) {
        this.classroom = classroom;
        this.student = student;
    }
}
