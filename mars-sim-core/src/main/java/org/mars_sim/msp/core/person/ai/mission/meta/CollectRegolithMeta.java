/**
 * Mars Simulation Project
 * CollectRegolithMeta.java
 * @version 3.1.0 2017-05-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A meta mission for the CollectRegolith mission.
 */
public class CollectRegolithMeta implements MetaMission {

	// private static Logger logger =
	// Logger.getLogger(CollectRegolithMeta.class.getName());

	/** Mission name */
	private static final String NAME = Msg.getString("Mission.description.collectRegolith"); //$NON-NLS-1$

	private static final int VALUE = 8000;

	/** starting sol for this mission to commence. */
	public final static int MIN_STARTING_SOL = 1;

    private static MissionManager missionManager = Simulation.instance().getMissionManager();
    
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Mission constructInstance(Person person) {
		return new CollectRegolith(person);
	}

	@Override
	public double getProbability(Person person) {

		double result = 0;

		if (person.isInSettlement()) {

			Settlement settlement = person.getSettlement();

			result = getProbability(settlement);

			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) {
				result *= job.getStartMissionProbabilityModifier(CollectRegolith.class);
				// If this town has a tourist objective, divided by bonus
				result = result / settlement.getGoodsManager().getTourismFactor();
			}

//			 logger.info("CollectRegolithMeta's probability : " +
//			 Math.round(result*100D)/100D);

			if (result < 0.5)
				return 0;
			else if (result > 1D)
				result = 1;

		}

		return result;
	}

  public double getProbability(Settlement settlement) {

        double result = 0D;
        
		int numEmbarked = VehicleMission.numEmbarkingMissions(settlement);
		
		if (missionManager == null)
			missionManager = Simulation.instance().getMissionManager();
		
		int numThisMission = missionManager.numParticularMissions(NAME, settlement);
		 
		// a settlement with <= 4 population can always do DigLocalRegolith task
		// should avoid the risk of mission.
		if (settlement.getIndoorPeopleCount() <= 1)// .getAllAssociatedPeople().size() <= 4)
			return 0;

		// Check if available rover.
		else if (!RoverMission.areVehiclesAvailable(settlement, false)) {
			return 0;
		}
		
		// Check if available backup rover.
		else if (!RoverMission.hasBackupRover(settlement)) {
			return 0;
		}

		// Check if settlement has enough basic resources for a rover mission.
		else if (!RoverMission.hasEnoughBasicResources(settlement, false)) {
			return 0;
		}

//        // Check for embarking missions.
//        else if (VehicleMission.hasEmbarkingMissions(settlement)) {
//            return 0;
//        }

		// Check if minimum number of people are available at the settlement.
		else if (!RoverMission.minAvailablePeopleAtSettlement(settlement, RoverMission.MIN_STAYING_MEMBERS)) {
			return 0;
		}

		// Check if min number of EVA suits at settlement.
		else if (Mission.getNumberAvailableEVASuitsAtSettlement(settlement) < RoverMission.MIN_GOING_MEMBERS) {
			return 0;
		}

		// Check for embarking missions.
		else if (settlement.getNumCitizens() / 4.0 < numEmbarked + numThisMission) {
			return 0;
		}
		
		else if (numThisMission > 1)
			return 0;
		
		else {
			result = settlement.getRegolithProbabilityValue() / VALUE;
		}
		
		if (result <= 0)
			return 0;

		result += CollectResourcesMission.getNewMissionProbability(settlement, Bag.class,
				CollectRegolith.REQUIRED_BAGS, CollectRegolith.MIN_PEOPLE);
		
		// Crowding modifier
		int crowding = settlement.getIndoorPeopleCount() - settlement.getPopulationCapacity();
		if (crowding > 0)
			result *= (crowding + 1);

		int f1 = numEmbarked;
		int f2 = numThisMission;
		if (numEmbarked == 0)
			f1 = 1;
		if (numThisMission == 0)
			f2 = 1;
		
		result *= settlement.getNumCitizens() / 4.0 / f1 / f2;
        
        return result;
    }
	  
	@Override
	public Mission constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param {{@link MissionManager}
	 */
	public static void setInstances(MissionManager m) {
		missionManager = m;
	}
}