/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javafx.embed.swing;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import sun.awt.CausedFocusEvent;

class KFMHelper {

    static final int SNFH_FAILURE = 0;
    static final int SNFH_SUCCESS_HANDLED = 1;
    static final int SNFH_SUCCESS_PROCEED = 2;

    private static Method processSynchronousLightweightTransferMethod;

    private static Method shouldNativelyFocusHeavyweightMethod;

    static boolean processSynchronousLightweightTransfer(Component target,
                                                         Component lightweightChild,
                                                         boolean temporary,
                                                         boolean focusedWindowChangeAllowed,
                                                         long time)
    {
        try {
            if (processSynchronousLightweightTransferMethod == null) {
                processSynchronousLightweightTransferMethod = AccessController
                        .doPrivileged(new PrivilegedExceptionAction<Method>() {
                            public Method run() throws IllegalAccessException,
                                    NoSuchMethodException {
                                Method m = KeyboardFocusManager.class
                                        .getDeclaredMethod(
                                                "processSynchronousLightweightTransfer",
                                                new Class[] { Component.class,
                                                        Component.class,
                                                        Boolean.TYPE,
                                                        Boolean.TYPE, Long.TYPE });
                                m.setAccessible(true);
                                return m;
                            }
                        });
            }
            Object[] params = new Object[] { target, lightweightChild,
                    Boolean.valueOf(temporary),
                    Boolean.valueOf(focusedWindowChangeAllowed),
                    Long.valueOf(time) };
            return ((Boolean) processSynchronousLightweightTransferMethod
                    .invoke(null, params)).booleanValue();
        } catch (PrivilegedActionException pae) {
            pae.printStackTrace();
            return false;
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            return false;
        } catch (IllegalArgumentException iaee) {
            iaee.printStackTrace();
            return false;
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            return false;
        }
    }

    static int shouldNativelyFocusHeavyweight(Component heavyweight,
                                              Component descendant,
                                              boolean temporary,
                                              boolean focusedWindowChangeAllowed,
                                              long time,
                                              CausedFocusEvent.Cause cause)
    {
        if (shouldNativelyFocusHeavyweightMethod == null) {
            Class<?>[] arg_types = new Class<?>[] {
                    Component.class, Component.class,
                    Boolean.TYPE, Boolean.TYPE, Long.TYPE,
                    CausedFocusEvent.Cause.class
            };

            try {
                shouldNativelyFocusHeavyweightMethod = KeyboardFocusManager.class
                        .getDeclaredMethod("shouldNativelyFocusHeavyweight",
                                arg_types);
                shouldNativelyFocusHeavyweightMethod.setAccessible(true);
                
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        Object[] args = new Object[] {
                heavyweight, descendant,
                Boolean.valueOf(temporary),
                Boolean.valueOf(focusedWindowChangeAllowed),
                Long.valueOf(time), cause
        };

        int result = SNFH_FAILURE;
        if (shouldNativelyFocusHeavyweightMethod != null) {
            try {
                result = ((Integer)
                        shouldNativelyFocusHeavyweightMethod.invoke(null, args)).intValue();
            } catch (IllegalAccessException e) {
                assert false;
                
            } catch (InvocationTargetException e) {
                assert false;
            }
        }

        return result;
    }

}
