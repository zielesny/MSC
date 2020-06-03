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

import de.whs.ibci.msc.utility.GuiUtilities;
import de.whs.ibci.msc.utility.GeneralUtilities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

/**
 * A configuration-dialog in which the user can specify the upper and lower bin 
 * borders as well as the upper and lower frequency bounds of a chart.
 *
 * @author Jan-Mathis Hein
 */
public class ChartConfigurationDialog extends ConfigurationDialog {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final GridPane inputPane;
    
    private final Label lowerBinBorderLabel, upperBinBorderLabel, lowerFrequencyBoundLabel, upperFrequencyBoundLabel;
    
    private final TextField lowerBinBorderInputField, upperBinBorderInputField, lowerFrequencyBoundInputField, upperFrequencyBoundInputField;
    
    private final Button resetButton, defaultButton;
    
    private final HBox buttonBox;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create the Dialog with all its controls and handle their layout and size
     * 
     * @param tmpOwner specifies the window that owns the ConfigurationDialog
     * @param tmpTitle specifies the title of the ConfigurationDialog
     */
    public ChartConfigurationDialog(Window tmpOwner, String tmpTitle) {
        super(tmpOwner, tmpTitle);
            this.inputPane = new GridPane();
            this.inputPane.setBorder(GuiUtilities.RAISED_BORDER);
            this.inputPane.setPadding(new Insets(10, 10, 10, 10));
            this.inputPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
            this.inputPane.setHgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                this.lowerBinBorderInputField = new TextField();
                this.lowerBinBorderInputField.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.lowerBinBorderInputField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.lowerBinBorderInputField, GuiUtilities.TEXT_FIELD_WIDTH);
                this.lowerBinBorderLabel = new Label(GeneralUtilities.getUIText("ChartConfigurationDialog.lowerBinBorderLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.lowerBinBorderLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.lowerBinBorderInputField, 1, 0);
            this.inputPane.add(this.lowerBinBorderLabel, 0, 0);
                this.upperBinBorderInputField = new TextField();
                this.upperBinBorderInputField.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.upperBinBorderInputField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.upperBinBorderInputField, GuiUtilities.TEXT_FIELD_WIDTH);
                this.upperBinBorderLabel = new Label(GeneralUtilities.getUIText("ChartConfigurationDialog.upperBinBorderLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.upperBinBorderLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.upperBinBorderInputField, 1, 1);
            this.inputPane.add(this.upperBinBorderLabel, 0, 1);
                this.lowerFrequencyBoundInputField = new TextField();
                this.lowerFrequencyBoundInputField.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.lowerFrequencyBoundInputField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.lowerFrequencyBoundInputField, GuiUtilities.TEXT_FIELD_WIDTH);
                this.lowerFrequencyBoundLabel = new Label(GeneralUtilities.getUIText("ChartConfigurationDialog.lowerFrequencyBoundLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.lowerFrequencyBoundLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(lowerFrequencyBoundInputField, 1, 2);
            this.inputPane.add(lowerFrequencyBoundLabel, 0, 2);
                this.upperFrequencyBoundInputField = new TextField();
                this.upperFrequencyBoundInputField.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.upperFrequencyBoundInputField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.upperFrequencyBoundInputField, GuiUtilities.TEXT_FIELD_WIDTH);
                this.upperFrequencyBoundLabel = new Label(GeneralUtilities.getUIText("ChartConfigurationDialog.upperFrequencyBoundLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.upperFrequencyBoundLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(upperFrequencyBoundInputField, 1, 3);
            this.inputPane.add(upperFrequencyBoundLabel, 0, 3);
                this.buttonBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.buttonBox.setAlignment(Pos.CENTER_RIGHT);
                    this.defaultButton = new Button(GeneralUtilities.getUIText("ChartConfigurationDialog.defaultButton.text"));
                    this.defaultButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.defaultButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.defaultButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                this.buttonBox.getChildren().add(this.defaultButton);
                    this.resetButton = new Button(GeneralUtilities.getUIText("ChartConfigurationDialog.resetButton.text"));
                    this.resetButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.resetButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.resetButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                this.buttonBox.getChildren().add(this.resetButton);
            this.inputPane.add(this.buttonBox, 1, 4);
        this.getContentPane().getChildren().add(this.inputPane);
        GuiUtilities.setMinMaxPrefHeight(this.getDialogPane(), 304);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /** 
     * Get the lowerBinBorderInputField
     * 
     * @return the lowerBinBorderInputField
     */
    public TextField getLowerBinBorderInputField() {
        return this.lowerBinBorderInputField;
    }
    
    /** 
     * Get the upperBinBorderInputField
     * 
     * @return the upperBinBorderInputField
     */
    public TextField getUpperBinBorderInputField() {
        return this.upperBinBorderInputField;
    }
    
    /** 
     * Get the lowerFrequencyBoundInputField
     * 
     * @return the lowerFrequencyBoundInputField
     */
    public TextField getLowerFrequencyBoundInputField() {
        return this.lowerFrequencyBoundInputField;
    }
    
    /** 
     * Get the upperFrequencyBoundInputField
     * 
     * @return the upperFrequencyBoundInputField
     */
    public TextField getUpperFrequencyBoundInputField() {
        return this.upperFrequencyBoundInputField;
    }
    
    /** 
     * Get the resetButton
     * 
     * @return the resetButton
     */
    public Button getResetButton() {
        return this.resetButton;
    }
    
    /** 
     * Get the defaultButton
     * 
     * @return the defaultButton
     */
    public Button getDefaultButton() {
        return this.defaultButton;
    }
    //</editor-fold>
    
}
