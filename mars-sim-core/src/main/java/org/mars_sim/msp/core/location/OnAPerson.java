/**
 * Mars Simulation Project
 * OnAPerson.java
* @version 3.1.0 2017-10-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.location;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;

public class OnAPerson implements LocationState, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String name = "Inside a building";
	private Unit unit;

	public String getName() {
		return name;
	}

	public LocationStateType getType() {
		return LocationStateType.ON_A_PERSON;
	}
	
	public OnAPerson(Unit unit) {
		this.unit = unit;
	}
/*
	public void leaveBuilding() {
		unit.setLocationState(unit.getSettlementVicinity());
	}

	public void enterBuilding() {
		unit.setLocationState(unit.getInsideBuilding());
	}

	public void departFromVicinity() {
		unit.setLocationState(unit.getOutsideOnMars());
	}

	public void returnToVicinity() {
		unit.setLocationState(unit.getSettlementVicinity());
	}


	public void embarkVehicleInVicinity() {
		//unit.setLocationState(unit.getInsideVehicle());
	}

	public void disembarkVehicleInVicinity() {
		unit.setLocationState(unit.getSettlementVicinity());
	}

	public void embarkVehicleInGarage() {
		//unit.setLocationState(unit.getInsideVehicle());
	}

	public void disembarkVehicleInGarage() {
		unit.setLocationState(unit.getInsideBuilding());
	}



	public void transferFromSettlementToPerson() {
		unit.setLocationState(unit.getOnAPerson());
	}

	public void transferFromPersonToSettlement() {
		unit.setLocationState(unit.getInsideBuilding());
	}

	public void transferFromPersonToVehicle() {
		//unit.setLocationState(unit.getInsideVehicle());
	}

	public void transferFromVehicleToPerson() {
		unit.setLocationState(unit.getOnAPerson());
	}
*/	
}