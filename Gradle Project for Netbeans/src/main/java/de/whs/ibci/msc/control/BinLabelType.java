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
package de.whs.ibci.msc.control;

/**
 * Defines different types of bin labels
 * 
 * @author Jan-Mathis Hein
 */
public enum BinLabelType {
    
    //<editor-fold defaultstate="collapsed" desc="Definitions">
    MIN ("Lower bin border"), MAX ("Upper bin border"), MEAN ("Mean of both borders"), INTERVAL ("Interval");
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * A more precise description for the BinLabelType
     */
    private final String description;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize the instance variables
     *
     * @param tmpDescription a more precise description for the BinLabelType
     */
    private BinLabelType(String tmpDescription) {
        this.description = tmpDescription;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Get a more precise description for the BinLabelType
     *
     * @return a more precise description for the BinLabelType
     */
    @Override
    public String toString() {
        return this.description;
    }
    //</editor-fold>
    
}
