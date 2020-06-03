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

import de.whs.ibci.msc.control.BinLabelType;
import de.whs.ibci.msc.control.JobController;
import de.whs.ibci.msc.utility.GuiUtilities;
import de.whs.ibci.msc.utility.GeneralUtilities;
import de.whs.ibci.msc.model.ComparisonFeature;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A Tab in which a job's output is displayed and that contains some controls  
 * with which the user can interact with the output
 *
 * @author Jan-Mathis Hein
 */
public class OutputTab extends Tab {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final HBox contentPane;
    
    private final VBox binBorderSlidersBox, frequencySlidersBox, binBorderBox, frequencyBox;
    
    private final AnchorPane chartPane;
    
    private final Label infoLabel, binBorderSlidersLabel, frequencySlidersLabel, numberOfBinsLabel;
    
    private final GridPane controlsPane, chartConfigurationPane;
    
    private final ChoiceBox<ComparisonFeature> chartChoiceBox;
    
    private final TextField numberOfBinsTextField;
    
    private final ChoiceBox<BinLabelType> binLabelChoiceBox;
    
    private final Button imageButton, chartConfigurationButton, defaultButton, binBorderConfigurationButton, summaryReportButton;
    
    private final Slider binBorder1Slider, binBorder2Slider, frequency1Slider, frequency2Slider;
    
    private final CheckBox relativeFrequenciesCheckBox, barLabelCheckBox;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create the Tab with all its controls and handle their layout and size
     * 
     * @param tmpJobNumber the job's identifier number
     */
    public OutputTab(String tmpJobNumber) {
        super(GeneralUtilities.getUIText("OutputTab.title.text") + tmpJobNumber);
        this.setClosable(true);
            this.contentPane = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
            // The 11 is intentional
            this.contentPane.setPadding(new Insets(10, 10, 11, 10));
                this.controlsPane = new GridPane();
                this.controlsPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.controlsPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                this.controlsPane.setBorder(GuiUtilities.RAISED_BORDER);
                this.controlsPane.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.infoLabel = new Label("");
                    this.infoLabel.setWrapText(true);
                    this.infoLabel.setAlignment(Pos.TOP_LEFT);
                this.controlsPane.add(this.infoLabel, 0, 0, 2, 1);
                    this.summaryReportButton = new Button(GeneralUtilities.getUIText("OutputTab.summaryReportButton.text"));
                    this.summaryReportButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.summaryReportButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefWidth(this.summaryReportButton, GuiUtilities.BUTTON_WIDTH);
                this.controlsPane.add(this.summaryReportButton, 0, 1, 1, 1);
                    this.barLabelCheckBox = new CheckBox(GeneralUtilities.getUIText("OutputTab.barLabelCheckBox.text"));
                    this.barLabelCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.barLabelCheckBox.tooltipText")));
                    GuiUtilities.setMinMaxPrefWidth(this.barLabelCheckBox, GuiUtilities.BUTTON_WIDTH);
                this.controlsPane.add(this.barLabelCheckBox, 1, 1, 1, 1);
                    this.chartChoiceBox = new ChoiceBox<>();
                    this.chartChoiceBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.chartChoiceBox.tooltipText")));
                    this.chartChoiceBox.setMaxWidth(Double.MAX_VALUE);
                this.controlsPane.add(this.chartChoiceBox, 0, 2, 2, 1);
                    this.binLabelChoiceBox = new ChoiceBox<>();
                    this.binLabelChoiceBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.binLabelChoiceBox.tooltipText")));
                    this.binLabelChoiceBox.setMaxWidth(Double.MAX_VALUE);
                this.controlsPane.add(this.binLabelChoiceBox, 0, 3, 2, 1);
                    this.chartConfigurationPane = new GridPane();
                    this.chartConfigurationPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                    this.chartConfigurationPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                    this.chartConfigurationPane.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.chartConfigurationPane.setBorder(GuiUtilities.LOWERED_BORDER);
                        this.chartConfigurationButton = new Button(GeneralUtilities.getUIText("OutputTab.configureButton.text"));
                        this.chartConfigurationButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.configureButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefWidth(this.chartConfigurationButton, GuiUtilities.BUTTON_WIDTH);
                    this.chartConfigurationPane.add(this.chartConfigurationButton, 0, 0, 1, 1);
                        this.defaultButton = new Button(GeneralUtilities.getUIText("ChartConfigurationDialog.defaultButton.text"));
                        this.defaultButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("ChartConfigurationDialog.defaultButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefWidth(this.defaultButton, GuiUtilities.BUTTON_WIDTH);
                    this.chartConfigurationPane.add(this.defaultButton, 1, 0, 1, 1);
                        this.binBorderConfigurationButton = new Button(GeneralUtilities.getUIText("OutputTab.binBorderConfigurationButton.text"));
                        this.binBorderConfigurationButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.binBorderConfigurationButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefWidth(this.binBorderConfigurationButton, GuiUtilities.BUTTON_WIDTH);
                    this.chartConfigurationPane.add(this.binBorderConfigurationButton, 0, 1, 1, 1);
                        this.relativeFrequenciesCheckBox = new CheckBox(GeneralUtilities.getUIText("OutputTab.relativeFrequenciesCheckBox.text"));
                        this.relativeFrequenciesCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.relativeFrequenciesCheckBox.tooltipText")));
                        GuiUtilities.setMinMaxPrefWidth(this.relativeFrequenciesCheckBox, GuiUtilities.BUTTON_WIDTH);
                    this.chartConfigurationPane.add(this.relativeFrequenciesCheckBox, 1, 1, 1, 1);
                        this.numberOfBinsLabel = new Label(GeneralUtilities.getUIText("OutputTab.numberOfBinsLabel.text"));
                        GuiUtilities.setMinMaxPrefWidth(this.numberOfBinsLabel, GuiUtilities.BUTTON_WIDTH);
                    this.chartConfigurationPane.add(this.numberOfBinsLabel, 0, 2, 1, 1);
                        this.numberOfBinsTextField = new TextField();
                        this.numberOfBinsTextField.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.numberOfBinsTextField.tooltipText")));
                        GuiUtilities.setMinMaxPrefWidth(this.numberOfBinsTextField, GuiUtilities.BUTTON_WIDTH);
                    this.chartConfigurationPane.add(this.numberOfBinsTextField, 1, 2, 1, 1);
                        this.binBorderBox = new VBox(-5);
                            this.binBorderSlidersLabel = new Label(GeneralUtilities.getUIText("OutputTab.binBorderSlidersLabel.text"));
                        this.binBorderBox.getChildren().add(this.binBorderSlidersLabel);
                            this.binBorderSlidersBox = new VBox(-10);
                                this.binBorder1Slider = new Slider();
                                this.binBorder1Slider.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.binBorderSlider.tooltipText")));
                                this.binBorder1Slider.setOrientation(Orientation.HORIZONTAL);
                                GuiUtilities.setMinMaxPrefHeight(this.binBorder1Slider, 50);
                                this.binBorder1Slider.setShowTickMarks(true);
                                this.binBorder1Slider.setShowTickLabels(true);
                            this.binBorderSlidersBox.getChildren().add(this.binBorder1Slider);
                                this.binBorder2Slider = new Slider();
                                this.binBorder2Slider.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.binBorderSlider.tooltipText")));
                                this.binBorder2Slider.setOrientation(Orientation.HORIZONTAL);
                                GuiUtilities.setMinMaxPrefHeight(this.binBorder2Slider, 20);
                                this.binBorder2Slider.setShowTickMarks(false);
                                this.binBorder2Slider.setShowTickLabels(false);
                            this.binBorderSlidersBox.getChildren().add(this.binBorder2Slider);
                        this.binBorderBox.getChildren().add(this.binBorderSlidersBox);
                    this.chartConfigurationPane.add(this.binBorderBox, 0, 3, 2, 1);
                        this.frequencyBox = new VBox(-5);
                            this.frequencySlidersLabel = new Label(GeneralUtilities.getUIText("OutputTab.frequencySlidersLabel.text"));
                        this.frequencyBox.getChildren().add(this.frequencySlidersLabel);
                            this.frequencySlidersBox = new VBox(-10);
                                this.frequency1Slider = new Slider();
                                this.frequency1Slider.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.frequencySlider.tooltipText")));
                                this.frequency1Slider.setOrientation(Orientation.HORIZONTAL);
                                GuiUtilities.setMinMaxPrefHeight(this.frequency1Slider, 50);
                                this.frequency1Slider.setShowTickMarks(true);
                                this.frequency1Slider.setShowTickLabels(true);
                            this.frequencySlidersBox.getChildren().add(this.frequency1Slider);
                                this.frequency2Slider = new Slider();
                                this.frequency2Slider.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.frequencySlider.tooltipText")));
                                this.frequency2Slider.setOrientation(Orientation.HORIZONTAL);
                                GuiUtilities.setMinMaxPrefHeight(this.frequency2Slider, 20);
                                this.frequency2Slider.setShowTickMarks(false);
                                this.frequency2Slider.setShowTickLabels(false);
                            this.frequencySlidersBox.getChildren().add(this.frequency2Slider);
                        this.frequencyBox.getChildren().add(this.frequencySlidersBox);
                    this.chartConfigurationPane.add(this.frequencyBox, 0, 4, 2, 1);
                this.controlsPane.add(this.chartConfigurationPane, 0, 4, 2, 1);
            this.contentPane.getChildren().add(this.controlsPane);
                this.chartPane = new AnchorPane();
                this.chartPane.setBorder(GuiUtilities.RAISED_BORDER);
                this.chartPane.setPrefWidth(999999);
                    this.imageButton = new Button(GeneralUtilities.getUIText("OutputTab.saveButton.text"));
                    this.imageButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("OutputTab.saveButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefWidth(this.imageButton, GuiUtilities.BUTTON_WIDTH);
                AnchorPane.setRightAnchor(this.imageButton, 10.0);
                AnchorPane.setTopAnchor(this.imageButton, 10.0);
                this.chartPane.getChildren().add(this.imageButton);
            this.contentPane.getChildren().add(this.chartPane);
        this.setContent(this.contentPane);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    //<editor-fold defaultstate="collapsed" desc="- Public properties">
    /**
     * Get the chartChoiceBox
     *
     * @return the chartChoiceBox
     */
    public ChoiceBox<ComparisonFeature> getChartChoiceBox() {
        return this.chartChoiceBox;
    }
    
    /**
     * Get the binLabelChoiceBox
     *
     * @return the binLabelChoiceBox
     */
    public ChoiceBox<BinLabelType> getBinLabelChoiceBox() {
        return this.binLabelChoiceBox;
    }
    
    /**
     * Get the numberOfBinsTextField
     *
     * @return the numberOfBinsTextField
     */
    public TextField getNumberOfBinsTextField() {
        return this.numberOfBinsTextField;
    }
    
    /**
     * Get the chartConfigurationButton
     *
     * @return the chartConfigurationButton
     */
    public Button getChartConfigurationButton() {
        return this.chartConfigurationButton;
    }
    
    /**
     * Get the imageButton
     *
     * @return the imageButton
     */
    public Button getImageButton() {
        return this.imageButton;
    }
    
    /**
     * Get the summaryReportButton
     *
     * @return the summaryReportButton
     */
    public Button getSummaryReportButton() {
        return this.summaryReportButton;
    }
    
    /**
     * Get the defaultButton
     *
     * @return the defaultButton
     */
    public Button getDefaultButton() {
        return this.defaultButton;
    }
    
    /**
     * Get the binBorderConfigurationButton
     *
     * @return the binBorderConfigurationButton
     */
    public Button getBinBorderConfigurationButton() {
        return this.binBorderConfigurationButton;
    }
    
    /**
     * Get the relativeFrequenciesCheckBox
     *
     * @return the relativeFrequenciesCheckBox
     */
    public CheckBox getRelativeFrequenciesCheckBox() {
        return this.relativeFrequenciesCheckBox;
    }
    
    /**
     * Get the barLabelCheckBox
     *
     * @return the barLabelCheckBox
     */
    public CheckBox getBarLabelCheckBox() {
        return this.barLabelCheckBox;
    }
    
    /**
     * Get the infoLabel
     *
     * @return the infoLabel
     */
    public Label getInfoLabel() {
        return this.infoLabel;
    }
    
    /**
     * Get the frequency1Slider
     *
     * @return the frequency1Slider
     */
    public Slider getFrequency1Slider() {
        return this.frequency1Slider;
    }
    
    /**
     * Get the frequency2Slider
     *
     * @return the frequency2Slider
     */
    public Slider getFrequency2Slider() {
        return this.frequency2Slider;
    }
    
    /**
     * Get the binBorder1Slider
     *
     * @return the binBorder1Slider
     */
    public Slider getBinBorder1Slider() {
        return this.binBorder1Slider;
    }
    
    /**
     * Get the binBorder2Slider
     *
     * @return the binBorder2Slider
     */
    public Slider getBinBorder2Slider() {
        return this.binBorder2Slider;
    }
    
    /**
     * Get the chartPane
     * 
     * @return the chartPane
     */
    public AnchorPane getChartPane() {
        return this.chartPane;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Other methods">
    /**
     * Set the set of ComparisonFeatures that will be the items of the 
     * chartChoiceBox
     *
     * @param tmpComparisonFeatureSet specifies the set of ComparisonFeatures
     */
    public void setChartChoiceBoxItems(Set<ComparisonFeature> tmpComparisonFeatureSet) {
        this.chartChoiceBox.setItems(FXCollections.observableArrayList(tmpComparisonFeatureSet));
    }
    
    /**
     * Set the set of BinLabelTypes that will be the items of the 
     * binLabelChoiceBox
     *
     * @param tmpBinLabelTypeSet specifies the set of BinLabelTypes
     */
    public void setBinLabelChoiceBoxItems(Set<BinLabelType> tmpBinLabelTypeSet) {
        this.binLabelChoiceBox.setItems(FXCollections.observableArrayList(tmpBinLabelTypeSet));
    }
    
    /**
     * Set a region that will be displayed in the chartPane
     *
     * @param tmpRegion specifies the region
     */
    public void setComparisonFeatureChart(Region tmpRegion){
        this.chartPane.getChildren().clear();
        AnchorPane.setRightAnchor(this.imageButton, 10.0);
        AnchorPane.setTopAnchor(this.imageButton, 10.0);
        this.chartPane.getChildren().add(this.imageButton);
        AnchorPane.setLeftAnchor(tmpRegion, 0.0);
        AnchorPane.setTopAnchor(tmpRegion, 38.0);
        // Alignes the chart with the button
        AnchorPane.setRightAnchor(tmpRegion, 3.0);
        AnchorPane.setBottomAnchor(tmpRegion, 0.0);
        this.chartPane.getChildren().add(tmpRegion);
    }
    //</editor-fold>
    //</editor-fold>
}
