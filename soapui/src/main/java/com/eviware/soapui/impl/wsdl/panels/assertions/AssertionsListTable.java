package com.eviware.soapui.impl.wsdl.panels.assertions;

import java.util.List;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

public class AssertionsListTable extends JXTable
{
	private List<Integer> nonSelectableIndexes;
	private boolean selectable;

	public AssertionsListTable( TableModel tableModel )
	{
		super( tableModel );
	}

	public void setNonSelectableIndexes( List<Integer> nonSelectableIndexes )
	{
		this.nonSelectableIndexes = nonSelectableIndexes;
	}

	public void setSelectable( boolean selectable )
	{
		this.selectable = selectable;
	}

	@Override
	/*
	 * Used for disabling certain assertions to be selected. When
	 * AddAssertionPanel is opened and no category has yet been selected all
	 * needs to be disabled (case of recently used assertions being available in
	 * the list but shouldn't be selectable ). After category is selected
	 * underlying assertions are enabled/disabled depending on assertions being
	 * applicable for the source/property
	 */
	public void changeSelection( int rowIndex, int columnIndex, boolean toggle, boolean extend )
	{
		if( !selectable )
			rowIndex = -1;
		else if( nonSelectableIndexes != null && nonSelectableIndexes.contains( rowIndex ) )
		{
			int currentIndex = getSelectedRow();
			if( rowIndex != currentIndex )
			{
				rowIndex = -1;
			}
		}
		// make the selection change
		super.changeSelection( rowIndex, columnIndex, toggle, extend );
	}

}
