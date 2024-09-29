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


package org.pentaho.agilebi.modeler.util;

import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.nodes.MainModelNode;
import org.pentaho.agilebi.modeler.nodes.RelationalModelNode;

/**
 * User: nbaker Date: Jul 14, 2010
 */
public class ModelerWorkspaceHelper extends BaseModelerWorkspaceHelper {

  public ModelerWorkspaceHelper( String locale ) {
    super( locale );
  }

  @Override
  protected MainModelNode getMainModelNode( ModelerWorkspace workspace ) {
    return new MainModelNode( workspace );
  }

  @Override
  protected RelationalModelNode getRelationalModelNode( ModelerWorkspace workspace ) {
    return new RelationalModelNode( workspace );
  }

}
