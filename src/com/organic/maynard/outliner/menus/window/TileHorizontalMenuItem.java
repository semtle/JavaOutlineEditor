/**
 * Copyright (C) 2002  Stan Krute <Stan@StanKrute.com>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 * 
 *  - Redistributions in binary form must reproduce the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided 
 *    with the distribution. 
 * 
 *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
 

/**
 * @author  $Author: maynardd $
 * @version $Revision: 1.3 $, $Date: 2004/02/02 10:17:42 $
 */

package com.organic.maynard.outliner.menus.window;

import com.organic.maynard.outliner.menus.*;
import com.organic.maynard.outliner.*;
import com.organic.maynard.outliner.guitree.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.xml.sax.*;
import java.util.Vector;

public class TileHorizontalMenuItem 
	extends AbstractOutlinerMenuItem 
	implements ActionListener, GUITreeComponent 
{
	
	// GUITreeComponent interface
	public void startSetup(Attributes atts) {
		super.startSetup(atts);
		addActionListener(this);
	}
	
	
	// ActionListener Interface
	public void actionPerformed(ActionEvent e) {
		// determine how many documents are open
		int openDocCount = Outliner.documents.openDocumentCount();
		
		// if no documents are open, leave
		if (openDocCount == 0){
			return ;
		}
		
		// a general purpose doc var
		OutlinerDocument doc = null;
		
		// let's build a list of not-iconified windows
		// [we don't touch iconified windows]
		Vector notIconified = new Vector();
		
		// for each open document
		for (int counter = 0; counter < openDocCount; counter++) {
			// grab the doc ref
			doc = (OutlinerDocument) Outliner.documents.getDocument(counter);
			
			// if we're not iconified
			if (! doc.isIcon()) {
				// add us to the list
				notIconified.add(doc);
			}
		}
		
		// store the count
		int openNotIconifiedDocCount = notIconified.size();
		
		// if everybody's iconified, leave
		if (openNotIconifiedDocCount == 0) {
			return;
		}
		
		// if we're in a maximized state ...
		if (Outliner.desktop.isMaximized()) {
			// leave that state
			Outliner.desktop.setMaximized(false);
			
			// if there's a topmost window
			doc = (OutlinerDocument) Outliner.documents.getMostRecentDocumentTouched();
			if (doc != null) {
				// have it leave the max state
				try {
					doc.setMaximum(false) ;
				} catch (java.beans.PropertyVetoException pve) {
					pve.printStackTrace();
				}
			}
		}
		
		// get the raw available space
		Dimension curAvailSpace = Outliner.desktop.getCurrentAvailableSpace();
		
		// since we're tiling, any scrollbars will melt away
		Outliner.desktop.addScrollbarsToAvailSpace (curAvailSpace);
		
		// determine how much room we have to play with
		double availWidth = curAvailSpace.getWidth();
		double availHeight = curAvailSpace.getHeight();
		
		// obtain minimum tiling row height
		// TBD -- for now we hardwire fake it
		int minTileRowHeight = 45;
		
		// determine the maximum number of rows
		int maxRows = (int)availHeight/minTileRowHeight;
		
		// some row vars
		int nominalRowHeight = 0;
		int finalRowHeight = 0;
		int actualRows = 0;
		
		// plenty of room ?
		boolean plentyOfRoom = maxRows >= openNotIconifiedDocCount;
		
		// determine actual number of rows we'll need
		actualRows = plentyOfRoom
			? openNotIconifiedDocCount 
			: maxRows;
		
		// some row arrays
		int [] rowHeights = new int[actualRows];
		int [] rowPositions = new int [actualRows];
		
		// if we have plenty of rows
		if (plentyOfRoom) {
			// we have a limit
			int limit = openNotIconifiedDocCount - 1;
			
			// determine nominal row height
			nominalRowHeight = (int) (availHeight/openNotIconifiedDocCount);
			// determine final row height
			finalRowHeight = (int)availHeight - (nominalRowHeight * limit);
			
			// set up all but the last row
			for (int row = 0; row < limit; row++) {
				rowHeights[row] = nominalRowHeight;
				rowPositions[row] = row * nominalRowHeight;
			}
			
			// set up the last row
			rowHeights[limit] = finalRowHeight ;
			rowPositions[limit] = (int)availHeight - finalRowHeight;
		// else we must squeeze extras on bottom rows
		} else {
			// how many extras are there ??
			// what's the minimum width ??
			// how many extras fit in a row 
			// do we have enuf rows for extras
		}
		
		// okay, everything's figured
		
		// some vars for window size and location info
		Point pLocation = new Point();
		Dimension dSize = new Dimension();
		
		// for each open doc
		for (int counter = 0; counter < openNotIconifiedDocCount; counter++) {
			// TBD [srk] make this a bit slicker
			// tile em in z order
			// right now we just use chrono order
			
			// grab the doc ref
			doc = (OutlinerDocument)notIconified.get(counter);
			
			// set up location
			pLocation.setLocation(0, rowPositions[counter]);
			
			// set up size
			dSize.setSize((int)availWidth, rowHeights[counter]);
			
			// set the doc's new location and size
			doc.setLocation(pLocation);
			doc.setSize(dSize);
		}
		
		// clean up the horizontal scrollbar's area
		Outliner.jsp.getHorizontalScrollBar().revalidate();
		
		// if it's greater than the number of rows, the leftovers
		// go in the bottom row, at minimum width, and if more leftovers,
		// spill up to next row, etc.\
		// determine row height
		// determine row y positions
		// for a one-doc row
		//	set width to max
		//	set height 
		// for a multi-doc row
		//	determine # of docs to be shown
		//	divide up space
		//	set widths
		//	set height
		// set one row at a time, top to bottom
		// order is same as document z-ordering
//		// if we're not in a totally-maximized state 
//		// [which would make this all pointless] ...
//		if (!Outliner.desktop.desktopManager.isMaximized()) {
//			
//			// grabaholda the topmost doc
//			OutlinerDocument doc = (OutlinerDocument) Outliner.documents.getMostRecentDocumentTouched();
//			
//      // tell it to leave maximized state
//			// see how wide we can get
//			Dimension curAvailSpace = Outliner.desktop.getCurrentAvailableSpace() ; 
//			double maxWidth = curAvailSpace.getWidth() ;
//			
//			// get the doc's current location
//			Point pLocation = doc.getLocation() ;
//			
//			// set its left point to the left edge of the content area
//			pLocation.setLocation(0, pLocation.getY()) ;
//			
//			// get the doc's current size
//			Dimension dSize = new Dimension() ;
//			dSize.setSize((int)maxWidth, (int)doc.getSize().getHeight()) ;
//			
//			// set the doc's new location and size
//			doc.setLocation(pLocation) ;
//			doc.setSize(dSize) ;
//			
//			// let the vertical scroll bar adjust for our new size
//			Outliner.jsp.getVerticalScrollBar().revalidate();
//			
//		} // end if we're not totally maximized
	}
}