import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class APIService {
    public static final String API_KEY = "c07e1adf4f0b59768847b8d40e64cbaf"; 
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"; 
    public static final String IMAGE_ORIGINAL_URL = "https://image.tmdb.org/t/p/original"; 

    private static final String BASE_URL_SEARCH = "https://api.themoviedb.org/3/search/movie";
    private static final String BASE_URL_POPULAR = "https://api.themoviedb.org/3/movie/popular";
    private static final String BASE_URL_PERSON_POPULAR = "https://api.themoviedb.org/3/person/popular";
    private static final String BASE_URL_SEARCH_PERSON = "https://api.themoviedb.org/3/search/person";
    private static final String BASE_URL_DISCOVER = "https://api.themoviedb.org/3/discover/movie";

    // --- MOVIE METHODS (UPDATED WITH PAGE) ---
    
    // 1. Search dengan Page
    public static List<Movie> getMovies(String q, int page) throws Exception {
        // Encode query + tambah parameter page
        String url = BASE_URL_SEARCH + "?api_key=" + API_KEY 
                     + "&query=" + java.net.URLEncoder.encode(q, "UTF-8") 
                     + "&page=" + page;
        return fetchData(url, "results");
    }
    
    // 2. Popular dengan Page
    public static List<Movie> getPopular(int page) throws Exception {
        return fetchData(BASE_URL_POPULAR + "?api_key=" + API_KEY + "&page=" + page, "results");
    }

    // 3. Genre dengan Page
    public static List<Movie> getMoviesByGenre(int genreId, int page) throws Exception {
        String url = BASE_URL_DISCOVER + "?api_key=" + API_KEY 
                     + "&with_genres=" + genreId 
                     + "&page=" + page;
        return fetchData(url, "results");
    }

    // --- EXISTING METHODS (UNCHANGED) ---
    
    public static List<Movie> getSimilarMovies(int movieId) throws Exception {
        return fetchData("https://api.themoviedb.org/3/movie/" + movieId + "/similar?api_key=" + API_KEY, "results");
    }

    public static String getTrailerKey(int movieId) throws Exception {
        String urlString = "https://api.themoviedb.org/3/movie/" + movieId + "/videos?api_key=" + API_KEY;
        String json = getJson(urlString);
        String[] parts = json.split("\\},\\{");
        for (String p : parts) {
            if (p.contains("\"site\":\"YouTube\"") && p.contains("\"type\":\"Trailer\"")) {
                return val(p, "\"key\":\"", "\"");
            }
        }
        return null;
    }

    public static List<Actor> getPopularActors() throws Exception {
        return fetchActorData(BASE_URL_PERSON_POPULAR + "?api_key=" + API_KEY);
    }

    public static List<Actor> searchActors(String query) throws Exception {
        String url = BASE_URL_SEARCH_PERSON + "?api_key=" + API_KEY + "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
        return fetchActorData(url);
    }

    public static List<Movie> getMoviesByActor(int actorId) throws Exception {
        String urlString = "https://api.themoviedb.org/3/person/" + actorId + "/movie_credits?api_key=" + API_KEY;
        return fetchData(urlString, "cast"); 
    }

    public static List<Actor> getMovieCast(int movieId) throws Exception {
        String urlString = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + API_KEY;
        return fetchActorData(urlString, "cast");
    }

    // --- HELPERS ---
    private static List<Actor> fetchActorData(String urlString, String arrayName) throws Exception {
        String json = getJson(urlString);
        List<Actor> res = new ArrayList<>();
        int arrayStart = json.indexOf("\"" + arrayName + "\":[");
        if (arrayStart == -1) return res;
        String arrayContent = json.substring(arrayStart);
        String[] parts = arrayContent.split("\\},\\{");
        for(String p : parts) {
            String name = val(p, "\"name\":\"", "\"");
            String path = val(p, "\"profile_path\":\"", "\"");
            String idStr = val(p, "\"id\":", ",");
            if(name != null && path != null && idStr != null) {
                try { res.add(new Actor(Integer.parseInt(idStr), name, path)); } catch(Exception e){}
            }
        }
        return res;
    }

    private static List<Actor> fetchActorData(String urlString) throws Exception {
        return fetchActorData(urlString, "results");
    }

    private static String getJson(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        if(c.getResponseCode()!=200) return "";
        Scanner s = new Scanner(url.openStream()); 
        StringBuilder sb = new StringBuilder();
        while(s.hasNext()) sb.append(s.nextLine()); 
        s.close();
        return sb.toString();
    }

    private static List<Movie> fetchData(String urlString, String arrayName) throws Exception {
        String json = getJson(urlString);
        List<Movie> res = new ArrayList<>();
        int arrayStart = json.indexOf("\"" + arrayName + "\":[");
        if (arrayStart == -1) return res;
        String arrayContent = json.substring(arrayStart);
        String[] parts = arrayContent.split("\\},\\{"); 
        for(String p : parts) {
            String t = val(p, "\"title\":\"", "\"");
            String path = val(p,"\"poster_path\":\"", "\"");
            String idStr = val(p, "\"id\":", ",");
            int id = 0;
            if(idStr != null) { try { id = Integer.parseInt(idStr); } catch(Exception e){} }

            if(t!=null && path != null && !path.equals("null")) {
                Movie m = new Movie(t, val(p,"\"overview\":\"", "\""), val(p,"\"release_date\":\"", "\""), valDouble(p), path);
                m.id = id;
                res.add(m);
            }
        }
        return res;
    }

    private static String val(String s, String start, String end) { 
        int i = s.indexOf(start); if(i<0) return null; i+=start.length(); int j = s.indexOf(end, i); 
        return j<0?null:s.substring(i,j).replace("\\\"", "\""); 
    }
    private static double valDouble(String s) { try { String v = val(s, "\"vote_average\":", ","); return v!=null?Double.parseDouble(v):0.0; } catch(Exception e){return 0.0;} }
}