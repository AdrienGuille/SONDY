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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.control.Control;
import javafx.scene.input.MouseButton;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * A {@link Control} that allows embedding of Swing widgets.
 * Before using this control, {@link SwingFX#init()} must be called before
 * initializing the graphics state.
 */
public class SwingView extends Control {

    private static final Map<EventType<?>, Integer> mouseEventMap;
    static {
        Map<EventType<?>, Integer> map = new HashMap<EventType<?>, Integer>();
        map.put(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                MouseEvent.MOUSE_PRESSED);
        map.put(javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                MouseEvent.MOUSE_RELEASED);
        map.put(javafx.scene.input.MouseEvent.MOUSE_ENTERED,
                MouseEvent.MOUSE_ENTERED);
        map.put(javafx.scene.input.MouseEvent.MOUSE_EXITED,
                MouseEvent.MOUSE_EXITED);
        map.put(javafx.scene.input.MouseEvent.MOUSE_MOVED,
                MouseEvent.MOUSE_MOVED);
        map.put(javafx.scene.input.MouseEvent.MOUSE_DRAGGED,
                MouseEvent.MOUSE_DRAGGED);
        mouseEventMap = Collections.unmodifiableMap(map);
    }

    private class MouseEventHandler implements
            EventHandler<javafx.scene.input.MouseEvent> {

        @Override
        public void handle(javafx.scene.input.MouseEvent jfxMouseEvent) {
            EventType<?> type = jfxMouseEvent.getEventType();
            int id = mouseEventMap.get(type);
            int button = getAWTButton(jfxMouseEvent.getButton());
            int modifiers = getAWTModifiers(jfxMouseEvent);
            final MouseEvent awtEvent =
                    new MouseEvent(component, id, System.currentTimeMillis(),
                                   modifiers,
                                   (int) jfxMouseEvent.getX(),
                                   (int) jfxMouseEvent.getY(),
                                   (int) jfxMouseEvent.getScreenX(),
                                   (int) jfxMouseEvent.getScreenY(),
                                   jfxMouseEvent.getClickCount(),
                                   false,
                                   button);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SwingEventDispatcherHelper.dispatchEvent(awtEvent, component);
                }
            });
        }

        private int getAWTModifiers(javafx.scene.input.MouseEvent jfxMouseEvent) {
            int mods = 0;
            if (jfxMouseEvent.isAltDown()) {
                mods |= MouseEvent.ALT_MASK | MouseEvent.ALT_DOWN_MASK;
            }
            if (jfxMouseEvent.isControlDown()) {
                mods |= MouseEvent.CTRL_MASK | MouseEvent.CTRL_DOWN_MASK;
            }
            if (jfxMouseEvent.isShiftDown()) {
                mods |= MouseEvent.SHIFT_MASK | MouseEvent.SHIFT_DOWN_MASK;
            }
            if (jfxMouseEvent.isMetaDown()) {
                mods |= MouseEvent.META_MASK | MouseEvent.META_DOWN_MASK;
            }
            if (jfxMouseEvent.isPrimaryButtonDown()) {
                mods |= MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON1_DOWN_MASK;
            }
            if (jfxMouseEvent.isSecondaryButtonDown()) {
                mods |= MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON2_DOWN_MASK;
            }
            if (jfxMouseEvent.isMiddleButtonDown()) {
                mods |= MouseEvent.BUTTON3_MASK | MouseEvent.BUTTON3_DOWN_MASK;
            }

            return mods;
        }

        private int getAWTButton(MouseButton button) {
            switch (button) {
            case PRIMARY:
                return MouseEvent.BUTTON1;
            case SECONDARY:
                return MouseEvent.BUTTON2;
            case MIDDLE:
                return MouseEvent.BUTTON3;
            case NONE:
            default:
                return MouseEvent.NOBUTTON;
            }
        }
    }

    private class PainterTask implements Runnable {
        @Override
        public void run() {
            getImageView().paintImage(getBackBuffer());
            painterTaskFuture = null;
        }
    }

    private static final long COMMIT_DELAY = 30;

    private JComponent component;

    private ProxyWindow proxy;
    private BufferedImageView imgView;

    private BufferedImage backBuffer;

    private static ScheduledExecutorService painterTimer =
            Executors.newScheduledThreadPool(1);

    private Runnable painterTask = new PainterTask();
    private ScheduledFuture<?> painterTaskFuture;

    public SwingView(JComponent comp) {
        
        if (!SwingFX.isInitialized()) {
            throw new InternalError("SwingFX.init() must be called before");
        }
        
        this.component = comp;

        imgView = new BufferedImageView();
    	getChildren().add(imgView);

        registerEvents();

        layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {

            @Override
            public void changed(ObservableValue<? extends Bounds> obs,
                    Bounds oldVal, final Bounds newVal) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        proxy.setSize((int) newVal.getWidth(),
                                      (int) newVal.getHeight());
                        component.setSize((int) newVal.getWidth(),
                                          (int) newVal.getHeight());
                        component.validate();
                    }
                    
                });
            }
        });

        Dimension min = component.getMinimumSize();
        setMinSize(min.getWidth(), min.getHeight());
        Dimension pref = component.getPreferredSize();
        setMinSize(pref.getWidth(), pref.getHeight());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                JComponent component = SwingView.this.component;
                proxy = new ProxyWindow(SwingView.this);
                proxy.setForeground(component.getForeground());
                proxy.setBackground(component.getBackground());
                proxy.setFont(component.getFont());
                proxy.add(component);
                component.addNotify();

                SwingEventDispatcherHelper.setLightweightDispatcher(component);
                component.setVisible(true);
                component.doLayout();
                component.repaint();
            }
        });
    }

    private void registerEvents() {
        MouseEventHandler handler = new MouseEventHandler();
        setOnMousePressed(handler);
        setOnMouseReleased(handler);
        setOnMouseMoved(handler);
        setOnMouseDragged(handler);
        setOnMouseEntered(handler);
        setOnMouseExited(handler);
        KeyEventHandler keyHandler = new KeyEventHandler(component);
        setOnKeyPressed(keyHandler);
        setOnKeyReleased(keyHandler);
        setOnKeyTyped(keyHandler);
        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                    Boolean oldValue, Boolean newValue) {
                if (newValue == Boolean.FALSE) {
                    FocusEvent fl = new FocusEvent(component,
                                                   FocusEvent.FOCUS_LOST);
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(fl);
                }
            }
        });
    }

    Component getComponent() {
        return component;
    }

    BufferedImageView getImageView() {
    	return imgView;
    }

    protected double computePrefWidth(double h) {
        return component.getPreferredSize().getWidth();
    }

    protected double computePrefHeight(double w) {
        return component.getPreferredSize().getHeight();
    }

    public synchronized void commit() {

        if (painterTaskFuture != null) {
          painterTaskFuture.cancel(false);
        }

        painterTaskFuture = painterTimer.schedule(painterTask, COMMIT_DELAY,
                                                  TimeUnit.MILLISECONDS);
    }

    BufferedImage getBackBuffer() {
        if (backBuffer == null || backBuffer.getWidth() < component.getWidth() ||
            backBuffer.getHeight() < component.getHeight())
        {
            backBuffer = new BufferedImage(component.getWidth(),
                                           component.getHeight(),
                                           BufferedImage.TYPE_INT_ARGB);
        }
        return backBuffer;
    }
}
