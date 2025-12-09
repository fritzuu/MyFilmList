import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
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
    private String currentMode = "POPULAR"; // "POPULAR", "GENRE", "SEARCH"
    private int currentGenreId = 0;
    private String currentQuery = "";
    // -----------------------------

    private final Map<String, Integer> genres = new LinkedHashMap<>();

    public HomePage(MyFilmList parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(24, 24, 27)); 

        // Init Genre ID
        genres.put("🔥 Popular", 0); 
        genres.put("💥 Action", 28);
        genres.put("😂 Comedy", 35);
        genres.put("👻 Horror", 27);
        genres.put("💕 Romance", 10749);
        genres.put("🤖 Sci-Fi", 878);
        genres.put("🎨 Animation", 16);

        // -- HEADER WRAPPER --
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(24, 24, 27));

        // 1. SEARCH BAR AREA
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

        // 2. CATEGORY BUTTONS AREA
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

        // 3. TITLE LABEL AREA
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 5));
        titlePanel.setBackground(new Color(24, 24, 27));
        titleLabel = new JLabel("What's Popular");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        topContainer.add(searchPanel);
        topContainer.add(categoryPanel);
        topContainer.add(titlePanel);

        // -- GRID --
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        gridPanel.setBackground(new Color(24, 24, 27));
        
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // -- FOOTER (PAGINATION PANEL) --
        // UPDATE 1: Ganti warna background footer agar beda dengan grid
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        footerPanel.setBackground(new Color(39, 39, 42)); // Warna sedikit lebih terang
        footerPanel.setOpaque(true);
        // Opsional: Garis pemisah tipis di atas footer
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)));
        
        btnPrev = new UIComponents.ModernButton("< Prev", new Color(24, 24, 27), Color.GRAY);
        btnNext = new UIComponents.ModernButton("Next >", new Color(99, 102, 241), new Color(79, 70, 229));
        
        lblPageIndicator = new JLabel("Page 1");
        lblPageIndicator.setForeground(Color.WHITE);
        lblPageIndicator.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        btnPrev.setPreferredSize(new Dimension(100, 40));
        btnNext.setPreferredSize(new Dimension(100, 40));
        
        // Logic Tombol Pagination
        btnPrev.addActionListener(e -> changePage(-1)); 
        btnNext.addActionListener(e -> changePage(1));  

        footerPanel.add(btnPrev);
        footerPanel.add(lblPageIndicator);
        footerPanel.add(btnNext);
        
        add(footerPanel, BorderLayout.SOUTH); 

        // -- ACTIONS --
        searchField.addActionListener(e -> performSearch());
        searchButton.addActionListener(e -> performSearch());
        navActorsButton.addActionListener(e -> parentFrame.showActorsPage());
        navWatchlistButton.addActionListener(e -> parentFrame.showWatchlistPage());

        loadPopular(); 
        updatePaginationButtons(); 
    }

    private void changePage(int increment) {
        if (isLoading) return; // Cegah double click
        
        int nextPage = currentPage + increment;
        if (nextPage < 1) return; 
        
        currentPage = nextPage;
        loadCurrentPageData(); 
    }

    private void loadCurrentPageData() {
        isLoading = true;
        lblPageIndicator.setText("Loading...");
        
        // UPDATE 2: Matikan tombol saat loading
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
                        
                        // UPDATE 3: Scroll Paksa ke Atas!
                        scrollPane.getVerticalScrollBar().setValue(0);
                        
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
        
        // Styling tombol disabled
        if (currentPage == 1) btnPrev.setBackground(Color.DARK_GRAY);
        else btnPrev.setBackground(new Color(24, 24, 27));
        
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
        if(q.isEmpty()) { 
            resetToHome(); 
            return; 
        }
        
        resetCategoryButtons(); 
        titleLabel.setText("Results for: \"" + q + "\"");

        currentMode = "SEARCH";
        currentQuery = q;
        currentPage = 1; 

        loadCurrentPageData();
    }

    private void populateGrid(List<Movie> movies) {
        gridPanel.removeAll();
        int rows = (int) Math.ceil(movies.size() / 5.0); 
        
        // UPDATE 4: Tambah buffer +50px agar baris terakhir tidak ketutupan footer
        gridPanel.setPreferredSize(new Dimension(1100, (rows * 360) + 50)); 
        
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