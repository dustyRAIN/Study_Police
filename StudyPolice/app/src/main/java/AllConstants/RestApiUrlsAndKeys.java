package AllConstants;

public class RestApiUrlsAndKeys {
    public static final String BASE_URL = "http://10.0.2.2:8000/";
    public static final String API_USER = BASE_URL + "users/";
    public static final String API_USER_EXISTS = API_USER + "exists/";
    public static final String API_USER_PROVIDER = API_USER + "provider/";
    public static final String API_USER_PROVIDER_TAKEN = API_USER_PROVIDER + "taken/";
    public static final String API_USER_DEMOUPLOAD = API_USER + "demoupload/";
    public static final String API_REST_AUTH = BASE_URL + "rest-auth/";
    public static final String API_USER_REGISTRATION = API_USER + "registration/";
    public static final String API_USER_GOOGLE = API_USER + "google/";
    public static final String API_USER_GOOGLE_LOGIN = API_USER_GOOGLE + "login/";
    public static final String API_REST_AUTH_LOGIN = API_REST_AUTH + "login/";
    public static final String API_REST_AUTH_LOGOUT = API_REST_AUTH + "logout/";
    public static final String API_REST_AUTH_USER = API_REST_AUTH + "user/";
    public static final String API_CLASSES = BASE_URL + "classes/";
    public static final String API_CLASSES_CREATE = API_CLASSES + "create/";
    public static final String API_CLASSES_CREATED = API_CLASSES + "created/";
    public static final String API_CLASSES_JOIN = API_CLASSES + "join/";
    public static final String API_CLASSES_JOINED = API_CLASSES + "joined/";
    public static final String API_CLASSES_STUDENTS = API_CLASSES + "students/";
    public static final String API_MATERIALS = BASE_URL + "materials/";
    public static final String API_MATERIALS_UPLOAD = API_MATERIALS + "upload/";
    public static final String API_MATERIALS_GET = API_MATERIALS + "get/";
    public static final String API_FACE = BASE_URL + "face/";
    public static final String API_FACE_MATCH = API_FACE + "match/";
    public static final String API_FACE_EXISTS = API_FACE + "exists/";
    public static final String API_FACE_POST = API_FACE + "post/";
    public static final String API_FACE_DELETE = API_FACE + "delete/";
    public static final String API_STATS = BASE_URL + "stats/";
    public static final String API_STATS_READTIME = API_STATS + "readtime/";
    public static final String API_STATS_READTIME_STUDENT = API_STATS_READTIME + "student/";
    public static final String API_STATS_READTIME_MATERIAL = API_STATS_READTIME + "material/";
    public static final String API_STATS_READTIME_ADD = API_STATS_READTIME + "add/";
    public static final String API_STATS_FREQUENCY = API_STATS + "frequency/";
    public static final String API_STATS_FREQUENCY_UPDATE = API_STATS_FREQUENCY + "update/";
    public static final String API_STATS_FREQUENCY_GET = API_STATS_FREQUENCY + "get/";
    public static final String API_MEDIA = BASE_URL + "media/";
    public static final String API_MEDIA_FACES = API_MEDIA + "Faces/";
    public static final String API_SHOUTS = BASE_URL + "shouts/";
    public static final String API_SHOUTS_ALL = API_SHOUTS + "all/";
    public static final String API_SHOUTS_SEEN = API_SHOUTS + "seen/";


    public static final String FORMAT_JSON = "?format=json";



    public static final String API_KEY_CSRFTOKEN = "csrftoken";
    public static final String API_KEY_SESSIONID = "sessionid";
}
