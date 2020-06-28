package ModelClasses;

public class GeneralShoutDetails {
    public int id;
    public ClassroomShortDetails classroom;
    public int user;
    public int seen;
    public int noti_type;
    public long time;

    public GeneralShoutDetails() {
    }

    public GeneralShoutDetails(int id, ClassroomShortDetails classroom, int user, int seen, int noti_type, long time) {
        this.id = id;
        this.classroom = classroom;
        this.user = user;
        this.seen = seen;
        this.noti_type = noti_type;
        this.time = time;
    }
}
