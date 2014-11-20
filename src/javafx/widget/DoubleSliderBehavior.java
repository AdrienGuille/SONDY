/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Modified by Altug Uzunali (altug.uzunali@gmail.com) at 08/02/2012
 * 
 * Added two thumbs functionality
 * 
 */
package javafx.widget;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
import static javafx.scene.input.KeyCode.F4;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.KP_DOWN;
import static javafx.scene.input.KeyCode.KP_LEFT;
import static javafx.scene.input.KeyCode.KP_RIGHT;
import static javafx.scene.input.KeyCode.KP_UP;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import com.sun.javafx.Utils;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.OrientedKeyBinding;


public class DoubleSliderBehavior extends BehaviorBase<DoubleSlider>{

	public DoubleSliderBehavior(DoubleSlider doubleSlider) {
		super(doubleSlider);
	}
	
    /**************************************************************************
     *                          Setup KeyBindings                             *
     *                                                                        *
     * We manually specify the focus traversal keys because DoubleSlider has  *
     * different usage for up/down arrow keys.                                *
     *************************************************************************/
    protected static final List<KeyBinding> DOUBLESLIDER_BINDINGS = new ArrayList<KeyBinding>();
    static {
        DOUBLESLIDER_BINDINGS.add(new KeyBinding(TAB, "TraverseNext"));
        DOUBLESLIDER_BINDINGS.add(new KeyBinding(TAB, "TraversePrevious").shift());
        // TODO XXX DEBUGGING ONLY
        DOUBLESLIDER_BINDINGS.add(new KeyBinding(F4, "TraverseDebug").alt().ctrl().shift());

        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(LEFT, "DecrementValue"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_LEFT, "DecrementValue"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(UP, "IncrementValue").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_UP, "IncrementValue").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(RIGHT, "IncrementValue"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_RIGHT, "IncrementValue"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(DOWN, "DecrementValue").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_DOWN, "DecrementValue").vertical());

        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(LEFT, "TraverseLeft").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_LEFT, "TraverseLeft").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(UP, "TraverseUp"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_UP, "TraverseUp"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(RIGHT, "TraverseRight").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_RIGHT, "TraverseRight").vertical());
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(DOWN, "TraverseDown"));
        DOUBLESLIDER_BINDINGS.add(new DoubleSliderKeyBinding(KP_DOWN, "TraverseDown"));

        DOUBLESLIDER_BINDINGS.add(new KeyBinding(HOME, KEY_RELEASED, "Home"));
        DOUBLESLIDER_BINDINGS.add(new KeyBinding(END, KEY_RELEASED, "End"));
    }

	
	@Override
	protected void callAction(String name){
        if ("Home".equals(name)) home();
        else if ("End".equals(name)) end();
        else if ("IncrementValue1".equals(name)) incrementValue1();
        else if ("DecrementValue1".equals(name)) decrementValue1();
        else if ("IncrementValue2".equals(name)) incrementValue2();
        else if ("DecrementValue2".equals(name)) decrementValue2();
        else super.callAction(name);
	}
	
    @Override protected List<KeyBinding> createKeyBindings() {
        return DOUBLESLIDER_BINDINGS;
    }
	
    /**
     * Invoked by the Slider {@link Skin} implementation whenever a mouse press
     * occurs on the "track" of the slider. This will cause the thumb to be
     * moved by some amount.
     *
     * @param position The mouse position on track with 0.0 being beginning of
     *        track and 1.0 being the end
     */
    public void trackPress(MouseEvent e, double position) {
        // determine the percentage of the way between min and max
        // represented by this mouse event
        final DoubleSlider doubleSlider = getControl();
        // If not already focused, request focus
        if (!doubleSlider.isFocused()) doubleSlider.requestFocus();
        if (doubleSlider.getOrientation().equals(Orientation.HORIZONTAL)) {
            doubleSlider.adjustValue(position * (doubleSlider.getMax() - doubleSlider.getMin()) + doubleSlider.getMin());
        } else {
            doubleSlider.adjustValue((1-position) * (doubleSlider.getMax() - doubleSlider.getMin()) + doubleSlider.getMin());
        }
    }
    
    /**
     */
    public void trackRelease(MouseEvent e, double position) {
    }

     /**
     * @param position The mouse position on track with 0.0 being beginning of
      *       track and 1.0 being the end
     */
    public void thumbPressed(MouseEvent e, double position) {
        // If not already focused, request focus
        final DoubleSlider doubleSlider = getControl();
        if (!doubleSlider.isFocused())  doubleSlider.requestFocus();
        doubleSlider.setValueChanging(true);
    }

    /**
     * @param position The mouse position on track with 0.0 being beginning of
     *        track and 1.0 being the end
     */
    public void thumbDragged(DoubleSliderSkin.ThumbNumber thumbNumber, MouseEvent e, double position) {
        final DoubleSlider doubleSlider = getControl();
        switch(thumbNumber){
        case Thumb1:
//        	doubleSlider.setValue1(Utils.clamp(doubleSlider.getMin(), (position * (doubleSlider.getMax() - doubleSlider.getMin())) + doubleSlider.getMin(), doubleSlider.getMax()));
        	doubleSlider.setValue1(Utils.clamp(doubleSlider.getMin(), (position * (doubleSlider.getMax() - doubleSlider.getMin())) + doubleSlider.getMin(), doubleSlider.getValue2()));
        	break;
        case Thumb2:
        	doubleSlider.setValue2(Utils.clamp(doubleSlider.getValue1(), (position * (doubleSlider.getMax() - doubleSlider.getMin())) + doubleSlider.getMin(), doubleSlider.getMax()));
        }
    }
    /**
     * When thumb is released valueChanging should be set to false.
     */
    public void thumbReleased(MouseEvent e) {
        final DoubleSlider doubleSlider = getControl();
        doubleSlider.setValueChanging(false);
        // RT-15207 When snapToTicks is true, slider value calculated in drag
        // is then snapped to the nearest tick on mouse release.
        // TODO: reconsider after tick mark implementation
        if (doubleSlider.isSnapToTicks()) {
            doubleSlider.setValue1(snapValueToTicks(doubleSlider.getValue1()));
        }
    }
    
    private double snapValueToTicks(double val) {
        final DoubleSlider doubleSlider = getControl();
        double v = val;
        double tickSpacing = 0;
        // compute the nearest tick to this value
        if (doubleSlider.getMinorTickCount() != 0) {
            tickSpacing = doubleSlider.getMajorTickUnit() / (Math.max(doubleSlider.getMinorTickCount(),0)+1);
        } else {
            tickSpacing = doubleSlider.getMajorTickUnit();
        }
        int prevTick = (int)((v - doubleSlider.getMin())/ tickSpacing);
        double prevTickValue = (prevTick) * tickSpacing + doubleSlider.getMin();
        double nextTickValue = (prevTick + 1) * tickSpacing + doubleSlider.getMin();
        v = Utils.nearest(prevTickValue, v, nextTickValue);
        return Utils.clamp(doubleSlider.getMin(), v, doubleSlider.getMax());
    }

	private void decrementValue2() {
		// TODO Auto-generated method stub
		
	}

	private void incrementValue2() {
		// TODO Auto-generated method stub
		
	}

	private void decrementValue1() {
		// TODO Auto-generated method stub
		
	}

	private void incrementValue1() {
		// TODO Auto-generated method stub
		
	}

	private void end() {
        final DoubleSlider doubleSlider = getControl();
        doubleSlider.adjustValue(doubleSlider.getMax());
	}


	private void home() {
        final DoubleSlider doubleSlider = getControl();
        doubleSlider.adjustValue(doubleSlider.getMin());
	}
	
    // Used only if snapToTicks is true.
    double computeIncrement() {
        final DoubleSlider doubleSlider = getControl();
        double tickSpacing = 0;
        if (doubleSlider.getMinorTickCount() != 0) {
            tickSpacing = doubleSlider.getMajorTickUnit() / (Math.max(doubleSlider.getMinorTickCount(),0)+1);
        } else {
            tickSpacing = doubleSlider.getMajorTickUnit();
        }

        if (doubleSlider.getBlockIncrement() > 0 && doubleSlider.getBlockIncrement() < tickSpacing) {
                return tickSpacing;
        }

        return doubleSlider.getBlockIncrement();
    }

    public static class DoubleSliderKeyBinding extends OrientedKeyBinding {
        public DoubleSliderKeyBinding(KeyCode code, String action) {
            super(code, action);
        }

        public DoubleSliderKeyBinding(KeyCode code, EventType<KeyEvent> type, String action) {
            super(code, type, action);
        }

        public @Override boolean getVertical(Control control) {
            return ((DoubleSlider)control).getOrientation() == Orientation.VERTICAL;
        }
    }

}
