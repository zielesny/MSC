/*
 * MSC - Molecule Set Comparator
 * Copyright (C) 2020 Jan-Mathis Hein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.whs.ibci.msc.utility;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Defines some constants and utility methods for a graphical user interface. 
 * This class can't be instantiated
 *
 * @author Jan-Mathis Hein
 */
public final class GuiUtilities {
    
    //<editor-fold defaultstate="collapsed" desc="Public class variables">
    /**
     * Standard horizontal gap between controls that are located in the same
     * region
     */
    public static final double HORIZONTAL_GAP_BETWEEN_CONTROLS = 10;
    
    /**
     * Standard vertical gap between controls that are located in the same
     * region
     */
    public static final double VERTICAL_GAP_BETWEEN_CONTROLS = 10;
    
    /**
     * Standard insets that are used to isolate different regions from 
     * each other
     */
    public static final Insets STANDARD_INSETS = new Insets(10);
    
    /**
     * An image of a white square with a dimension of 200 x 200
     */
    public static final Image WHITE_SQUARE;
    
    /**
     * A border that lets a region appear as if it is lowered
     */
    public static final Border LOWERED_BORDER = new Border(new BorderStroke(
            Color.color(0.4, 0.4, 0.4), Color.color(0.99, 0.99, 0.99),
            Color.color(0.99, 0.99, 0.99), Color.color(0.4, 0.4, 0.4),
            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(2), new Insets(0)
    ));
    
    /**
     * A border that lets a region appear as if it is raised
     */
    public static final Border RAISED_BORDER = new Border(new BorderStroke(
            Color.color(0.99, 0.99, 0.99), Color.color(0.4, 0.4, 0.4),
            Color.color(0.4, 0.4, 0.4), Color.color(0.99, 0.99, 0.99),
            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(2), new Insets(0)
    ));
    
    /**
     * A thin red border
     */
    public static final Border RED_BORDER = new Border(new BorderStroke(
            Paint.valueOf("Red"), Paint.valueOf("Red"), Paint.valueOf("Red"),
            Paint.valueOf("Red"), BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            new CornerRadii(0), new BorderWidths(2), new Insets(0)
    ));
    
    /**
     * A thin green border
     */
    public static final Border GREEN_BORDER = new Border(new BorderStroke(
            Paint.valueOf("Green"), Paint.valueOf("Green"), Paint.valueOf("Green"),
            Paint.valueOf("Green"), BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            new CornerRadii(0), new BorderWidths(2), new Insets(0)
    ));
    
    /**
     * A thin black border
     */
    public static final Border BLACK_BORDER = new Border(new BorderStroke(
            Paint.valueOf("Black"), Paint.valueOf("Black"), Paint.valueOf("Black"),
            Paint.valueOf("Black"), BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
            new CornerRadii(0), new BorderWidths(2), new Insets(0)
    ));
    
    /**
     * The standard width of a button
     */
    public static final double BUTTON_WIDTH = 100;
    
    /**
     * The standard width of a textfield
     */
    public static final double TEXT_FIELD_WIDTH = 160;
    
    /**
     * The standard width of a small button
     */
    public static final double SMALL_BUTTON_WIDTH = 75.2;
    
    /**
     * The standard height of a button
     */
    public static final double BUTTON_HEIGHT = 25;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Static block">
    static {
        BufferedImage tmpImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D tmpGraphics2D = tmpImage.createGraphics();
        tmpGraphics2D.setColor(java.awt.Color.WHITE);
        tmpGraphics2D.fillRect(0, 0, tmpImage.getWidth(), tmpImage.getHeight());
        WHITE_SQUARE = SwingFXUtils.toFXImage(tmpImage, null);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public class methods">
    /**
     * Set the maximal, minimal and prefered size of a specified region
     *
     * @param tmpRegion specifies the region
     * @param tmpWidth specifies the height
     * @param tmpHeight specifies the width
     */
    public static void setMinMaxPrefSize(Region tmpRegion, double tmpWidth, double tmpHeight) {
        GuiUtilities.setMinMaxPrefWidth(tmpRegion, tmpWidth);
        GuiUtilities.setMinMaxPrefHeight(tmpRegion, tmpHeight);
    }
    
    /**
     * Set the maximal, minimal and prefered width of a specified region
     *
     * @param tmpRegion specifies the region
     * @param tmpWidth specifies the width
     */
    public static void setMinMaxPrefWidth(Region tmpRegion, double tmpWidth) {
        tmpRegion.setMinWidth(tmpWidth);
        tmpRegion.setMaxWidth(tmpWidth);
        tmpRegion.setPrefWidth(tmpWidth);
    }
    
    /**
     * Set the maximal, minimal and prefered height of a specified region
     *
     * @param tmpRegion specifies the region
     * @param tmpHeight specifies the height
     */
    public static void setMinMaxPrefHeight(Region tmpRegion, double tmpHeight) {
        tmpRegion.setMinHeight(tmpHeight);
        tmpRegion.setMaxHeight(tmpHeight);
        tmpRegion.setPrefHeight(tmpHeight);
    }
    //</editor-fold>
    
}
