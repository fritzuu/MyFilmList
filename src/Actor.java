import java.io.Serializable;

public class Actor implements Serializable {
    int id;
    String name;
    String profilePath;

    public Actor(int id, String name, String profilePath) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
    }
}