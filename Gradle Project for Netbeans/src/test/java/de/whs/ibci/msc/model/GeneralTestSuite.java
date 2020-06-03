/*
 * Copyright (C) 2019 Jan-Mathis Hein
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that combines all test classes
 *
 * @author Jan-Mathis Hein
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    de.whs.ibci.msc.model.ParseAndCompareTaskTest.class, 
    de.whs.ibci.msc.model.HistogramDataManagerTest.class
})
public class GeneralTestSuite {
    
}
