import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class UIComponents {
    
    // 1. BUTTON KEREN
    public static class ModernButton extends JButton {
        Color col, hov;
        public ModernButton(String t, Color c, Color h) {
            super(t); this.col = c; this.hov = h;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBackground(col);
            addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){ if(isEnabled()) setBackground(hov); repaint(); }
                public void mouseExited(MouseEvent e){ if(isEnabled()) setBackground(col); repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.setColor(getForeground()); 
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-4)/2); 
            g2.dispose();
        }
    }

    // 2. INPUT FIELD KEREN
    public static class ModernField extends JTextField {
        public ModernField() {
            super(20); setOpaque(false); setForeground(Color.WHITE); setCaretColor(Color.WHITE);
            setFont(new Font("SansSerif", Font.PLAIN, 16)); setBorder(new EmptyBorder(10, 15, 10, 15));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(39, 39, 42)); // Warna BG Card
            g2.fillRoundRect(0,0,getWidth(),getHeight(), 10, 10);
            super.paintComponent(g); g2.dispose();
        }
    }

    // 3. PANEL GAMBAR (Untuk Detail)
    public static class PosterPanel extends JPanel {
        private Image img;
        public void setImage(Image i) { this.img = i; repaint(); }
        public void reset() { this.img = null; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(img != null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}