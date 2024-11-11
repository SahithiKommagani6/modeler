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


package org.pentaho.agilebi.modeler;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pentaho.agilebi.modeler.nodes.AvailableField;
import org.pentaho.agilebi.modeler.nodes.AvailableItemCollection;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaData;
import org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection;
import org.pentaho.agilebi.modeler.nodes.HierarchyMetaData;
import org.pentaho.agilebi.modeler.nodes.LevelMetaData;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.MeasureMetaData;
import org.pentaho.agilebi.modeler.nodes.MeasuresCollection;
import org.pentaho.agilebi.modeler.nodes.MemberPropertyMetaData;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;

/**
 * User: nbaker Date: 4/15/11
 */
public class ModelsDnDIT extends AbstractModelerTest {

  @Test
  public void testValidDropIndications() throws ModelerException {
    this.generateTestDomain();
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper( "en_US" );
    helper.autoModelFlat( workspace );

    MeasuresCollection measures = workspace.getModel().getMeasures();
    AvailableItemCollection items = workspace.getAvailableTables();

    AvailableField firstField = items.getAsAvailableTablesList().get( 0 ).getChildren().get( 0 );
    DimensionMetaData firstDim = workspace.getModel().getDimensions().get( 0 );
    HierarchyMetaData firstHier = firstDim.get( 0 );
    LevelMetaData firstLevel = firstHier.get( 0 );
    MeasureMetaData firstMeasure = measures.get( 0 );

    // can move a field into the measures collection
    assertTrue( measures.acceptsDrop( firstField ) );

    // can move a field into the Dimensions collection
    assertTrue( workspace.getModel().getDimensions().acceptsDrop( firstField ) );

    // available field to dimension
    assertTrue( firstDim.acceptsDrop( firstField ) );

    // measure to dimension
    assertTrue( firstDim.acceptsDrop( firstMeasure ) );

    // Hierarchy to dimension
    assertTrue( firstDim.acceptsDrop( firstHier ) );

    // Level to dimension
    assertTrue( firstDim.acceptsDrop( firstLevel ) );

    // Level to Hierarchy
    assertTrue( firstHier.acceptsDrop( firstLevel ) );

    // Measure to Hierarchy
    assertTrue( firstHier.acceptsDrop( firstMeasure ) );

    // measures can be reordered in the measures collection
    assertTrue( measures.acceptsDrop( firstMeasure ) );

    assertFalse( firstLevel.acceptsDrop( new MemberPropertyMetaData( firstLevel, "test" ) ) );
    assertTrue( firstLevel.acceptsDrop( workspace.createMemberPropertyForParentWithNode( firstLevel, workspace
        .createColumnBackedNode( firstField, ModelerPerspective.ANALYSIS ) ) ) );
    assertTrue( firstLevel.acceptsDrop( firstField ) );
  }

  @Test
  public void testInvalidDropIndications() throws ModelerException {
    this.generateTestDomain();
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper( "en_US" );
    helper.autoModelFlat( workspace );

    MeasuresCollection measures = workspace.getModel().getMeasures();
    AvailableItemCollection items = workspace.getAvailableTables();
    DimensionMetaDataCollection dimensions = workspace.getModel().getDimensions();
    MainModelNode mainNode = workspace.getModel();

    AvailableField firstField = items.getAsAvailableTablesList().get( 0 ).getChildren().get( 0 );
    AvailableField secondField = items.getAsAvailableTablesList().get( 0 ).getChildren().get( 1 );
    DimensionMetaData firstDim = workspace.getModel().getDimensions().get( 0 );
    DimensionMetaData secondDim = workspace.getModel().getDimensions().get( 1 );
    HierarchyMetaData firstHier = firstDim.get( 0 );
    HierarchyMetaData secondHier = secondDim.get( 0 );

    LevelMetaData firstLevel = firstHier.get( 0 );
    LevelMetaData secondLevel = secondHier.get( 0 );
    MeasureMetaData firstMeasure = measures.get( 0 );
    MeasureMetaData secondMeasure = measures.get( 1 );

    // top-down drops are invalid, only bottom up
    assertFalse( firstHier.acceptsDrop( firstDim ) );
    assertFalse( firstLevel.acceptsDrop( firstDim ) );
    assertFalse( firstLevel.acceptsDrop( firstHier ) );
    assertFalse( dimensions.acceptsDrop( workspace.getModel() ) );

    // cannot drag dimensions and measures collections not the mainModelNode anywhere
    assertFalse( dimensions.acceptsDrop( measures ) );
    assertFalse( mainNode.acceptsDrop( measures ) );
    assertFalse( firstDim.acceptsDrop( measures ) );
    assertFalse( firstHier.acceptsDrop( measures ) );
    assertFalse( firstLevel.acceptsDrop( measures ) );

    assertFalse( measures.acceptsDrop( dimensions ) );
    assertFalse( mainNode.acceptsDrop( dimensions ) );
    assertFalse( firstDim.acceptsDrop( dimensions ) );
    assertFalse( firstHier.acceptsDrop( dimensions ) );
    assertFalse( firstLevel.acceptsDrop( dimensions ) );

    assertFalse( measures.acceptsDrop( mainNode ) );
    assertFalse( dimensions.acceptsDrop( mainNode ) );
    assertFalse( firstDim.acceptsDrop( mainNode ) );
    assertFalse( firstHier.acceptsDrop( mainNode ) );
    assertFalse( firstLevel.acceptsDrop( mainNode ) );

    // same type of node onto another... universally bad
    assertFalse( firstLevel.acceptsDrop( secondLevel ) );
    assertFalse( firstHier.acceptsDrop( secondHier ) );
    assertFalse( firstDim.acceptsDrop( secondDim ) );
    assertFalse( firstMeasure.acceptsDrop( secondMeasure ) );

  }

  @Test
  public void testValidDrops() throws ModelerException {
    this.generateTestDomain();
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper( "en_US" );
    helper.autoModelFlat( workspace );

    MeasuresCollection measures = workspace.getModel().getMeasures();
    AvailableItemCollection items = workspace.getAvailableTables();

    AvailableField firstField = items.getAsAvailableTablesList().get( 0 ).getChildren().get( 0 );
    DimensionMetaData firstDim = workspace.getModel().getDimensions().get( 0 );
    HierarchyMetaData firstHier = firstDim.get( 0 );
    LevelMetaData firstLevel = firstHier.get( 0 );
    MeasureMetaData firstMeasure = measures.get( 0 );

    // can move a field into the measures collection
    assertNotNull( measures.onDrop( firstField ) );

    // can move a field into the Dimensions collection
    assertNotNull( workspace.getModel().getDimensions().onDrop( firstField ) );

    // available field to dimension
    assertNotNull( firstDim.onDrop( firstField ) );

    // measure to dimension
    assertNotNull( firstDim.onDrop( firstMeasure ) );

    // Hierarchy to dimension
    assertNotNull( firstDim.onDrop( firstHier ) );

    // Level to dimension
    assertNotNull( firstDim.onDrop( firstLevel ) );

    // Level to Hierarchy
    assertNotNull( firstHier.onDrop( firstLevel ) );

    // Measure to Hierarchy
    assertNotNull( firstHier.onDrop( firstMeasure ) );

    // measures can be reordered in the measures collection
    assertNotNull( measures.onDrop( firstMeasure ) );

    assertNotNull( firstLevel.onDrop( firstField ) );
  }

  @Test
  public void testInvalidDrops() throws ModelerException {
    this.generateTestDomain();
    ModelerWorkspaceHelper helper = new ModelerWorkspaceHelper( "en_US" );
    helper.autoModelFlat( workspace );

    MeasuresCollection measures = workspace.getModel().getMeasures();
    AvailableItemCollection items = workspace.getAvailableTables();
    DimensionMetaDataCollection dimensions = workspace.getModel().getDimensions();
    MainModelNode mainNode = workspace.getModel();

    AvailableField firstField = items.getAsAvailableTablesList().get( 0 ).getChildren().get( 0 );
    AvailableField secondField = items.getAsAvailableTablesList().get( 0 ).getChildren().get( 1 );
    DimensionMetaData firstDim = workspace.getModel().getDimensions().get( 0 );
    DimensionMetaData secondDim = workspace.getModel().getDimensions().get( 1 );
    HierarchyMetaData firstHier = firstDim.get( 0 );
    HierarchyMetaData secondHier = secondDim.get( 0 );

    LevelMetaData firstLevel = firstHier.get( 0 );
    LevelMetaData secondLevel = secondHier.get( 0 );
    MeasureMetaData firstMeasure = measures.get( 0 );
    MeasureMetaData secondMeasure = measures.get( 1 );

    // top-down drops are invalid, only bottom up
    assertNull( firstHier.onDrop( firstDim ) );

    assertNull( firstLevel.onDrop( firstDim ) );

    assertNull( firstLevel.onDrop( firstHier ) );

    assertNull( dimensions.onDrop( workspace.getModel() ) );

    // cannot drag dimensions and measures collections not the mainModelNode anywhere
    assertNull( dimensions.onDrop( measures ) );

    assertNull( mainNode.onDrop( measures ) );

    assertNull( firstDim.onDrop( measures ) );

    assertNull( firstHier.onDrop( measures ) );

    assertNull( firstLevel.onDrop( measures ) );

    assertNull( measures.onDrop( dimensions ) );

    assertNull( mainNode.onDrop( dimensions ) );

    assertNull( firstDim.onDrop( dimensions ) );

    assertNull( firstHier.onDrop( dimensions ) );

    assertNull( firstLevel.onDrop( dimensions ) );

    assertNull( measures.onDrop( mainNode ) );

    assertNull( dimensions.onDrop( mainNode ) );

    assertNull( firstDim.onDrop( mainNode ) );

    assertNull( firstHier.onDrop( mainNode ) );

    assertNull( firstLevel.onDrop( mainNode ) );

    // same type of node onto another... universally bad
    assertNull( firstLevel.onDrop( secondLevel ) );

    assertNull( firstHier.onDrop( secondHier ) );

    assertNull( firstDim.onDrop( secondDim ) );

    assertNull( firstMeasure.onDrop( secondMeasure ) );

  }

}
