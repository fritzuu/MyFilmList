import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class MyFilmList extends JFrame {

    // KONFIGURASI API
    private static final String API_KEY = "c07e1adf4f0b59768847b8d40e64cbaf"; 
    
    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";
    // Menggunakan ukuran w185 agar poster terlihat lebih jelas
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/original"; 

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

    public MyFilmList() {
        // Inisialisasi Data
        watchlist = new ArrayList<>();
        loadWatchlistData(); 

        // Setup Window
        setTitle("MyFilmList");
        setSize(900, 600); // Ukuran sedikit diperlebar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // PANEL ATAS
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchField = new JTextField(30);
        searchButton = new JButton("Cari Film");
        showWatchlistButton = new JButton("Lihat Watchlist Saya");

        topPanel.add(new JLabel("Judul Film:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(showWatchlistButton);

        add(topPanel, BorderLayout.NORTH);

        // PANEL TENGAH (Split Pane)
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
        
        // Label Poster
        posterLabel = new JLabel("Poster", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(185, 278)); // Ukuran standar poster
        posterLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(Color.LIGHT_GRAY); // Warna background saat loading

        JPanel contentDetailPanel = new JPanel(new BorderLayout());
        contentDetailPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        
        // Membungkus poster dengan panel agar posisinya rapi di kiri
        JPanel posterPanelWrapper = new JPanel(new BorderLayout());
        posterPanelWrapper.add(posterLabel, BorderLayout.NORTH);
        contentDetailPanel.add(posterPanelWrapper, BorderLayout.WEST);

        addToWatchlistButton = new JButton("Tambahkan ke Watchlist");
        addToWatchlistButton.setEnabled(false);

        detailsPanel.add(contentDetailPanel, BorderLayout.CENTER);
        detailsPanel.add(addToWatchlistButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, detailsPanel);
        splitPane.setDividerLocation(300);
        
        add(splitPane, BorderLayout.CENTER);

        // EVENT HANDLING
        searchButton.addActionListener(e -> performSearch());
        showWatchlistButton.addActionListener(e -> showWatchlist());
        addToWatchlistButton.addActionListener(e -> addToWatchlist());

        movieJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Movie selected = movieJList.getSelectedValue();
                if (selected != null) {
                    displayMovieDetails(selected);
                    
                    // Cek status watchlist
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

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan judul film!");
            return;
        }

        searchButton.setEnabled(false);
        listModel.clear();
        detailsArea.setText("Sedang mencari...");

        // Thread untuk request API
        Thread apiThread = new Thread(() -> {
            try {
                List<Movie> movies = MovieAPIService.searchMovies(query);
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
                    ex.printStackTrace(); // Cek console untuk detail error
                });
            }
        });
        apiThread.start();
    }

    
     // Menampilkan detail film DAN memuat gambar poster secara asinkron

    private void displayMovieDetails(Movie m) {
        StringBuilder sb = new StringBuilder();
        sb.append("Judul: ").append(m.getTitle()).append("\n");
        sb.append("Rilis: ").append(m.getReleaseDate()).append("\n");
        sb.append("Rating: ").append(m.getVoteAverage()).append("/10").append("\n\n");
        sb.append("Sinopsis:\n").append(m.getOverview());
        detailsArea.setText(sb.toString());

        // Reset Poster ke Loading State
        posterLabel.setIcon(null); 
        posterLabel.setText("Loading Image...");
        
        // Proses Download Gambar Poster di Thread Baru
        // Ini agar GUI tidak macet saat download gambar
        if (m.getPosterPath() != null && !m.getPosterPath().equals("null") && !m.getPosterPath().isEmpty()) {
            new Thread(() -> {
                try {
                    // Konstruksi URL Gambar
                    String imageUrl = IMAGE_BASE_URL + m.getPosterPath();
                    URL url = new URL(imageUrl);
                    
                    // Baca gambar dari internet
                    Image image = ImageIO.read(url);
                    
                    if (image != null) {
                        // Resize gambar agar pas dengan label (Opsional tapi disarankan)
                        // w185 pixel lebar, tinggi proporsional sekitar 278
                        Image scaledImage = image.getScaledInstance(185, 278, Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(scaledImage);

                        // Update GUI harus di Event Dispatch Thread
                        SwingUtilities.invokeLater(() -> {
                            posterLabel.setText(""); // Hapus teks loading
                            posterLabel.setIcon(icon);
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        posterLabel.setText("Gagal Muat Gambar");
                        System.err.println("Gagal load gambar: " + e.getMessage());
                    });
                }
            }).start();
        } else {
            posterLabel.setText("Tidak ada poster");
        }
    }

    private void addToWatchlist() {
        Movie selected = movieJList.getSelectedValue();
        if (selected != null && !watchlist.contains(selected)) {
            watchlist.add(selected);
            saveWatchlistData();
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
        posterLabel.setIcon(null);
        posterLabel.setText("Pilih film");
        addToWatchlistButton.setEnabled(false);
    }

    private void saveWatchlistData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(WATCHLIST_FILE))) {
            oos.writeObject(watchlist);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan watchlist: " + e.getMessage());
        }
    }

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
        SwingUtilities.invokeLater(() -> new MyFilmList().setVisible(true));
    }

    // INNER CLASS: MODEL DATA
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
        public String getPosterPath() { return posterPath; } // Getter baru untuk poster
        
        @Override
        public String toString() {
            return title + " (" + (releaseDate.length() >= 4 ? releaseDate.substring(0, 4) : "?") + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Movie movie = (Movie) o;
            return title.equals(movie.title) && releaseDate.equals(movie.releaseDate);
        }
    }

    // INNER CLASS: API SERVICE
    static class MovieAPIService {
        public static List<Movie> searchMovies(String query) throws Exception {
            List<Movie> movies = new ArrayList<>();
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
            String urlString = BASE_URL + "?api_key=" + API_KEY + "&query=" + encodedQuery;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }

            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            String jsonResponse = inline.toString();
            String[] rawObjects = jsonResponse.split("\\},\\{");

            for (String rawObj : rawObjects) {
                String title = extractValue(rawObj, "\"title\":\"", "\"");
                String releaseDate = extractValue(rawObj, "\"release_date\":\"", "\"");
                String overview = extractValue(rawObj, "\"overview\":\"", "\"");
                String posterPath = extractValue(rawObj, "\"poster_path\":\"", "\""); // Ambil path poster
                String voteStr = extractValue(rawObj, "\"vote_average\":", ",");

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

        private static String extractValue(String source, String prefix, String suffix) {
            int start = source.indexOf(prefix);
            if (start == -1) return null;
            start += prefix.length();
            int end = source.indexOf(suffix, start);
            if (end == -1) return null;
            return source.substring(start, end);
        }

        private static String unescapeJavaString(String st) {
            return st.replace("\\\"", "\"").replace("\\n", "\n");
        }
    }
}