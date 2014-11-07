/**
 * Mars Simulation Project
 * MakingSoy.java
 * @version 3.07 2014-11-06
 * @author Manny Kung				
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The MakingSoy class is a building function for making soymilk.
 */
public class MakingSoy
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(MakingSoy.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.MAKINGSOY;

    /** The base amount of work time (cooking skill 0) to produce fresh soymilk. */
    public static final double FRESHSOYMILK_WORK_REQUIRED = 40D;
   
    // The number of sols the soymilk can be preserved
    public static final double SOLS_SOYMILK_PRESERVED = 5D;
    
    // Assuming 200 g of soybeans mixed with 4 liter/kg of water
    public static final double RATIO_WATER_TO_SOYBEAN = 20D;
    // Assuming 1 kg of soybean can make 3 kg of Tofu
    public static final double RATIO_TOFU_TO_SOYBEAN = 3D;
    
    // Assuming 1 serving/cup of soymilk is 500mL 
    // it uses 25 kg of soybean
    public static final double KG_PER_SERVING_SOYMILK = .025D;
    
    // Keep at least 2 kg of soybeans in storage
    public static final double MINIMUM_SOYBEAN_TO_KEEP = 2D;
    // Keep at least 2 kg of water in storage
    public static final double MINIMUM_WATER_TO_KEEP = 50D;

    // Data members
    private int cookCapacity;
    private List<FreshSoymilk> freshSoymilkList;
    private double makingSoyWorkTime;

    private Building building;
    
    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public MakingSoy(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        this.building = building;
        
        logger.info("just called MakingSoy's constructor");
        
        makingSoyWorkTime = 0D;
        freshSoymilkList = new ArrayList<FreshSoymilk>();

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = config.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getCookingActivitySpots(building.getBuildingType()));
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // TODO: calibrate this demand
    	// Demand is 1 makingSoy capacity for every five inhabitants.
        double demand = settlement.getAllAssociatedPeople().size() / 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                MakingSoy makingSoyFunction = (MakingSoy) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .25D + .25D;
                supply += makingSoyFunction.cookCapacity * wearModifier;
            }
        }

        double makingSoyCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double makingSoyCapacity = config.getCookCapacity(buildingName);

        return makingSoyCapacity * makingSoyCapacityValue;
    }

    /**
     * Get the maximum number of cooks supported by this facility.
     * @return max number of cooks
     */
    public int getCookCapacity() {
        return cookCapacity;
    }

    /**
     * Get the current number of cooks using this facility.
     * @return number of cooks
     */
    public int getNumCooks() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) result++;
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Gets the skill level of the best cook using this facility.
     * @return skill level.
     */
    public int getBestSoySkill() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Person person = i.next();
                    Task task = person.getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) {
                        int makingSoySkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
                        if (makingSoySkill > result) result = makingSoySkill;
                    }
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Checks if there are any FreshSoymilkList in this facility.
     * @return true if yes
     */
    public boolean hasFreshSoymilk() {
        return (freshSoymilkList.size() > 0);
    }

    /**
     * Gets the number of cups of fresh soymilk in this facility.
     * @return number of freshSoymilkList
     */
    public int getNumServingsFreshSoymilk() {
        return freshSoymilkList.size();
    }

    /**
     * Gets freshSoymilk from this facility.
     * @return freshSoymilk
     */
    public FreshSoymilk getFreshSoymilk() {
        FreshSoymilk bestSoymilk = null;
        int bestQuality = -1;
        Iterator<FreshSoymilk> i = freshSoymilkList.iterator();
        while (i.hasNext()) {
            FreshSoymilk freshSoymilk = i.next();
            if (freshSoymilk.getQuality() > bestQuality) {
                bestQuality = freshSoymilk.getQuality();
                bestSoymilk = freshSoymilk;
            }
        }

        if (bestSoymilk != null) {
        	freshSoymilkList.remove(bestSoymilk);
        	// remove soymilk from amount resource 
        	removeSoymilkFromAmountResource();
         }
        return bestSoymilk;
    }
    
    /**
     * Remove soymilk from its AmountResource container
     * @return none
     */
    // 2014-11-06 Added removeSoymilkFromAmountResource()
    public void removeSoymilkFromAmountResource() {
       	double soybeansConsumed = KG_PER_SERVING_SOYMILK; 
        double waterConsumed = soybeansConsumed * RATIO_WATER_TO_SOYBEAN;
        AmountResource soymilkAR = AmountResource.findAmountResource("soymilk");
        getBuilding().getInventory().retrieveAmountResource(soymilkAR, waterConsumed);
    }
    
    /**
     * Gets the quality of the best quality freshSoymilk at the facility.
     * @return quality
     */
    public int getBestSoymilkQuality() {
        int bestQuality = 0;
        Iterator<FreshSoymilk> i = freshSoymilkList.iterator();
        while (i.hasNext()) {
            FreshSoymilk freshSoymilk = i.next();
            if (freshSoymilk.getQuality() > bestQuality) bestQuality = freshSoymilk.getQuality();
        }

        return bestQuality;
    }

    /**
     * Cleanup kitchen after mealtime.
     */
    public void cleanup() {
        makingSoyWorkTime = 0D;
    }

    /**
     * Adds makingSoy work to this facility. 
     * The amount of work is dependent upon the person's makingSoy skill.
     * @param workTime work time (millisols)
      */
    public void addWork(double workTime) {
    	makingSoyWorkTime += workTime; 
    	boolean enoughTime = false;
    	if (makingSoyWorkTime >= FRESHSOYMILK_WORK_REQUIRED) 
    		enoughTime = true;
    	else 
    		enoughTime = false;
        logger.info("addWork() : makingSoyWorkTime is " + makingSoyWorkTime);
        logger.info("addWork() : workTime is " + workTime);
    	// TODO: check if this is proportional to the population
        double size = building.getBuildingManager().getSettlement().getAllAssociatedPeople().size();
        double maxCup = size * 4;
    	//double soybeansConsumed;
        
    	// check if there are new harvest, if it does, set soyIsAvailable to true
    	double soybeansAvailable = checkAmountOfSoybeans();
    	//double waterConsumed = soybeansConsumed * WATER_CONTENT_FACTOR;
    	double waterAvailable = checkAmountOfWater();
    	
        logger.info("addWork() : " + freshSoymilkList.size() + " servings of fresh soymilk available");
    	
        boolean soyIsAvailable = false;
    	// TODO: check the population size. 
    	// should NOT make more than pop size * 3 
     	if (soybeansAvailable >= MINIMUM_SOYBEAN_TO_KEEP 
     			&& waterAvailable > MINIMUM_WATER_TO_KEEP
     			&& freshSoymilkList.size() < maxCup ) { 
     		soyIsAvailable = true;
     	}
     	
     	if (enoughTime && soyIsAvailable)
            makingSoyChoice();
        
     	logger.info("end of addWork()");
     } // end of void addWork()
    
    public double checkAmountOfSoybeans() {
        AmountResource soybeans = AmountResource.findAmountResource("Soybeans");
        double soybeansAvailable = getBuilding().getInventory().getAmountResourceStored(soybeans, false);
        //logger.info("checkAmountOfSoybeans() : soybeansAvailable is " + soybeansAvailable);
        return soybeansAvailable ;
    }
    
    public double checkAmountOfWater() {
        AmountResource water = AmountResource.findAmountResource("water");
        double waterAvailable = getBuilding().getInventory().getAmountResourceStored(water, false);
        return waterAvailable ;
    }
 
    /**
     * Makes Soymilk and Tofu  
     * @param none
     */
    public void makingSoyChoice() {
    	
        int soymilkQuality = getBestSoySkill();
        // TODO: how to use time ?
        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();

        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        // TODO: 2014-11-06 need to calibrate soybeansConsumed
        //double soybeansConsumed = config.getFoodConsumptionRate() * (1D / 40D);
        double soybeansConsumed = KG_PER_SERVING_SOYMILK; 
        double waterConsumed = soybeansConsumed * RATIO_WATER_TO_SOYBEAN;
        logger.info("makingSoyChoice() : " + soybeansConsumed + " kg of soybeans will be consumed");
        logger.info("makingSoyChoice() : " + waterConsumed + " kg of water will be consumed");
         
        AmountResource waterAR = AmountResource.findAmountResource("water");
        double waterAvailable = getBuilding().getInventory().getAmountResourceStored(waterAR, false);
        AmountResource soybeansAR = AmountResource.findAmountResource("Soybeans");
        double soybeansAvailable = getBuilding().getInventory().getAmountResourceStored(soybeansAR, false);
        AmountResource legumesAR = AmountResource.findAmountResource("Legume Group");
        double legumesAvailable = getBuilding().getInventory().getAmountResourceStored(legumesAR, false);
        
        if ((soybeansAvailable > soybeansConsumed) 
        		&& (waterAvailable > waterConsumed)
        		&& (legumesAvailable > soybeansConsumed)) { 
   
	        logger.info("makingSoyChoice() : amount of soybeans available is " + soybeansAvailable + " kg");
	
	        getBuilding().getInventory().retrieveAmountResource(soybeansAR, soybeansConsumed);
	        getBuilding().getInventory().retrieveAmountResource(legumesAR, soybeansConsumed);
	        getBuilding().getInventory().retrieveAmountResource(waterAR, waterConsumed);
	    	        
	        //	System.out.println("makingSoy.java : addWork() : makingSoy vegetables using "  
	      	//		+ foodAmount + ", vegetables remaining is " + (foodAvailable-foodAmount) );
	        freshSoymilkList.add(new FreshSoymilk(soymilkQuality, time));
	        logger.info("makingSoyChoice() : 1 serving of fresh soymilk was just made");

    		Inventory inv = getBuilding().getInventory();
            AmountResource soymilkAR = AmountResource.findAmountResource("Soymilk");      
            AmountResource tofuAR = AmountResource.findAmountResource("Tofu");
            
            double soymilkinKg = soybeansConsumed * RATIO_WATER_TO_SOYBEAN ; 
            double tofuinKg = soybeansConsumed * RATIO_TOFU_TO_SOYBEAN;
            		
	       	inv.storeAmountResource(soymilkAR, soymilkinKg, true);
	       	inv.storeAmountResource(tofuAR, tofuinKg, true);
	        logger.info("makingSoyChoice() : total " + freshSoymilkList.size() + " servings of fresh soymilk is available now");
	        logger.info("makingSoyChoice() : " + soymilkinKg + " liter(s) soymilk is just made");

	        if (logger.isLoggable(Level.FINEST)) {
	        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
	        			" has prepared " + freshSoymilkList.size() + " servings of tasty soymilk (quality is " + soymilkQuality + ")");
	        }
        } // end of if ((soybeansAvailable > soybeansConsumed)...
        else { 
        	//soyIsAvailable = false; // used in addWork()
         	   logger.info("makingSoyChoice() : no more soybeans or water available for making fresh soymilk!");        	
        }
    }

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     * 2014-10-08: mkung - Packed expired freshSoymilk into food (turned 1 freshSoymilk unit into 1 food unit)
     */
    public void timePassing(double time) {
        // Toss away expired freshSoymilkList
        Iterator<FreshSoymilk> i = freshSoymilkList.iterator();
        while (i.hasNext()) {
            FreshSoymilk freshSoymilk = i.next();
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            if (MarsClock.getTimeDiff(freshSoymilk.getExpirationTime(), currentTime) < 0D) {
            	
            	i.remove();
               	// 2014-11-06 soymilk cannot be preserved after a certain number of sols           	 
            	removeSoymilkFromAmountResource();
            	
                if(logger.isLoggable(Level.FINEST)) {
                     logger.finest("Fresh soymilk has lost its freshness at " + 
                     getBuilding().getBuildingManager().getSettlement().getName());
                }
            }
        }
    }

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return getNumCooks() * 10D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {
        return 0;
    }

    @Override
    public double getMaintenanceTime() {
        return cookCapacity * 10D;
    }

    @Override
    public void destroy() {
        super.destroy();

        freshSoymilkList.clear();
        freshSoymilkList = null;
    }

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}