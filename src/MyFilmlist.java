import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URL; 

public class MyFilmList extends JFrame {

    // DATA MODEL
    private List<Movie> watchlistData;
    private final String FILE_NAME = "mymovielog_watchlist.dat";

    // VIEWS
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private HomePage homePage;
    private WatchlistPage watchlistPage;
    private ActorsPage actorsPage;
    private ActorProfilePage actorProfilePage;

    public MyFilmList() {
        // 1. Load Data
        watchlistData = new ArrayList<>();
        loadData();

        // 2. Setup Window
        setTitle("MyFilmList - Full Discovery");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ============================================================
        // UPDATE LOGO APLIKASI (SUPPORT MAC DOCK)
        // ============================================================
        try {
            // Mengarah ke folder src/logo/logo.png
            URL iconURL = getClass().getResource("/logo/logo.png");
            
            if (iconURL != null) {
                Image icon = Toolkit.getDefaultToolkit().getImage(iconURL);
                
                // 1. Ubah icon di Title Bar (Windows/Linux/Mac Window)
                setIconImage(icon); 
                
                // 2. KHUSUS MAC OS: Ubah icon besar di Dock
                // Kita gunakan try-catch agar tidak error jika dijalankan di Windows lama
                try {
                    // Cek apakah OS mendukung fitur Taskbar (Java 9+)
                    if (Taskbar.isTaskbarSupported()) {
                        Taskbar.getTaskbar().setIconImage(icon);
                    }
                } catch (UnsupportedOperationException e) {
                    System.out.println("Fitur ubah icon Dock tidak didukung di OS ini (Aman, diabaikan).");
                } catch (SecurityException e) {
                    System.out.println("Izin akses Taskbar ditolak.");
                }

            } else {
                System.err.println("Gagal load gambar! Cek apakah file ada di src/logo/logo.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ============================================================

        // 3. Setup Navigation
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Init Halaman
        homePage = new HomePage(this);
        watchlistPage = new WatchlistPage(this);
        actorsPage = new ActorsPage(this);            
        actorProfilePage = new ActorProfilePage(this); 

        // Register ke CardLayout
        mainPanel.add(homePage, "HOME");
        mainPanel.add(watchlistPage, "WATCHLIST");
        mainPanel.add(actorsPage, "ACTORS");           
        mainPanel.add(actorProfilePage, "ACTOR_PROFILE"); 

        add(mainPanel);
    }

    // --- NAVIGATION ---
    public void showHomePage() {
        cardLayout.show(mainPanel, "HOME");
    }

    public void showWatchlistPage() {
        watchlistPage.refreshData(); 
        cardLayout.show(mainPanel, "WATCHLIST");
    }

    public void showActorsPage() {
        cardLayout.show(mainPanel, "ACTORS");
    }

    public void showActorProfile(Actor actor) {
        actorProfilePage.loadActorMovies(actor); 
        cardLayout.show(mainPanel, "ACTOR_PROFILE");
    }

    // --- POPUP DETAIL ---
    public void showDetailPopup(Movie m) {
        new MovieDetailDialog(this, m).setVisible(true);
    }

    // --- DATA CONTROLLER ---
    public List<Movie> getWatchlistData() { return watchlistData; }
    public boolean isInWatchlist(Movie m) { return watchlistData.contains(m); }

    public void addToWatchlist(Movie m) {
        if (!watchlistData.contains(m)) {
            watchlistData.add(m);
            saveData();
        }
    }

    public void removeFromWatchlist(Movie m) {
        watchlistData.remove(m);
        saveData();
        watchlistPage.refreshData(); 
    }

    // --- SAVE / LOAD ---
    private void saveData() { 
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) { 
            o.writeObject(watchlistData); 
        } catch (Exception e) {} 
    }
    @SuppressWarnings("unchecked")
    private void loadData() { 
        File f = new File(FILE_NAME); 
        if(f.exists()) { 
            try (ObjectInputStream i = new ObjectInputStream(new FileInputStream(f))) { 
                watchlistData = (ArrayList<Movie>) i.readObject(); 
            } catch (Exception e) {} 
        } 
    }

    public static void main(String[] args) {
        // Baris ini untuk membuat tampilan Mac lebih native (gelap)
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
        System.setProperty("awt.useSystemAAFontSettings", "on"); 
        
        SwingUtilities.invokeLater(() -> new MyFilmList().setVisible(true));
    }
}