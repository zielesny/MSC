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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

/**
 * A Dialog in which the user can configure some of the application's 
 * preferences
 *
 * @author Jan-Mathis Hein
 */
public class PreferencesConfigurationDialog extends ConfigurationDialog {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final GridPane inputPane;
    
    private final Label numberOfParallelThreadsLabel, defaultNumberOfBinsLabel, maximalNumberOfMoleculePairsToSaveLabel, imageQualityLabel;
    
    private final TextField numberOfParallelThreadsField, defaultNumberOfBinsField, maximalNumberOfMoleculePairsToSaveField;
    
    private final Button resetButton;
    
    private final Slider imageQualitySlider;
    
    private final HBox resetButtonBox;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create the Dialog with all its controls and handle their layout and size
     * 
     * @param tmpOwner specifies the window that owns the Dialog
     * @param tmpTitle specifies the title of the Dialog
     */
    public PreferencesConfigurationDialog(Window tmpOwner, String tmpTitle) {
        super(tmpOwner, tmpTitle);
            this.inputPane = new GridPane();
            this.inputPane.setBorder(GuiUtilities.RAISED_BORDER);
            this.inputPane.setPadding(GuiUtilities.STANDARD_INSETS);
            this.inputPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
            this.inputPane.setHgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                this.numberOfParallelThreadsLabel = new Label(GeneralUtilities.getUIText("PreferencesConfigurationDialog.numberOfParallelThreadsLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.numberOfParallelThreadsLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.numberOfParallelThreadsLabel, 0, 0);
                this.numberOfParallelThreadsField = new TextField();
                this.numberOfParallelThreadsField.setTooltip(new Tooltip(GeneralUtilities.getUIText("PreferencesConfigurationDialog.numberOfParallelThreadsField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.numberOfParallelThreadsField, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.numberOfParallelThreadsField, 1, 0);
                this.defaultNumberOfBinsLabel = new Label(GeneralUtilities.getUIText("PreferencesConfigurationDialog.defaultNumberOfBinsLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.defaultNumberOfBinsLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.defaultNumberOfBinsLabel, 0, 1);
                this.defaultNumberOfBinsField = new TextField();
                this.defaultNumberOfBinsField.setTooltip(new Tooltip(GeneralUtilities.getUIText("PreferencesConfigurationDialog.defaultNumberOfBinsField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.defaultNumberOfBinsField, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.defaultNumberOfBinsField, 1, 1);
                this.maximalNumberOfMoleculePairsToSaveLabel = new Label(GeneralUtilities.getUIText("PreferencesConfigurationDialog.maximalNumberOfMoleculePairsToSaveLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.maximalNumberOfMoleculePairsToSaveLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.maximalNumberOfMoleculePairsToSaveLabel, 0, 2);
                this.maximalNumberOfMoleculePairsToSaveField = new TextField();
                this.maximalNumberOfMoleculePairsToSaveField.setTooltip(new Tooltip(GeneralUtilities.getUIText("PreferencesConfigurationDialog.maximalNumberOfMoleculePairsToSaveField.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.maximalNumberOfMoleculePairsToSaveField, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.maximalNumberOfMoleculePairsToSaveField, 1, 2);
                this.imageQualityLabel = new Label(GeneralUtilities.getUIText("PreferencesConfigurationDialog.imageQualityLabel.text"));
                GuiUtilities.setMinMaxPrefWidth(this.imageQualityLabel, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.imageQualityLabel, 0, 3);
                this.imageQualitySlider = new Slider(0.0, 1.0, 0.5);
                this.imageQualitySlider.setMajorTickUnit(0.25);
                this.imageQualitySlider.setOrientation(Orientation.HORIZONTAL);
                this.imageQualitySlider.setShowTickLabels(true);
                this.imageQualitySlider.setShowTickMarks(true);
                this.imageQualitySlider.setTooltip(new Tooltip(GeneralUtilities.getUIText("PreferencesConfigurationDialog.imageQualitySlider.tooltipText")));
                GuiUtilities.setMinMaxPrefWidth(this.imageQualitySlider, GuiUtilities.TEXT_FIELD_WIDTH);
            this.inputPane.add(this.imageQualitySlider, 1, 3);
                this.resetButtonBox = new HBox();
                this.resetButtonBox.setAlignment(Pos.CENTER_RIGHT);
                    this.resetButton = new Button(GeneralUtilities.getUIText("PreferencesConfigurationDialog.resetButton.text"));
                    this.resetButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("PreferencesConfigurationDialog.resetButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.resetButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                this.resetButtonBox.getChildren().add(this.resetButton);
            this.inputPane.add(this.resetButtonBox, 1, 4);
        this.getContentPane().getChildren().add(this.inputPane);
        this.setHeaderText(GeneralUtilities.getUIText("PreferencesConfigurationDialog.Header.text"));
        GuiUtilities.setMinMaxPrefHeight(this.getDialogPane(), 317.0);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the numberOfParallelThreadsField
     * 
     * @return the numberOfParallelThreadsField
     */
    public TextField getNumberOfParallelThreadsField() {
        return this.numberOfParallelThreadsField;
    }
    
    /**
     * Get the defaultNumberOfBinsField
     * 
     * @return the defaultNumberOfBinsField
     */
    public TextField getDefaultNumberOfBinsField() {
        return this.defaultNumberOfBinsField;
    }
    
    /**
     * Get the maximalNumberOfMoleculePairsToSaveField
     * 
     * @return the maximalNumberOfMoleculePairsToSaveField
     */
    public TextField getMaximalNumberOfMoleculePairsToSaveField() {
        return this.maximalNumberOfMoleculePairsToSaveField;
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
     * Get the imageQualitySlider
     * 
     * @return the imageQualitySlider
     */
    public Slider getImageQualitySlider() {
        return this.imageQualitySlider;
    }
    //</editor-fold>
    
}
