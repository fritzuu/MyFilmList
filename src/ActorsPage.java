import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ActorsPage extends JPanel {
    private MyFilmList parentFrame;
    private JPanel gridPanel;
    
    // Komponen Search
    private JTextField searchField;
    private JButton searchButton;

    public ActorsPage(MyFilmList parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(24, 24, 27));

        // HEADER
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        header.setBackground(new Color(24, 24, 27));
        
        JButton btnBack = new UIComponents.ModernButton("← Back", new Color(39, 39, 42), Color.GRAY);
        
        // Setup Search Field
        searchField = new UIComponents.ModernField();
        searchField.setPreferredSize(new Dimension(300, 40));
        
        // Setup Search Button
        searchButton = new UIComponents.ModernButton("Search Actor", new Color(99, 102, 241), new Color(79, 70, 229));
        
        header.add(btnBack);
        header.add(searchField);
        header.add(searchButton);

        // GRID
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 25));
        gridPanel.setBackground(new Color(24, 24, 27));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // --- ACTIONS ---
        btnBack.addActionListener(e -> parentFrame.showHomePage());
        
        // Tombol Search diklik
        searchButton.addActionListener(e -> performSearch());
        
        // Tekan ENTER di keyboard
        searchField.addActionListener(e -> performSearch());

        // Load Data Awal (Popular)
        loadPopularActors();
    }

    private void loadPopularActors() {
        gridPanel.removeAll();
        gridPanel.repaint();
        
        new Thread(() -> {
            try {
                List<Actor> actors = APIService.getPopularActors();
                SwingUtilities.invokeLater(() -> populateGrid(actors));
            } catch(Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void performSearch() {
        String q = searchField.getText().trim();
        
        // Jika kosong, balik ke Popular
        if(q.isEmpty()) {
            loadPopularActors();
            return;
        }

        searchButton.setText("Searching...");
        searchButton.setEnabled(false);
        gridPanel.removeAll();
        gridPanel.repaint();

        new Thread(() -> {
            try {
                List<Actor> actors = APIService.searchActors(q);
                SwingUtilities.invokeLater(() -> {
                    if(actors.isEmpty()) {
                        JLabel lbl = new JLabel("No actor found for '" + q + "'");
                        lbl.setForeground(Color.WHITE);
                        lbl.setFont(new Font("SansSerif", Font.PLAIN, 18));
                        gridPanel.add(lbl);
                    } else {
                        populateGrid(actors);
                    }
                    searchButton.setText("Search Actor");
                    searchButton.setEnabled(true);
                    
                    gridPanel.revalidate();
                    gridPanel.repaint();
                });
            } catch(Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    searchButton.setText("Search Actor");
                    searchButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void populateGrid(List<Actor> actors) {
        gridPanel.removeAll();
        int rows = (int) Math.ceil(actors.size() / 6.0); 
        gridPanel.setPreferredSize(new Dimension(1100, rows * 240));

        for(Actor a : actors) {
            ActorCard card = new ActorCard(a, () -> parentFrame.showActorProfile(a));
            gridPanel.add(card);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }
}