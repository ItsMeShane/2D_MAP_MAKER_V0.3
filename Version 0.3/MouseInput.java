package main;

import javax.swing.*;
import java.awt.event.*;

public class MouseInput extends MouseAdapter {

    MapPanel mapPanel;

    public MouseInput(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }
    int pressedX, pressedY;


    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            int X = (int) (e.getX() / mapPanel.zoomScale);
            int Y = (int) (e.getY() / mapPanel.zoomScale);
            if (Tools.selectedTool.equals("Fill")) {
                mapPanel.fill(X, Y);
                mapPanel.repaint();
                mapPanel.numMap_P2();
            }
        }
    }


    public void mousePressed(MouseEvent e) {
        int X = (int) (e.getX() / mapPanel.zoomScale);
        int Y = (int) (e.getY() / mapPanel.zoomScale);
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressedX = e.getX();
            pressedY = e.getY();
            if (Tools.selectedTool.equals("Pen")) {
                if (e.isControlDown() || e.isShiftDown()) {
                    mapPanel.tileLocationX = -1;
                    mapPanel.tileLocationY = -1;
                } else {
                    mapPanel.tileLocationX = X;
                    mapPanel.tileLocationY = Y;
                    mapPanel.numMap_P1(mapPanel.tileLocationX, mapPanel.tileLocationY);
                    mapPanel.repaint();
                }
            } else if (Tools.selectedTool.equals("Rectangle")) {
                if (!e.isControlDown() && !e.isShiftDown()) {
                    mapPanel.pointStart.x = X;
                    mapPanel.pointStart.y = Y;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mapPanel.tileLocationX = -1;
        mapPanel.tileLocationY = -1;
        mapPanel.numMap_P2();
        mapPanel.storeState();
        mapPanel.wipeSelection();
        mapPanel.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int X = (int) (e.getX() / mapPanel.zoomScale);
        int Y = (int) (e.getY() / mapPanel.zoomScale);
        if (e.isControlDown() && e.isShiftDown()) {
            // Ctrl + shift + drag = move scroll bars (map)
            if (e.getX() > pressedX) // left
                mapPanel.mapScroll.getHorizontalScrollBar().setValue(mapPanel.mapScroll.getHorizontalScrollBar().getValue() - Math.abs(pressedX - e.getX()));
            if (e.getX() < pressedX) // right
                mapPanel.mapScroll.getHorizontalScrollBar().setValue(mapPanel.mapScroll.getHorizontalScrollBar().getValue() + Math.abs(pressedX - e.getX()));
            if (e.getY() > pressedY) // up
                mapPanel.mapScroll.getVerticalScrollBar().setValue(mapPanel.mapScroll.getVerticalScrollBar().getValue() - Math.abs(pressedY - e.getY()));
            if (e.getY() < pressedY) // down
                mapPanel.mapScroll.getVerticalScrollBar().setValue(mapPanel.mapScroll.getVerticalScrollBar().getValue() + Math.abs(pressedY - e.getY()));
        } else if (e.isControlDown() || e.isShiftDown()) {
            // if Ctrl or shift held but not both then don't draw
            mapPanel.tileLocationX = -1;
            mapPanel.tileLocationY = -1;
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (Tools.selectedTool.equals("Pen")) {
                // calculate where to draw tile based on tile scale and how zoomed in you are
                mapPanel.tileLocationX = X;
                mapPanel.tileLocationY = Y;
                mapPanel.numMap_P1(mapPanel.tileLocationX, mapPanel.tileLocationY);
            } else if (Tools.selectedTool.equals("Rectangle")) {
                mapPanel.pointEnd.x = X;
                mapPanel.pointEnd.y = Y;
            }
            mapPanel.repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        boolean zoomed = false;

        int scrollingDirection = e.getWheelRotation();
        // zoom out // scroll towards
        if (scrollingDirection == 1) {
            if (e.isControlDown()) {
                // checks if zooming out would be in limit
                if (mapPanel.mapScroll.getHeight() < mapPanel.getPreferredSize().getHeight() + 150 || mapPanel.mapScroll.getWidth() < mapPanel.getPreferredSize().getWidth() + 150) {
                    mapPanel.zoom /= 1.1;
                    zoomed = true;
                }
            }
            else if (!e.isShiftDown())
                mapPanel.mapScroll.getVerticalScrollBar().setValue((int) (mapPanel.mapScroll.getVerticalScrollBar().getValue() + mapPanel.zoomScale / 2));    // right
            else
                mapPanel.mapScroll.getHorizontalScrollBar().setValue((int) (mapPanel.mapScroll.getHorizontalScrollBar().getValue() + mapPanel.zoomScale / 2));    // down
        }
        // zoom in // scroll away
        else if (scrollingDirection == -1) {
            if (e.isControlDown()) {
                // checks if zooming in would be in limit
                if (mapPanel.zoom * 1.1 < 25000) { // 1000 ~ 10 times zoom
                    mapPanel.zoom = mapPanel.zoom * 1.1;
                    zoomed = true;
                }
            }
            else if (!e.isShiftDown())
                mapPanel.mapScroll.getVerticalScrollBar().setValue((int) (mapPanel.mapScroll.getVerticalScrollBar().getValue() - mapPanel.zoomScale / 2));   // left
            else
                mapPanel.mapScroll.getHorizontalScrollBar().setValue((int) (mapPanel.mapScroll.getHorizontalScrollBar().getValue() - mapPanel.zoomScale / 2));    // up
        }
        // if you zoomed then update alignPort and revalidate
        if (zoomed) {
            mapPanel.zoomScale = (float) (mapPanel.zoom / 100f);
            mapPanel.alignViewPort(e.getPoint());
            mapPanel.revalidate();
            mapPanel.mapScroll.repaint();
        }
    }
}
