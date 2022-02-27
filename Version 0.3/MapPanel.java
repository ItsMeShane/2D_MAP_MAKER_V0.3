package main;

import main.tiles.TileBar;
import main.tiles.TileList;

import static main.Layers.*;

import static main.TileBarSettings.*;
import static java.awt.Color.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.Stack;

public class MapPanel extends JPanel {

    // default map dimensions
    int maxMapTilesX = 100;
    int maxMapTilesY = 100;

    // default empty map size (based on map dimensions)
    public StringBuilder emptyLayer = new StringBuilder();

    {
        for (int i = 0; i < maxMapTilesY; i++) {
            for (int ii = 0; ii < maxMapTilesX; ii++)
                emptyLayer.append("0 ");
            emptyLayer.append("\n");
        }
    }

    // integer array holding tile values
    int[][] L1_mapTileCoordinate = new int[maxMapTilesX][maxMapTilesY];
    int[][] L2_mapTileCoordinate = new int[maxMapTilesX][maxMapTilesY];
    int[][] L3_mapTileCoordinate = new int[maxMapTilesX][maxMapTilesY];
    int[][] L4_mapTileCoordinate = new int[maxMapTilesX][maxMapTilesY];

    // basically the same as above but as a String
    StringBuilder L1_mapStringLine = new StringBuilder(emptyLayer);
    StringBuilder L2_mapStringLine = new StringBuilder(emptyLayer);
    StringBuilder L3_mapStringLine = new StringBuilder(emptyLayer);
    StringBuilder L4_mapStringLine = new StringBuilder(emptyLayer);

    // sets location to draw tile (-1 if not drawing)
    int tileLocationX = -1;
    int tileLocationY = -1;

    // points used for drawing rectangles and selecting tiles
    Point pointStart = new Point(0, 0);
    Point pointEnd = new Point(0, 0);

    Stack<String> undo = new Stack<>();

    MouseInput mouseInput = new MouseInput(this);

    public Rectangle propertyBounds = new Rectangle(0, 0, maxMapTilesX * 2, maxMapTilesY * 2);

    Color lightGray = new Color(134, 134, 134);
    Color rectFill = new Color(20, 181, 224, 90);
    Color rectBorder = new Color(20, 181, 224);
    Color collision = new Color(224, 20, 20, 50);

    // the current zoom level (100 zoomScale means the image is shown in original size)
    public double zoom = 1000;
    public double zoomScale = 10;
    public double lastZoomScale = 0;

    public JScrollPane mapScroll;
    TileList tileList;
    TileBar tileBar;

    public MapPanel(JScrollPane mapScroll, TileList tileList, TileBar tileBar) throws IOException {
        this.mapScroll = mapScroll;
        this.tileList = tileList;
        this.tileBar = tileBar;
        this.setBackground(gray);
        this.setPreferredSize(new Dimension(maxMapTilesX * 150, maxMapTilesY * 150));
        this.addMouseListener(mouseInput);
        this.addMouseMotionListener(mouseInput);
        this.addMouseWheelListener(mouseInput);
        storeState();
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) Math.round(maxMapTilesX * zoomScale), (int) Math.round(maxMapTilesY * zoomScale));
    }

    public void alignViewPort(Point mousePosition) {
        // if the scale didn't change there is nothing we should do
        if (zoomScale != lastZoomScale) {
            // compute the factor by that the image zoom has changed
            double scaleChange = zoomScale / lastZoomScale;
            // compute the scaled mouse position
            Point scaledMousePosition = new Point((int) Math.round(mousePosition.x * scaleChange), (int) Math.round(mousePosition.y * scaleChange));
            // retrieve the current viewport position
            Point viewportPosition = mapScroll.getViewport().getViewPosition();
            // compute the new viewport position
            Point newViewportPosition = new Point(viewportPosition.x + scaledMousePosition.x - mousePosition.x, viewportPosition.y + scaledMousePosition.y - mousePosition.y);
            // update the viewport position
            mapScroll.getViewport().setViewPosition(newViewportPosition);
            // remember the last scale
            lastZoomScale = zoomScale;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.scale(zoomScale, zoomScale);
        g2d.setColor(collision);
        g2d.setStroke(new BasicStroke(0.04f));

        // draw tiles from number maps
        for (int row = 0; row < maxMapTilesY; row++)
            for (int file = 0; file < maxMapTilesX; file++) {
                // we don't paint tile if tile == 0 because 0 represents empty tile. this drastically improves performance
                // layer one
                if (showLayerOne && L1_mapTileCoordinate[file][row] != 0)
                    g2d.drawImage(tileList.tileID[L1_mapTileCoordinate[file][row]].image, file, row, 1, 1, null);
                // layer two
                if (showLayerTwo && L2_mapTileCoordinate[file][row] != 0)
                    g2d.drawImage(tileList.tileID[L2_mapTileCoordinate[file][row]].image, file, row, 1, 1, null);
                // layer three
                if (showLayerThree && L3_mapTileCoordinate[file][row] != 0)
                    g2d.drawImage(tileList.tileID[L3_mapTileCoordinate[file][row]].image, file, row, 1, 1, null);
                // layer four
                if (showLayerFour && L4_mapTileCoordinate[file][row] != 0)
                    g2d.drawImage(tileList.tileID[L4_mapTileCoordinate[file][row]].image, file, row, 1, 1, null);
                // paint collision
                if (showCollisionState.equals("On"))
                    if (tileList.tileID[L1_mapTileCoordinate[file][row]].collision && showLayerOne ||
                            tileList.tileID[L2_mapTileCoordinate[file][row]].collision && showLayerTwo ||
                            tileList.tileID[L3_mapTileCoordinate[file][row]].collision && showLayerThree ||
                            tileList.tileID[L4_mapTileCoordinate[file][row]].collision && showLayerFour) {
                        g2d.fillRect(file, row, 1, 1);
                        g2d.drawRect(file, row, 1, 1);
                    }
            }

        // draw checkered bg (aesthetic only)
        g2d.setColor(lightGray);
        for (int row = 0; row < maxMapTilesY; row++)
            for (int file = row % 2 == 0 ? 0 : 1; file < maxMapTilesX; file += 2) {
                // only draw background if there are no tiles in front of background
                if ((L1_mapTileCoordinate[file][row] == 0 || L1_mapTileCoordinate[file][row] == tileList.tileID.length / 2) &&
                        (L2_mapTileCoordinate[file][row] == 0 || L2_mapTileCoordinate[file][row] == tileList.tileID.length / 2) &&
                        (L3_mapTileCoordinate[file][row] == 0 || L3_mapTileCoordinate[file][row] == tileList.tileID.length / 2) &&
                        (L4_mapTileCoordinate[file][row] == 0 || L4_mapTileCoordinate[file][row] == tileList.tileID.length / 2)) {
                    g2d.fillRect(file, row, 1, 1);
                }
            }

        // paint properties (for cool patterns to appear draw properties before checkered bg)
        if (Tools.showProperties) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 1).deriveFont(0.20f));
            g2d.setColor(BLACK);
            for (int row = 0; row < maxMapTilesY; row++)
                for (int file = 0; file < maxMapTilesX; file++) {
                    if (showLayerOne && L1_mapTileCoordinate[file][row] != 0)
                        g2d.drawString("1", (file + 0.1f), (row + 0.2f));
                    if (showLayerTwo && L2_mapTileCoordinate[file][row] != 0)
                        g2d.drawString("2", (file + 0.8f), (row + 0.2f));
                    if (showLayerThree && L3_mapTileCoordinate[file][row] != 0)
                        g2d.drawString("3", (file + 0.1f), (row + 0.9f));
                    if (showLayerFour && L4_mapTileCoordinate[file][row] != 0)
                        g2d.drawString("4", (file + 0.8f), (row + 0.9f));
                }
            g2d.setStroke(new BasicStroke(0.03f));
            for (int row = 0; row < maxMapTilesY + 1; row++)
                g2d.draw(new Line2D.Double(0, row, maxMapTilesX, row));
            for (int file = 0; file < maxMapTilesX + 1; file++)
                g2d.draw(new Line2D.Double(file, 0, file, maxMapTilesY));
        }

        // DRAW SELECTION BOX (RECTANGLE)

        g2d.setStroke(new BasicStroke(0.1f));

        // up left
        if (up(pointStart, pointEnd) && left(pointStart, pointEnd)) {
            // cycles through every tile. if tile is selected then draw selected image
            for (int row = pointEnd.y; row < pointStart.y + 1; row++)
                for (int file = pointEnd.x; file < pointStart.x + 1; file++)
                    numMap_P1(file, row);
            // draws selection highlight and border
            g2d.setColor(rectFill);
            g2d.fillRect(pointEnd.x, pointEnd.y, pointStart.x - pointEnd.x + 1, pointStart.y - pointEnd.y + 1);
            g2d.setColor(rectBorder);
            g2d.drawRect(pointEnd.x, pointEnd.y, pointStart.x - pointEnd.x + 1, pointStart.y - pointEnd.y + 1);
        }
        // up right
        else if (up(pointStart, pointEnd) && right(pointStart, pointEnd)) {
            // cycles through every tile. if tile is selected then draw selected image
            for (int row = pointEnd.y; row < pointStart.y + 1; row++)
                for (int file = pointStart.x; file < pointEnd.x + 1; file++)
                    numMap_P1(file, row);
            // draws selection highlight and border
            g2d.setColor(rectFill);
            g2d.fillRect(pointStart.x, pointEnd.y, pointEnd.x - pointStart.x + 1, pointStart.y - pointEnd.y + 1);
            g2d.setColor(rectBorder);
            g2d.drawRect(pointStart.x, pointEnd.y, pointEnd.x - pointStart.x + 1, pointStart.y - pointEnd.y + 1);
        }
        // down right
        else if (down(pointStart, pointEnd) && right(pointStart, pointEnd)) {
            // cycles through tiles within selection and draws selection box
            for (int row = pointStart.y; row < pointEnd.y + 1; row++)
                for (int file = pointStart.x; file < pointEnd.x + 1; file++)
                    numMap_P1(file, row);
            // draws selection highlight and border
            g2d.setColor(rectFill);
            g2d.fillRect(pointStart.x, pointStart.y, pointEnd.x - pointStart.x + 1, pointEnd.y - pointStart.y + 1);
            g2d.setColor(rectBorder);
            g2d.drawRect(pointStart.x, pointStart.y, pointEnd.x - pointStart.x + 1, pointEnd.y - pointStart.y + 1);
        }
        // down left
        else if (down(pointStart, pointEnd) && left(pointStart, pointEnd)) {
            // cycles through every tile. if tile is selected then draw selected image
            for (int row = pointStart.y; row < pointEnd.y + 1; row++)
                for (int file = pointEnd.x; file < pointStart.x + 1; file++)
                    numMap_P1(file, row);
            // draws selection highlight and border
            g2d.setColor(rectFill);
            g2d.fillRect(pointEnd.x, pointStart.y, pointStart.x - pointEnd.x + 1, pointEnd.y - pointStart.y + 1);
            g2d.setColor(rectBorder);
            g2d.drawRect(pointEnd.x, pointStart.y, pointStart.x - pointEnd.x + 1, pointEnd.y - pointStart.y + 1);
        }
        // straight up or straight left
        else if (up(pointStart, pointEnd) || left(pointStart, pointEnd)) {
            for (int row = pointEnd.y; row < pointStart.y + 1; row++)
                for (int file = pointEnd.x; file < pointStart.x + 1; file++)
                    numMap_P1(file, row);
            g2d.setColor(rectFill);
            g2d.fillRect(pointEnd.x, pointEnd.y, pointStart.x - pointEnd.x + 1, pointStart.y - pointEnd.y + 1);
            g2d.setColor(rectBorder);
            g2d.drawRect(pointEnd.x, pointEnd.y, pointStart.x - pointEnd.x + 1, pointStart.y - pointEnd.y + 1);
        }
        // straight down or straight right
        else if (down(pointStart, pointEnd) || right(pointStart, pointEnd)) {
            for (int row = pointStart.y; row < pointEnd.y + 1; row++)
                for (int file = pointStart.x; file < pointEnd.x + 1; file++)
                    numMap_P1(file, row);
            g2d.setColor(rectFill);
            g2d.fillRect(pointStart.x, pointStart.y, pointEnd.x - pointStart.x + 1, pointEnd.y - pointStart.y + 1);
            g2d.setColor(rectBorder);
            g2d.drawRect(pointStart.x, pointStart.y, pointEnd.x - pointStart.x + 1, pointEnd.y - pointStart.y + 1);
        }
        // removes old graphics data
        g2d.dispose();
    }


    boolean left(Point pointA, Point pointB) {
        return pointA.x > pointB.x;
    }

    boolean right(Point pointA, Point pointB) {
        return pointA.x < pointB.x;
    }

    boolean up(Point pointA, Point pointB) {
        return pointA.y > pointB.y;
    }

    boolean down(Point pointA, Point pointB) {
        return pointA.y < pointB.y;
    }


    void wipeSelection() {
        pointStart.x = 0;
        pointStart.y = 0;
        pointEnd.x = 0;
        pointEnd.y = 0;
    }


    public void fill(int x, int y) {
        // fill on selected layer
        switch (selectedLayer) {
            case "Layer 1":
                if (showLayerOne) {
                    if (L1_mapTileCoordinate[x][y] == tileBar.tileValue)
                        return;
                    floodFill(L1_mapTileCoordinate, x, y, L1_mapTileCoordinate[x][y], tileBar.tileValue);
                }
                break;
            case "Layer 2":
                if (showLayerTwo) {
                    if (L2_mapTileCoordinate[x][y] == tileBar.tileValue)
                        return;
                    floodFill(L2_mapTileCoordinate, x, y, L2_mapTileCoordinate[x][y], tileBar.tileValue);
                }
                break;
            case "Layer 3":
                if (showLayerThree) {
                    if (L3_mapTileCoordinate[x][y] == tileBar.tileValue)
                        return;
                    floodFill(L3_mapTileCoordinate, x, y, L3_mapTileCoordinate[x][y], tileBar.tileValue);
                }
                break;
            case "Layer 4":
                if (showLayerFour) {
                    if (L4_mapTileCoordinate[x][y] == tileBar.tileValue)
                        return;
                    floodFill(L4_mapTileCoordinate, x, y, L4_mapTileCoordinate[x][y], tileBar.tileValue);
                }
                break;
        }
    }

    // A recursive method to replace previous tile "oldTile" at "(x, y)"
    // and all surrounding tiles of (x, y) with new tile "newTile"
    void floodFill(int[][] layerNumMap, int x, int y, int oldTile, int newTile) {
        // return if tile to be filled is out of bounds
        if (x < 0 || x >= maxMapTilesX || y < 0 || y >= maxMapTilesY || layerNumMap[x][y] != oldTile)
            return;

        // Replace the tile at (x, y)
        layerNumMap[x][y] = newTile;
        if (collisionState.equals("On"))
            layerNumMap[x][y] += tileList.tileID.length / 2;

        // Recur for up, right, down and left
        floodFill(layerNumMap, x + 1, y, oldTile, newTile);
        floodFill(layerNumMap, x - 1, y, oldTile, newTile);
        floodFill(layerNumMap, x, y + 1, oldTile, newTile);
        floodFill(layerNumMap, x, y - 1, oldTile, newTile);
    }

    void numMap_P1(int x, int y) {
        // checks if in bounds
        if (x < maxMapTilesX && y < maxMapTilesY && x > -1 && y > -1)
            switch (selectedLayer) {
                case "Layer 1":
                    if (showLayerOne) {
                        // tile at mouse action = selected tile type
                        L1_mapTileCoordinate[x][y] = tileBar.tileValue;
                        if (collisionState.equals("On"))
                            L1_mapTileCoordinate[x][y] += tileList.tileID.length / 2;
                    }
                    break;
                case "Layer 2":
                    if (showLayerTwo) {
                        L2_mapTileCoordinate[x][y] = tileBar.tileValue;
                        if (collisionState.equals("On"))
                            L2_mapTileCoordinate[x][y] += tileList.tileID.length / 2;
                    }
                    break;
                case "Layer 3":
                    if (showLayerThree) {
                        L3_mapTileCoordinate[x][y] = tileBar.tileValue;
                        if (collisionState.equals("On"))
                            L3_mapTileCoordinate[x][y] += tileList.tileID.length / 2;
                    }
                    break;
                case "Layer 4":
                    if (showLayerFour) {
                        L4_mapTileCoordinate[x][y] = tileBar.tileValue;
                        if (collisionState.equals("On"))
                            L4_mapTileCoordinate[x][y] += tileList.tileID.length / 2;
                    }
                    break;
            }
    }

    void numMap_P2() {
        // turns int array mapTileCoordinate
        // into a String array mapStringLine
        switch (selectedLayer) {
            case "Layer 1":
                if (showLayerOne) {
                    L1_mapStringLine = new StringBuilder();
                    for (int row = 0; row < maxMapTilesY; row++) {
                        for (int file = 0; file < maxMapTilesX; file++)
                            L1_mapStringLine.append(L1_mapTileCoordinate[file][row]).append(" ");
                        L1_mapStringLine.append("\n");
                    }
                }
                break;
            case "Layer 2":
                if (showLayerTwo) {
                    L2_mapStringLine = new StringBuilder();
                    for (int row = 0; row < maxMapTilesY; row++) {
                        for (int file = 0; file < maxMapTilesX; file++)
                            L2_mapStringLine.append(L2_mapTileCoordinate[file][row]).append(" ");
                        L2_mapStringLine.append("\n");
                    }
                }
                break;
            case "Layer 3":
                if (showLayerThree) {
                    L3_mapStringLine = new StringBuilder();
                    for (int row = 0; row < maxMapTilesY; row++) {
                        for (int file = 0; file < maxMapTilesX; file++)
                            L3_mapStringLine.append(L3_mapTileCoordinate[file][row]).append(" ");
                        L3_mapStringLine.append("\n");
                    }
                }
                break;
            case "Layer 4":
                if (showLayerFour) {
                    L4_mapStringLine = new StringBuilder();
                    for (int row = 0; row < maxMapTilesY; row++) {
                        for (int file = 0; file < maxMapTilesX; file++)
                            L4_mapStringLine.append(L4_mapTileCoordinate[file][row]).append(" ");
                        L4_mapStringLine.append("\n");
                    }
                }
                break;
        }
    }

    void storeState() {
        undo.push("" + L1_mapStringLine + L2_mapStringLine + L3_mapStringLine + L4_mapStringLine + maxMapTilesX + " " + maxMapTilesY);
        // keeps stack size at 30
        if (undo.size() > 31) undo.remove(0);
    }
}