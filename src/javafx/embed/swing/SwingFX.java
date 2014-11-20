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

import java.awt.KeyboardFocusManager;
import java.lang.reflect.Field;

import javax.swing.RepaintManager;

/**
 * Helper class for {@link SwingView}. The purpose of this class is to
 * initialize the special {@link RepaintManager} needed to hook Swing components
 * into JavaFX views.<br /><br />
 * 
 * The {@link #init()} method must be called before initaliazing the graphics
 * state of the application, typically within the main method before calling
 * one of the {@link Application#launch(Class, String...))} methods. 
 */
public class SwingFX {

    private static boolean initialized;
    
    /**
     * Initializes and install the {@link RepaintManager} needed by
     * {@link SwingView}.
     */
    public static void init() {
        System.setProperty("swing.volatileImageBufferEnabled", "false");
        
        // workaround for Mac OS X JDK that cast Graphics to SunGraphics2D
        // internally, not painting our components
        // this workaround should not be needed from JDK 7 onward
        String enableNativeBuffering = "true";
//        if (System.getProperty("os.name").contains("Mac")) {            
//            enableNativeBuffering = "false";
//        }
        System.setProperty("awt.nativeDoubleBuffering", enableNativeBuffering);
        
//        new JFXPanel(); // needed as a trick to launch it on a Mac
        
        Class<KeyboardFocusManager> kfmCls = KeyboardFocusManager.class;
        Field peer;
        try {
            peer = kfmCls.getDeclaredField("peer");
            peer.setAccessible(true);
            peer.set(KeyboardFocusManager.getCurrentKeyboardFocusManager(),
                     FXSwingKeyboardFocusManagerPeer.getInstance());
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialized = true;
    }
    
    static boolean isInitialized() {
        return initialized;
    }
}
