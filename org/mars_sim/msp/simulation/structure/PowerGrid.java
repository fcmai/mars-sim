/**
 * Mars Simulation Project
 * PowerGrid.java
 * @version 2.75 2004-04-03
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure;
 
import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
 
/**
 * The PowerGrid class is a settlement's building power grid.
 */
public class PowerGrid implements Serializable {
        
    // Statc data members
    public static final String POWER_UP_MODE = "Power up";
    public static final String POWER_DOWN_MODE = "Power down";
        
    // Data members
    private String powerMode;
    private double powerGenerated;
    private double powerRequired;
    private boolean sufficientPower;
    private Settlement settlement;
    
    /**
     * Constructor
     */
    public PowerGrid(Settlement settlement) {
        this.settlement = settlement;
        powerMode = POWER_UP_MODE;
        powerGenerated = 0D;;
        powerRequired = 0D;
        sufficientPower = true;
    }
    
    /**
     * Gets the power grid mode.
     * @return power grid mode string.
     */
    public String getPowerMode() {
    	return powerMode;
    }
    
    /**
     * Sets the power grid mode.
     * @param newPowerMode the new power grid mode.
     */
    public void setPowerMode(String newPowerMode) {
    	if (POWER_UP_MODE.equals(newPowerMode)) powerMode = POWER_UP_MODE;
    	else if (POWER_DOWN_MODE.equals(newPowerMode)) powerMode = POWER_DOWN_MODE;
    }
    
    /**
     * Gets the generated power in the grid.
     * @return power in kW
     */
    public double getGeneratedPower() {
        return powerGenerated;
    }
    
    /**
     * Gets the power required from the grid.
     * @return power in kW
     */
    public double getRequiredPower() {
        return powerRequired;
    }
    
    /**
     * Checks if there is enough power in the grid for all 
     * buildings to be set to full power.
     * @return true if sufficient power
     */
    public boolean isSufficientPower() {
        return sufficientPower;
    }
    
    /**
     * Time passing for power grid.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) throws BuildingException {
        
        // Clear and recalculate power
        powerGenerated = 0D;
        powerRequired = 0D;
        
        BuildingManager manager = settlement.getBuildingManager();
        // System.out.println(settlement.getName() + " power situation: ");
        // Determine total power generated by buildings.
        Iterator iPow = manager.getBuildings(PowerGeneration.NAME).iterator();
        while (iPow.hasNext()) {
        	Building building = (Building) iPow.next();
            PowerGeneration gen = (PowerGeneration) building.getFunction(PowerGeneration.NAME);
            powerGenerated += gen.getGeneratedPower();
            // System.out.println(((Building) gen).getName() + " generated: " + gen.getGeneratedPower());
        }
        // System.out.println("Total power generated: " + powerGenerated);
        List buildings = manager.getBuildings();
        
        if (powerMode.equals(POWER_UP_MODE)) {
        	// Determine total power used by buildings when set to full power mode.
        	Iterator iUsed = buildings.iterator();
        	while (iUsed.hasNext()) {
            	Building building = (Building) iUsed.next();
            	building.setPowerMode(Building.FULL_POWER);
            	powerRequired += building.getFullPowerRequired();
            	// System.out.println(building.getName() + " full power used: " + building.getFullPowerRequired());
        	}
        	// System.out.println("Total full power required: " + powerRequired);
        }
        else if (powerMode.equals(POWER_DOWN_MODE)) {
			// Determine total power used by buildings when set to power down mode.
			Iterator iUsed = buildings.iterator();
			while (iUsed.hasNext()) {
				Building building = (Building) iUsed.next();
			    building.setPowerMode(Building.POWER_DOWN);
				powerRequired += building.getPoweredDownPowerRequired();
				// System.out.println(building.getName() + " power down power used: " + building.getPoweredDownPowerRequired());
			}
			// System.out.println("Total power down power required: " + powerRequired);
        }
        
        // Check if there is enough power generated to fully supply each building.
        if (powerRequired <= powerGenerated) {
            sufficientPower = true;
        }
        else {
            sufficientPower = false;
            double neededPower = powerRequired - powerGenerated;
            
            // Reduce each building's power mode to low power until 
            // required power reduction is met.
            if (powerMode.equals(POWER_DOWN_MODE)) {
            	Iterator iLowPower = buildings.iterator();
            	while (iLowPower.hasNext() && (neededPower > 0D)) {
                	Building building = (Building) iLowPower.next();
                	if (!powerSurplus(building, Building.FULL_POWER)) {
                    building.setPowerMode(Building.POWER_DOWN);
                    neededPower -= building.getFullPowerRequired() - 
                        building.getPoweredDownPowerRequired();
                	}
            	}
            }
            
            // If power needs are still not met, turn off the power to each 
            // uninhabitable building until required power reduction is met.
            if (neededPower > 0D) {
                Iterator iNoPower = buildings.iterator();
                while (iNoPower.hasNext() && (neededPower > 0D)) {
                    Building building = (Building) iNoPower.next();
                    if (!powerSurplus(building, Building.POWER_DOWN) && 
                    		!(building.hasFunction(LifeSupport.NAME))) {
                        building.setPowerMode(Building.NO_POWER);
                        neededPower -= building.getPoweredDownPowerRequired();
                    }
                }
            }
            
            // If power needs are still not met, turn off the power to each inhabitable building 
            // until required power reduction is met.
            if (neededPower > 0D) {
                Iterator iNoPower = buildings.iterator();
                while (iNoPower.hasNext() && (neededPower > 0D)) {
                    Building building = (Building) iNoPower.next();
                    if (!powerSurplus(building, Building.POWER_DOWN) && 
                        	building.hasFunction(LifeSupport.NAME)) {
                        building.setPowerMode(Building.NO_POWER);
                        neededPower -= building.getPoweredDownPowerRequired();
                    }
                }
            }
        }
    }  
    
    /**
     * Checks if building generates more power 
     * than it uses in a given power mode.
     *
     * @param building the building
     * @param mode the building's power mode to check.
     * @return true if building supplies more power than it uses.
     * throws BuildingException if error in power generation.
     */
    private boolean powerSurplus(Building building, String mode) throws BuildingException {
        double generated = 0D;
        if (building.hasFunction(PowerGeneration.NAME)) {
        	PowerGeneration powerGeneration = (PowerGeneration) building.getFunction(PowerGeneration.NAME);
        	generated = powerGeneration.getGeneratedPower(); 
        }
            
        double used = 0D;
        if (mode.equals(Building.FULL_POWER)) used = building.getFullPowerRequired();
        else if (mode.equals(Building.POWER_DOWN)) used = building.getPoweredDownPowerRequired();
        
        if (generated > used) return true;
        else return false;
    }
}