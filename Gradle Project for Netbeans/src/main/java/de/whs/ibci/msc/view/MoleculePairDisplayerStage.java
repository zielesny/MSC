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

import de.whs.ibci.msc.model.ComparisonFeature;
import de.whs.ibci.msc.utility.GuiUtilities;
import de.whs.ibci.msc.utility.GeneralUtilities;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * A window or more specifically a Stage on which a collection of molecule pairs 
 * can be displayed.
 *
 * @author Jan-Mathis Hein
 */
public class MoleculePairDisplayerStage extends Stage {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final Button saveLeftListButton, saveRightListButton, saveLeftImageButton, saveRightImageButton, nextButton, previousButton, lastButton, firstButton;
    
    private final Scene scene;
    
    private final TextField positionTextField;
    
    private final ImageView leftMoleculeImageView, rightMoleculeImageView;
    
    private final Label infoLabel;
    
    private final HBox moleculePairBox, positionControlsBox, otherControlsBox;
    
    private final VBox controlsBox;
    
    private final GridPane mainPane, infoPane, savingControlsPane;
    
    private final FlowPane additionalComparisonFeatureControlsPane;
    
    private final ScrollPane additionalComparisonFeaturesScrollPane;
    
    private final StackPane leftMoleculePane, rightMoleculePane;
    
    private final Control[][] additionalComparisonFeatureControls;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Creates the stage with all its controls and handle their layout and size
     */
    public MoleculePairDisplayerStage() {
        super();
        this.mainPane = new GridPane();
        this.mainPane.setPadding(GuiUtilities.STANDARD_INSETS);
        this.mainPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
        this.mainPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
        this.mainPane.getColumnConstraints().addAll(
            new ColumnConstraints(60, 100, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true),
            new ColumnConstraints()
        );
        this.mainPane.getRowConstraints().addAll(
            new RowConstraints(),
            new RowConstraints(49.6, 49.6, Double.MAX_VALUE, Priority.ALWAYS, VPos.TOP, true)
        );
            this.moleculePairBox = new HBox();
            this.moleculePairBox.setBorder(GuiUtilities.RAISED_BORDER);
                this.leftMoleculePane = new StackPane();
                this.leftMoleculePane.setBorder(new Border(new BorderStroke(
                            Paint.valueOf("Black"), Paint.valueOf("Black"), Paint.valueOf("Black"),
                            Paint.valueOf("Black"), BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                            new CornerRadii(0), new BorderWidths(2, 1, 2, 2), new Insets(10, 0, 10, 10)
                )));
                    this.leftMoleculeImageView = new ImageView();
                    this.leftMoleculeImageView.setPreserveRatio(true);
                this.leftMoleculePane.getChildren().add(this.leftMoleculeImageView);
                this.rightMoleculePane = new StackPane();
                this.rightMoleculePane.setBorder(new Border(new BorderStroke(
                            Paint.valueOf("Black"), Paint.valueOf("Black"), Paint.valueOf("Black"),
                            Paint.valueOf("Black"), BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                            BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                            new CornerRadii(0), new BorderWidths(2, 2, 2, 1), new Insets(10, 10, 10, 0)
                )));
                    this.rightMoleculeImageView = new ImageView();
                    this.rightMoleculeImageView.setPreserveRatio(true);
                this.rightMoleculePane.getChildren().add(this.rightMoleculeImageView);
            this.moleculePairBox.getChildren().addAll(this.leftMoleculePane, this.rightMoleculePane);
        this.mainPane.add(this.moleculePairBox, 1, 0, 1, 1);
            this.infoPane = new GridPane();
            this.infoPane.setBorder(GuiUtilities.RAISED_BORDER);
            this.infoPane.setPadding(GuiUtilities.STANDARD_INSETS);
            this.infoPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
            this.infoPane.setMaxWidth(Double.MAX_VALUE);
            this.infoPane.getRowConstraints().addAll(
                new RowConstraints(),
                new RowConstraints(20, 20, Double.MAX_VALUE, Priority.ALWAYS, VPos.TOP, true)
            );
            this.infoPane.getColumnConstraints().addAll(
                new ColumnConstraints(40, 80, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true)
            );
                this.infoLabel = new Label();
                this.infoLabel.setWrapText(true);
                this.infoLabel.setAlignment(Pos.TOP_LEFT);
                this.infoLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            this.infoPane.add(this.infoLabel, 0, 0);
                this.additionalComparisonFeaturesScrollPane = new ScrollPane();
                this.additionalComparisonFeaturesScrollPane.setFitToWidth(true);
                this.additionalComparisonFeaturesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
                this.additionalComparisonFeaturesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                this.additionalComparisonFeaturesScrollPane.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.additionalComparisonFeatureControlsPane = new FlowPane();
                    this.additionalComparisonFeatureControlsPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                    this.additionalComparisonFeatureControlsPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                        this.additionalComparisonFeatureControls = new Control[ComparisonFeature.values().length][2];
                        CheckBox tmpCheckBox;
                        Label tmpLabel;
                        HBox tmpBox;
                        for (int i = 0; i < this.additionalComparisonFeatureControls.length; i++) {
                            tmpBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                            GuiUtilities.setMinMaxPrefWidth(tmpBox, 320);
                                tmpCheckBox = new CheckBox(ComparisonFeature.values()[i].toString());
                                tmpCheckBox.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.additionalComparisonFeatureCheckBox.tooltipText")));
                                GuiUtilities.setMinMaxPrefWidth(tmpCheckBox, 160);
                                this.additionalComparisonFeatureControls[i][0] = tmpCheckBox;
                            tmpBox.getChildren().add(tmpCheckBox);
                                tmpLabel = new Label("");
                                this.additionalComparisonFeatureControls[i][1] = tmpLabel;
                            tmpBox.getChildren().add(tmpLabel);
                            this.additionalComparisonFeatureControlsPane.getChildren().add(tmpBox);
                        }
                this.additionalComparisonFeaturesScrollPane.setContent(this.additionalComparisonFeatureControlsPane);
            this.infoPane.add(this.additionalComparisonFeaturesScrollPane, 0, 1);
        this.mainPane.add(this.infoPane, 0, 0, 1, 2);
            this.controlsBox = new VBox(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
            this.controlsBox.setBorder(GuiUtilities.RAISED_BORDER);
            this.controlsBox.setPadding(GuiUtilities.STANDARD_INSETS);
                this.positionControlsBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.positionControlsBox.setAlignment(Pos.TOP_CENTER);
                    this.firstButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.firstButton.text"));
                    this.firstButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.firstButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.firstButton, 30, GuiUtilities.BUTTON_HEIGHT);
                    this.previousButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.previousButton.text"));
                    this.previousButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.previousButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.previousButton, 30, GuiUtilities.BUTTON_HEIGHT);
                    this.positionTextField = new TextField();
                    this.positionTextField.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.positionTextField.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.positionTextField, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.nextButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.nextButton.text"));
                    this.nextButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.nextButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.nextButton, 30, GuiUtilities.BUTTON_HEIGHT);
                    this.lastButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.lastButton.text"));
                    this.lastButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.lastButton.tooltipText")));
                    GuiUtilities.setMinMaxPrefSize(this.lastButton, 30, GuiUtilities.BUTTON_HEIGHT);
                this.positionControlsBox.getChildren().addAll(this.firstButton, this.previousButton, this.positionTextField, this.nextButton, this.lastButton);
            this.controlsBox.getChildren().addAll(this.positionControlsBox);
                this.otherControlsBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                    this.savingControlsPane = new GridPane();
                    this.savingControlsPane.setVgap(GuiUtilities.VERTICAL_GAP_BETWEEN_CONTROLS);
                    this.savingControlsPane.setHgap(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                    this.savingControlsPane.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.savingControlsPane.setBorder(GuiUtilities.LOWERED_BORDER);
                        this.saveLeftListButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveLeftListButton.text"));
                        this.saveLeftListButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveLeftListButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefSize(this.saveLeftListButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.savingControlsPane.add(this.saveLeftListButton, 0, 0);
                        this.saveRightListButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveRightListButton.text"));
                        this.saveRightListButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveRightListButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefSize(this.saveRightListButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.savingControlsPane.add(this.saveRightListButton, 0, 1);
                        this.saveLeftImageButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveLeftImageButton.text"));
                        this.saveLeftImageButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveLeftImageButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefSize(this.saveLeftImageButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.savingControlsPane.add(this.saveLeftImageButton, 1, 0);
                        this.saveRightImageButton = new Button(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveRightImageButton.text"));
                        this.saveRightImageButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MoleculePairDisplayerStage.saveRightImageButton.tooltipText")));
                        GuiUtilities.setMinMaxPrefSize(this.saveRightImageButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.savingControlsPane.add(this.saveRightImageButton, 1, 1);
                this.otherControlsBox.getChildren().add(this.savingControlsPane);
            this.controlsBox.getChildren().add(this.otherControlsBox);
        this.mainPane.add(this.controlsBox, 1, 1, 1, 1);
        this.scene = new Scene(this.mainPane);
        this.scene.getStylesheets().add("resources/MainStyle.css");
        this.setScene(this.scene);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the nextButton
     * 
     * @return the nextButton
     */
    public Button getNextButton() {
        return this.nextButton;
    }
    
    /**
     * Get the previousButton
     * 
     * @return the previousButton
     */
    public Button getPreviousButton() {
        return this.previousButton;
    }
    
    /**
     * Get the saveLeftListButton
     * 
     * @return the saveLeftListButton
     */
    public Button getSaveLeftListButton() {
        return this.saveLeftListButton;
    }
    
    /**
     * Get the saveRightListButton
     * 
     * @return the saveRightListButton
     */
    public Button getSaveRightListButton() {
        return this.saveRightListButton;
    }
    
    /**
     * Get the saveLeftImageButton
     * 
     * @return the saveLeftImageButton
     */
    public Button getSaveLeftImageButton() {
        return this.saveLeftImageButton;
    }
    
    /**
     * Get the saveRightImageButton
     * 
     * @return the saveRightImageButton
     */
    public Button getSaveRightImageButton() {
        return this.saveRightImageButton;
    }

    /**
     * Get the lastButton
     * 
     * @return the lastButton
     */
    public Button getLastButton() {
        return this.lastButton;
    }

    /**
     * Get the firstButton
     * 
     * @return the firstButton
     */
    public Button getFirstButton() {
        return this.firstButton;
    }
    
    /**
     * Get the infoPane
     * 
     * @return the infoPane
     */
    public GridPane getInfoPane() {
        return this.infoPane;
    }
    
    /**
     * Get the leftMoleculeImageView
     * 
     * @return the leftMoleculeImageView
     */
    public ImageView getLeftMoleculeImageView() {
        return this.leftMoleculeImageView;
    }
    
    /**
     * Get the rightMoleculeImageView
     * 
     * @return the rightMoleculeImageView
     */
    public ImageView getRightMoleculeImageView() {
        return this.rightMoleculeImageView;
    }

    /**
     * Get the infoLabel
     * 
     * @return the infoLabel
     */
    public Label getInfoLabel() {
        return infoLabel;
    }

    /**
     * Get the positionTextField
     * 
     * @return the positionTextField
     */
    public TextField getPositionTextField() {
        return this.positionTextField;
    }
    
    /**
     * Get the additionalComparisonFeatureControls array where each element is 
     * an array of the form {checkbox, label}
     * 
     * @return the additionalComparisonFeatureControls array
     */
    public Control[][] getAdditionalComparisonFeatureControls() {
        return this.additionalComparisonFeatureControls;
    }
    //</editor-fold>
    
}
