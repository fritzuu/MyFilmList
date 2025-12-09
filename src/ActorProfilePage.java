import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ActorProfilePage extends JPanel {
    private MyFilmList parentFrame;
    private JPanel gridPanel;
    private JLabel lblActorName;

    public ActorProfilePage(MyFilmList parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(24, 24, 27));

        // HEADER
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        header.setBackground(new Color(39, 39, 42)); // Warna Header beda dikit
        
        JButton btnBack = new UIComponents.ModernButton("← Back to Actors", new Color(24, 24, 27), Color.GRAY);
        
        lblActorName = new JLabel("Actor Name");
        lblActorName.setForeground(new Color(99, 102, 241)); // Warna Aksen
        lblActorName.setFont(new Font("SansSerif", Font.BOLD, 24));
        
        header.add(btnBack);
        header.add(lblActorName);

        // GRID FILM
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        gridPanel.setBackground(new Color(24, 24, 27));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        btnBack.addActionListener(e -> parentFrame.showActorsPage());
    }

    // Dipanggil oleh MyFilmList saat mau menampilkan halaman ini
    public void loadActorMovies(Actor actor) {
        lblActorName.setText(actor.name + "'s Movies");
        gridPanel.removeAll();
        gridPanel.repaint();
        
        JLabel loading = new JLabel("Loading movies...", SwingConstants.CENTER);
        loading.setForeground(Color.GRAY);
        gridPanel.add(loading);
        gridPanel.revalidate();

        new Thread(() -> {
            try {
                List<Movie> movies = APIService.getMoviesByActor(actor.id);
                SwingUtilities.invokeLater(() -> {
                    gridPanel.removeAll();
                    if(movies.isEmpty()) {
                        gridPanel.add(new JLabel("No movies found."));
                    } else {
                        int rows = (int) Math.ceil(movies.size() / 5.0);
                        gridPanel.setPreferredSize(new Dimension(1100, rows * 360));
                        
                        for(Movie m : movies) {
                            MovieCard card = new MovieCard(m, () -> parentFrame.showDetailPopup(m));
                            gridPanel.add(card);
                        }
                    }
                    gridPanel.revalidate();
                    gridPanel.repaint();
                });
            } catch(Exception e) { e.printStackTrace(); }
        }).start();
    }
}