package main;

import main.tiles.TileBar;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static main.Main.*;

public class TileBarSettings extends JPanel {

    TileBar tileBar;
    JScrollPane barScroll;
    MapPanel mapPanel;

    static String collisionState;
    static String showCollisionState;
    static Options options;
    Border border = BorderFactory.createLineBorder(Color.YELLOW.darker().darker(), 5);

    public TileBarSettings(TileBar tileBar, JScrollPane barScroll, MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        this.tileBar = tileBar;
        this.barScroll = barScroll;
        this.setLayout(new GridLayout(2, 2));
        this.setDoubleBuffered(true);
        this.setBorder(border);
        this.setMinimumSize(new Dimension(0, 150));

        collisionState = "Off";
        showCollisionState = "Off";
        options = new Options(mapPanel);

        initializeTileBarSettings();
    }

    void initializeTileBarSettings() {

        JButton collision = new JButton("Collision: " + collisionState);
        collision.setBackground(new Color(182, 66, 66));
        collision.setFocusable(false);
        collision.setFocusPainted(false);
        collision.setVisible(true);
        collision.setFont(rockwell);
        collision.setForeground(Color.black);
        collision.setBorder(border);
        collision.setOpaque(true);
        collision.addActionListener(e -> {
            switch (collisionState) {
                case "Off":
                    collisionState = "On";
                    collision.setBackground(new Color(91, 182, 66));
                    break;
                case "On":
                    collisionState = "Off";
                    collision.setBackground(new Color(182, 66, 66));
                    break;
            }
            collision.setText("Collision: " + collisionState);
            mapPanel.repaint();
        });

        JButton showColl = new JButton("Show Collision: " + showCollisionState);
        showColl.setPreferredSize(new Dimension(250, 50));
        showColl.setBackground(new Color(182, 66, 66));
        showColl.setFocusable(false);
        showColl.setFocusPainted(false);
        showColl.setVisible(true);
        showColl.setFont(rockwell);
        showColl.setForeground(Color.black);
        showColl.setBorder(border);
        showColl.setOpaque(true);
        showColl.addActionListener(e -> {
            switch (showCollisionState) {
                case "Off":
                    showCollisionState = "On";
                    showColl.setBackground(new Color(91, 182, 66));
                    break;
                case "On":
                    showCollisionState = "Off";
                    showColl.setBackground(new Color(182, 66, 66));
                    break;
            }
            showColl.setText("Show Collision: " + showCollisionState);
            mapPanel.repaint();
        });

        JButton options = new JButton("Options");
        options.setPreferredSize(new Dimension(250, 50));
        options.setBackground(Color.ORANGE);
        options.setFocusable(false);
        options.setFocusPainted(false);
        options.setVisible(true);
        options.setFont(rockwell);
        options.setForeground(Color.black);
        options.setBorder(border);
        options.setOpaque(true);
        options.addActionListener(e -> {
//            UIManager.put("OptionPane.background",new ColorUIResource(new Color(255, 242, 0, 255)));
            JOptionPane.showOptionDialog(null, TileBarSettings.options,"Options", JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
        });

        String[] filterOptions = new String[]{
                "Everything",
                "Grass",
                "Grass Aesthetics",
                "Trees",
                "Leveled Grass",
                "Farming Aesthetics",
                "Fences",
                "Floors",
                "Walls",
                "Roofs",
                "Stairs and Ladders",
                "Building Aesthetics",
                "Doors",
                "Signs",
                "Containers",
                "Statues and Structures",
                "Market Supplies",
                "Water",
                "Shadows",
                "Miscellaneous"
        };

        JComboBox<String> filter = new JComboBox<>(filterOptions);
        filter.setBackground(Color.ORANGE);
        filter.setBorder(border);
        filter.setFont(new Font("Rockwell", Font.BOLD, 17));
        DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
        filter.setRenderer(defaultRenderer);
        filter.setFocusable(false);
        filter.addActionListener(e -> {
            switch (filter.getItemAt(filter.getSelectedIndex())) {
                case "Everything":
                    tileBar.add_EVERYTHING();
                    break;
                case "Grass":
                    tileBar.add_GRASS();
                    break;
                case "Grass Aesthetics":
                    tileBar.add_GRASS_AESTHETICS();
                    break;
                case "Trees":
                    tileBar.add_TREES();
                    break;
                case "Walls":
                    tileBar.add_WALLS();
                    break;
                case "Floors":
                    tileBar.add_FLOORS();
                    break;
                case "Roofs":
                    tileBar.add_ROOFS();
                    break;
                case "Stairs and Ladders":
                    tileBar.add_STAIRS_LADDERS();
                    break;
                case "Leveled Grass":
                    tileBar.add_LEVELED_GRASS();
                    break;
                case "Farming Aesthetics":
                    tileBar.add_FARMING_AESTHETICS();
                    break;
                case "Fences":
                    tileBar.add_FENCES();
                    break;
                case "Building Aesthetics":
                    tileBar.add_BUILDING_AESTHETICS();
                    break;
                case "Doors":
                    tileBar.add_DOORS();
                    break;
                case "Signs":
                    tileBar.add_SIGNS();
                    break;
                case "Containers":
                    tileBar.add_CONTAINERS();
                    break;
                case "Statues and Structures":
                    tileBar.add_STATUES_STRUCTURES();
                    break;
                case "Market Supplies":
                    tileBar.add_MARKET_SUPPLIES();
                    break;
                case "Water":
                    tileBar.add_WATER();
                    break;
                case "Shadows":
                    tileBar.add_SHADOWS();
                    break;
                case "Miscellaneous":
                    tileBar.add_OTHER();
                    break;
            }
            // this repaints the tiles. idfk know why repaint() doesn't
            // work but this is the only solution that I could find
            barScroll.getVerticalScrollBar().setValue(1);
            barScroll.getVerticalScrollBar().setValue(0);
        });
        this.add(filter);
        this.add(options);
        this.add(collision);
        this.add(showColl);
    }
}