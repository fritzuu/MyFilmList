import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

public class MovieDetailDialog extends JDialog {

    private MyFilmList controller;
    private Movie movie;
    private JPanel infoContentPanel;

    public MovieDetailDialog(MyFilmList owner, Movie m) {
        super(owner, m.title, true);
        this.controller = owner;
        this.movie = m;

        setSize(950, 700); // Perbesar lagi biar muat Cast
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout());

        // --- KIRI: POSTER ---
        UIComponents.PosterPanel p = new UIComponents.PosterPanel();
        p.setPreferredSize(new Dimension(320, 700));
        new Thread(() -> {
            try {
                URL u = new URL(APIService.IMAGE_ORIGINAL_URL + m.posterPath);
                Image i = ImageIO.read(u);
                SwingUtilities.invokeLater(() -> p.setImage(i));
            } catch(Exception e){}
        }).start();

        // --- KANAN: SCROLLABLE INFO ---
        infoContentPanel = new JPanel();
        infoContentPanel.setLayout(new BoxLayout(infoContentPanel, BoxLayout.Y_AXIS));
        infoContentPanel.setBackground(new Color(24, 24, 27));
        infoContentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JScrollPane scrollInfo = new JScrollPane(infoContentPanel);
        scrollInfo.setBorder(null);
        scrollInfo.getVerticalScrollBar().setUnitIncrement(16);
        scrollInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // 1. DATA FILM
        addMovieInfo(m);
        
        // 2. TOMBOL AKSI
        JPanel btnPanel = createButtonsPanel();
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoContentPanel.add(Box.createVerticalStrut(20));
        infoContentPanel.add(btnPanel);

        // 3. CAST SECTION (BARU)
        addCastSection();

        // 4. SIMILAR MOVIES
        addSimilarMoviesSection();

        add(p, BorderLayout.WEST);
        add(scrollInfo, BorderLayout.CENTER);
    }

    private void addMovieInfo(Movie m) {
        JLabel lblTitle = new JLabel("<html>" + m.title + "</html>");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDate = new JLabel("Released: " + m.releaseDate);
        lblDate.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDate.setForeground(new Color(161, 161, 170));
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVote = new JLabel("⭐ Rating: " + m.vote + " / 10");
        lblVote.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblVote.setForeground(new Color(245, 197, 24));
        lblVote.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtDesc = new JTextArea(m.overview);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setEditable(false);
        txtDesc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txtDesc.setForeground(new Color(220, 220, 220));
        txtDesc.setBackground(new Color(24, 24, 27));
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoContentPanel.add(lblTitle);
        infoContentPanel.add(Box.createVerticalStrut(10));
        infoContentPanel.add(lblDate);
        infoContentPanel.add(Box.createVerticalStrut(5));
        infoContentPanel.add(lblVote);
        infoContentPanel.add(Box.createVerticalStrut(20));
        infoContentPanel.add(txtDesc);
    }

    // --- CAST SECTION (FITUR BARU) ---
    private void addCastSection() {
        JLabel lblHeader = new JLabel("Top Cast");
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel castContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        castContainer.setBackground(new Color(24, 24, 27));
        castContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoContentPanel.add(Box.createVerticalStrut(30));
        infoContentPanel.add(lblHeader);
        infoContentPanel.add(castContainer);

        new Thread(() -> {
            try {
                List<Actor> cast = APIService.getMovieCast(movie.id);
                SwingUtilities.invokeLater(() -> {
                    if (cast.isEmpty()) {
                        JLabel empty = new JLabel("No cast info.");
                        empty.setForeground(Color.GRAY);
                        castContainer.add(empty);
                    } else {
                        // Ambil 5 aktor teratas
                        int limit = Math.min(cast.size(), 5);
                        for(int i=0; i<limit; i++) {
                            castContainer.add(new SmallActorCard(cast.get(i)));
                        }
                    }
                    castContainer.revalidate();
                    castContainer.repaint();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void addSimilarMoviesSection() {
        JLabel lblHeader = new JLabel("You Might Also Like");
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel similarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        similarPanel.setBackground(new Color(24, 24, 27));
        similarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoContentPanel.add(Box.createVerticalStrut(30));
        infoContentPanel.add(lblHeader);
        infoContentPanel.add(similarPanel);

        new Thread(() -> {
            try {
                List<Movie> similars = APIService.getSimilarMovies(movie.id);
                SwingUtilities.invokeLater(() -> {
                    if (similars.isEmpty()) {
                        JLabel empty = new JLabel("No recommendations.");
                        empty.setForeground(Color.GRAY);
                        similarPanel.add(empty);
                    } else {
                        int limit = Math.min(similars.size(), 4);
                        for (int i = 0; i < limit; i++) {
                            similarPanel.add(new SmallMovieCard(similars.get(i)));
                        }
                    }
                    similarPanel.revalidate();
                    similarPanel.repaint();
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(new Color(24, 24, 27));
        panel.setMaximumSize(new Dimension(600, 50));

        JButton btnTrailer = new UIComponents.ModernButton("▶ Watch Trailer", Color.WHITE, new Color(220, 220, 220));
        btnTrailer.setForeground(Color.BLACK);
        
        btnTrailer.addActionListener(e -> {
            btnTrailer.setText("Opening...");
            btnTrailer.setEnabled(false);
            new Thread(() -> {
                try {
                    String k = APIService.getTrailerKey(movie.id);
                    if(k!=null) Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v="+k));
                    else SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Trailer not found!"));
                } catch(Exception ex){}
                SwingUtilities.invokeLater(() -> { btnTrailer.setText("▶ Watch Trailer"); btnTrailer.setEnabled(true); });
            }).start();
        });

        boolean isFav = controller.isInWatchlist(movie);
        JButton btnWatchlist = new UIComponents.ModernButton(
            isFav ? "Remove Watchlist" : "Add Watchlist", 
            isFav ? new Color(220, 38, 38) : new Color(99, 102, 241), 
            isFav ? new Color(185, 28, 28) : new Color(79, 70, 229)
        );
        
        btnWatchlist.addActionListener(e -> {
            if(controller.isInWatchlist(movie)) {
                controller.removeFromWatchlist(movie);
                dispose();
            } else {
                controller.addToWatchlist(movie);
                btnWatchlist.setText("Remove Watchlist");
                btnWatchlist.setBackground(new Color(220, 38, 38));
            }
        });

        panel.add(btnTrailer);
        panel.add(btnWatchlist);
        return panel;
    }

    // --- INNER CLASS: KARTU FILM KECIL ---
    class SmallMovieCard extends JPanel {
        private Image img;
        public SmallMovieCard(Movie m) {
            setPreferredSize(new Dimension(100, 160));
            setBackground(new Color(24, 24, 27));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(m.title);

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    dispose(); // Tutup dialog ini
                    controller.showDetailPopup(m); // Buka dialog baru
                }
            });

            new Thread(() -> {
                try {
                    URL u = new URL(APIService.IMAGE_BASE_URL + m.posterPath);
                    img = ImageIO.read(u);
                    SwingUtilities.invokeLater(this::repaint);
                } catch(Exception e){}
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(img != null) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(img, 0, 0, 100, 150, null);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String t = movie.title.length() > 12 ? movie.title.substring(0,10)+".." : movie.title;
        }
    }

    // --- INNER CLASS: KARTU AKTOR KECIL (BULAT) ---
    class SmallActorCard extends JPanel {
        private Image img;
        private Actor actor;
        public SmallActorCard(Actor a) {
            this.actor = a;
            setPreferredSize(new Dimension(90, 120)); // Size pas untuk foto + nama
            setBackground(new Color(24, 24, 27));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(a.name);

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    dispose(); // Tutup dialog
                    controller.showActorProfile(a); // Buka profil aktor
                }
            });

            new Thread(() -> {
                try {
                    URL u = new URL(APIService.IMAGE_BASE_URL + a.profilePath);
                    img = ImageIO.read(u);
                    SwingUtilities.invokeLater(this::repaint);
                } catch(Exception e){}
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Foto Bulat
            int size = 80;
            if(img != null) {
                Shape clip = new java.awt.geom.Ellipse2D.Float(5, 0, size, size);
                g2.setClip(clip);
                g2.drawImage(img, 5, 0, size, size, null);
                g2.setClip(null);
            } else {
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(5, 0, size, size);
            }

            // Nama Aktor
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            String n = actor.name;
            if(n.length() > 12) n = n.substring(0, 10) + "..";
            int x = (getWidth() - fm.stringWidth(n)) / 2;
            g2.drawString(n, x, 95);
        }
    }
}