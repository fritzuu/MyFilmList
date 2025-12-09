import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class WatchlistPage extends JPanel {

    private MyFilmList parentFrame;
    private JPanel gridPanel;
    private JLabel statsLabel; // Label untuk statistik

    public WatchlistPage(MyFilmList parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(24, 24, 27));

        // -- HEADER --
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(39, 39, 42)); 
        header.setBorder(new EmptyBorder(15, 20, 15, 20)); 

        // KIRI: Tombol Back + Judul
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftHeader.setOpaque(false);
        
        JButton btnBack = new UIComponents.ModernButton("← Back", new Color(24, 24, 27), Color.GRAY);
        btnBack.setPreferredSize(new Dimension(80, 35));
        
        JLabel title = new JLabel("My Collection");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        leftHeader.add(btnBack);
        leftHeader.add(title);

        // KANAN: Stats + Export
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightHeader.setOpaque(false);

        // Statistik Label (Contoh: "Watched: 2/5")
        statsLabel = new JLabel("Watched: 0/0");
        statsLabel.setForeground(new Color(200, 200, 200));
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statsLabel.setBorder(new EmptyBorder(0, 0, 0, 20)); // Jarak ke tombol export

        JButton btnExport = new JButton("📄 Export");
        btnExport.setBackground(new Color(24, 24, 27));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setPreferredSize(new Dimension(100, 35));

        rightHeader.add(statsLabel);
        rightHeader.add(btnExport);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        // -- GRID --
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 30)); // Gap vertical dibesarkan untuk checkbox
        gridPanel.setBackground(new Color(24, 24, 27));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // -- EVENTS --
        btnBack.addActionListener(e -> parentFrame.showHomePage());
        btnExport.addActionListener(e -> exportWatchlist());
    }

    public void refreshData() {
        gridPanel.removeAll();
        List<Movie> data = parentFrame.getWatchlistData();

        int watchedCount = 0;

        if (data.isEmpty()) {
            JLabel lbl = new JLabel("Your watchlist is empty.");
            lbl.setForeground(Color.GRAY);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
            lbl.setBorder(new EmptyBorder(50, 50, 0, 0));
            gridPanel.add(lbl);
        } else {
            // Hitung ukuran panel
            int rows = (int) Math.ceil(data.size() / 5.0); 
            // Tinggi dibesarkan sedikit (+ 40px per baris) untuk muat checkbox
            gridPanel.setPreferredSize(new Dimension(1100, Math.max(800, rows * 400))); 

            for (Movie m : data) {
                if (m.isWatched) watchedCount++; // Hitung statistik
                
                // 1. Buat Wrapper Panel (Untuk menampung Kartu + Checkbox)
                JPanel wrapper = new JPanel();
                wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
                wrapper.setBackground(new Color(24, 24, 27));
                wrapper.setOpaque(false);
                
                // 2. Kartu Film
                MovieCard card = new MovieCard(m, () -> parentFrame.showDetailPopup(m));
                
                // Efek Visual: Jika sudah ditonton, bikin agak transparan (opsional)
                if (m.isWatched) {
                    for(Component c : card.getComponents()) c.setEnabled(false); // Efek dimmed sederhana
                }

                // 3. Checkbox "Mark as Watched"
                JCheckBox chkWatched = new JCheckBox("Sudah Ditonton");
                chkWatched.setForeground(m.isWatched ? new Color(74, 222, 128) : Color.GRAY); // Hijau jika true
                chkWatched.setFont(new Font("SansSerif", Font.BOLD, 12));
                chkWatched.setOpaque(false);
                chkWatched.setSelected(m.isWatched);
                chkWatched.setCursor(new Cursor(Cursor.HAND_CURSOR));
                chkWatched.setAlignmentX(Component.CENTER_ALIGNMENT); // Posisi tengah

                // Logika saat dicentang
                chkWatched.addActionListener(e -> {
                    m.isWatched = chkWatched.isSelected(); // Update data di object Movie
                    parentFrame.addToWatchlist(m); // Trigger save data (timpa data lama)
                    refreshData(); // Refresh UI untuk update warna & statistik
                });

                wrapper.add(card);
                wrapper.add(Box.createVerticalStrut(5)); // Jarak
                wrapper.add(chkWatched);

                gridPanel.add(wrapper);
            }
        }
        
        // Update Statistik Header
        statsLabel.setText("Watched: " + watchedCount + " / " + data.size());
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void exportWatchlist() {
        List<Movie> movies = parentFrame.getWatchlistData();
        if (movies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Watchlist kosong!", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("MyWatchlist.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) file = new File(file.getAbsolutePath() + ".txt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("MY WATCHLIST\n============\n\n");
                int no = 1;
                for (Movie m : movies) {
                    // Tambahkan status di file txt
                    String status = m.isWatched ? "[SUDAH DITONTON]" : "[BELUM]";
                    
                    writer.write(no + ". " + m.title + " " + status + "\n");
                    writer.write("   Rating: " + m.vote + "\n");
                    writer.write("--------------------\n");
                    no++;
                }
                JOptionPane.showMessageDialog(this, "Berhasil export!");
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}