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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import sun.awt.ConstrainableGraphics;

class ProxyGraphics extends Graphics2D implements ConstrainableGraphics {

    private SwingView swingView;

    private Graphics2D proxy;

    ProxyGraphics(SwingView v, Graphics2D p) {
        swingView = v;
        proxy = p;
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        proxy.draw3DRect(x, y, width, height, raised);
        commit();
    }
    
    @Override
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        proxy.drawBytes(data, offset, length, x, y);
        commit();
    }
    
    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        proxy.drawChars(data, offset, length, x, y);
        commit();
    }
    
    @Override
    public Graphics create(int x, int y, int width, int height) {
        Graphics2D copy = (Graphics2D) proxy.create(x, y, width, height);
        ProxyGraphics graphics = new ProxyGraphics(swingView, copy);
        return graphics;
    }
    
    @Override
    public void drawPolygon(Polygon p) {
        proxy.drawPolygon(p);
        commit();
    }
    
    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        proxy.fill3DRect(x, y, width, height, raised);
        commit();
    }
    
    @Override
    public void drawRect(int x, int y, int width, int height) {
        proxy.drawRect(x, y, width, height);
        commit();
    }
    
    @Override
    public void fillPolygon(Polygon p) {
       proxy.fillPolygon(p);
       commit();
    }
    
    @Override
    public Rectangle getClipBounds(Rectangle r) {
        return proxy.getClipBounds(r);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public Rectangle getClipRect() {
        return proxy.getClipRect();
    }
    
    @Override
    public FontMetrics getFontMetrics() {
        return proxy.getFontMetrics();
    }
    
    @Override
    public boolean hitClip(int x, int y, int width, int height) {
        return proxy.hitClip(x, y, width, height);
    }

    @Override
    public void draw(Shape s) {
        proxy.draw(s);
        commit();
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        boolean b = proxy.drawImage(img, xform, obs);
        commit();
        return b;
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        proxy.drawImage(img, op, x, y);
        commit();
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        proxy.drawRenderedImage(img, xform);
        commit();
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        proxy.drawRenderableImage(img, xform);
        commit();
    }

    @Override
    public void drawString(String str, int x, int y) {
        proxy.drawString(str, x, y);
        commit();
    }

    @Override
    public void drawString(String str, float x, float y) {
        proxy.drawString(str, x, y);
        commit();
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        proxy.drawString(iterator, x, y);
        commit();
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x,
            float y) {
        proxy.drawString(iterator, x, y);
        commit();
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        proxy.drawGlyphVector(g, x, y);
        commit();
    }

    @Override
    public void fill(Shape s) {
        proxy.fill(s);
        commit();
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return proxy.hit(rect, s, onStroke);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return proxy.getDeviceConfiguration();
    }

    @Override
    public void setComposite(Composite comp) {
        proxy.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
        proxy.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke s) {
        proxy.setStroke(s);
    }

    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {
        proxy.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(Key hintKey) {
        return proxy.getRenderingHint(hintKey);
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        proxy.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        proxy.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return proxy.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        proxy.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        proxy.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        proxy.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        proxy.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        proxy.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        proxy.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        proxy.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        proxy.setTransform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return proxy.getTransform();
    }

    @Override
    public Paint getPaint() {
        return proxy.getPaint();
    }

    @Override
    public Composite getComposite() {
        return proxy.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        proxy.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return proxy.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return proxy.getStroke();
    }

    @Override
    public void clip(Shape s) {
        proxy.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return proxy.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        Graphics2D copy = (Graphics2D) proxy.create();
        ProxyGraphics graphics = new ProxyGraphics(swingView, copy);
        return graphics;
    }

    @Override
    public Color getColor() {
        return proxy.getColor();
    }

    @Override
    public void setColor(Color c) {
        proxy.setColor(c);
    }

    @Override
    public void setPaintMode() {
        proxy.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        proxy.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return proxy.getFont();
    }

    @Override
    public void setFont(Font font) {
        proxy.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return proxy.getFontMetrics(f);
    }

    @Override
    public Rectangle getClipBounds() {
        return proxy.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        proxy.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        proxy.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return proxy.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        proxy.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        proxy.copyArea(x, y, width, height, dx, dy);
        commit();
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        proxy.drawLine(x1, y1, x2, y2);
        commit();
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        proxy.fillRect(x, y, width, height);
        commit();
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        proxy.clearRect(x, y, width, height);
        commit();
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height,
            int arcWidth, int arcHeight) {
        proxy.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        commit();
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height,
            int arcWidth, int arcHeight) {
        proxy.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        commit();
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        proxy.drawOval(x, y, width, height);
        commit();
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        proxy.fillOval(x, y, width, height);
        commit();
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle,
            int arcAngle) {
        proxy.drawArc(x, y, width, height, startAngle, arcAngle);
        commit();
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle,
            int arcAngle) {
        proxy.fillArc(x, y, width, height, startAngle, arcAngle);
        commit();
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        proxy.drawPolyline(xPoints, yPoints, nPoints);
        commit();
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        proxy.drawPolygon(xPoints, yPoints, nPoints);
        commit();
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        proxy.fillPolygon(xPoints, yPoints, nPoints);
        commit();
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        boolean b = proxy.drawImage(img, x, y, observer);
        commit();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
            ImageObserver observer) {
        boolean b = proxy.drawImage(img, x, y, width, height, observer);
        commit();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor,
            ImageObserver observer) {
        boolean b = proxy.drawImage(img, x, y, bgcolor, observer);
        commit();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height,
            Color bgcolor, ImageObserver observer) {
        boolean b = proxy.drawImage(img, x, y, width, height, bgcolor, observer);
        commit();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        boolean b = proxy.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        commit();
        return b;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2, Color bgcolor,
            ImageObserver observer) {
        boolean b = proxy.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        commit();
        return b;
    }

    @Override
    public void dispose() {
        proxy.dispose();
        commit();
    }

    private void commit() {
        swingView.commit();
    }

    @Override
    public void constrain(int x, int y, int w, int h) {
        if (proxy instanceof ConstrainableGraphics) {
            ((ConstrainableGraphics) proxy).constrain(x, y, w, h);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
