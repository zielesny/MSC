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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * A base framework for a Dialog for configuration purposes
 *
 * @author Jan-Mathis Hein
 */
public abstract class ConfigurationDialog extends Dialog<ButtonType> {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    private final Button applyButton, cancelButton;
    
    private final StackPane contentPane;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create the Dialog with all its controls and handle their layout and size
     * 
     * @param tmpOwner specifies the window that owns the Dialog
     * @param tmpTitle specifies the title of the Dialog
     */
    public ConfigurationDialog(Window tmpOwner, String tmpTitle) {
        super();
        this.initOwner(tmpOwner);
        this.setTitle(tmpTitle);
        this.setHeaderText(GeneralUtilities.getUIText("ConfigurationDialog.Header.text"));
            ButtonType tmpApplyButtonType = new ButtonType(GeneralUtilities.getUIText("ConfigurationDialog.applyButtonType.text"), ButtonBar.ButtonData.APPLY);
            ButtonType tmpCancelButtonType = new ButtonType(GeneralUtilities.getUIText("ConfigurationDialog.cancelButtonType.text"), ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().addAll(tmpApplyButtonType, tmpCancelButtonType);
            this.applyButton = (Button) this.getDialogPane().lookupButton(tmpApplyButtonType);
            GuiUtilities.setMinMaxPrefSize(this.applyButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
            this.applyButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("ConfigurationDialog.applyButton.tooltipText")));
            this.cancelButton = (Button) this.getDialogPane().lookupButton(tmpCancelButtonType);
            GuiUtilities.setMinMaxPrefSize(this.cancelButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
            this.cancelButton.setTooltip(new Tooltip(GeneralUtilities.getUIText("ConfigurationDialog.cancelButton.tooltipText")));
            this.contentPane = new StackPane();
            this.contentPane.setPadding(GuiUtilities.STANDARD_INSETS);
            this.contentPane.setBorder(new Border(new BorderStroke(
                        Color.WHITE, Color.WHITE, Color.color(0.1, 0.1, 0.1), 
                        Color.WHITE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, 
                        BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, 
                        new CornerRadii(0), new BorderWidths(0, 0, 2, 0), new Insets(0)
            )));
        this.getDialogPane().setContent(this.contentPane);
        
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public properties">
    /**
     * Get the cancelButton
     *
     * @return the cancelButton
     */
    public final Button getCancelButton() {
        return this.cancelButton;
    }
    
    /**
     * Get the applyButton
     *
     * @return the applyButton
     */
    public final Button getApplyButton() {
        return this.applyButton;
    }
    
    /**
     * Get the contentPane
     *
     * @return the contentPane
     */
    public final StackPane getContentPane() {
        return this.contentPane;
    }
    //</editor-fold>
    
}
