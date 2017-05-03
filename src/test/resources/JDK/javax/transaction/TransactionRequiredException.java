/*
 * Copyright (c) 1998, 1999, Oracle and/or its affiliates. All rights reserved.
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
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */

package javax.transaction;

/**
 * This exception indicates that a request carried a null transaction context,
 * but the target object requires an activate transaction.
 */
public class TransactionRequiredException extends java.rmi.RemoteException
{
    public TransactionRequiredException()
    {
        super();
    }

    public TransactionRequiredException(String msg)
    {
        super(msg);
    }
}
