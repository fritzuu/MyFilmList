import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.imageio.ImageIO;

public class ActorCard extends JPanel {
    private Actor actor;
    private Image profileImg;
    private final int W = 160;
    private final int H = 220;

    public ActorCard(Actor a, Runnable onClick) {
        this.actor = a;
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(24, 24, 27)); // BG_MAIN
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { onClick.run(); }
            public void mouseEntered(MouseEvent e) { setBackground(new Color(39, 39, 42)); repaint(); }
            public void mouseExited(MouseEvent e) { setBackground(new Color(24, 24, 27)); repaint(); }
        });

        // Load Gambar
        new Thread(() -> {
            try {
                URL u = new URL(APIService.IMAGE_BASE_URL + a.profilePath);
                profileImg = ImageIO.read(u);
                SwingUtilities.invokeLater(this::repaint);
            } catch (Exception e) {}
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bingkai Foto (Circle / Rounded)
        int imgSize = 140;
        int imgX = (W - imgSize) / 2;
        
        if (profileImg != null) {
            Shape clip = new java.awt.geom.Ellipse2D.Float(imgX, 10, imgSize, imgSize);
            g2.setClip(clip);
            g2.drawImage(profileImg, imgX, 10, imgSize, imgSize, null);
            g2.setClip(null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.fillOval(imgX, 10, imgSize, imgSize);
        }

        // Nama Aktor
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        String name = actor.name;
        // Potong nama kalau kepanjangan
        if (fm.stringWidth(name) > W - 10) name = name.substring(0, 15) + "...";
        int nameX = (W - fm.stringWidth(name)) / 2;
        g2.drawString(name, nameX, 180);
    }
}