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
import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.BufferCapabilities.FlipContents;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuBar;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.ContainerPeer;
import java.awt.peer.FramePeer;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import sun.awt.CausedFocusEvent.Cause;
import sun.java2d.pipe.Region;

class ProxyWindowPeer implements FramePeer {

    private ProxyWindow window;

    ProxyWindowPeer(ProxyWindow w) {
        window = w;
    }

    @Override
    public void toFront() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void toBack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFocusableWindowState() {
        throw new UnsupportedOperationException();
    }

    // @Override No longer present in JDK6
    public boolean requestWindowFocus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setModalBlocked(Dialog blocker, boolean blocked) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateMinimumSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateIconImages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOpacity(float opacity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        // Noop.
    }

    @Override
    public void updateWindow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void repositionSecurityWarning() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public void beginValidate() {
        // Nothing to do here.
    }

    @Override
    public void endValidate() {
        // Nothing to do here.
    }

    @Override
    public void beginLayout() {
        // Nothing to do here.
    }

    @Override
    public void endLayout() {
        // Nothing to do here.
    }

    // @Override Not present in JDK7
    public boolean isPaintPending() {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public void restack() {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public boolean isRestackSupported() {
        return false;
    }

    // @Override Not present in JDK7
    public Insets insets() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isObscured() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canDetermineObscurity() {
        return false;
    }

    @Override
    public void setVisible(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnabled(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void paint(Graphics g) {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public void repaint(long tm, int x, int y, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(Graphics g) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBounds(int x, int y, int width, int height, int op) {
        // Nothing to do here.
    }

    @Override
    public void handleEvent(AWTEvent e) {
        // Nothing to do here.
    }

    @Override
    public void coalescePaintEvent(PaintEvent e) {
     // Nothing to do here.
    }

    @Override
    public Point getLocationOnScreen() {
        Point loc = new Point();
        Node node = window.getProxyView();
        while (true) {
            loc.x += node.getLayoutX();
            loc.y += node.getLayoutY();
            Node parent = node.getParent();
            if (parent == null) {
                break;
            }
            node = parent;
        }
        Scene scene = node.getScene();
        loc.x += scene.getX();
        loc.y += scene.getY();
        Window window = scene.getWindow();
        loc.x += window.getX();
        loc.y += window.getY();
        return loc;
    }

    @Override
    public Dimension getPreferredSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension getMinimumSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColorModel getColorModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }

    @Override
    public Graphics getGraphics() {
        ProxyGraphics g = new ProxyGraphics(window.getProxyView(), (Graphics2D)
                        window.getProxyView().getBackBuffer().getGraphics());
        g.setColor(window.getForeground());
        g.setBackground(window.getBackground());
        g.setFont(window.getFont());
        return g;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setForeground(Color c) {
        // Noop.
    }

    @Override
    public void setBackground(Color c) {
        // Noop.
    }

    @Override
    public void setFont(Font f) {
        // Noop.
    }

    @Override
    public void updateCursorImmediately() {
        // TODO: Implement.
    }

    @Override
    public boolean requestFocus(Component lightweightChild, boolean temporary,
            boolean focusedWindowChangeAllowed, long time, Cause cause) {
        if (KFMHelper.processSynchronousLightweightTransfer(window,
                lightweightChild, temporary, focusedWindowChangeAllowed, time)) {
            return true;
        }

        int result = KFMHelper.shouldNativelyFocusHeavyweight(window,
                lightweightChild, temporary, focusedWindowChangeAllowed, time,
                cause);

        switch (result) {
        case KFMHelper.SNFH_FAILURE:
            return false;
        case KFMHelper.SNFH_SUCCESS_PROCEED:
            return FXSwingKeyboardFocusManagerPeer.getInstance().
                    requestFocus(window, lightweightChild, temporary,
                                 focusedWindowChangeAllowed, time, cause);
        case KFMHelper.SNFH_SUCCESS_HANDLED:
            // Either lightweight or excessive request - all events are
            // generated.
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean isFocusable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Image createImage(ImageProducer producer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Image createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public VolatileImage createVolatileImage(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean handlesWheelScrolling() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createBuffers(int numBuffers, BufferCapabilities caps)
            throws AWTException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Image getBackBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flip(int x1, int y1, int x2, int y2, FlipContents flipAction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroyBuffers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reparent(ContainerPeer newContainer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReparentSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void layout() {
        // Nothing to do here.
    }

    // @Override Not present in JDK7
    public Rectangle getBounds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyShape(Region shape) {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public Dimension preferredSize() {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public Dimension minimumSize() {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public void show() {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK7
    public void hide() {
        throw new UnsupportedOperationException();
    }

    // @Override no more present in JDK7
    public void enable() {
        throw new UnsupportedOperationException();
    }

    // @Override no more present in JDK7
    public void disable() {
        throw new UnsupportedOperationException();
    }

    // @Override no more present in JDK7
    public void reshape(int x, int y, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTitle(String title) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMenuBar(MenuBar mb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResizable(boolean resizeable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setState(int state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public void setMaximizedBounds(Rectangle bounds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoundsPrivate(int x, int y, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rectangle getBoundsPrivate() {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK6
    public void setZOrder(ComponentPeer above) {
        throw new UnsupportedOperationException();
    }

    // @Override Not present in JDK6
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAlwaysOnTopState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
