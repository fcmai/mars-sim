/**
 * Mars Simulation Project
 * HistoricalEventManager.java
 * @version 3.1.0 2017-09-09
 * @author Barry Evans
 */

package org.mars_sim.msp.core.events;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.narrator.Narrator;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;


/**
 * This class provides a manager that maintains a model of the events that have
 * occurred during the current simulation run. It provides support for a
 * listener pattern so the external objects can be notified when new events have
 * been registered. The manager maintains an ordered list in terms of decreasing
 * time, i.e. most recent event first. It should be noted that the throughput of
 * new events of the manager can be in the order of 100 event per simulation
 * tick.
 */
public class HistoricalEventManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/**
	 * This defines the maximum number of events that are stored. It should be a
	 * standard property.
	 */
//	private final static int TRANSIENT_EVENTS = 5000;

	private transient List<HistoricalEventListener> listeners;

	private Narrator narrator;

	private MarsClock marsClock;

	private volatile static List<HistoricalEvent> lastEvents = new ArrayList<>();

	// The following list cannot be static since it needs to be serialized
	private List<SimpleEvent> eventsRegistry;

	// The following 4 list cannot be static since they need to be serialized
	private List<String> whatList;
	private List<String> whileDoingList;
	private List<String> whoList;
	private List<String> loc0List;
	private List<String> loc1List;

	/**
	 * Create a new EventManager that represents a particular simulation.
	 */
	public HistoricalEventManager() {
		// logger.info("HistoricalEventManager's constructor is on " +
		// Thread.currentThread().getName());
		// Note : the masterClock and marsClock CANNOAT initialized until the simulation
		// start
		listeners = new ArrayList<HistoricalEventListener>();
//		events = new LinkedList<HistoricalEvent>();
		eventsRegistry = new ArrayList<>();
		narrator = new Narrator();
//		lastEvents = new ArrayList<>();
		initMaps();
	}

	private void initMaps() {
		whatList = new ArrayList<>();
		whileDoingList = new ArrayList<>();
		whoList = new ArrayList<>();
		loc0List = new ArrayList<>();
		loc1List = new ArrayList<>();

	}

	/**
	 * Add a historical event listener
	 * 
	 * @param newListener listener to add.
	 */
	// 5 models or panels called addListener()
	public void addListener(HistoricalEventListener newListener) {
		if (listeners == null)
			listeners = new ArrayList<HistoricalEventListener>();
		if (!listeners.contains(newListener))
			listeners.add(newListener);
	}

	/**
	 * Removes a historical event listener.
	 * 
	 * @param oldListener listener to remove.
	 */
	public void removeListener(HistoricalEventListener oldListener) {
		if (listeners.contains(oldListener))
			listeners.remove(oldListener);
	}

//	/**
//	 * Get the event at a specified index.
//	 * @param index Index of event to retrieve.
//	 * @return Historical event.
//	 */
//	public HistoricalEvent getEvent(int index) {		
//		return events.get(index);
//	}

	/**
	 * Get the event at a specified index.
	 * 
	 * @param index Index of event to retrieve.
	 * @return Historical event.
	 */
	public SimpleEvent getEvent(int index) {
		return eventsRegistry.get(index);
	}

	public boolean isSameEvent(HistoricalEvent newEvent) {
//		boolean result = false;
		if (lastEvents != null && !lastEvents.isEmpty()) {
			for (HistoricalEvent e : lastEvents) {

				if (e.getType() == newEvent.getType() && e.getCategory() == newEvent.getCategory()
						&& e.getWhatCause().equals(newEvent.getWhatCause())
						&& e.getWhileDoing().equals(newEvent.getWhileDoing()) 
						&& e.getWho().equals(newEvent.getWho())
						&& e.getLocation0().equals(newEvent.getLocation0())
						&& e.getLocation1().equals(newEvent.getLocation1())) {
//					result = true;
					return true;
				}

			}
		}
		return false;
	}

	/**
	 * An new event needs registering with the manager. The event will be time
	 * stamped with the current clock time and inserted at position zero.
	 * 
	 * @param newEvent The event to register.
	 */
	public synchronized void registerNewEvent(HistoricalEvent newEvent) {
//		HistoricalEventCategory category = newEvent.getCategory();
		if (newEvent.getCategory() == HistoricalEventCategory.TASK)
			return;
		else if (newEvent.getType() == EventType.MISSION_START)
			return;
		else if (newEvent.getType() == EventType.MISSION_JOINING)
			return;
		else if (newEvent.getType() == EventType.MISSION_FINISH)
			return;
		else if (newEvent.getType() == EventType.MISSION_NOT_ENOUGH_RESOURCES)
			return;
		else if (isSameEvent(newEvent))
			return;

		if (lastEvents == null)
			lastEvents = new ArrayList<>();

		lastEvents.add(newEvent);
		if (lastEvents.size() > 7)
			lastEvents.remove(0);

		// check if event is MALFUNCTION or MEDICAL, save it for notification box
		// display
		// Make space for the new event.
//			if (events.size() >= TRANSIENT_EVENTS) {
//				int excess = events.size() - (TRANSIENT_EVENTS - 1);
//				removeEvents(events.size() - excess, excess);
//			}
		// Note : the elaborate if-else conditions below is for passing the maven test
		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

		MarsClock timestamp = (MarsClock) marsClock.clone();

		if (timestamp == null)
			throw new IllegalStateException("timestamp is null");

		newEvent.setTimestamp(timestamp);

		SimpleEvent se = convert2SimpleEvent(newEvent, timestamp);

		if (listeners == null) {
			listeners = new ArrayList<HistoricalEventListener>();
		}

		Iterator<HistoricalEventListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			HistoricalEventListener l = iter.next();
			l.eventAdded(0, se, newEvent);
//			l.eventAdded(0, se);
//			l.eventAdded(0, newEvent);
		}

		narrator.translate(newEvent);
	}

	private SimpleEvent convert2SimpleEvent(HistoricalEvent event, MarsClock timestamp) {
		short missionSol = (short) (timestamp.getMissionSol());//event.getTimestamp().getMissionSol());
		float millisols = (float) (event.getTimestamp().getMillisol());
		byte cat = (byte) (event.getCategory().ordinal());
		byte type = (byte) (event.getType().ordinal());
		short what = (short) (getID(whatList, event.getWhatCause()));
		short whileDoing = (short) (getID(whileDoingList, event.getWhileDoing()));
		short who = (short) (getID(whoList, event.getWho()));
		short loc0 = (short) (getID(loc0List, event.getLocation0()));
		short loc1 = (short) (getID(loc1List, event.getLocation1()));

//		System.out.println("HistoricalEventManager's mission sol : " + missionSol);
		SimpleEvent se = new SimpleEvent(missionSol, millisols, cat, type, what, whileDoing, who, loc0, loc1);
		eventsRegistry.add(0, se);
		return se;
	}

//	public int getID(Map<Integer, String> map, String s) {
//		if (map.containsValue(s)) {
//			Set<Integer> id = map.keySet();
//			for (Integer i : id)
//				return i;
////			List<String> values = new ArrayList<>(map.values());
//			
//		} else {
//			int size = map.size();
//			map.put(size + 1, s);
//			return size + 1;
//		}
//		return -1;
//	}
	
	public int getID(List<String> list, String s) {
		if (list.contains(s)) {
			return list.indexOf(s);
		} else {
			int size = list.size();
			list.add(s);
			return size;
		}
	}
	
//	public String getStr(Map<Integer, String> map, Integer id) {
//		return map.get(id);
//	}

//	public String getStr(List<String> list, Integer id) {
//		return list.get(id);
//	}
	
	public String getWhat(int id) {
		return whatList.get(id);
	}

	public String getWhileDoing(int id) {
		return whileDoingList.get(id);
	}

	public String getWho(int id) {
		return whoList.get(id);
	}

	public String getLoc0(int id) {
		return loc0List.get(id);
	}

	public String getLoc1(int id) {
		return loc1List.get(id);
	}

//	/**
//	 * An event is removed from the list.
//	 * @param index Index of the event to be removed.
//	 * @param number Number to remove.
//	 */
//	private void removeEvents(int index, int number) {
//
//		// Remove the rows
//		for(int i = index; i < (index + number); i++) {
//			events.remove(i);
//		}
//
//		Iterator<HistoricalEventListener> iter = listeners.iterator();
//		while(iter.hasNext()) iter.next().eventsRemoved(index, index + number);
//	}

//	/**
//	 * Get the number of events in the manager.
//	 * @return Stored event count.
//	 */
//	public int size() {
//		return events.size();
//	}

//	public List<HistoricalEvent> getEvents() {
//		return events;
//	}

	public List<SimpleEvent> getEvents() {
		return eventsRegistry;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		listeners.clear();
		listeners = null;
//		events.clear();
//		events = null;
		eventsRegistry.clear();
		eventsRegistry = null;
	}
}