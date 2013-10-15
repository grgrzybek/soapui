/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.factories.ButtonBarFactory;
import org.apache.commons.lang.mutable.MutableBoolean;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Text field for editing a rest resource. Pops up a separate dialog to edit parts of the resource separately if the
 * rest resource has parents or children.
 */
public class RestResourceEditor extends JTextField
{
	private RestResource editingRestResource;
	private MutableBoolean updating;

	public RestResourceEditor( RestResource editingRestResource, MutableBoolean updating )
	{
		super( editingRestResource.getFullPath() );
		this.editingRestResource = editingRestResource;
		this.updating = updating;
		if( isResourceLonely( editingRestResource ) )
		{
			getDocument().addDocumentListener( new LonelyDocumentListener() );
			addFocusListener( new FocusListener()
			{
				public void focusLost( FocusEvent e )
				{
					scanForTemplateParameters();
				}

				public void focusGained( FocusEvent e )
				{
				}
			} );

		}
		else
		{
			//TODO: Do some better listening
			addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( MouseEvent e )
				{
					setEditable( false );
					showDialog();
					setEditable( true );
				}
			} );
		}
	}

	private void scanForTemplateParameters()
	{
		for( RestResource restResource : getRestResources() )
		{
			for( String p : RestUtils.extractTemplateParams( restResource.getPath() ) )
			{
				if( !resourceOrParentHasProperty( restResource, p ) )
				{
					if( UISupport.confirm( "Add template parameter [" + p + "] to resource?", "Add Parameter" ) )
					{
						RestParamProperty property = restResource.addProperty( p );
						property.setStyle( RestParamsPropertyHolder.ParameterStyle.TEMPLATE );
						String value = UISupport.prompt( "Specify default value for parameter [" + p + "]",
								"Add Parameter", "" );
						if( value != null )
						{
							property.setDefaultValue( value );
							property.setValue( value );
						}
					}
				}
			}
		}
	}

	private boolean resourceOrParentHasProperty( RestResource restResource, String name )
	{
		for( RestResource r = restResource; r != null; r = r.getParentResource() )
		{
			if( r.hasProperty( name ) )
			{
				return true;
			}
		}
		return false;
	}

	private boolean isResourceLonely( RestResource restResource )
	{
		return restResource.getParentResource() == null && restResource.getChildResourceCount() == 0;
	}

	public void showDialog()
	{
		final JDialog dialog = new JDialog( UISupport.getMainFrame(), "Resource Path", true );
		dialog.setResizable( false );
		final JPanel panel = new JPanel( new BorderLayout() );

		Box contentBox = Box.createVerticalBox();

		int index = 0;

		ImageIcon icon = UISupport.createImageIcon( "/hake.png" );

		final JLabel changeWarningLabel = new JLabel( " " );
		changeWarningLabel.setBorder( BorderFactory.createCompoundBorder(
				contentBox.getBorder(),
				BorderFactory.createEmptyBorder( 10, 0, 10, 0 ) ) );
		final java.util.List<RestSubResourceTextField> restSubResourceTextFields = new ArrayList<RestSubResourceTextField>();
		DocumentListener pathChangedListener = new DocumentListenerAdapter()
		{
			@Override
			public void update( Document document )
			{
				int affectedRequestCount = 0;
				for( RestSubResourceTextField restResourceTextField : restSubResourceTextFields )
				{
					if( !restResourceTextField.getTextField().getText().equals( restResourceTextField.getRestResource().getPath() ) )
					{
						affectedRequestCount = restResourceTextField.getAffectedRequestCount();
						break;
					}
				}
				if( affectedRequestCount > 0 )
				{
					changeWarningLabel.setText( String.format( "<html>Changes will affect: <b>%d</b> request%s</html>",
							affectedRequestCount, affectedRequestCount > 1 ? "s" : "" ) );
					changeWarningLabel.setVisible( true );
				}
				else
				{
					changeWarningLabel.setVisible( false );
				}
			}
		};
		for( RestResource restResource : getRestResources() )
		{
			Box row = Box.createHorizontalBox();
			row.setAlignmentX( 0 );

			if( index > 1 )
			{
				row.add( Box.createHorizontalStrut( ( index - 1 ) * icon.getIconWidth() ) );
			}
			if( index >= 1 )
			{
				row.add( new JLabel( icon ) );
			}

			RestSubResourceTextField restSubResourceTextField = new RestSubResourceTextField( restResource );
			restSubResourceTextField.getTextField().getDocument().addDocumentListener( pathChangedListener );
			restSubResourceTextFields.add( restSubResourceTextField );

			Box textFieldBox = Box.createVerticalBox();
			textFieldBox.add( Box.createVerticalGlue() );
			textFieldBox.add( restSubResourceTextField.getTextField() );
			row.add( textFieldBox );

			contentBox.add( row );

			index++;
		}

		panel.add( contentBox, BorderLayout.NORTH );

		panel.add( changeWarningLabel, BorderLayout.CENTER );

		JButton okButton = new JButton( "OK" );
		JButton cancelButton = new JButton( "Cancel" );

		JPanel buttonBar = ButtonBarFactory.buildRightAlignedBar( okButton, cancelButton );

		panel.add( buttonBar, BorderLayout.SOUTH );
		panel.setBorder( BorderFactory.createCompoundBorder(
				panel.getBorder(),
				BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) );

		okButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				for( RestSubResourceTextField restSubResourceTextField : restSubResourceTextFields )
				{
					restSubResourceTextField.getRestResource().setPath( restSubResourceTextField.getTextField().getText() );
				}
				dialog.setVisible( false );
				scanForTemplateParameters();
			}
		} );
		cancelButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				dialog.setVisible( false );
			}
		} );
		dialog.getRootPane().setContentPane( panel );
		dialog.pack();
		UISupport.showDialog( dialog );
	}

	private List<RestResource> getRestResources()
	{
		final List<RestResource> resources = new ArrayList<RestResource>();
		for( RestResource r = editingRestResource; r != null; r = r.getParentResource() )
		{
			resources.add( r );
		}
		Collections.reverse( resources );
		return resources;
	}

	private class RestSubResourceTextField
	{
		private RestResource restResource;
		private JTextField textField;
		private Integer affectedRequestCount;

		private RestSubResourceTextField( RestResource restResource )
		{
			this.restResource = restResource;
			textField = new JTextField( restResource.getPath() );
			textField.setMaximumSize( new Dimension( 150, ( int )textField.getPreferredSize().getHeight() ) );
			textField.setPreferredSize( new Dimension( 150, ( int )textField.getPreferredSize().getHeight() ) );
		}

		public JTextField getTextField()
		{
			return textField;
		}

		public RestResource getRestResource()
		{
			return restResource;
		}

		public int getAffectedRequestCount()
		{
			if( affectedRequestCount == null )
			{
				affectedRequestCount = restResource.getAllChildResources().length + 1;
			}
			return affectedRequestCount;
		}
	}

	private class LonelyDocumentListener extends DocumentListenerAdapter
	{
		@Override
		public void update( Document document )
		{
			if( updating.booleanValue() )
			{
				return;
			}
			updating.setValue( true );
			editingRestResource.setPath( getText( document ) );
			updating.setValue( false );
		}
	}
}
