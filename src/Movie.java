import java.io.Serializable;

public class Movie implements Serializable {
    // Tambahkan ID dan variable baru isWatched
    int id; 
    String title, overview, releaseDate, posterPath; 
    double vote;
    
    // BARU: Status sudah ditonton atau belum (Default false)
    public boolean isWatched = false; 

    public Movie(String t, String o, String r, double v, String p) { 
        this.title = t; 
        this.overview = o; 
        this.releaseDate = r; 
        this.vote = v; 
        this.posterPath = p; 
    }

    public String toString() { return title; }

    @Override
    public boolean equals(Object o) { 
        if(o instanceof Movie) return title.equals(((Movie)o).title); 
        return false;
    }
}