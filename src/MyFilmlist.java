import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MyMovieLogApp
 * Aplikasi pencatat film ala MyAnimeList menggunakan TMDB API.
 * * Materi yang Diterapkan:
 * 1. Java GUI (Swing): JFrame, JPanel, JList, CardLayout, dll.
 * 2. Event Handling: ActionListener untuk tombol search dan save.
 * 3. Java I/O: Serialisasi objek untuk menyimpan Watchlist ke file lokal.
 * 4. Exceptions: Try-catch untuk menangani error jaringan dan file.
 * 5. String: Parsing JSON manual menggunakan String methods dan Regex.
 * 6. Collections & Generics: Menggunakan ArrayList<Movie> dan ListModel.
 * 7. Multithreading: Melakukan request API di thread terpisah agar GUI tidak macet.
 */
public class MyFilmlist extends JFrame {

    // --- KONFIGURASI API ---
    // PENTING: Ganti tulisan di bawah dengan API Key TMDB Anda sendiri!
    private static final String API_KEY = "GANTI_DENGAN_API_KEY_TMDB_ANDA"; 
    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w92"; // Ukuran gambar kecil

    // --- KOMPONEN GUI ---
    private JTextField searchField;
    private JButton searchButton, addToWatchlistButton, showWatchlistButton;
    private JList<Movie> movieJList;
    private DefaultListModel<Movie> listModel;
    private JTextArea detailsArea;
    private JLabel posterLabel;

    // --- DATA ---
    private List<Movie> watchlist;
    private final String WATCHLIST_FILE = "mymovielog_watchlist.dat";

    public MyFilmlist() {
        // Inisialisasi Data
        watchlist = new ArrayList<>();
        loadWatchlistData(); // Materi IO: Memuat data dari file

        // Setup Window (Materi GUI)
        setTitle("MyMovieLog - Movie Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- PANEL ATAS (Pencarian) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchField = new JTextField(30);
        searchButton = new JButton("Cari Film");
        showWatchlistButton = new JButton("Lihat Watchlist Saya");

        topPanel.add(new JLabel("Judul Film:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(showWatchlistButton);

        add(topPanel, BorderLayout.NORTH);

        // --- PANEL TENGAH (Split Pane: List & Details) ---
        listModel = new DefaultListModel<>();
        movieJList = new JList<>(listModel);
        movieJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(movieJList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Hasil Pencarian"));

        // Panel Detail
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Detail Film"));
        
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        posterLabel = new JLabel("Poster", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(100, 150));
        posterLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel contentDetailPanel = new JPanel(new BorderLayout());
        contentDetailPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        contentDetailPanel.add(posterLabel, BorderLayout.WEST);

        addToWatchlistButton = new JButton("Tambahkan ke Watchlist");
        addToWatchlistButton.setEnabled(false);

        detailsPanel.add(contentDetailPanel, BorderLayout.CENTER);
        detailsPanel.add(addToWatchlistButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, detailsPanel);
        splitPane.setDividerLocation(300);
        
        add(splitPane, BorderLayout.CENTER);

        // --- EVENT HANDLING (Materi Event Handling) ---
        
        // 1. Tombol Cari
        searchButton.addActionListener(e -> performSearch());
        
        // 2. Tombol Lihat Watchlist
        showWatchlistButton.addActionListener(e -> showWatchlist());

        // 3. Tombol Tambah ke Watchlist
        addToWatchlistButton.addActionListener(e -> addToWatchlist());

        // 4. Seleksi List
        movieJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Movie selected = movieJList.getSelectedValue();
                if (selected != null) {
                    displayMovieDetails(selected);
                    addToWatchlistButton.setEnabled(true);
                    // Cek jika sudah ada di watchlist
                    if (watchlist.contains(selected)) {
                        addToWatchlistButton.setText("Sudah di Watchlist");
                        addToWatchlistButton.setEnabled(false);
                    } else {
                        addToWatchlistButton.setText("Tambahkan ke Watchlist");
                        addToWatchlistButton.setEnabled(true);
                    }
                }
            }
        });
    }

    /**
     * Logika Pencarian menggunakan Multithreading.
     * Materi: Multithreading & Exceptions
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan judul film!");
            return;
        }

        if (API_KEY.equals("GANTI_DENGAN_API_KEY_TMDB_ANDA")) {
            JOptionPane.showMessageDialog(this, "API Key belum diatur! Silakan edit kodingan dan masukkan API Key TMDB Anda.");
            return;
        }

        // GUI Update sebelum thread jalan
        searchButton.setEnabled(false);
        listModel.clear();
        detailsArea.setText("Sedang mencari...");

        // Membuat Thread baru agar GUI tidak freeze saat request API
        Thread apiThread = new Thread(() -> {
            try {
                // Proses I/O Jaringan
                List<Movie> movies = MovieAPIService.searchMovies(query);

                // Update GUI kembali di Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    if (movies.isEmpty()) {
                        detailsArea.setText("Tidak ada film ditemukan.");
                    } else {
                        for (Movie m : movies) {
                            listModel.addElement(m);
                        }
                        detailsArea.setText("Pilih film untuk melihat detail.");
                    }
                    searchButton.setEnabled(true);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    detailsArea.setText("Error: " + ex.getMessage());
                    searchButton.setEnabled(true);
                    ex.printStackTrace();
                });
            }
        });

        apiThread.start();
    }

    private void displayMovieDetails(Movie m) {
        StringBuilder sb = new StringBuilder();
        sb.append("Judul: ").append(m.getTitle()).append("\n");
        sb.append("Rilis: ").append(m.getReleaseDate()).append("\n");
        sb.append("Rating: ").append(m.getVoteAverage()).append("/10").append("\n\n");
        sb.append("Sinopsis:\n").append(m.getOverview());
        detailsArea.setText(sb.toString());

        // (Opsional) Load Poster Image bisa ditambahkan di sini dengan threading terpisah
        posterLabel.setText("<html><div style='text-align: center;'>[Poster]<br>"+m.getTitle()+"</div></html>");
    }

    private void addToWatchlist() {
        Movie selected = movieJList.getSelectedValue();
        if (selected != null && !watchlist.contains(selected)) {
            watchlist.add(selected);
            saveWatchlistData(); // Simpan ke file
            JOptionPane.showMessageDialog(this, "Berhasil ditambahkan ke Watchlist!");
            addToWatchlistButton.setText("Sudah di Watchlist");
            addToWatchlistButton.setEnabled(false);
        }
    }

    private void showWatchlist() {
        listModel.clear();
        for (Movie m : watchlist) {
            listModel.addElement(m);
        }
        detailsArea.setText("Menampilkan Watchlist Anda.\nTotal: " + watchlist.size() + " film.");
        addToWatchlistButton.setEnabled(false);
    }

    /**
     * Menyimpan data Watchlist ke file lokal.
     * Materi: Java IO (ObjectOutputStream) & Exceptions
     */
    private void saveWatchlistData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(WATCHLIST_FILE))) {
            oos.writeObject(watchlist);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan watchlist: " + e.getMessage());
        }
    }

    /**
     * Memuat data Watchlist dari file lokal.
     * Materi: Java IO (ObjectInputStream) & Exceptions
     */
    @SuppressWarnings("unchecked")
    private void loadWatchlistData() {
        File f = new File(WATCHLIST_FILE);
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                watchlist = (ArrayList<Movie>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Gagal memuat watchlist: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Menjalankan GUI di thread yang aman
        SwingUtilities.invokeLater(() -> new MyFilmlist().setVisible(true));
    }

    // ==========================================================
    // INNER CLASS: MODEL DATA (Materi Generics & Collections)
    // ==========================================================
    
    // Implement Serializable agar bisa disimpan ke file (Materi Java IO)
    static class Movie implements Serializable {
        private String title;
        private String overview;
        private String releaseDate;
        private double voteAverage;
        private String posterPath;

        public Movie(String title, String overview, String releaseDate, double voteAverage, String posterPath) {
            this.title = title;
            this.overview = overview;
            this.releaseDate = releaseDate;
            this.voteAverage = voteAverage;
            this.posterPath = posterPath;
        }

        public String getTitle() { return title; }
        public String getOverview() { return overview; }
        public String getReleaseDate() { return releaseDate; }
        public double getVoteAverage() { return voteAverage; }
        
        // Override toString agar tampil rapi di JList
        @Override
        public String toString() {
            return title + " (" + (releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : "?") + ")";
        }

        // Override equals untuk pengecekan duplikasi di watchlist
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Movie movie = (Movie) o;
            return title.equals(movie.title) && releaseDate.equals(movie.releaseDate);
        }
    }

    // ==========================================================
    // INNER CLASS: API SERVICE (Materi Java IO & String)
    // ==========================================================
    static class MovieAPIService {

        public static List<Movie> searchMovies(String query) throws Exception {
            List<Movie> movies = new ArrayList<>();
            
            // Encode query agar URL valid (mengganti spasi dengan %20, dll)
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String urlString = BASE_URL + "?api_key=" + API_KEY + "&query=" + encodedQuery;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }

            // Membaca respon API (JSON) ke dalam String
            // Materi: Java IO (Scanner/BufferedReader)
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            // Parsing JSON Manual (Materi String & Regex)
            // Karena tidak pakai library Gson/Jackson, kita parse manual stringnya.
            // Format JSON TMDB: { "results": [ { "title": "...", ... }, ... ] }
            String jsonResponse = inline.toString();

            // Sederhanakan parsing dengan Regex
            // Mencari pola: "title":"(.*?)" ... "release_date":"(.*?)"
            // Ini adalah pendekatan sederhana untuk tugas kuliah. 
            // Untuk produksi, WAJIB pakai library JSON.
            
            // Kita split berdasarkan objek kurung kurawal pembuka "{" yang menandakan objek film baru
            String[] rawObjects = jsonResponse.split("\\},\\{");

            for (String rawObj : rawObjects) {
                String title = extractValue(rawObj, "\"title\":\"", "\"");
                String releaseDate = extractValue(rawObj, "\"release_date\":\"", "\"");
                String overview = extractValue(rawObj, "\"overview\":\"", "\"");
                String posterPath = extractValue(rawObj, "\"poster_path\":\"", "\"");
                String voteStr = extractValue(rawObj, "\"vote_average\":", ",");

                // Bersihkan unicode escape sequence sederhana jika ada
                if (title != null) title = unescapeJavaString(title);
                if (overview != null) overview = unescapeJavaString(overview);

                if (title != null && !title.isEmpty()) {
                    double vote = 0.0;
                    try {
                        if (voteStr != null) vote = Double.parseDouble(voteStr);
                    } catch (NumberFormatException e) { /* ignore */ }
                    
                    if (releaseDate == null) releaseDate = "Unknown";
                    
                    movies.add(new Movie(title, overview, releaseDate, vote, posterPath));
                }
            }

            return movies;
        }

        // Metode Helper Manual Parsing JSON (Materi String)
        private static String extractValue(String source, String prefix, String suffix) {
            int start = source.indexOf(prefix);
            if (start == -1) return null;
            start += prefix.length();
            
            int end = source.indexOf(suffix, start);
            if (end == -1) return null;
            
            return source.substring(start, end);
        }

        // Helper untuk membersihkan teks JSON sederhana
        private static String unescapeJavaString(String st) {
            // Ganti escape char JSON standar
            return st.replace("\\\"", "\"").replace("\\n", "\n");
        }
    }
}