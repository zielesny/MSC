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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Box for the configuration of a single bin border
 *
 * @author Jan-Mathis Hein
 */
public class BinBorderConfigurationBox extends HBox {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final Label label;
    
    private final TextField textField;
    
    private final Button removeButton, addButton;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initializes the instance variables
     * 
     * @param tmpTextFieldValue initial value for the textField
     * @param tmpLabelText specifies the text for the label
     */
    public BinBorderConfigurationBox(String tmpTextFieldValue, String tmpLabelText) {
        super(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
            this.label = new Label(tmpLabelText);
            GuiUtilities.setMinMaxPrefSize(this.label, 20, GuiUtilities.BUTTON_HEIGHT);
            this.textField = new TextField(tmpTextFieldValue);
            GuiUtilities.setMinMaxPrefSize(this.textField, 150, GuiUtilities.BUTTON_HEIGHT);
            this.removeButton = new Button(GeneralUtilities.getUIText("BinBorderConfigurationBox.removeButton.text"));
            this.removeButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("BinBorderConfigurationBox.removeButton.tooltipText")));
            GuiUtilities.setMinMaxPrefSize(this.removeButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
            this.addButton = new Button(GeneralUtilities.getUIText("BinBorderConfigurationBox.addButton.text"));
            this.addButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("BinBorderConfigurationBox.addButton.tooltipText")));
            GuiUtilities.setMinMaxPrefSize(this.addButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
        this.getChildren().addAll(this.label, this.textField, this.removeButton, this.addButton);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the textField
     *
     * @return the textField
     */
    public TextField getTextField() {
        return this.textField;
    }
    
    /**
     * Get the label
     *
     * @return the label
     */
    public Label getLabel() {
        return this.label;
    }
    
    /**
     * Get the removeButton
     *
     * @return the removeButton
     */
    public Button getRemoveButton() {
        return this.removeButton;
    }
    
    /**
     * Get the addButton
     *
     * @return the addButton
     */
    public Button getAddButton() {
        return this.addButton;
    }
    //</editor-fold>
    
}
