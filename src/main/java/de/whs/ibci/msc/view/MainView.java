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
import de.whs.ibci.msc.utility.MSCConstants;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The MainView holds the main application window which contains a MenuBar, 
 * the TabPane in which the In and OutputTabs will be displayed and 
 * some Controls
 *
 * @author Jan-Mathis Hein
 */
public class MainView {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final Button centerButton, minButton, maxButton;
    
    private final MenuBar menuBar;
    
    private final Menu preferencesMenu, logMenu, helpMenu, jobMenu, directoryMenu;
    
    private final MenuItem saveJobMenuItem, otherPreferencesMenuItem, newJobMenuItem,
        loadJobOutputMenuItem, defaultInputDirectoryMenuItem, defaultOutputDirectoryMenuItem, 
        defaultImageDirectoryMenutItem, defaultMoleculeListDirectoryMenuItem, browseLogMenuItem, 
        resetLogMenuItem, aboutMenuItem, defaultSummaryReportDirectoryMenuItem;
    
    private final Stage primaryStage;
    
    private final Scene scene;
    
    private final BorderPane mainPane;
    
    private final TabPane tabPane;
    
    private final HBox windowControlsBox;
    
    private final StackPane centerPane;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Set up the main window with all its controls and 
     * handle their layout and size
     * 
     * @param tmpPrimaryStage the main application window or more specifically 
     * the primary stage of the application
     */
    public MainView(Stage tmpPrimaryStage) {
        this.primaryStage = tmpPrimaryStage;
        this.primaryStage.setTitle(GeneralUtilities.getUIText("MainView.title.text") + " " + MSCConstants.MSC_VERSION);
            this.mainPane = new BorderPane();
                //<editor-fold defaultstate="collapsed" desc="menuBar">
                this.menuBar = new MenuBar();
                    this.jobMenu = new Menu(GeneralUtilities.getUIText("MainView.jobMenu.text"));
                    this.jobMenu.setStyle("-fx-background-color: rgb(180, 180, 180);-fx-border-color: rgb(150, 150, 150);");
                        this.newJobMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.newJobMenuItem.text"));
                    this.jobMenu.getItems().add(this.newJobMenuItem);
                        this.saveJobMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.saveJobMenuItem.text"));
                    this.jobMenu.getItems().add(this.saveJobMenuItem);
                        this.loadJobOutputMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.loadJobOutputMenuItem.text"));
                    this.jobMenu.getItems().add(this.loadJobOutputMenuItem);
                this.menuBar.getMenus().add(this.jobMenu);
                    this.preferencesMenu = new Menu(GeneralUtilities.getUIText("MainView.preferencesMenu.text"));
                    this.preferencesMenu.setStyle("-fx-background-color: rgb(180, 180, 180);-fx-border-color: rgb(150, 150, 150);");
                        this.directoryMenu = new Menu(GeneralUtilities.getUIText("MainView.directoryMenu.text"));
                            this.defaultInputDirectoryMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.inputDirectoryMenuItem.text"));
                            this.defaultOutputDirectoryMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.outputDirectoryMenuItem.text"));
                            this.defaultImageDirectoryMenutItem = new MenuItem(GeneralUtilities.getUIText("MainView.imageDirectoryMenuItem.text"));
                            this.defaultMoleculeListDirectoryMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.moleculeListDirectoryMenuItem.text"));
                            this.defaultSummaryReportDirectoryMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.summaryReportDirectoryMenuItem.text"));
                        this.directoryMenu.getItems().addAll(
                            this.defaultInputDirectoryMenuItem, this.defaultOutputDirectoryMenuItem, 
                            this.defaultImageDirectoryMenutItem, this.defaultMoleculeListDirectoryMenuItem, 
                            this.defaultSummaryReportDirectoryMenuItem
                        );
                    this.preferencesMenu.getItems().add(this.directoryMenu);
                        this.otherPreferencesMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.otherPreferencesMenuItem.text"));
                    this.preferencesMenu.getItems().add(this.otherPreferencesMenuItem);
                this.menuBar.getMenus().add(this.preferencesMenu);
                    this.helpMenu = new Menu(GeneralUtilities.getUIText("MainView.helpMenu.text"));
                    this.helpMenu.setStyle("-fx-background-color: rgb(180, 180, 180);-fx-border-color: rgb(150, 150, 150);");
                        this.logMenu = new Menu(GeneralUtilities.getUIText("MainView.logMenu.text"));
                            this.browseLogMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.browseLogMenuItem.text"));
                            this.resetLogMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.resetLogMenuItem.text"));
                        this.logMenu.getItems().addAll(this.browseLogMenuItem, this.resetLogMenuItem);
                    this.helpMenu.getItems().add(this.logMenu);
                        this.aboutMenuItem = new MenuItem(GeneralUtilities.getUIText("MainView.aboutMenuItem.text"));
                    this.helpMenu.getItems().add(this.aboutMenuItem);
                this.menuBar.getMenus().add(this.helpMenu);
            this.mainPane.setTop(this.menuBar);
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="tabPane">
                this.centerPane = new StackPane();
                this.centerPane.setStyle("-fx-background-color: rgb(220, 220, 220);");
                    this.tabPane = new TabPane();
                this.centerPane.getChildren().add(this.tabPane);
            this.mainPane.setCenter(this.centerPane);
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="windowControlsPane">
                this.windowControlsBox = new HBox(GuiUtilities.HORIZONTAL_GAP_BETWEEN_CONTROLS);
                this.windowControlsBox.setBorder(new Border(new BorderStroke(
                            Color.color(0.1, 0.1, 0.1), Color.WHITE, Color.WHITE, 
                            Color.WHITE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, 
                            BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, 
                            new CornerRadii(0), new BorderWidths(2, 0, 0, 0), new Insets(0)
                )));
                this.windowControlsBox.setPadding(GuiUtilities.STANDARD_INSETS);
                    this.maxButton = new Button(GeneralUtilities.getUIText("MainView.maxButton.text"));
                    GuiUtilities.setMinMaxPrefSize(this.maxButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.maxButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MainView.maxButton.tooltipText")));
                    this.minButton = new Button(GeneralUtilities.getUIText("MainView.minButton.text"));
                    GuiUtilities.setMinMaxPrefSize(this.minButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.minButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MainView.minButton.tooltipText")));
                    this.centerButton = new Button(GeneralUtilities.getUIText("MainView.centerButton.text"));
                    GuiUtilities.setMinMaxPrefSize(this.centerButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                    this.centerButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("MainView.centerButton.tooltipText")));
                this.windowControlsBox.getChildren().addAll(this.maxButton, this.minButton, this.centerButton);
            this.mainPane.setBottom(this.windowControlsBox);
                //</editor-fold>
        this.scene = new Scene(this.mainPane);
        this.scene.getStylesheets().add("resources/MainStyle.css");
        this.primaryStage.setScene(this.scene);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the browseLogMenuItem
     * 
     * @return the browseLogMenuItem
     */
    public MenuItem getBrowseLogMenuItem() {
        return this.browseLogMenuItem;
    }

    /**
     * Get the resetLogMenuItem
     * 
     * @return the resetLogMenuItem
     */
    public MenuItem getResetLogMenuItem() {
        return this.resetLogMenuItem;
    }

    /**
     * Get the otherPreferencesMenuItem
     * 
     * @return the otherPreferencesMenuItem
     */
    public MenuItem getOtherPreferencesMenuItem() {
        return this.otherPreferencesMenuItem;
    }

    /**
     * Get the defaultOutputDirectoryMenuItem
     * 
     * @return the defaultOutputDirectoryMenuItem
     */
    public MenuItem getDefaultOutputDirectoryMenuItem() {
        return this.defaultOutputDirectoryMenuItem;
    }

    /**
     * Get the defaultImageDirectoryMenutItem
     * 
     * @return the defaultImageDirectoryMenutItem
     */
    public MenuItem getDefaultImageDirectoryMenutItem() {
        return this.defaultImageDirectoryMenutItem;
    }

    /**
     * Get the defaultMoleculeListDirectoryMenuItem
     * 
     * @return the defaultMoleculeListDirectoryMenuItem
     */
    public MenuItem getDefaultMoleculeListDirectoryMenuItem() {
        return this.defaultMoleculeListDirectoryMenuItem;
    }

    /**
     * Get the defaultSummaryReportDirectoryMenuItem
     * 
     * @return the defaultSummaryReportDirectoryMenuItem
     */
    public MenuItem getDefaultSummaryReportDirectoryMenuItem() {
        return this.defaultSummaryReportDirectoryMenuItem;
    }

    /**
     * Get the newJobMenuItem
     * 
     * @return the newJobMenuItem
     */
    public MenuItem getNewJobMenuItem() {
        return this.newJobMenuItem;
    }

    /**
     * Get the loadJobOutputMenuItem
     * 
     * @return the loadJobOutputMenuItem
     */
    public MenuItem getLoadJobOutputMenuItem() {
        return this.loadJobOutputMenuItem;
    }

    /**
     * Get the defaultInputDirectoryMenuItem
     * 
     * @return the defaultInputDirectoryMenuItem
     */
    public MenuItem getDefaultInputDirectoryMenuItem() {
        return this.defaultInputDirectoryMenuItem;
    }

    /**
     * Get the saveJobMenuItem
     * 
     * @return the saveJobMenuItem
     */
    public MenuItem getSaveJobMenuItem() {
        return this.saveJobMenuItem;
    }

    /**
     * Get the aboutMenuItem
     * 
     * @return the aboutMenuItem
     */
    public MenuItem getAboutMenuItem() {
        return this.aboutMenuItem;
    }

    /**
     * Get the centerButton
     * 
     * @return the centerButton
     */
    public Button getCenterButton() {
        return this.centerButton;
    }

    /**
     * Get the minButton
     * 
     * @return the minButton
     */
    public Button getMinButton() {
        return this.minButton;
    }

    /**
     * Get the maxButton
     * 
     * @return the maxButton
     */
    public Button getMaxButton() {
        return this.maxButton;
    }

    /**
     * Get the primaryStage
     * 
     * @return the primaryStage
     */
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    /**
     * Get the scene
     * 
     * @return the scene
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * Get the centerPane
     * 
     * @return the centerPane
     */
    public StackPane getCenterPane() {
        return this.centerPane;
    }

    /**
     * Get the tabPane
     * 
     * @return the tabPane
     */
    public TabPane getTabPane() {
        return this.tabPane;
    }
    //</editor-fold>
    
}
