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

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.StackPane;

import com.sun.javafx.scene.control.skin.SkinBase;

public class DoubleSliderSkin extends SkinBase<DoubleSlider, DoubleSliderBehavior>{

	public enum ThumbNumber {
		Thumb1,
		Thumb2
	}
	private double trackStart;
	private double thumbTop;
	private double thumbLeft;
	private double thumbWidth;
	private double thumbHeight;
	private double trackLength;
	private double preDragThumbPos;
	private Point2D dragStart; 
	private boolean showTickMarks; 
	private NumberAxis tickLine = null;
	private double trackToTickGap = 2;

	private StackPane thumb1;
	private StackPane thumb2;
	private StackPane track;
	/**
	 * @param doubleSlider
	 * @param doubleSliderBehavior
	 */
	public DoubleSliderSkin(DoubleSlider doubleSlider) {
		super(doubleSlider, new DoubleSliderBehavior(doubleSlider));
		initialize();
		requestLayout();
		registerChangeListener(doubleSlider.minProperty(), "MIN");
		registerChangeListener(doubleSlider.maxProperty(), "MAX");
		registerChangeListener(doubleSlider.value1Property(), "VALUE1");
		registerChangeListener(doubleSlider.value2Property(), "VALUE2");
		registerChangeListener(doubleSlider.orientationProperty(), "ORIENTATION");
		registerChangeListener(doubleSlider.showTickMarksProperty(), "SHOW_TICK_MARKS");
		registerChangeListener(doubleSlider.showTickLabelsProperty(), "SHOW_TICK_LABELS");
		registerChangeListener(doubleSlider.majorTickUnitProperty(), "MAJOR_TICK_UNIT");
		registerChangeListener(doubleSlider.minorTickCountProperty(), "MINOR_TICK_COUNT");
	}

	private void initialize() {
		thumb1 = new StackPane();
		thumb1.getStyleClass().setAll("thumb1");

		thumb2 = new StackPane();
		thumb2.getStyleClass().setAll("thumb2");

		track = new StackPane();
		track.getStyleClass().setAll("track");
		//        horizontal = getSkinnable().isVertical();

		getChildren().clear();
		getChildren().addAll(track, thumb1, thumb2);
		setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
		track.setOnMousePressed( new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				if (!thumb1.isPressed()) {
					if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
						getBehavior().trackPress(me, (me.getX() / trackLength));
					} else getBehavior().trackPress(me, (me.getY() / trackLength));
				}
			}
		});

		track.setOnMouseReleased( new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				//Nothing being done with the second param in sliderBehavior
				//So, passing a dummy value
				getBehavior().trackRelease(me, 0.0f);
			}
		});

		thumb1.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				getBehavior().thumbPressed(me, 0.0f);
				dragStart = thumb1.localToParent(me.getX(), me.getY());
				preDragThumbPos = (getSkinnable().getValue1() - getSkinnable().getMin()) /
						(getSkinnable().getMax() - getSkinnable().getMin());
			}
		});

		thumb1.setOnMouseReleased(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				getBehavior().thumbReleased(me);
			}
		});

		thumb1.setOnMouseDragged(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				Point2D cur = thumb1.localToParent(me.getX(), me.getY());
				double dragPos = (getSkinnable().getOrientation() == Orientation.HORIZONTAL)?
						cur.getX() - dragStart.getX() : -(cur.getY() - dragStart.getY());
						getBehavior().thumbDragged(ThumbNumber.Thumb1, me, preDragThumbPos + dragPos / trackLength);
			}
		});

		thumb2.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				getBehavior().thumbPressed(me, 0.0f);
				dragStart = thumb2.localToParent(me.getX(), me.getY());
				preDragThumbPos = (getSkinnable().getValue2() - getSkinnable().getMin()) /
						(getSkinnable().getMax() - getSkinnable().getMin());
			}
		});

		thumb2.setOnMouseReleased(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				getBehavior().thumbReleased(me);
			}
		});

		thumb2.setOnMouseDragged(new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override public void handle(javafx.scene.input.MouseEvent me) {
				Point2D cur = thumb2.localToParent(me.getX(), me.getY());
				double dragPos = (getSkinnable().getOrientation() == Orientation.HORIZONTAL)?
						cur.getX() - dragStart.getX() : -(cur.getY() - dragStart.getY());
						getBehavior().thumbDragged(ThumbNumber.Thumb2, me, preDragThumbPos + dragPos / trackLength);
			}
		});
	}

	private void setShowTickMarks(boolean ticksVisible, boolean labelsVisible) {
		showTickMarks = (ticksVisible || labelsVisible);
		DoubleSlider doubleSlider = getSkinnable();
		if (showTickMarks) {
			if (tickLine == null) {
				tickLine = new NumberAxis();
				tickLine.setAutoRanging(false);
				tickLine.setSide(doubleSlider.getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (doubleSlider.getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
				tickLine.setUpperBound(doubleSlider.getMax());
				tickLine.setLowerBound(doubleSlider.getMin());
				tickLine.setTickUnit(doubleSlider.getMajorTickUnit());
				tickLine.setTickMarkVisible(ticksVisible);
				tickLine.setTickLabelsVisible(labelsVisible);
				tickLine.setMinorTickVisible(ticksVisible);
//				tickLine.setTickLabelFill(Color.WHITE);
				// add 1 to the slider minor tick count since the axis draws one
				// less minor ticks than the number given.
				tickLine.setMinorTickCount(Math.max(doubleSlider.getMinorTickCount(),0) + 1);
				// TODO change slider API to Integer from Number
				//            if (slider.getLabelFormatter() != null)
				//                tickLine.setFormatTickLabel(slider.getLabelFormatter());
				//            tickLine.dataChanged();
				getChildren().clear();
				getChildren().addAll(tickLine, track, thumb1, thumb2);
			} else {
				tickLine.setTickLabelsVisible(labelsVisible);
				tickLine.setTickMarkVisible(ticksVisible);
				tickLine.setMinorTickVisible(ticksVisible);
			}
		} 
		else  {
			getChildren().clear();
			getChildren().addAll(track, thumb1, thumb2);
			//            tickLine = null;
		}

		requestLayout();
	}

	/**
	 * Called when ever either min, max or value changes, so thumb's layoutX, Y is recomputed.
	 */
	void positionThumb() {
		DoubleSlider s = getSkinnable();
		boolean horizontal = getSkinnable().getOrientation() == Orientation.HORIZONTAL;
		double lx1 = (horizontal) ? trackStart + (((trackLength * ((s.getValue1() - s.getMin()) /
				(s.getMax() - s.getMin()))) - thumbWidth/2)) : thumbLeft + (thumbWidth / 4);
		double ly1 = (horizontal) ? thumbTop :
			getInsets().getTop() + trackLength - (trackLength * ((s.getValue1() - s.getMin()) /
					(s.getMax() - s.getMin()))) + thumbHeight / 4; 
		double lx2 = (horizontal) ? trackStart + (((trackLength * ((s.getValue2() - s.getMin()) /
				(s.getMax() - s.getMin()))) - thumbWidth/2)) : thumbLeft - (thumbWidth / 4);
		double ly2 = (horizontal) ? thumbTop :
			getInsets().getTop() + trackLength - (trackLength * ((s.getValue2() - s.getMin()) /
					(s.getMax() - s.getMin()))) - thumbHeight / 4; 
		thumb1.setLayoutX(lx1);
		thumb1.setLayoutY(ly1);
		thumb2.setLayoutX(lx2);
		thumb2.setLayoutY(ly2);
	}

	@Override protected void layoutChildren() {
		// calculate the available space

		double x = getInsets().getLeft();
		double y = getInsets().getTop();
		double w = getWidth() - (getInsets().getLeft() + getInsets().getRight());
		double h = getHeight() - (getInsets().getTop() + getInsets().getBottom());
		// resize thumb to preferred size
		thumbWidth = thumb2.prefWidth(-1);
		thumbHeight = thumb2.prefHeight(-1);
		//		thumb1.resize(thumbWidth, thumbHeight);
		thumb1.resize(thumbWidth / 2, thumbHeight);
		thumb2.resize(thumbWidth / 2, thumbHeight);
		thumb2.setTranslateX(thumbWidth / 2);
		// we are assuming the is common radius's for all corners on the track
		//       double trackRadius = (track.impl_getBackgroundFills() != null && track.impl_getBackgroundFills().size() > 0) ?
		//           track.impl_getBackgroundFills().get(0).getTopLeftCornerRadius() : 0;
		double trackRadius = 0;
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			double tickLineHeight =  (showTickMarks) ? tickLine.prefHeight(-1) : 0;
			double trackHeight = track.prefHeight(-1);
			double trackAreaHeight = Math.max(trackHeight,thumbHeight);
			double totalHeightNeeded = trackAreaHeight  + ((showTickMarks) ? trackToTickGap + tickLineHeight : 0);
			double startY = y + ((h - totalHeightNeeded) / 2); // center slider in available height vertically
			trackLength = w - thumbWidth;
			trackStart = x + (thumbWidth / 2);
			double trackTop = (int)(startY + ((trackAreaHeight - trackHeight) / 2));
			thumbTop = (int)(startY + ((trackAreaHeight - thumbHeight) / 2));
			positionThumb();
			// layout track
			track.resizeRelocate(trackStart - trackRadius, trackTop , trackLength + trackRadius + trackRadius, trackHeight);
			// layout tick line
			if (showTickMarks) {
				tickLine.setLayoutX(trackStart);
				tickLine.setLayoutY(trackTop + trackHeight + trackToTickGap);
				tickLine.resize(trackLength, tickLineHeight);
				tickLine.requestAxisLayout();
			} else {
				if (tickLine != null) {
					tickLine.resize(0,0);
					tickLine.requestAxisLayout();
				}
				tickLine = null;
			}
		} else {
			double tickLineWidth = (showTickMarks) ? tickLine.prefWidth(-1) : 0;
			double trackWidth = track.prefWidth(-1);
			double trackAreaWidth = Math.max(trackWidth,thumbWidth);
			double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap + tickLineWidth : 0) ;
			double startX = x + ((w - totalWidthNeeded)/2); // center slider in available width horizontally
			trackLength = h - thumbHeight;
			trackStart = y + (thumbHeight / 2);
			double trackLeft = (int)(startX + ((trackAreaWidth - trackWidth) / 2));
			thumbLeft = (int)(startX + ((trackAreaWidth - thumbWidth) / 2));
			// Thumbs are rotated in vertical orientation (Altug Uzunali)
			thumb1.setRotate(-90);
			thumb2.setRotate(-90);
			positionThumb();
			// layout track
			track.resizeRelocate(trackLeft, trackStart - trackRadius, trackWidth, trackLength + trackRadius + trackRadius);
			// layout tick line
			if (showTickMarks) {
				tickLine.setLayoutX(trackLeft + trackWidth + trackToTickGap);
				tickLine.setLayoutY(trackStart);
				tickLine.resize(tickLineWidth, trackLength);
				tickLine.requestAxisLayout();
			} else {
				if (tickLine != null) {
					tickLine.resize(0,0);
					tickLine.requestAxisLayout();
				}
				tickLine = null;
			}
		}
	}
	@Override protected void handleControlPropertyChanged(String p) {
		super.handleControlPropertyChanged(p);
		if ("ORIENTATION".equals(p)) {
			if (showTickMarks && tickLine != null) {
				tickLine.setSide(getSkinnable().getOrientation() == Orientation.VERTICAL ? Side.RIGHT : (getSkinnable().getOrientation() == null) ? Side.RIGHT: Side.BOTTOM);
			}
			requestLayout();
		} else if ("VALUE1".equals(p)) {
			positionThumb();
		} else if ("VALUE2".equals(p)) {
			positionThumb();
		} else if ("MIN".equals(p) ) {
			if (showTickMarks && tickLine != null) {
				tickLine.setLowerBound(getSkinnable().getMin());
			}
			requestLayout();
		} else if ("MAX".equals(p)) {
			if (showTickMarks && tickLine != null) {
				tickLine.setUpperBound(getSkinnable().getMax());
			}
			requestLayout();
		} else if ("SHOW_TICK_MARKS".equals(p) || "SHOW_TICK_LABELS".equals(p)) {
			setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
		}  else if ("MAJOR_TICK_UNIT".equals(p)) {
			if (tickLine != null) {
				tickLine.setTickUnit(getSkinnable().getMajorTickUnit());
				requestLayout();
			}
		} else if ("MINOR_TICK_COUNT".equals(p)) {
			if (tickLine != null) {
				tickLine.setMinorTickCount(Math.max(getSkinnable().getMinorTickCount(),0) + 1);
				requestLayout();
			}
		}
	}
	double minTrackLength() {
		return 2*thumb1.prefWidth(-1);
	}

	@Override protected double computeMinWidth(double height) {
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			return (getInsets().getLeft() + minTrackLength() + thumb1.minWidth(-1) + getInsets().getRight());
		} else {
			return(getInsets().getLeft() + thumb1.prefWidth(-1) + getInsets().getRight());
		}
	}

	@Override protected double computeMinHeight(double width) {
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			return(getInsets().getTop() + thumb1.prefHeight(-1) + getInsets().getBottom());
		} else {
			return(getInsets().getTop() + minTrackLength() + thumb1.prefHeight(-1) + getInsets().getBottom());
		}
	}

	@Override protected double computePrefWidth(double height) {
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			if(showTickMarks) {
				return Math.max(140, tickLine.prefWidth(-1));
			} else {
				return 140;
			}
		} else {
			//return (padding.getLeft()) + Math.max(thumb.prefWidth(-1), track.prefWidth(-1)) + padding.getRight();
			return (getInsets().getLeft()) + Math.max(thumb1.prefWidth(-1), track.prefWidth(-1)) +
					((showTickMarks) ? (trackToTickGap+tickLine.prefWidth(-1)) : 0) + getInsets().getRight();
		}
	}

	@Override protected double computePrefHeight(double width) {
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			return getInsets().getTop() + Math.max(thumb1.prefHeight(-1), track.prefHeight(-1)) +
					((showTickMarks) ? (trackToTickGap+tickLine.prefHeight(-1)) : 0)  + getInsets().getBottom();
		} else {
			if(showTickMarks) {
				return Math.max(140, tickLine.prefHeight(-1));
			} else {
				return 140;
			}
		}
	}

	@Override protected double computeMaxWidth(double height) {
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			return Double.MAX_VALUE;
		} else {
			return getSkinnable().prefWidth(-1);
		}
	}

	@Override protected double computeMaxHeight(double width) {
		if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
			return getSkinnable().prefHeight(width);
		} else {
			return Double.MAX_VALUE;
		}
	}
}
