/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.request;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.rest.panels.component.RestResourceEditor;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestInterface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.JXToolBar;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.*;
import java.awt.*;

public class RestRequestDesktopPanel extends
		AbstractRestRequestDesktopPanel<RestRequestInterface, RestRequestInterface>
{
	protected TextPanelWithTopLabel resourcePanel;
	protected TextPanelWithTopLabel queryPanel;
	private JButton addToTestCaseButton;
	private MutableBoolean updating;

	public RestRequestDesktopPanel( RestRequestInterface modelItem )
	{
		super( modelItem, modelItem );
	}

	@Override
	protected void initializeFields()
	{
		String path = getRequest().getResource().getFullPath();
		updating = new MutableBoolean();
		resourcePanel = new TextPanelWithTopLabel( "Resource", path, new RestResourceEditor( getRequest().getResource(), updating ) );

		String query = RestUtils.getQueryParamsString( getRequest().getParams(), getRequest() );
		queryPanel = new TextPanelWithTopLabel( "Query", query, false );

	}

	@Override
	protected void init( RestRequestInterface request )
	{
		addToTestCaseButton = createActionButton( SwingActionDelegate.createDelegate(
				AddRestRequestToTestCaseAction.SOAPUI_ACTION_ID, getRequest(), null, "/addToTestCase.gif" ), true );

		super.init( request );
	}

	protected String getHelpUrl()
	{
		return HelpUrls.RESTREQUESTEDITOR_HELP_URL;
	}

	public void setEnabled( boolean enabled )
	{
		super.setEnabled( enabled );
		addToTestCaseButton.setEnabled( enabled );
	}

	@Override
	protected void addTopToolbarComponents( JXToolBar toolBar )
	{
		addResourceAndQueryField( toolBar );
	}

	@Override
	protected void addBottomToolbar( JPanel panel )
	{
		//RestRequestDesktopPanel does not need a bottom tool bar
	}

	@Override
	protected void updateUiValues()
	{
		if( updating.booleanValue() )
		{
			return;
		}
		updating.setValue( true );
		rebuildResourcePanelText();
		resetQueryPanelText();
		updating.setValue( false );
	}

	@Override
	protected void insertButtons( JXToolBar toolbar )
	{
		toolbar.add( addToTestCaseButton );
	}

	private void resetQueryPanelText()
	{
		queryPanel.setText( RestUtils.getQueryParamsString( getRequest().getParams(), getRequest() ) );

	}

	protected class TextPanelWithTopLabel extends JPanel
	{
		private final Color MAC_DISABLED_BGCOLOR = new Color( 232, 232, 232 );

		JLabel textLabel;
		JTextField textField;


		TextPanelWithTopLabel( String label, String text, JTextField textField )
		{
			textLabel = new JLabel( label );
			this.textField = textField;
			textField.setText( text );
			setToolTipText( text );
			super.setLayout( new BorderLayout() );
			super.add( textLabel, BorderLayout.NORTH );
			super.add( textField, BorderLayout.SOUTH );
		}

		public TextPanelWithTopLabel( String label, String text )
		{
			this( label, text, new JTextField() );
		}

		public TextPanelWithTopLabel( String label, String text, boolean isEditable )
		{
			this( label, text );
			textField.setEditable( isEditable );
			if( !isEditable && UISupport.isMac() )
			{
				textField.setBackground( MAC_DISABLED_BGCOLOR );
			}
		}

		public String getText()
		{
			return textField.getText();
		}

		public void setText( String text )
		{
			textField.setText( text );
			setToolTipText( text );
		}

		@Override
		public void setToolTipText( String text )
		{
			super.setToolTipText( text );
			textLabel.setToolTipText( text );
			textField.setToolTipText( text );
		}
	}

	private void addResourceAndQueryField( JXToolBar toolbar )
	{
		if( !( getRequest() instanceof RestTestRequestInterface ) )
		{

			toolbar.addWithOnlyMinimumHeight( resourcePanel );

			toolbar.add( Box.createHorizontalStrut( 4 ) );


			toolbar.addWithOnlyMinimumHeight( queryPanel );
		}
	}

	protected void rebuildResourcePanelText()
	{
		if( resourcePanel != null )
		{
			resourcePanel.setText( getRequest().getResource().getFullPath() );
		}
	}
}
