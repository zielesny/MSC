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
package de.whs.ibci.msc.model;

/**
 * An Exception that is thrown whenever the Molecule Set Comparator application 
 * received unexpected input with which it can't continue operating
 *
 * @author Jan-Mathis Hein
 */
public class MSCInputException extends Exception {
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * A serial version UID to check whether the loaded data is compatible
     */
    private static final long serialVersionUID = 1L;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * A message that furhter describes the exception
     */
    private String message;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Create an Exception without a message
     */
    public MSCInputException() {
        super();
    }
    
    /**
     * Create an Exception with the specified message
     * 
     * @param tmpMessage specifies the message
     */
    public MSCInputException(String tmpMessage) {
        super(tmpMessage);
        this.message = tmpMessage;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Return a string representation of the MSCInputException
     * 
     * @return a string representation of the MSCInputException
     */
    @Override
    public String toString() {
        return "InsufficientDataException: " + this.message;
    }
    //</editor-fold>
    
}
