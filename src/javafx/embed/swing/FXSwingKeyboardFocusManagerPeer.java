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
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.util.logging.Logger;

import sun.awt.AppContext;
import sun.awt.CausedFocusEvent;
import sun.awt.CausedFocusEvent.Cause;
import sun.awt.SunToolkit;

class FXSwingKeyboardFocusManagerPeer implements KeyboardFocusManagerPeer {

    private static Logger log =
            Logger.getLogger(FXSwingKeyboardFocusManagerPeer.class.getName());

    private static FXSwingKeyboardFocusManagerPeer instance;

    static FXSwingKeyboardFocusManagerPeer getInstance() {
        if (instance == null) {
            instance = new FXSwingKeyboardFocusManagerPeer();
        }
        return instance;
    }

    private FXSwingKeyboardFocusManagerPeer() { /* nothing to do */ }

    private Window currentFocusedWindow;
    private Component currentFocusOwner;

    /**
     * Returns the currently focused window.
     *
     * @return the currently focused window
     *
     * @see KeyboardFocusManager#getNativeFocusedWindow()
     */
    @Override
    public Window getCurrentFocusedWindow() {
        return currentFocusedWindow;
    }

    /**
     * Sets the component that should become the focus owner.
     *
     * @param comp the component to become the focus owner
     *
     * @see KeyboardFocusManager#setNativeFocusOwner(Component)
     */
    @Override
    public void setCurrentFocusOwner(Component comp) {
        currentFocusOwner = comp;
        if (currentFocusOwner instanceof ProxyWindow) {
            ((ProxyWindow) currentFocusOwner).getProxyView().requestFocus();
        }
    }

    /**
     * Returns the component that currently owns the input focus.
     *
     * @return the component that currently owns the input focus
     *
     * @see KeyboardFocusManager#getNativeFocusOwner()
     */
    @Override
    public Component getCurrentFocusOwner() {
        return currentFocusOwner;
    }

    /**
     * Clears the current global focus owner.
     *
     * @param activeWindow
     *
     * @see KeyboardFocusManager#clearGlobalFocusOwner()
     */
    @Override
    public void clearGlobalFocusOwner(Window activeWindow) {
        // TODO: Implement.
    }

    public void setCurrentFocusedWindow(Window w) {
        currentFocusedWindow = w;
        if (currentFocusedWindow instanceof ProxyWindow) {
            ((ProxyWindow) currentFocusedWindow).getProxyView().requestFocus();
        }
    }

    boolean requestFocus(Component target, Component lightweightChild, boolean temporary,
                         boolean focusedWindowChangeAllowed, long time, Cause cause) {

        if (target instanceof ProxyWindow) {
            ((ProxyWindow) target).getProxyView().requestFocus();
        } else {
            log.warning("Cannot handle non-ProxyWindow component");
        }
        return false;
    }

    @SuppressWarnings("restriction")
    private void postEvent(AWTEvent ev) {
        SunToolkit.postEvent(AppContext.getAppContext(), ev);
    }

    void focusGained(ProxyWindow w) {
        log.fine("Focus gained: " + w);
        @SuppressWarnings("restriction")
        FocusEvent fg = new CausedFocusEvent(w, FocusEvent.FOCUS_GAINED, false,
                                             currentFocusOwner,
                                             Cause.NATIVE_SYSTEM);
        postEvent(fg);
    }

    void focusLost(ProxyWindow w) {
        log.fine("Focus lost: " + w);
        @SuppressWarnings("restriction")
        FocusEvent fl = new CausedFocusEvent(w, FocusEvent.FOCUS_LOST, false,
                                             null, Cause.NATIVE_SYSTEM);
        postEvent(fl);
    }

}
