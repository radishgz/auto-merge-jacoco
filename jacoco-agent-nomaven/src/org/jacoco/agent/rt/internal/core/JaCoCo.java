/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *    
 *******************************************************************************/
package org.jacoco.agent.rt.internal.core;
import java.util.ResourceBundle;

public final class JaCoCo
{
  public static final String VERSION;
  public static final String HOMEURL;
  public static final String RUNTIMEPACKAGE;

  static
  {
    ResourceBundle bundle = ResourceBundle.getBundle("org.jacoco.agent.rt.internal.core.jacoco");

    VERSION = bundle.getString("VERSION");
    HOMEURL = bundle.getString("HOMEURL");
    RUNTIMEPACKAGE = bundle.getString("RUNTIMEPACKAGE");
  }
}
