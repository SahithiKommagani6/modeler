/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.agilebi.modeler.nodes;

import java.util.HashMap;

import org.pentaho.agilebi.modeler.ColumnBackedNode;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.propforms.LevelsPropertiesForm;
import org.pentaho.metadata.model.IPhysicalTable;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.ui.xul.stereotype.Bindable;

@SuppressWarnings( "unchecked" )
public class LevelMetaData extends BaseColumnBackedMetaData<MemberPropertyMetaData> {

  private static final long serialVersionUID = -8026104295937064671L;
  private static final String IMAGE = "images/sm_level_icon.png";

  public LevelMetaData() {
    super();
    super.setUniqueList( true );
  }

  public LevelMetaData( HierarchyMetaData parent, String name ) {
    super( name );
    super.setUniqueList( true );
    setParent( parent );
  }

  @Bindable
  public String toString() {
    return "Level Name: " + name + "\nColumn Name: " + columnName;
  }

  @Override
  @Bindable
  public String getValidImage() {
    return IMAGE;
  }

  public HierarchyMetaData getHierarchyMetaData() {
    return (HierarchyMetaData) getParent();
  }

  public boolean isTimeLevel() {
    HierarchyMetaData hierarchyMetaData = getHierarchyMetaData();
    if ( hierarchyMetaData == null ) {
      return false;
    }
    return hierarchyMetaData.isTimeHierarchy();
  }

  @Override
  public Class<LevelsPropertiesForm> getPropertiesForm() {
    return LevelsPropertiesForm.class;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( columnName == null ) ? 0 : columnName.hashCode() );
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    result = prime * result + ( ( parent == null ) ? 0 : parent.hashCode() );
    result = prime * result + ( uniqueMembers ? Boolean.TRUE : Boolean.FALSE ).hashCode();
    return result;
  }

  public boolean equals( LevelMetaData obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    LevelMetaData other = obj;
    if ( columnName == null ) {
      if ( other.columnName != null ) {
        return false;
      }
    } else if ( !columnName.equals( other.columnName ) ) {
      return false;
    }
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    if ( parent == null ) {
      if ( other.parent != null ) {
        return false;
      }
    } else if ( !parent.equals( other.parent ) ) {
      return false;
    }
    return true;
  }

  @Override
  public boolean acceptsDrop( Object obj ) {
    String myTableId = this.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId();
    if ( obj instanceof AvailableField ) {
      AvailableField field = (AvailableField) obj;
      return myTableId.equals( field.getPhysicalColumn().getPhysicalTable().getId() );
    } else if ( obj instanceof MemberPropertyMetaData ) {
      MemberPropertyMetaData field = (MemberPropertyMetaData) obj;
      if ( field.getLogicalColumn() != null ) {
        return myTableId.equals( field.getLogicalColumn().getPhysicalColumn().getPhysicalTable().getId() );
      }
    }
    return false;
  }

  @Override
  public Object onDrop( Object data ) throws ModelerException {
    try {
      MemberPropertyMetaData memberProp = null;
      if ( data instanceof AvailableField ) {
        ColumnBackedNode node =
            getWorkspace().createColumnBackedNode( (AvailableField) data, ModelerPerspective.ANALYSIS );
        memberProp = getWorkspace().createMemberPropertyForParentWithNode( this, node );
      } else if ( data instanceof MemberPropertyMetaData ) {
        memberProp = (MemberPropertyMetaData) data;
        memberProp.setParent( this );
      } else {
        return null;
      }
      LogicalTable existingTable = getLogicalColumn().getLogicalTable();
      LogicalColumn col = memberProp.getLogicalColumn();
      if ( col == null ) {
        return null;
      }
      if ( col.getLogicalTable().getId() != existingTable.getId() ) {
        throw new IllegalStateException( ModelerMessagesHolder.getMessages().getString(
            "DROP.ERROR.MEMBER_PROP_FROM_DIFFERENT_TABLE" ) );
      }
      return memberProp;
    } catch ( Exception e ) {
      throw new ModelerException( e );
    }
  }

  @Override
  public void validate() {
    super.validate();
    if ( isTimeLevel() ) {
      DataRole dataRole = this.getDataRole();
      if ( !( dataRole instanceof TimeRole ) || dataRole == TimeRole.DUMMY ) {
        valid = false;
        validationMessages.add( ModelerMessagesHolder.getMessages().getString(
            getValidationMessageKey( "TIME_LEVEL_TYPE_NOT_SET" ), getName() ) );
      }
    }
    HashMap<String, MemberPropertyMetaData> usedNames = new HashMap<String, MemberPropertyMetaData>();
    if ( children.size() > 0 ) {
      for ( MemberPropertyMetaData memberProp : children ) {
        valid &= memberProp.isValid();
        validationMessages.addAll( memberProp.getValidationMessages() );
        if ( usedNames.containsKey( memberProp.getName() ) ) {
          valid = false;
          String dupeString =
              ModelerMessagesHolder.getMessages().getString(
                  getValidationMessageKey( "DUPLICATE_MEMBER_PROPERTY_NAMES" ), memberProp.getName() );
          validationMessages.add( dupeString );

          memberProp.invalidate();
          if ( !memberProp.getValidationMessages().contains( dupeString ) ) {
            memberProp.getValidationMessages().add( dupeString );
          }

          MemberPropertyMetaData m = usedNames.get( memberProp.getName() );
          if ( m.isValid() ) {
            m.invalidate();
            if ( !m.getValidationMessages().contains( dupeString ) ) {
              m.getValidationMessages().add( dupeString );
            }
          }
        } else {
          usedNames.put( memberProp.getName(), memberProp );
        }
      }
    }
  }

  @Override
  public String getValidationMessageKey( String key ) {
    return "validation.level." + key;
  }

  @Override
  public IPhysicalTable getTableRestriction() {
    // if the level has children (member props), restrict to the current table
    if ( this.size() > 0 && this.getLogicalColumn() != null ) {
      return this.getLogicalColumn().getPhysicalColumn().getPhysicalTable();
    }
    // restricted by siblings table
    if ( parent != null && parent.size() > 0 ) {
      for ( LevelMetaData sibling : getHierarchyMetaData() ) {
        if ( sibling != this && sibling.getLogicalColumn() != null ) {
          return sibling.getLogicalColumn().getPhysicalColumn().getPhysicalTable();
        }
      }
    }
    return null;
  }

  public MemberPropertyMetaData getLatitudeField() {
    for ( MemberPropertyMetaData member : this ) {
      if ( member.getName().equalsIgnoreCase( GeoContext.LATITUDE ) ) {
        return member;
      }
    }
    return null;
  }

  public MemberPropertyMetaData getLongitudeField() {
    for ( MemberPropertyMetaData member : this ) {
      if ( member.getName().equalsIgnoreCase( GeoContext.LONGITUDE ) ) {
        return member;
      }
    }
    return null;
  }

  @Override
  protected boolean compareChildren( MemberPropertyMetaData child, MemberPropertyMetaData newChild ) {
    return child.getName().equals( newChild.getName() );
  }
}
