import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.imageio.ImageIO;

public class MovieCard extends JPanel {
    private Movie movie;
    private Image posterImg;
    private final int W = 200;
    
    // Warna kita hardcode atau ambil dari Main, disini hardcode dulu biar simpel
    private final Color BG_MAIN = new Color(24, 24, 27);
    private final Color BG_CARD = new Color(39, 39, 42);
    private final Color STAR_COLOR = new Color(245, 197, 24);

    public MovieCard(Movie m, Runnable onClick) {
        this.movie = m;
        setPreferredSize(new Dimension(W, 340));
        setBackground(BG_MAIN);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Event klik
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onClick.run(); }
            public void mouseEntered(MouseEvent e) { setBackground(BG_CARD); repaint(); }
            public void mouseExited(MouseEvent e) { setBackground(BG_MAIN); repaint(); }
        });

        // Load Gambar Kecil (Async)
        new Thread(() -> {
            try {
                // Mengambil URL dari APIService
                URL u = new URL(APIService.IMAGE_BASE_URL + m.posterPath);
                posterImg = ImageIO.read(u);
                SwingUtilities.invokeLater(this::repaint);
            } catch (Exception e) {}
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background placeholder
        g2.setColor(BG_CARD);
        g2.fillRoundRect(0, 0, W, 300, 10, 10);

        // Gambar Poster
        if (posterImg != null) {
            Shape clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, W, 300, 10, 10);
            g2.setClip(clip);
            g2.drawImage(posterImg, 0, 0, W, 300, null);
            g2.setClip(null);
        }

        // Overlay Rating
        g2.setColor(new Color(0,0,0, 180));
        g2.fillRoundRect(8, 8, 55, 24, 12, 12);
        g2.setColor(STAR_COLOR);
        g2.drawString("★ " + movie.vote, 15, 25);

        // Judul
        g2.setColor(new Color(250, 250, 250));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String title = movie.title;
        if(title.length() > 22) title = title.substring(0, 20) + "...";
        
        FontMetrics fm = g2.getFontMetrics();
        int x = (W - fm.stringWidth(title)) / 2;
        g2.drawString(title, x, 325);
    }
}