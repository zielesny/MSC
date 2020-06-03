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
import de.whs.ibci.msc.model.InputType;
import de.whs.ibci.msc.model.ComparisonFeature;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * A Tab in which the user can specify his input for a job
 *
 * @author Jan-Mathis Hein
 */
public class InputTab extends Tab {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final ChoiceBox<InputType> inputType1ChoiceBox, inputType2ChoiceBox;
    
    private final Button chooseFile1Button, chooseFile2Button, jobStartButton,
        reverseComparisonFeatureSelectionButton, jobCancelButton, selectAllComparisonFeaturesButton;
    
    private final StackPane startCancelPane;
    
    private final ProgressBar jobProgressBar;
    
    private final Label inputFile1Label, inputFile2Label, jobInfoLabel;
    
    private final ScrollPane scrollPane;
    
    private final GridPane mainPane, inputFilesPane;
    
    private final HBox startProgressCancelBox;
    
    private final VBox tanimotoCheckBoxesBox, atomCountsCheckBoxesBox, bondCountsCheckBoxesBox, functionalGroupCountsCheckBoxesBox, 
        logPValuesCheckBoxesBox, autocorrelationCheckBoxesBox, electronicDescriptorsCheckBoxesBox, miscellaneousCheckBoxesBox, 
        comparisonFeatureSelectionBox;
    
    private final AnchorPane comparisonFeaturePane, comparisonFeatureButtonsPane, infoStartProgressCancelPane;
    
    private final CheckBox[] comparisonFeatureCheckBoxes;
    
    private final CheckBox selectAllTanimotoCheckBox, selectAllAtomCountsCheckBox, selectAllMiscellaneousCheckBox, 
        selectAllBondCountsCheckBox, selectAllFunctionalGroupCountsCheckBox, selectAllLogPValuesCheckBox, 
        selectAllAutocorrelationCheckBox, selectAllElectronicDescriptorsCheckBox;
    
    private final TitledPane buttonsTitledPane, tanimotoTitledPane, atomCountsTitledPane, 
        miscellaneousTitledPane, bondCountsTitledPane, functionalGroupCountsTitledPane, logPValuesTitledPane, 
        autocorrelationTitledPane, electronicDescriptorsTitledPane;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create the Tab with all its controls and handle their layout and size
     * 
     * @param tmpJobNumber specifies the title of the Tab
     */
    public InputTab(String tmpJobNumber) {
        super(GeneralUtilities.getUIText("InputTab.title.text") + tmpJobNumber);
        this.setClosable(true);
            this.mainPane = new GridPane();
            this.mainPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
            this.mainPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
            this.mainPane.getColumnConstraints().addAll(
                new ColumnConstraints(),
                new ColumnConstraints(234.0, 234.0, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true)
            );
            this.mainPane.getRowConstraints().addAll(
                new RowConstraints(142.4, 142.4, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true),
                new RowConstraints()
            );
            // The 12 is intentional
            this.mainPane.setPadding(new Insets(10, 10, 12, 10));
            //<editor-fold defaultstate="collapsed" desc="inputFilesPane">
                this.inputFilesPane = new GridPane();
                this.inputFilesPane.setPadding(GuiUtilities.STANDARD_INSETS);
                this.inputFilesPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.inputFilesPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                this.inputFilesPane.setBorder(GuiUtilities.RAISED_BORDER);
                    ObservableList<InputType> inputTypeCollection = FXCollections.observableArrayList(new LinkedList<>(Arrays.asList(InputType.values())));
                    this.inputType1ChoiceBox = new ChoiceBox<>(inputTypeCollection);
                    this.inputType1ChoiceBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.inputType1ChoiceBox.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.inputType1ChoiceBox, GuiUtilities.BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.inputType2ChoiceBox = new ChoiceBox<>(inputTypeCollection);
                    this.inputType2ChoiceBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.inputType2ChoiceBox.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.inputType2ChoiceBox, GuiUtilities.BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.chooseFile1Button = new Button(GeneralUtilities.getUIText("InputTab.chooseFile1Button.text"));
                    this.chooseFile1Button.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.chooseFile1Button.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.chooseFile1Button, GuiUtilities.BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.chooseFile2Button = new Button(GeneralUtilities.getUIText("InputTab.chooseFile2Button.text"));
                    this.chooseFile2Button.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.chooseFile2Button.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.chooseFile2Button, GuiUtilities.BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.inputFile1Label = new Label(GeneralUtilities.getUIText("InputTab.inputFile1Label.text"));
                    GuiUtilities.setMinMaxPrefWidth(this.inputFile1Label, GuiUtilities.BUTTON_WIDTH * 2 + GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                    this.inputFile2Label = new Label(GeneralUtilities.getUIText("InputTab.inputFile2Label.text"));
                    GuiUtilities.setMinMaxPrefWidth(this.inputFile2Label, GuiUtilities.BUTTON_WIDTH * 2 + GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.inputFilesPane.add(this.inputType1ChoiceBox, 0, 0);
                this.inputFilesPane.add(this.chooseFile1Button, 1, 0);
                this.inputFilesPane.add(this.inputFile1Label, 0, 1, 2, 1);
                this.inputFilesPane.add(this.inputType2ChoiceBox, 0, 2);
                this.inputFilesPane.add(this.chooseFile2Button, 1, 2);
                this.inputFilesPane.add(this.inputFile2Label, 0, 3, 2, 1);
            this.mainPane.add(this.inputFilesPane, 0, 0);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="comparisonFeaturesAndComparePane">
                this.comparisonFeaturePane = new AnchorPane();
                this.comparisonFeaturePane.setPadding(GuiUtilities.STANDARD_INSETS);
                this.comparisonFeaturePane.setBorder(GuiUtilities.RAISED_BORDER);
                    this.scrollPane = new ScrollPane();
                    this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                    // The content node is resized to fit the width of the scrollpane
                    this.scrollPane.setFitToWidth(true);
                        this.comparisonFeatureSelectionBox = new VBox();
                        this.comparisonFeatureSelectionBox.setStyle("-fx-background-color: rgb(200, 200, 200);");
                            this.buttonsTitledPane = new TitledPane();
                            this.buttonsTitledPane.setText(GeneralUtilities.getUIText("InputTab.comparisonFeatureButtonsTitlePane.text"));
                            this.buttonsTitledPane.setAnimated(false);
                            this.buttonsTitledPane.setExpanded(false);
                                this.comparisonFeatureButtonsPane = new AnchorPane();
                                this.comparisonFeatureButtonsPane.setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllComparisonFeaturesButton = new Button(GeneralUtilities.getUIText("InputTab.selectAllComparisonFeaturesButton.text"));
                                    this.selectAllComparisonFeaturesButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllComparisonFeaturesButton.tooltipText")));
                                    GuiUtilities.setMinMaxPrefSize(this.selectAllComparisonFeaturesButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                                    AnchorPane.setLeftAnchor(this.selectAllComparisonFeaturesButton, 0.0);
                                    AnchorPane.setTopAnchor(this.selectAllComparisonFeaturesButton, 0.0);
                                    this.reverseComparisonFeatureSelectionButton = new Button(GeneralUtilities.getUIText("InputTab.reverseComparisonFeatureSelectionButton.text"));
                                    this.reverseComparisonFeatureSelectionButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.reverseComparisonFeatureSelectionButton.tooltipText")));
                                    GuiUtilities.setMinMaxPrefSize(this.reverseComparisonFeatureSelectionButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                                    AnchorPane.setRightAnchor(this.reverseComparisonFeatureSelectionButton, 0.0);
                                    AnchorPane.setTopAnchor(this.reverseComparisonFeatureSelectionButton, 0.0);
                                this.comparisonFeatureButtonsPane.getChildren().setAll(this.selectAllComparisonFeaturesButton, this.reverseComparisonFeatureSelectionButton);
                            this.buttonsTitledPane.setContent(this.comparisonFeatureButtonsPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.buttonsTitledPane);
                            //<editor-fold defaultstate="collapsed" desc="Initialize TitledPanes">
                            this.tanimotoTitledPane = new TitledPane();
                            this.tanimotoTitledPane.setText(GeneralUtilities.getUIText("InputTab.tanimotoTitledPane.text"));
                            this.tanimotoTitledPane.setAnimated(false);
                            this.tanimotoTitledPane.setExpanded(false);
                            this.atomCountsTitledPane = new TitledPane();
                            this.atomCountsTitledPane.setText(GeneralUtilities.getUIText("InputTab.atomCountsTitledPane.text"));
                            this.atomCountsTitledPane.setAnimated(false);
                            this.atomCountsTitledPane.setExpanded(false);
                            this.bondCountsTitledPane = new TitledPane();
                            this.bondCountsTitledPane.setText(GeneralUtilities.getUIText("InputTab.bondCountsTitledPane.text"));
                            this.bondCountsTitledPane.setAnimated(false);
                            this.bondCountsTitledPane.setExpanded(false);
                            this.functionalGroupCountsTitledPane = new TitledPane();
                            this.functionalGroupCountsTitledPane.setText(GeneralUtilities.getUIText("InputTab.functionalGroupCountsTitledPane.text"));
                            this.functionalGroupCountsTitledPane.setAnimated(false);
                            this.functionalGroupCountsTitledPane.setExpanded(false);
                            this.logPValuesTitledPane = new TitledPane();
                            this.logPValuesTitledPane.setText(GeneralUtilities.getUIText("InputTab.logPValuesTitledPane.text"));
                            this.logPValuesTitledPane.setAnimated(false);
                            this.logPValuesTitledPane.setExpanded(false);
                            this.autocorrelationTitledPane = new TitledPane();
                            this.autocorrelationTitledPane.setText(GeneralUtilities.getUIText("InputTab.autocorrelationTitledPane.text"));
                            this.autocorrelationTitledPane.setAnimated(false);
                            this.autocorrelationTitledPane.setExpanded(false);
                            this.electronicDescriptorsTitledPane = new TitledPane();
                            this.electronicDescriptorsTitledPane.setText(GeneralUtilities.getUIText("InputTab.electricsDescriptorsTitledPane.text"));
                            this.electronicDescriptorsTitledPane.setAnimated(false);
                            this.electronicDescriptorsTitledPane.setExpanded(false);
                            this.miscellaneousTitledPane = new TitledPane();
                            this.miscellaneousTitledPane.setText(GeneralUtilities.getUIText("InputTab.miscellaneousTitledPane.text"));
                            this.miscellaneousTitledPane.setAnimated(false);
                            this.miscellaneousTitledPane.setExpanded(false);
                            //</editor-fold>
                                //<editor-fold defaultstate="collapsed" desc="Initialize VBoxes">
                                this.tanimotoCheckBoxesBox = new VBox();
                                this.tanimotoCheckBoxesBox.setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.tanimotoCheckBoxesBox.setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllTanimotoCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllTanimotoCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.tanimotoCheckBoxesBox.getChildren().add(this.selectAllTanimotoCheckBox);
                                this.atomCountsCheckBoxesBox = new VBox();
                                this.atomCountsCheckBoxesBox.setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.atomCountsCheckBoxesBox.setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllAtomCountsCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllAtomCountsCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.atomCountsCheckBoxesBox.getChildren().add(this.selectAllAtomCountsCheckBox);
                                this.bondCountsCheckBoxesBox = new VBox();
                                this.bondCountsCheckBoxesBox.setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.bondCountsCheckBoxesBox.setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllBondCountsCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllBondCountsCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.bondCountsCheckBoxesBox.getChildren().add(this.selectAllBondCountsCheckBox);
                                this.functionalGroupCountsCheckBoxesBox = new VBox();
                                this.functionalGroupCountsCheckBoxesBox.setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.functionalGroupCountsCheckBoxesBox.setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllFunctionalGroupCountsCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllFunctionalGroupCountsCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.functionalGroupCountsCheckBoxesBox.getChildren().add(this.selectAllFunctionalGroupCountsCheckBox);
                                this.logPValuesCheckBoxesBox = new VBox();
                                this.logPValuesCheckBoxesBox .setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.logPValuesCheckBoxesBox .setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllLogPValuesCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllLogPValuesCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.logPValuesCheckBoxesBox.getChildren().add(this.selectAllLogPValuesCheckBox);
                                this.autocorrelationCheckBoxesBox = new VBox();
                                this.autocorrelationCheckBoxesBox .setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.autocorrelationCheckBoxesBox .setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllAutocorrelationCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllAutocorrelationCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.autocorrelationCheckBoxesBox.getChildren().add(this.selectAllAutocorrelationCheckBox);
                                this.electronicDescriptorsCheckBoxesBox = new VBox();
                                this.electronicDescriptorsCheckBoxesBox .setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.electronicDescriptorsCheckBoxesBox .setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllElectronicDescriptorsCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllElectronicDescriptorsCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.electronicDescriptorsCheckBoxesBox.getChildren().add(this.selectAllElectronicDescriptorsCheckBox);
                                this.miscellaneousCheckBoxesBox = new VBox();
                                this.miscellaneousCheckBoxesBox.setSpacing(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                                this.miscellaneousCheckBoxesBox.setPadding(GuiUtilities.STANDARD_INSETS);
                                    this.selectAllMiscellaneousCheckBox = new CheckBox(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.text"));
                                    this.selectAllMiscellaneousCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.selectAllCheckBox.tooltipText")));
                                this.miscellaneousCheckBoxesBox.getChildren().add(this.selectAllMiscellaneousCheckBox);
                                //</editor-fold>
                                    //<editor-fold defaultstate="collapsed" desc="Initialize CheckBoxes">
                                    this.comparisonFeatureCheckBoxes = new CheckBox[ComparisonFeature.values().length];
                                    CheckBox tmpCheckBox;
                                    for (int i = 0; i < this.comparisonFeatureCheckBoxes.length; i++) {
                                        tmpCheckBox = new CheckBox(ComparisonFeature.values()[i].toString());
                                        tmpCheckBox.setTooltip(new Tooltip(ComparisonFeature.values()[i].getDetailedDescription()));
                                        if (i == ComparisonFeature.TANIMOTO_BASIC_FINGERPRINTER.getFeatureNumber() ||
                                                i == ComparisonFeature.TANIMOTO_LINGO_FINGERPRINTER.getFeatureNumber() ||
                                                i == ComparisonFeature.TANIMOTO_EXTENDED_FINGERPRINTER.getFeatureNumber() ||
                                                i == ComparisonFeature.TANIMOTO_ESTATE_FINGERPRINTER.getFeatureNumber() ||
                                                i == ComparisonFeature.TANIMOTO_PUBCHEM_FINGERPRINTER.getFeatureNumber() ||
                                                i == ComparisonFeature.TANIMOTO_SHORTEST_PATH_FINGERPRINTER.getFeatureNumber() ||
                                                i == ComparisonFeature.TANIMOTO_SUBSTRUCTURE_FINGERPRINTER.getFeatureNumber()) {
                                            this.tanimotoCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else if (i == ComparisonFeature.ATOM_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.CARBON_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.NITROGEN_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.OXYGEN_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.PHOSPHOR_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SULFUR_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.AROMATIC_ATOM_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C1SP1_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C1SP2_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C1SP3_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C2SP1_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C2SP2_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C2SP3_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C3SP2_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C3SP3_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.C4SP3_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SPIRO_ATOM_COUNT.getFeatureNumber()) {
                                            this.atomCountsCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else if (i == ComparisonFeature.BOND_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SINGLE_BOND_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.DOUBLE_BOND_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.TRIPLE_BOND_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.QUADRUPLE_BOND_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.AROMATIC_BOND_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.ROTATABLE_BOND_COUNT.getFeatureNumber()) {
                                            this.bondCountsCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else if (i == ComparisonFeature.ACIDIC_GROUP_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.BASIC_GROUP_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.H_BOND_ACCEPTOR_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.H_BOND_DONOR_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_3_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_4_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_5_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_6_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_7_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_8_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.SIZE_9_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.AROMATIC_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber() ||
                                                i == ComparisonFeature.ALL_SMALL_RINGS_COUNT.getFeatureNumber()) {
                                            this.functionalGroupCountsCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else if (i == ComparisonFeature.A_LOGP.getFeatureNumber() ||
                                                i == ComparisonFeature.A_LOGP_2.getFeatureNumber() ||
                                                i == ComparisonFeature.X_LOGP.getFeatureNumber() ||
                                                i == ComparisonFeature.MANNHOLD_LOGP.getFeatureNumber() ||
                                                i == ComparisonFeature.JP_LOGP.getFeatureNumber()) {
                                            this.logPValuesCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else if (i == ComparisonFeature.ATS_CHARGE1.getFeatureNumber() ||
                                                i == ComparisonFeature.ATS_MASS1.getFeatureNumber() ||
                                                i == ComparisonFeature.ATS_POLARIZABILITY1.getFeatureNumber()) {
                                            this.autocorrelationCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else if (i == ComparisonFeature.APOL.getFeatureNumber() ||
                                                i == ComparisonFeature.BPOL.getFeatureNumber() ||
                                                i == ComparisonFeature.FPSA.getFeatureNumber() ||
                                                i == ComparisonFeature.TPSA.getFeatureNumber() ||
                                                i == ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()) {
                                            this.electronicDescriptorsCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        } else {
                                            this.miscellaneousCheckBoxesBox.getChildren().add(tmpCheckBox);
                                        }
                                        this.comparisonFeatureCheckBoxes[i] = tmpCheckBox;
                                    }
                                    //</editor-fold>
                            this.tanimotoTitledPane.setContent(this.tanimotoCheckBoxesBox);
                            this.atomCountsTitledPane.setContent(this.atomCountsCheckBoxesBox);
                            this.bondCountsTitledPane.setContent(this.bondCountsCheckBoxesBox);
                            this.functionalGroupCountsTitledPane.setContent(this.functionalGroupCountsCheckBoxesBox);
                            this.logPValuesTitledPane.setContent(this.logPValuesCheckBoxesBox);
                            this.autocorrelationTitledPane.setContent(this.autocorrelationCheckBoxesBox);
                            this.electronicDescriptorsTitledPane.setContent(this.electronicDescriptorsCheckBoxesBox);
                            this.miscellaneousTitledPane.setContent(this.miscellaneousCheckBoxesBox);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.tanimotoTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.atomCountsTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.bondCountsTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.functionalGroupCountsTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.logPValuesTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.autocorrelationTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.electronicDescriptorsTitledPane);
                        this.comparisonFeatureSelectionBox.getChildren().add(this.miscellaneousTitledPane);
                    this.scrollPane.setContent(this.comparisonFeatureSelectionBox);
                    AnchorPane.setLeftAnchor(this.scrollPane, 0.0);
                    AnchorPane.setTopAnchor(this.scrollPane, 0.0);
                    AnchorPane.setRightAnchor(this.scrollPane, 0.0);
                    AnchorPane.setBottomAnchor(this.scrollPane, 0.0);
                this.comparisonFeaturePane.getChildren().add(this.scrollPane);
            this.mainPane.add(this.comparisonFeaturePane, 1, 0);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="jobProgressAndInfoBox">
                this.infoStartProgressCancelPane = new AnchorPane();
                this.infoStartProgressCancelPane.setBorder(GuiUtilities.RAISED_BORDER);
                this.infoStartProgressCancelPane.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.jobInfoLabel = new Label("");
                    AnchorPane.setLeftAnchor(this.jobInfoLabel, 0.0);
                    AnchorPane.setTopAnchor(this.jobInfoLabel, 0.0);
                    AnchorPane.setBottomAnchor(this.jobInfoLabel, 0.0);
                this.infoStartProgressCancelPane.getChildren().add(this.jobInfoLabel);
                    this.startProgressCancelBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                    this.startProgressCancelBox.setAlignment(Pos.CENTER);
                    AnchorPane.setRightAnchor(this.startProgressCancelBox, 0.0);
                    AnchorPane.setBottomAnchor(this.startProgressCancelBox, 0.0);
                    AnchorPane.setTopAnchor(this.startProgressCancelBox, 0.0);
                        this.jobProgressBar = new ProgressBar(0);
                        GuiUtilities.setMinMaxPrefWidth(this.jobProgressBar, GuiUtilities.BUTTON_WIDTH);
                    this.startProgressCancelBox.getChildren().add(this.jobProgressBar);
                        this.startCancelPane = new StackPane();
                            this.jobCancelButton = new Button(GeneralUtilities.getUIText("InputTab.jobCancelButton.text"));
                            this.jobCancelButton.setVisible(false);
                            this.jobCancelButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.jobCancelButton.tooltipText")));
                            GuiUtilities.setMinMaxPrefSize(this.jobCancelButton, GuiUtilities.BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                        this.startCancelPane.getChildren().add(this.jobCancelButton);
                            this.jobStartButton = new Button(GeneralUtilities.getUIText("InputTab.jobStartButton.text"));
                            this.jobStartButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("InputTab.jobStartButton.tooltipText")));
                            GuiUtilities.setMinMaxPrefSize(this.jobStartButton, GuiUtilities.BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                        this.startCancelPane.getChildren().add(this.jobStartButton);
                    this.startProgressCancelBox.getChildren().add(this.startCancelPane);
                this.infoStartProgressCancelPane.getChildren().add(this.startProgressCancelBox);
            this.mainPane.add(this.infoStartProgressCancelPane, 0, 1, 2, 1);
            //</editor-fold>
        this.setContent(this.mainPane);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the reverseComparisonFeatureSelectionButton
     * 
     * @return the reverseComparisonFeatureSelectionButton
     */
    public Button getReverseComparisonFeatureSelectionButton() {
        return this.reverseComparisonFeatureSelectionButton;
    }
    
    /**
     * Get the selectAllComparisonFeaturesButton
     * 
     * @return the selectAllComparisonFeaturesButton
     */
    public Button getSelectAllComparisonFeaturesButton() {
        return this.selectAllComparisonFeaturesButton;
    }
    
    /**
     * Get the buttonsTitledPane
     * 
     * @return the buttonsTitledPane
     */
    public TitledPane getButtonsTitledPane() {
        return this.buttonsTitledPane;
    }
    
    /**
     * Get a list containing all xxxCheckBoxesBox boxes. Order corresponds 
     * to the one of getSelectAllCheckBoxes()
     * 
     * @return a list containing all xxxCheckBoxesBox boxes
     */
    public List<VBox> getCheckBoxesBoxes() {
        LinkedList<VBox> tmpList = new LinkedList<>();
        tmpList.add(this.atomCountsCheckBoxesBox);
        tmpList.add(this.autocorrelationCheckBoxesBox);
        tmpList.add(this.bondCountsCheckBoxesBox);
        tmpList.add(this.electronicDescriptorsCheckBoxesBox);
        tmpList.add(this.functionalGroupCountsCheckBoxesBox);
        tmpList.add(this.logPValuesCheckBoxesBox);
        tmpList.add(this.miscellaneousCheckBoxesBox);
        tmpList.add(this.tanimotoCheckBoxesBox);
        return tmpList;
    }
    
    /**
     * Get a list containing all selectAllXxxCheckBox check boxes. Order 
     * corresponds to the one of getCheckBoxesBoxes()
     * 
     * @return a list containing all selectAllXxxCheckBox check boxes
     */
    public List<CheckBox> getSelectAllCheckBoxes() {
        LinkedList<CheckBox> tmpList = new LinkedList<>();
        tmpList.add(this.selectAllAtomCountsCheckBox);
        tmpList.add(this.selectAllAutocorrelationCheckBox);
        tmpList.add(this.selectAllBondCountsCheckBox);
        tmpList.add(this.selectAllElectronicDescriptorsCheckBox);
        tmpList.add(this.selectAllFunctionalGroupCountsCheckBox);
        tmpList.add(this.selectAllLogPValuesCheckBox);
        tmpList.add(this.selectAllMiscellaneousCheckBox);
        tmpList.add(this.selectAllTanimotoCheckBox);
        return tmpList;
    }
    
    /**
     * Get the comparisonFeatureCheckBoxes
     * 
     * @return the comparisonFeatureCheckBoxes
     */
    public CheckBox[] getComparisonFeatureCheckBoxes() {
        return this.comparisonFeatureCheckBoxes;
    }
    
    /**
     * Get the chooseFile1Button
     * 
     * @return the chooseFile1Button
     */
    public Button getChooseFile1Button() {
        return this.chooseFile1Button;
    }
    
    /**
     * Get the compareSetsButton
     * 
     * @return the compareSetsButton
     */
    public Button getJobStartButton() {
        return this.jobStartButton;
    }
    
    /**
     * Get the inputFile1Label
     * 
     * @return the inputFile1Label
     */
    public Label getInputFile1Label() {
        return this.inputFile1Label;
    }
    
    /**
     * Get the inputFile2Label
     * 
     * @return the inputFile2Label
     */
    public Label getInputFile2Label() {
        return this.inputFile2Label;
    }
    
    /**
     * Get the chooseFile2Button
     * 
     * @return the chooseFile2Button
     */
    public Button getChooseFile2Button() {
        return chooseFile2Button;
    }
    
    /**
     * Get the inputType1ChoiceBox
     * 
     * @return the inputType1ChoiceBox
     */
    public ChoiceBox<InputType> getInputType1ChoiceBox() {
        return this.inputType1ChoiceBox;
    }
    
    /**
     * Get the inputType2ChoiceBox
     * 
     * @return the inputType2ChoiceBox
     */
    public ChoiceBox<InputType> getInputType2ChoiceBox() {
        return this.inputType2ChoiceBox;
    }
    
    /**
     * Get the jobCancelButton
     * 
     * @return the jobCancelButton
     */
    public Button getJobCancelButton() {
        return this.jobCancelButton;
    }
    
    /**
     * Get the jobProgressBar
     * 
     * @return the jobProgressBar
     */
    public ProgressBar getJobProgressBar() {
        return this.jobProgressBar;
    }
    
    /**
     * Get the jobInfoLabel
     * 
     * @return the jobInfoLabel
     */
    public Label getJobInfoLabel() {
        return this.jobInfoLabel;
    }
    //</editor-fold>
    
}
