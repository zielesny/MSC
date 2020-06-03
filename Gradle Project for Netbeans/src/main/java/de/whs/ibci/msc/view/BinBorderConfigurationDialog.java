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
package de.whs.ibci.msc.view;

import de.whs.ibci.msc.utility.GeneralUtilities;
import de.whs.ibci.msc.utility.GuiUtilities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * A configuration-dialog in which the user can specify any number of bin 
 * borders with specifically set values.
 *
 * @author Jan-Mathis Hein
 */
public class BinBorderConfigurationDialog extends ConfigurationDialog {

    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final Button resetButton;
    
    private final ScrollPane scrollPane;
    
    private final VBox binBorderConfigurationBoxesBox;
    
    private final HBox controlsBox;
    
    private final GridPane inputPane;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create the Dialog with all its controls and handle their layout and size
     * 
     * @param tmpOwner specifies the window that owns the ConfigurationDialog
     * @param tmpTitle specifies the title of the ConfigurationDialog
     */
    public BinBorderConfigurationDialog(Window tmpOwner, String tmpTitle) {
        super(tmpOwner, tmpTitle);
            this.inputPane = new GridPane();
            this.inputPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
            this.inputPane.setBorder(GuiUtilities.RAISED_BORDER);
            this.inputPane.setPadding(GuiUtilities.STANDARD_INSETS);
                this.scrollPane = new ScrollPane();
                this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                this.scrollPane.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.binBorderConfigurationBoxesBox = new VBox(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                    // This fixes a bug where the ScrollPane cuts off its content at the bottom
                    this.binBorderConfigurationBoxesBox.setPadding(new Insets(0, 0, 10, 0));
                this.scrollPane.setContent(this.binBorderConfigurationBoxesBox);
            this.inputPane.add(this.scrollPane, 0, 0);
                this.controlsBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.controlsBox.setAlignment(Pos.CENTER_RIGHT);
                    this.resetButton = new Button(GeneralUtilities.getUIText("BinBorderConfigurationDialog.resetButton.text"));
                    this.resetButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("BinBorderConfigurationDialog.resetButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.resetButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                this.controlsBox.getChildren().add(this.resetButton);
            this.inputPane.add(this.controlsBox, 0, 1);
        this.getContentPane().getChildren().add(this.inputPane);
        this.setHeaderText(GeneralUtilities.getUIText("BinBorderConfigurationDialog.Header.text"));
        GuiUtilities.setMinMaxPrefHeight(this.getDialogPane(), 500);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the resetButton
     *
     * @return the resetButton
     */
    public Button getResetButton() {
        return this.resetButton;
    }
    
    
    /**
     * Get the binBorderConfigurationBoxesBox
     *
     * @return the binBorderConfigurationBoxesBox
     */
    public VBox getBinBorderConfigurationBoxesBox() {
        return this.binBorderConfigurationBoxesBox;
    }
    //</editor-fold>
    
}
