/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.agilebi.modeler.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * Created: 3/24/11
 * 
 * @author rfellows
 */
public abstract class BaseAggregationMetaDataNode extends BaseColumnBackedMetaData {
  private static final long serialVersionUID = -6574834791283543091L;

  public static final String FORMAT_NONE = "NONE"; //$NON-NLS-1$

  protected String format = FORMAT_NONE;
  protected String fieldTypeDesc = "---";
  protected String levelTypeDesc = "---";
  protected AggregationType defaultAggregation;
  protected String displayName;

  protected String locale;

  protected List<AggregationType> selectedAggregations = new Vector<AggregationType>();
  private List<AggregationType> possibleAggregations = new ArrayList<AggregationType>();

  protected List<String> formatstring;

  public List<AggregationType> getNumericAggregationTypes() {
    return Arrays.asList( AggregationType.SUM, AggregationType.AVERAGE, AggregationType.MINIMUM,
        AggregationType.MAXIMUM, AggregationType.COUNT, AggregationType.COUNT_DISTINCT );
  }

  public List<AggregationType> getTextAggregationTypes() {
    return Arrays.asList( AggregationType.COUNT, AggregationType.COUNT_DISTINCT );
  }

  public BaseAggregationMetaDataNode( String locale ) {
    this.locale = locale;
  }

  public BaseAggregationMetaDataNode( String fieldName, String format, String displayName, String locale ) {
    super( fieldName );
    this.format = format;
    this.displayName = displayName;
    this.locale = locale;
  }

  @Bindable
  public void setName( String name ) {
    if ( !( name == null ) ) {
      super.setName( name );
      if ( logicalColumn != null ) {
        logicalColumn.setName( new LocalizedString( locale, name ) );
      }
    }
  }

  @Bindable
  public String getFormat() {
    if ( format == null || "".equals( format ) ) {
      return FORMAT_NONE;
    }
    return format;
  }

  @Bindable
  public void setFormat( String format ) {
    String previousFormat = this.format;
    this.format = format;
    firePropertyChange( "format", previousFormat, format );
  }

  @Bindable
  public String getFieldTypeDesc() {
    return fieldTypeDesc;
  }

  @Bindable
  public void setFieldTypeDesc( String fieldTypeDesc ) {
    this.fieldTypeDesc = fieldTypeDesc;
  }

  @Bindable
  public String getLevelTypeDesc() {
    return levelTypeDesc;
  }

  @Bindable
  public void setLevelTypeDesc( String levelTypeDesc ) {
    this.levelTypeDesc = levelTypeDesc;
  }

  @Bindable
  public AggregationType getDefaultAggregation() {
    if ( logicalColumn == null ) {
      return null;
    }
    if ( defaultAggregation == null ) {
      switch ( logicalColumn.getDataType() ) {
        case NUMERIC:
          defaultAggregation = AggregationType.SUM;
          break;
        default:
          defaultAggregation = AggregationType.COUNT;
      }
    }
    return defaultAggregation;
  }

  @Bindable
  public void setDefaultAggregation( AggregationType aggType ) {
    AggregationType previousAggregation = this.defaultAggregation;
    this.defaultAggregation = aggType;
    this.firePropertyChange( "defaultAggregation", previousAggregation, defaultAggregation );
  }

  @Override
  public void setLogicalColumn( LogicalColumn col ) {
    DataType previousDataType = null;
    if ( logicalColumn != null ) {
      previousDataType = logicalColumn.getDataType();
    }
    if ( col == null ) {
      super.setLogicalColumn( col );
      return;
    }
    super.setLogicalColumn( col );
    DataType newDataType = logicalColumn.getDataType();
    if ( previousDataType == null || previousDataType != newDataType ) {
      if ( logicalColumn.getDataType() == DataType.NUMERIC ) {
        setPossibleAggregations( getNumericAggregationTypes() );
        setDefaultAggregation( AggregationType.SUM );
      } else {
        setPossibleAggregations( getTextAggregationTypes() );
        setDefaultAggregation( AggregationType.NONE );
      }

      // if we are given an aggtype, use that rather than the default
      if ( logicalColumn.getAggregationType() != null ) {
        setDefaultAggregation( logicalColumn.getAggregationType() );
      }

      // If a previously defined list exists, use it. Otherwise select all possible as a default
      if ( col.getAggregationList() != null && col.getAggregationList().isEmpty() == false ) {
        setSelectedAggregations( col.getAggregationList() );
      } else {
        setSelectedAggregations( possibleAggregations );
      }
    }
  }

  private void setPossibleAggregations( List<AggregationType> aggregationTypes ) {
    this.possibleAggregations.clear();
    this.possibleAggregations.addAll( aggregationTypes );
    firePropertyChange( "possibleAggregations", null, this.possibleAggregations );
  }

  public List<AggregationType> getPossibleAggregations() {
    return possibleAggregations;
  }

  @Bindable
  public List<AggregationType> getSelectedAggregations() {
    return selectedAggregations;
  }

  @Bindable
  public void setSelectedAggregations( List<AggregationType> selectedAggregations ) {
    this.selectedAggregations = selectedAggregations;
    firePropertyChange( "selectedAggregations", null, this.selectedAggregations );
  }

  @Bindable
  public List<String> getFormatstring() {
    return formatstring;
  }

  @Bindable
  public void setFormatstring( List<String> formatstring ) {
    List<String> prevXulMenuList = this.formatstring;
    this.formatstring = formatstring;
    this.firePropertyChange( "formatstring", prevXulMenuList, formatstring );
  }

  public boolean equals( Object o ) {
    if ( o == null || o instanceof BaseAggregationMetaDataNode == false ) {
      return false;
    }
    BaseAggregationMetaDataNode f = (BaseAggregationMetaDataNode) o;

    if ( o == this ) {
      return true;
    }

    if ( f.getLogicalColumn() == null || this.getLogicalColumn() == null ) {
      return false;
    }

    if ( f.getLogicalColumn().getId().equals( this.getLogicalColumn().getId() ) ) {
      return true;
    }
    return false;
  }

  public String toString() {
    return name;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    return null;
  }

}
