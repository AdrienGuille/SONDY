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

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JComponent;

class SwingEventDispatcherHelper {
    
    private static final long MOUSE_MASK = AWTEvent.MOUSE_EVENT_MASK |
                                           AWTEvent.MOUSE_MOTION_EVENT_MASK |
                                           AWTEvent.MOUSE_WHEEL_EVENT_MASK;
    
    private static Field dispatcherField;
    private static Constructor<?> newLightweightDispatcher;
    private static Method dispatchMethod;
    private static Method enableEvents;

    // grab this idea from CacioCavallo
    static void initReflection() {
        try {
            
            // lightweight dispatcher
            dispatcherField = Container.class.getDeclaredField("dispatcher");
            dispatcherField.setAccessible(true);
            
            Class<?> dispatcherCls = Class.forName("java.awt.LightweightDispatcher");
            newLightweightDispatcher =
                    dispatcherCls.getDeclaredConstructor(new Class[] { 
                                                            Container.class
                                                         });
            newLightweightDispatcher.setAccessible(true);
            
            dispatchMethod = dispatcherCls.getDeclaredMethod("dispatchEvent",
                                                             AWTEvent.class);
            dispatchMethod.setAccessible(true);

            enableEvents = dispatcherCls.getDeclaredMethod("enableEvents",
                                                           new Class[] {
                                                                long.class
                                                           });
            enableEvents.setAccessible(true);
            
        } catch (Exception ex) {
            
            System.err.println(ex);
            
            InternalError err = new InternalError();
            err.initCause(ex);
            throw err;
        }
    }
 
    static void setLightweightDispatcher(JComponent component) {
        if (dispatcherField == null) {
            initReflection();
        }
        try {
            Object dispatcher = newLightweightDispatcher.newInstance(component);
            enableEvents.invoke(dispatcher, MOUSE_MASK | AWTEvent.KEY_EVENT_MASK);
            dispatcherField.set(component, dispatcher);
            component.addKeyListener(new KeyAdapter(){});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Performs lightweight dispatching for the specified event on this window.
     * This only calls the lightweight dispatcher. We cannot simply
     * call dispatchEvent() because that would also send the event to the
     * Toolkit dispatching mechanism (AWTEventListener, etc), which has ugly
     * side effects, like popups closing too early.
     *
     * @param e the event to be dispatched
     */
    static void dispatchEvent(AWTEvent awtEvent, JComponent component) {

            if (dispatcherField == null) {
                initReflection();
            }
            try {
                Object dispatcher = dispatcherField.get(component);
                if (dispatcher != null) {
                    dispatchMethod.invoke(dispatcher, awtEvent);
                }
            } catch (Exception ex) {
                InternalError err = new InternalError();
                err.initCause(ex);
                throw err;
            }


    }
}
