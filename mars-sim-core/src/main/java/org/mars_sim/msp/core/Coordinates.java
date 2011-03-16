/**
 * Mars Simulation Project
 * Coordinates.java
 * @version 3.00 2011-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.mars.Mars;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;

/** Spherical Coordinates. Represents a location on virtual Mars in
 *  spherical coordinates. It provides some useful methods involving
 *  those coordinates, as well as some static methods for general
 *  coordinate calculations.
 *  THETA is longitute in (0-2PI) radians
 *  PHI is latitude in (-PI - PI) radians (although only 0-PI) makes any sense for the renderer
 	RHO rho diameter of planet (in km) or 2* MARS_RADIUS_KM = 3393.0
 */
public class Coordinates implements Serializable {

    // Data members
    private double phi; // Phi value of coordinates PHI is latitude in (-PI - PI) radians (although only 0-PI) seem to be legal values 
    private double theta; // Theta value of coordinates, THETA is longitute in (0-2PI) radians

    private double sinPhi; // Sine of phi (stored for efficiency)
    private double sinTheta; // Sine of theta (stored for efficiency)
    private double cosPhi; // Cosine of phi (stored for efficiency)
    private double cosTheta; // Cosine of theta (stored for efficiency)

    // Half the map circumference in pixels.
    private static final double HALF_CIRCUM_PIXELS = 1440D;
    
    // 2 x PI
    private static final double TWO_PI = 2.0D * Math.PI;

    /** Constructs a Coordinates object
     *  @param phi the phi angle of the spherical coordinate
     *  @param theta the theta angle of the spherical coordinate
     */
    public Coordinates(double phi, double theta) {

        // Set Coordinates
        this.setPhi(phi);
        this.setTheta(theta);

        // Set trigonometric functions
        setTrigFunctions();
    }

    /** Clone constructor
     *  @param originalCoordinates the Coordinates object to be cloned
     */
    public Coordinates(Coordinates originalCoordinates) {
        this(originalCoordinates.phi, originalCoordinates.theta);
    }
    
    /**
     * Constructor with a latitude and longitude string.
     * @param latitude String representing latitude value. ex. "25.344 N"
     * @param longitude String representing longitude value. ex. "63.5532 W"
     * @throws Exception if latitude or longitude strings are invalid.
     */
    public Coordinates(String latitude, String longitude) {
    	this(parseLatitude(latitude), parseLongitude(longitude));
    }

    /** Sets commonly-used trigonometric functions of coordinates */
    private void setTrigFunctions() {
        sinPhi = Math.sin(phi);
        sinTheta = Math.sin(theta);
        cosPhi = Math.cos(phi);
        cosTheta = Math.cos(theta);
    }

    /**
     * Generate a string representation of this object. It will be the same
     * format as the formattedString method.
     *
     * @return String description of Coordinate.
     * @see #getFormattedString()
     */
    public String toString() {
        return getFormattedString();
    }

    /** phi accessor
     *  @return the phi angle value of the coordinate
     */
    public double getPhi() {
        return phi;
    }

    /** phi mutator
     *  @param newPhi the new phi angle value for the coordinate
     */
    public void setPhi(double newPhi) {
       if (newPhi < 0) {phi = 0;} else if (newPhi > Math.PI) { phi = Math.PI;} else phi=newPhi;
       //phi  = newPhi;
    	setTrigFunctions();
    }

    /** theta accessor
     *  @return the theta angle value of the coordinate
     */
    public double getTheta() {
        return theta;
    }

    /** theta mutator
     *  @param newTheta the new theta angle value for the coordinate
     */
    public void setTheta(double newTheta) {
       theta = Math.abs(newTheta%TWO_PI);
       //theta = newTheta;
    	setTrigFunctions();
    }

    /** sine of phi.
     *  @return the sine of the phi angle value of the coordinate
     */
    public double getSinPhi() {
        // <tip> would it be help to use lazy evaluation of sinPhi here? </tip>
        return sinPhi;
    }

    /** sine of theta
     *  @return the sine of the theta angle value of the coordinate
     */
    public double getSinTheta() {
        return sinTheta;
    }

    /** cosine of phi
     *  @return the cosine of the phi angle value of the coordinate
     */
    public double getCosPhi() {
        return cosPhi;
    }

    /** cosine of theta
     *  @return the cosine of the theta angle value of the coordinate
     */
    public double getCosTheta() {
        return cosTheta;
    }

    /** Set coordinates
     *  @param newCoordinates Coordinates object who's location should be matched by
     *  this Coordinates object
     */
    public void setCoords(Coordinates newCoordinates) {

        // Update coordinates
        //phi = newCoordinates.getPhi();
        //theta = newCoordinates.getTheta();
    	this.setPhi(newCoordinates.phi);
    	this.setTheta(newCoordinates.theta);
        // Update trigonometric functions
        setTrigFunctions();
    }

    /** Returns true if coordinates have equal phi and theta values
     *  @param otherCoords Coordinates object to be matched against
     *  @return true if Coordinates values match, false otherwise
     */
    public boolean equals(Object otherCoords) {

        if ((otherCoords != null) && (otherCoords instanceof Coordinates)) {
        	Coordinates other = (Coordinates) otherCoords;
            if ((phi == other.phi) && (theta == other.theta))
                return true;
        }

        return false;
    }
    
    /**
     * Gets the hash code for this object.
     * @return hash code.
     */
    public int hashCode() {
    	return (int) ((phi * 1000D) + (theta * 1000D));
    }

    /** Returns the arc angle in radians between this location and the
     *  given coordinates
     *  @param otherCoords remote Coordinates object
     *  @return angle (in radians) to the remote Coordinates object
     */
    public double getAngle(Coordinates otherCoords) {

        double temp1 = cosPhi * otherCoords.cosPhi;
        double temp2 = sinPhi * otherCoords.sinPhi;
        double temp3 = Math.cos(Math.abs(theta - otherCoords.theta));
        double temp4 = temp1 + (temp2 * temp3);
        
        // Make sure temp4 is in valid -1 to 1 range.
        if (temp4 > 1D) temp4 = 1D;
        else if (temp4 < -1D) temp4 = -1D;

        return Math.acos(temp4);
    }

    /** Returns the distance in kilometers between this location and
     *  the given coordinates
     *  @param otherCoords remote Coordinates object
     *  @return distance (in km) to the remote Coordinates object
     */
    public double getDistance(Coordinates otherCoords) {

        double rho = Mars.MARS_RADIUS_KM;
        double angle = getAngle(otherCoords);

        return rho * angle;
    }

    /** 
     * Gets a common formatted string to represent this location.
     * @return formatted longitude & latitude string for this Coordinates object
     * @see #getFormattedLongitudeString()
     * @see #getFormattedLatitudeString()
     */
    public String getFormattedString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getFormattedLatitudeString());
        buffer.append(' ');
        buffer.append(getFormattedLongitudeString());
        return buffer.toString();
    }

    /** 
     * Gets a common formatted string to represent longitude for
     * this location. 
     * ex. "35.6 E"
     * @return formatted longitude string for this Coordinates object
     */
    public String getFormattedLongitudeString() {
        return getFormattedLongitudeString(theta);
    }
    
    /** 
     * Gets a common formatted string to represent longitude for
     * this location. 
     * ex. "35.6 E"
     * @param theta the radian theta value for the location.
     * @return formatted longitude string for this Coordinates object
     */
    public static String getFormattedLongitudeString(double theta) {
        double degrees;
        char direction;

        degrees = 0D;
        direction = ' ';
        
        if ((theta < Math.PI) && (theta >= 0D)) {
            degrees = Math.toDegrees(theta);
            direction = 'E';
        } else if (theta >= Math.PI) {
            degrees = Math.toDegrees(TWO_PI - theta);
            direction = 'W';
        }

        DecimalFormat formatter = new DecimalFormat("0.0");

        return formatter.format(degrees) + "\u00BA " + direction;
    }

    /** 
     * Gets a common formatted string to represent latitude for this
     * location. 
     * ex. "35.6 S"
     * @return formatted latitude string for this Coordinates object
     */
    public String getFormattedLatitudeString() {
        return getFormattedLatitudeString(phi);
    }
    
    /** 
     * Gets a common formatted string to represent latitude for this
     * location. 
     * ex. "35.6 S"
     * @param phi the radian phi value for the location.
     * @return formatted latitude string for this Coordinates object
     */
    public static String getFormattedLatitudeString(double phi) {

        double degrees;
        double piHalf = Math.PI / 2.0;
        char direction;

        degrees = 0D;
        direction = ' ';
        
        if (phi <= piHalf) {
            degrees = ((piHalf - phi) / piHalf) * 90D;
            direction = 'N';
        } else if (phi > piHalf) {
            degrees = ((phi - piHalf) / piHalf) * 90D;
            direction = 'S';
        }

        DecimalFormat formatter = new DecimalFormat("0.0");

        return formatter.format(degrees) + "\u00BA " + direction;
    }

    /** Converts spherical coordinates to rectangular coordinates.
     *  Returns integer x and y display coordinates for spherical
     *  location.
     *  @param newCoords offsetted location
     *  @param centerCoords location of the center of the map
     *  @param rho diameter of planet (in km)
     *  @param half_map half the map's width (in pixels)
     *  @param low_edge lower edge of map (in pixels)
     *  @return pixel offset value for map
     */
    static public IntPoint findRectPosition(Coordinates newCoords, Coordinates centerCoords,
            double rho, int half_map, int low_edge) {

        return centerCoords.findRectPosition(newCoords.phi, newCoords.theta, rho,
            half_map, low_edge);
    }
    
    /**
     * Converts spherical coordinates to rectangular coordinates.
     * Returns integer x and y display coordinates for spherical
     * location.
     *
     * @param newPhi the new phi coordinate
     * @param newTheta the new theta coordinate
     * @param rho diameter of planet (in km)
     * @param half_map half the map's width (in pixels)
     * @param low_edge lower edge of map (in pixels)
     * @return pixel offset value for map
     */
    public IntPoint findRectPosition(double newPhi, double newTheta, double rho, int half_map, int low_edge) {
        
//        double sin_offset = 0D - sinPhi;
//        double cos_offset = 0D - cosPhi;
//        double col_correction = (Math.PI / -2D) - getTheta();
        final double temp_col = newTheta + ((Math.PI / -2D) - theta);
        final double temp_buff_x = rho * Math.sin(newPhi);
//        double temp_buff_y1 = temp_buff_x * (0D - cosPhi);
//        double temp_buff_y2 = rho * Math.cos(newPhi) * (0D - sinPhi);
        int buff_x = ((int) Math.round(temp_buff_x * Math.cos(temp_col)) + half_map) - low_edge;
        int buff_y = ((int) Math.round(((temp_buff_x * (0D - cosPhi)) * Math.sin(temp_col)) + (rho * Math.cos(newPhi) * (0D - sinPhi))) + half_map) - low_edge;

        return new IntPoint(buff_x, buff_y);
    }

    /** Converts linear rectangular XY position change to spherical coordinates
     *  @param x change in x value (in km)
     *  @param y change in y value (in km)
     *  @return new spherical location
     */
    public Coordinates convertRectToSpherical(double x, double y) {
    	return convertRectToSpherical(x, y, HALF_CIRCUM_PIXELS / Math.PI);
   	}

    /** Converts linear rectangular XY position change to spherical coordinates
     *  with rho value for map.
     *  @param x change in x value (in km)
     *  @param y change in y value (in km)
     *  @param rho rho value of map used
     *  @return new spherical location
     */
    public Coordinates convertRectToSpherical(double x, double y, double rho) {
        Coordinates result = new Coordinates(0D, 0D);
        convertRectToSpherical(x, y, rho, result);
        return result;
    }

    /** Converts linear rectangular XY position change to spherical coordinates
     *  with rho value for map.
     *  @param x change in x value (in km)
     *  @param y change in y value (in km)
     *  @param rho rho value of map used
     *  @param newCoordinates Coordinates object to put the result in
     */
    public void convertRectToSpherical(double x, double y, double rho, Coordinates newCoordinates) {

        double z = Math.sqrt((rho * rho) - (x * x) - (y * y));

        double x2 = x;
        double y2 = (y * cosPhi) + (z * sinPhi);
        double z2 = (z * cosPhi) - (y * sinPhi);

        double x3 = (x2 * cosTheta) + (y2 * sinTheta);
        double y3 = (y2 * cosTheta) - (x2 * sinTheta);
        double z3 = z2;

        double phi_new = Math.acos(z3 / rho);
        double theta_new = Math.asin(x3 / (rho * Math.sin(phi_new)));

        if (x3 >= 0) {
            if (y3 < 0)
                theta_new = Math.PI - theta_new;
        } else {
            if (y3 < 0)
                theta_new = Math.PI - theta_new;
            else
                theta_new = TWO_PI + theta_new;
        }

        newCoordinates.setPhi(phi_new);
        newCoordinates.setTheta(theta_new);
    }

    /** Returns angle direction to another location on surface of
     *  sphere 0 degrees is north (clockwise)
     *  @param otherCoords target location
     *  @return angle direction to target (in radians)
     */
    public Direction getDirectionToPoint(Coordinates otherCoords) {

        double rho = HALF_CIRCUM_PIXELS / Math.PI;
        int half_map = 720;
        int low_edge = 0;

        IntPoint pos = findRectPosition(otherCoords, this, rho, half_map, low_edge);
        pos.setLocation(pos.getX() - half_map, pos.getY() - half_map);

        double result = 0D;

        if ((pos.getX() == 0) && (pos.getY() == 0)) {
            double tempAngle = getAngle(otherCoords);
            if (tempAngle > (Math.PI / 2D)) {
                result = 0D;
            } else {
                if (getDistance(otherCoords) <= 1D) {
                    result = 0D;
                } else {
                    if ((otherCoords.phi - phi) != 0D) {
                        result = Math.atan((otherCoords.theta - theta) / (otherCoords.phi - phi));
                    }
                }
            }
        } else {
            result = Math.atan(Math.abs(pos.getX() / pos.getY()));
        }

        if (pos.getX() < 0) {
            if (pos.getY() < 0) {
                result = TWO_PI - result;
            } else {
                result = Math.PI + result;
            }
        } else {
            if (pos.getY() < 0) {
//              result = result;
            } else {
                result = Math.PI - result;
            }
        }

        return new Direction(result);
    }

    /** Gets a new location with a given direction and distance
     *  from the current location.
     *  @param direction direction to new location
     *  @param distance distance to new location (in km)
     *  @return new location coordinates
     */
    public Coordinates getNewLocation(Direction direction, double distance) {

        double iterationDistance = 10D;
        int iterations = (int) (distance / iterationDistance);
        double remainder = 0D;
        if (distance > 10D) remainder = distance - (iterations * iterationDistance);
        else remainder = distance;

        // Get successive iteration locations.
        Coordinates startCoords = this;
        for (int x=0; x < iterations; x++) {
            double newY = -1D * direction.getCosDirection() * (iterationDistance / 7.4D);
            double newX = direction.getSinDirection() * (iterationDistance / 7.4D);
            startCoords = startCoords.convertRectToSpherical(newX, newY);
        }

        // Get final location based on remainder.
        double finalY = -1D * direction.getCosDirection() * (remainder / 7.4D);
        double finalX = direction.getSinDirection() * (remainder / 7.4D);
        Coordinates finalCoordinates = startCoords.convertRectToSpherical(finalX, finalY);

        return finalCoordinates;
    }

	/** 
	 * Parse a latitude string into a phi value.
	 * ex. "25.344 N"
	 * @param latitude as string
	 * @return phi value
	 * @throws ParseException if latitude string could not be parsed.
	 */
	public static double parseLatitude(String latitude) {
		double latValue = 0D;

		String cleanLatitude = latitude.toUpperCase().trim();
		
		if (cleanLatitude.isEmpty()) throw new IllegalStateException("Latitude is blank");
		
		try {
			String numberString = cleanLatitude.substring(0, cleanLatitude.length() - 1).trim();
			if (numberString.endsWith("\u00BA")) numberString = numberString.substring(0, numberString.length() - 1);
			latValue = Double.parseDouble(numberString);
		}
		catch(NumberFormatException e) { 
			throw new IllegalStateException("Latitude number invalid: " + latitude);
		}
		
		if ((latValue > 90D) || (latValue < 0)) throw new IllegalStateException("Latitude value out of range: " + latValue);
		
		char direction = cleanLatitude.charAt(latitude.length() - 1);
		if (direction == 'N') latValue = 90D - latValue;
		else if (direction == 'S') latValue += 90D;
		else throw new IllegalStateException("Latitude direction wrong: " + direction);

		double phi = Math.PI * ((latValue%180) / 180D);
		return phi;
	}
	
	/** 
	 * Parse a longitude string into a theta value.
	 * ex. "63.5532 W"
	 * @param longitude as string
	 * @return theta value
	 * @throws ParseException if longitude string could not be parsed.
	 */
	public static double parseLongitude(String longitude)  {
		double longValue = 0D;

		String cleanLongitude = longitude.toUpperCase().trim();
		
		if (cleanLongitude.isEmpty()) throw new IllegalStateException("Longitude is blank");
		
		try {
			String numberString = cleanLongitude.substring(0, cleanLongitude.length() - 1).trim();
			if (numberString.endsWith("\u00BA")) numberString = numberString.substring(0, numberString.length() - 1);
			longValue = Double.parseDouble(numberString);
		}
		catch(NumberFormatException e) { 
			throw new IllegalStateException("Longitude number invalid: " + longitude);
		}
		
		if ((longValue > 180D) || (longValue < 0)) throw new IllegalStateException("Longitude value out of range: " + longValue);
		
		char direction = cleanLongitude.charAt(cleanLongitude.length() - 1);
		if (direction == 'W') longValue = 360D - longValue;
		else if (direction != 'E') throw new IllegalStateException("Longitude direction wrong: " + direction);

		double theta = (2 * Math.PI) * (longValue / 360D);
		return theta;
	}	
	
	/**
	 * Gets a random latitude.
	 * @return latitude
	 */
	public static double getRandomLatitude() {
		// Random latitude should be less likely to be near the poles.
		double phi = RandomUtil.getRandomDouble(Math.PI / 2D) + RandomUtil.getRandomDouble(Math.PI / 2D);
		return phi;
	}
    
	/**
	 * Gets a random longitude.
	 * @return longitude
	 */
	public static double getRandomLongitude() {
		double theta = Math.random() * (2D * Math.PI);
		return theta;
	}
}