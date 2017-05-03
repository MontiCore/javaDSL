/*
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package com.sun.beans.editors;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.List;

/**
 * Property editor for java.lang.Enum subclasses.
 *
 * @see PropertyEditor
 *
 * @since 1.7
 *
 * @author Sergey A. Malenkov
 */
public class EnumEditor implements PropertyEditor {
    private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    private final Class type;
    private final String[] tags;

    private Object value;

    public EnumEditor( Class type ) {
        Object[] values = type.getEnumConstants();
        if ( values == null ) {
            throw new IllegalArgumentException( "Unsupported " + type );
        }
        this.type = type;
        this.tags = new String[values.length];
        for ( int i = 0; i < values.length; i++ ) {
            this.tags[i] = ( ( Enum )values[i] ).name();
        }
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue( Object value ) {
        if ( ( value != null ) && !this.type.isInstance( value ) ) {
            throw new IllegalArgumentException( "Unsupported value: " + value );
        }
        Object oldValue;
        PropertyChangeListener[] listeners;
        synchronized ( this.listeners ) {
            oldValue = this.value;
            this.value = value;

            if ( ( value == null ) ? oldValue == null : value.equals( oldValue ) ) {
                return; // do not fire event if value is not changed
            }
            int size = this.listeners.size();
            if ( size == 0 ) {
                return; // do not fire event if there are no any listener
            }
            listeners = this.listeners.toArray( new PropertyChangeListener[size] );
        }
        PropertyChangeEvent event = new PropertyChangeEvent( this, null, oldValue, value );
        for ( PropertyChangeListener listener : listeners ) {
            listener.propertyChange( event );
        }
    }

    public String getAsText() {
        return ( this.value != null )
                ? ( ( Enum )this.value ).name()
                : null;
    }

    public void setAsText( String text ) {
        setValue( ( text != null )
                ? Enum.valueOf( this.type, text )
                : null );
    }

    public String[] getTags() {
        return this.tags.clone();
    }

    public String getJavaInitializationString() {
        String name = getAsText();
        return ( name != null )
                ? this.type.getName() + '.' + name
                : "null";
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue( Graphics gfx, Rectangle box ) {
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    public Component getCustomEditor() {
        return null;
    }

    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        synchronized ( this.listeners ) {
            this.listeners.add( listener );
        }
    }

    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        synchronized ( this.listeners ) {
            this.listeners.remove( listener );
        }
    }
}
