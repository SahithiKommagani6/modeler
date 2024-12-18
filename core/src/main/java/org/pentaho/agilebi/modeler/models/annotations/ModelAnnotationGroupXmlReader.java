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


package org.pentaho.agilebi.modeler.models.annotations;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.agilebi.modeler.models.annotations.data.ColumnMapping;
import org.pentaho.agilebi.modeler.models.annotations.data.DataProvider;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.metadata.model.concept.types.DataType;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAnnotationGroupXmlReader {

  public ModelAnnotationGroup readModelAnnotationGroup( Node step ) throws KettleXMLException {

    ModelAnnotationGroup modelAnnotationGroup = new ModelAnnotationGroup();
    Node annotations = XMLHandler.getSubNode( step, "annotations" );
    int annotationsCount = XMLHandler.countNodes( annotations, "annotation" );
    for ( int i = 0; i < annotationsCount; i++ ) {
      try {
        Node annotation = XMLHandler.getSubNodeByNr( annotations, "annotation", i );
        String name = XMLHandler.getTagValue( annotation, "name" );
        String field = XMLHandler.getTagValue( annotation, "field" );
        String type = XMLHandler.getTagValue( annotation, "type" );

        Node properties = XMLHandler.getSubNode( annotation, "properties" );
        int propertiesCount = XMLHandler.countNodes( properties, "property" );

        // Create model annotation
        ModelAnnotation<?> modelAnnotation = create( type, field );
        if ( StringUtils.isNotBlank( name ) ) {
          modelAnnotation.setName( name );
        }

        // Populate annotation properties
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        for ( int j = 0; j < propertiesCount; j++ ) {
          Node property = XMLHandler.getSubNodeByNr( properties, "property", j );
          String n = XMLHandler.getTagValue( property, "name" );
          String v = XMLHandler.getTagValue( property, "value" );
          map.put( n, v );
        }
        modelAnnotation.populateAnnotation( map );

        // Add to group
        modelAnnotationGroup.add( modelAnnotation );
      } catch ( KettleException ke ) {
        //logError( ke.getMessage() );
      }
    }

    modelAnnotationGroup
        .setSharedDimension( BooleanUtils.toBoolean( XMLHandler.getTagValue( annotations, "sharedDimension" ) ) );
    modelAnnotationGroup.setDescription( XMLHandler.getTagValue( annotations, "description" ) );
    readDataProviders( annotations, modelAnnotationGroup );

    return modelAnnotationGroup;
  }

  public void readDataProviders( final Node node, final ModelAnnotationGroup group ) {

    if ( node == null || group == null ) {
      return;
    }

    List<DataProvider> dataProviders = new ArrayList<DataProvider>();
    Node dataProvidersNode = XMLHandler.getSubNode( node, "data-providers" );
    int dataProviderCount = XMLHandler.countNodes( dataProvidersNode, "data-provider" );
    for ( int i = 0; i < dataProviderCount; i++ ) {
      try {

        Node dataProviderNode = XMLHandler.getSubNodeByNr( dataProvidersNode, "data-provider", i );

        DataProvider dataProvider = new DataProvider();
        dataProvider.setName( XMLHandler.getTagValue( dataProviderNode, "name" ) );
        dataProvider.setSchemaName( XMLHandler.getTagValue( dataProviderNode, "schemaName" ) );
        dataProvider.setTableName( XMLHandler.getTagValue( dataProviderNode, "tableName" ) );
        dataProvider.setDatabaseMetaNameRef( XMLHandler.getTagValue( dataProviderNode, "databaseMetaRef" ) );

        readColumnMappings( dataProviderNode, dataProvider );

        dataProviders.add( dataProvider );
      } catch ( Exception ke ) {
        //logError( ke.getMessage() );
      }
    }
    group.setDataProviders( dataProviders );
  }

  public void readColumnMappings( final Node dataProviderNode, DataProvider dataProvider ) {

    if ( dataProviderNode == null || dataProvider == null ) {
      return;
    }

    List<ColumnMapping> columnMappings = new ArrayList<ColumnMapping>();
    Node columnMappingNodes = XMLHandler.getSubNode( dataProviderNode, "column-mappings" );
    int columnMappingCount = XMLHandler.countNodes( columnMappingNodes, "column-mapping" );
    for ( int i = 0; i < columnMappingCount; i++ ) {
      try {

        Node columnMappingNode = XMLHandler.getSubNodeByNr( columnMappingNodes, "column-mapping", i );

        ColumnMapping columnMapping = new ColumnMapping();
        columnMapping.setName( XMLHandler.getTagValue( columnMappingNode, "name" ) );
        columnMapping.setColumnName( XMLHandler.getTagValue( columnMappingNode, "columnName" ) );

        String dataType = XMLHandler.getTagValue( columnMappingNode, "dataType" );
        if ( StringUtils.isNotBlank( dataType ) ) {
          columnMapping
              .setColumnDataType( DataType.valueOf( dataType ) );
        }

        columnMappings.add( columnMapping );
      } catch ( Exception ke ) {
        //logError( ke.getMessage() );
      }
    }
    dataProvider.setColumnMappings( columnMappings );
  }

  public static ModelAnnotation<?> create( String annotationType, String field )
      throws KettleException {

    if ( annotationType != null ) {
      return create( ModelAnnotation.Type.valueOf( annotationType ), field );
    }
    return new ModelAnnotation<AnnotationType>();
  }

  private static ModelAnnotation<? extends AnnotationType> create( ModelAnnotation.Type annotationType, String field ) {
    switch ( annotationType ) {
      case CREATE_ATTRIBUTE:
        CreateAttribute ca = new CreateAttribute();
        ca.setField( field );
        return new ModelAnnotation<CreateAttribute>( ca );
      case CREATE_MEASURE:
        CreateMeasure cm = new CreateMeasure();
        cm.setField( field );
        return new ModelAnnotation<CreateMeasure>( cm );
      case CREATE_DIMENSION_KEY:
        CreateDimensionKey cdk = new CreateDimensionKey();
        cdk.setField( field );
        return new ModelAnnotation<CreateDimensionKey>( cdk );
      case LINK_DIMENSION:
        LinkDimension ld = new LinkDimension();
        ld.setField( field );
        return new ModelAnnotation<LinkDimension>( ld );
      case UPDATE_MEASURE:
        UpdateMeasure um = new UpdateMeasure();
        return new ModelAnnotation<UpdateMeasure>( um );
      case CREATE_CALCULATED_MEMBER:
        CreateCalculatedMember calculatedMember = new CreateCalculatedMember();
        return new ModelAnnotation<CreateCalculatedMember>( calculatedMember );
      case UPDATE_CALCULATED_MEMBER:
        UpdateCalculatedMember updateCalculatedMember = new UpdateCalculatedMember();
        return new ModelAnnotation<UpdateCalculatedMember>( updateCalculatedMember );
      case SHOW_HIDE_ATTRIBUTE:
        ShowHideAttribute hideAttribute = new ShowHideAttribute();
        return new ModelAnnotation<>( hideAttribute );
      case SHOW_HIDE_MEASURE:
        ShowHideMeasure hideMeasure = new ShowHideMeasure();
        return new ModelAnnotation<>( hideMeasure );
      case UPDATE_ATTRIBUTE:
        UpdateAttribute updateAttribute = new UpdateAttribute();
        return new ModelAnnotation<>( updateAttribute );
      default:
        BlankAnnotation ba = new BlankAnnotation();
        ba.setField( field );
        return new ModelAnnotation<AnnotationType>( ba );
    }
  }
}
