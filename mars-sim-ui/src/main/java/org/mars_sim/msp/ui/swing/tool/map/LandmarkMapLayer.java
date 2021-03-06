/**
 * Mars Simulation Project
 * LandmarkMapLayer.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Landmark;

/**
 * The LandmarkMapLayer is a graphics layer to display landmarks.
 */
public class LandmarkMapLayer
implements MapLayer {

	/** Diameter of marking circle. */
	private int CIRCLE_DIAMETER = 10; //FIXME: make proportional to actual loaded diameter.

	/** Blue color for surface map. */
	private Color SURFACE_COLOR = new Color(127, 127, 255);

	/** Gray color for surface map. */
	private Color TOPO_COLOR = new Color(50, 50, 50);

	/** Label font. */
	private Font MAP_LABEL_FONT = new Font("Serif", Font.PLAIN, 12);

	/** Horizontal offset for label. */
	private int LABEL_HORIZONTAL_OFFSET = 2;
	
	private static List<Landmark> landmarks = Simulation.instance().getMars().getSurfaceFeatures().getLandmarks();

	private double angle = CannedMarsMap.HALF_MAP_ANGLE;
	/**
	 * Displays the layer on the map image.
	 *
	 * @param mapCenter the location of the center of the map.
	 * @param mapType the type of map.
	 * @param g graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
	    Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint( RenderingHints.  KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		for (Landmark landmark : landmarks) {
			if (mapCenter.getAngle(landmark.getLandmarkLocation()) < angle)
				displayLandmark(landmark, mapCenter, mapType, g2d);
		}
	}

	/**
	 * Display a landmark on the map layer.
	 * @param landmark {@link Landmark} the landmark to be displayed.
	 * @param mapCenter {@link Coordinates} the location of the center of the map.
	 * @param mapType {@LINK String} type of map.
	 * @param g {@link Graphics} the graphics context.
	 */	
	private void displayLandmark(Landmark landmark, Coordinates mapCenter, String mapType, Graphics2D g2d) {

		// Determine display location of landmark.
		IntPoint location = MapUtils.getRectPosition(landmark.getLandmarkLocation(), mapCenter, mapType);

		// Determine circle location.
		int locX = location.getiX() - (CIRCLE_DIAMETER / 2);
		int locY = location.getiY() - (CIRCLE_DIAMETER / 2);

		// Set the color
		if (TopoMarsMap.TYPE.equals(mapType)) g2d.setColor(TOPO_COLOR);
		else g2d.setColor(SURFACE_COLOR);

		// Draw a circle at the location.
		g2d.drawOval(locX, locY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);

		// Find location to display label.
		int locLabelX = location.getiX() + (CIRCLE_DIAMETER / 2) + LABEL_HORIZONTAL_OFFSET;
		int locLabelY = location.getiY() + CIRCLE_DIAMETER;

		// Set the label font.
		g2d.setFont(MAP_LABEL_FONT);

		// Draw the landmark name.
		if (landmark != null)
			g2d.drawString(landmark.getLandmarkName(), locLabelX, locLabelY);
	}
}