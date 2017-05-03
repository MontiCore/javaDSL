/*
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
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
/*
 * COMPONENT_NAME: idl.parser
 *
 * ORIGINS: 27
 *
 * Licensed Materials - Property of IBM
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 1999
 * RMI-IIOP v1.0
 *
 */

package com.sun.tools.corba.se.idl;

// NOTES:

import java.util.Vector;

import com.sun.tools.corba.se.idl.TypedefEntry;

/**
 * This class encapsulates one branch of a union.  Here are some examples
 * of what it may contain:
 * <dl>
 * <dt>
 * <pre>
 * case 1: short x;
 * </pre>
 * <dd><short x, <1>, false>
 * <dt>
 * <pre>
 * case 0:
 * case 8:
 * case 2: long x;
 * </pre>
 * <dd><long x, <0, 8, 2>, false>
 * <dt>
 * <pre>
 * default: long x;
 * </pre>
 * <dd><long x, <>, true>
 * <dt>
 * <pre>
 * case 0:
 * case 2:
 * default: char c;
 * </pre>
 * <dd><char c, <0, 2>, true>
 * </dl>
 **/
public class UnionBranch
{
  /** The type definition for the branch. */
  public TypedefEntry typedef;
  /** A vector of Expression's, one for each label in the order in which
      they appear in the IDL file.  The default branch has no label. */
  public Vector labels = new Vector ();
  /** true if this is the default branch. */
  public boolean isDefault = false;
} // class UnionBranch
