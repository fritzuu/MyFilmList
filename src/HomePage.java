import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class HomePage extends JPanel {

    private MyFilmList parentFrame; 
    private JPanel gridPanel;
    private JTextField searchField;
    private JButton searchButton, navActorsButton, navWatchlistButton;
    private JPanel categoryPanel; 
    private JLabel titleLabel; 
    private JScrollPane scrollPane; 

    // --- PAGINATION UI & STATE ---
    private JButton btnPrev, btnNext;
    private JLabel lblPageIndicator;
    
    private int currentPage = 1;
    private boolean isLoading = false;
    private String currentMode = "POPULAR"; 
    private int currentGenreId = 0;
    private String currentQuery = "";
    // -----------------------------

    private final Map<String, Integer> genres = new LinkedHashMap<>();

    public HomePage(MyFilmList parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(24, 24, 27)); 

        genres.put("🔥 Popular", 0); 
        genres.put("💥 Action", 28);
        genres.put("😂 Comedy", 35);
        genres.put("👻 Horror", 27);
        genres.put("💕 Romance", 10749);
        genres.put("🤖 Sci-Fi", 878);
        genres.put("🎨 Animation", 16);

        // -- HEADER WRAPPER (Wadah Utama Bagian Atas) --
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(24, 24, 27));

        // 1. SEARCH BAR
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        searchPanel.setBackground(new Color(24, 24, 27));

        searchField = new UIComponents.ModernField();
        searchField.setPreferredSize(new Dimension(300, 45));
        
        searchButton = new UIComponents.ModernButton("Search", new Color(99, 102, 241), new Color(79, 70, 229));
        navActorsButton = new UIComponents.ModernButton("Actors", new Color(39, 39, 42), Color.GRAY);
        navWatchlistButton = new UIComponents.ModernButton("My Watchlist", new Color(39, 39, 42), Color.GRAY);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(navActorsButton); 
        searchPanel.add(navWatchlistButton);

        // 2. CATEGORY BUTTONS
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        categoryPanel.setBackground(new Color(24, 24, 27));
        categoryPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); 

        for (String genreName : genres.keySet()) {
            JButton btn = new CategoryButton(genreName);
            int id = genres.get(genreName);
            btn.addActionListener(e -> {
                resetCategoryButtons(); 
                btn.setBackground(new Color(99, 102, 241)); 
                if (id == 0) titleLabel.setText("What's Popular");
                else titleLabel.setText("Genre: " + genreName);
                loadMoviesByGenre(id);
            });
            if (id == 0) btn.setBackground(new Color(99, 102, 241));
            categoryPanel.add(btn);
        }

        // 3. TITLE LABEL
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5));
        titlePanel.setBackground(new Color(24, 24, 27));
        titleLabel = new JLabel("What's Popular");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // 4. PAGINATION PANEL (PINDAH KE ATAS SINI!)
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5)); // Rata Kanan biar rapi
        paginationPanel.setBackground(new Color(24, 24, 27));
        paginationPanel.setBorder(new EmptyBorder(0, 0, 10, 25)); // Jarak dikit
        
        btnPrev = new UIComponents.ModernButton("<", new Color(39, 39, 42), Color.GRAY); // Tombol kecil aja
        btnNext = new UIComponents.ModernButton(">", new Color(99, 102, 241), new Color(79, 70, 229));
        
        lblPageIndicator = new JLabel("Page 1");
        lblPageIndicator.setForeground(Color.WHITE);
        lblPageIndicator.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPageIndicator.setBorder(new EmptyBorder(0, 10, 0, 10)); // Jarak teks
        
        btnPrev.setPreferredSize(new Dimension(50, 35));
        btnNext.setPreferredSize(new Dimension(50, 35));
        
        btnPrev.addActionListener(e -> changePage(-1)); 
        btnNext.addActionListener(e -> changePage(1));  

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageIndicator);
        paginationPanel.add(btnNext);

        // GABUNGKAN SEMUA KE TOP CONTAINER
        topContainer.add(searchPanel);
        topContainer.add(categoryPanel);
        // Kita bikin baris baru untuk Judul (Kiri) dan Pagination (Kanan) biar sejajar
        JPanel titleAndPageContainer = new JPanel(new BorderLayout());
        titleAndPageContainer.setBackground(new Color(24, 24, 27));
        titleAndPageContainer.add(titlePanel, BorderLayout.WEST);
        titleAndPageContainer.add(paginationPanel, BorderLayout.EAST);
        
        topContainer.add(titleAndPageContainer);

        // -- GRID PANEL --
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        gridPanel.setBackground(new Color(24, 24, 27));
        
        // Padding Bawah Normal saja (tidak perlu tebal-tebal lagi)
        gridPanel.setBorder(new EmptyBorder(10, 20, 50, 20)); 
        
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(topContainer, BorderLayout.NORTH); // HEADER DI ATAS
        add(scrollPane, BorderLayout.CENTER);  // SCROLL DI TENGAH (Penuhi sisa layar)
        // Bagian SOUTH (Footer) dihapus total karena pagination sudah di atas.

        // -- ACTIONS --
        searchField.addActionListener(e -> performSearch());
        searchButton.addActionListener(e -> performSearch());
        navActorsButton.addActionListener(e -> parentFrame.showActorsPage());
        navWatchlistButton.addActionListener(e -> parentFrame.showWatchlistPage());

        loadPopular(); 
        updatePaginationButtons(); 
    }

    // --- LOGIC STANDARD (Gak perlu hitung tinggi aneh-aneh lagi) ---
    
    private void changePage(int increment) {
        if (isLoading) return; 
        int nextPage = currentPage + increment;
        if (nextPage < 1) return; 
        currentPage = nextPage;
        loadCurrentPageData(); 
    }

    private void loadCurrentPageData() {
        isLoading = true;
        lblPageIndicator.setText("Loading...");
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        
        gridPanel.removeAll();
        gridPanel.repaint();

        new Thread(() -> {
            try {
                List<Movie> movies = null;
                switch (currentMode) {
                    case "POPULAR":
                        movies = APIService.getPopular(currentPage);
                        break;
                    case "GENRE":
                        movies = APIService.getMoviesByGenre(currentGenreId, currentPage);
                        break;
                    case "SEARCH":
                        movies = APIService.getMovies(currentQuery, currentPage);
                        break;
                }

                List<Movie> finalMovies = movies;
                SwingUtilities.invokeLater(() -> {
                    if (finalMovies != null && !finalMovies.isEmpty()) {
                        populateGrid(finalMovies);
                        // Reset scroll ke atas saat ganti halaman
                        if (scrollPane != null) scrollPane.getVerticalScrollBar().setValue(0);
                    } else {
                        gridPanel.add(new JLabel("No more movies."));
                        gridPanel.revalidate();
                        gridPanel.repaint();
                    }
                    isLoading = false;
                    updatePaginationButtons();
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    isLoading = false;
                    updatePaginationButtons();
                });
            }
        }).start();
    }

    private void updatePaginationButtons() {
        lblPageIndicator.setText("Page " + currentPage);
        btnPrev.setEnabled(currentPage > 1);
        if (currentPage == 1) btnPrev.setBackground(Color.DARK_GRAY);
        else btnPrev.setBackground(new Color(39, 39, 42));
        btnNext.setEnabled(true);
    }

    private void resetCategoryButtons() {
        for (Component c : categoryPanel.getComponents()) {
            if (c instanceof JButton) {
                c.setBackground(new Color(39, 39, 42)); 
            }
        }
    }

    private void resetToHome() {
        searchField.setText("");
        titleLabel.setText("What's Popular");
        for (Component c : categoryPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                if (btn.getText().contains("Popular")) btn.setBackground(new Color(99, 102, 241));
                else btn.setBackground(new Color(39, 39, 42));
            }
        }
        loadPopular();
    }

    private void loadMoviesByGenre(int id) {
        currentGenreId = id;
        currentPage = 1; 
        if (id == 0) currentMode = "POPULAR";
        else currentMode = "GENRE";
        loadCurrentPageData();
    }

    private void loadPopular() {
        loadMoviesByGenre(0);
    }

    private void performSearch() {
        String q = searchField.getText().trim();
        if(q.isEmpty()) { resetToHome(); return; }
        
        resetCategoryButtons(); 
        titleLabel.setText("Results for: \"" + q + "\"");
        currentMode = "SEARCH";
        currentQuery = q;
        currentPage = 1; 
        loadCurrentPageData();
    }

    private void populateGrid(List<Movie> movies) {
        gridPanel.removeAll();
        
        // KEMBALIKAN LOGIKA TINGGI PANEL KE "AUTO" (FLOWLAYOUT STANDARD)
        // Kita cukup set lebar, tinggi biar ngikutin konten (0)
        // FlowLayout memang agak tricky di ScrollPane, jadi kita set PreferredSize manual tapi simpel
        
        int rows = (int) Math.ceil(movies.size() / 5.0); // Asumsi kasar
        // Kasih ruang agak banyak (misal 5 kolom jadi 4 baris, tapi kita siapin buat 5 baris biar aman)
        // Kali ini aman karena di bawah GRID tidak ada footer yang menghalangi!
        gridPanel.setPreferredSize(new Dimension(1000, rows * 380)); 
        
        for (Movie m : movies) {
            MovieCard card = new MovieCard(m, () -> parentFrame.showDetailPopup(m));
            gridPanel.add(card);
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    class CategoryButton extends JButton {
        public CategoryButton(String text) {
            super(text);
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(Color.WHITE); 
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(new Color(39, 39, 42)); 
            setOpaque(true);
            setPreferredSize(new Dimension(120, 35)); 
        }
    }
}