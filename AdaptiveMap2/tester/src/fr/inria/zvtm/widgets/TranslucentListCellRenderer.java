/*   AUTHOR :           Julien Husson (jhusson@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TranslucentListCellRenderer.java 3346 2010-06-11 11:44:08Z epietrig $
 */
 
package fr.inria.zvtm.widgets;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Identifies components that can be used as "rubber stamps" to paint the cells in a JList
 * @author julien-h
 *
 */

public class TranslucentListCellRenderer extends JLabel implements
		ListCellRenderer {

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	static final String ELLIPSIS = "...";
	static final int MAX_BOUND = 27;

	public Component getListCellRendererComponent(JList list, Object value, // value
			// to
			// display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createMatteBorder(0, 0, 0, 0, Color.lightGray), BorderFactory
				.createEmptyBorder(1, 6, 1, 3)));
		if (list.getName() != null) {
			String str = ((String) value).substring(0, ((String) value)
					.length() - 4);
			if (str.length() > MAX_BOUND) {
				String cardinality = ((String) value).substring(
						((String) value).length() - 4, ((String) value)
								.length());
				value = value.toString().substring(0, MAX_BOUND) + ELLIPSIS
						+ cardinality;
			}
		}
		setText(value.toString());
		setBackground(isSelected ? new Color(0, 255, 0, 40) : new Color(0, 0,
				0, 40));
		setForeground(isSelected ? list.getSelectionForeground() : list
				.getForeground());
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
	
}
