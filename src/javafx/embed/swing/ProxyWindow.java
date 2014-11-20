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
import java.awt.Frame;
import java.lang.reflect.Field;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

class ProxyWindow extends Frame {

    private static Field peer;
    
    private static void initReflection() {
        try {
            peer = Component.class.getDeclaredField("peer");
            peer.setAccessible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    static {
        initReflection();
    }
    
    private SwingView proxyView;
    
    ProxyWindow(SwingView proxyView) {
        this.proxyView = proxyView;
        
        try {
            peer.set(this, new ProxyWindowPeer(this));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        proxyView.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> obs,
                                Boolean oldValue, Boolean newValue) {
                FXSwingKeyboardFocusManagerPeer peer =
                        FXSwingKeyboardFocusManagerPeer.getInstance();
                if (newValue) {
                    peer.focusGained(ProxyWindow.this);
                } else {
                    peer.focusLost(ProxyWindow.this);
                }
            }
        });
    }

    @Override
    public boolean isVisible() {
        return proxyView.isVisible();
    }
    @Override
    public boolean isShowing() {
        return proxyView.isVisible();
    }

    @Override
    public boolean isFocusTraversable() {
        return proxyView.isFocusTraversable();
    }

    SwingView getProxyView() {
        return proxyView;
    }
}
