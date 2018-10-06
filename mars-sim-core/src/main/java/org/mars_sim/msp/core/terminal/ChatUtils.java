/**
 * Mars Simulation Project
 * ChatUtils.java
 * @version 3.1.0 2018-09-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.terminal;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.health.Complaint;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.person.health.HealthProblem;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;


public class ChatUtils {

	public final static String SYSTEM = "System";
	public final static String SYSTEM_PROMPT = "System : ";
	public final static String YOU_PROMPT = "You : ";
	public final static String REQUEST_HEIGHT_CHANGE = YOU_PROMPT + "I'd like to change the chat box height to ";
	public final static String REQUEST_HELP = YOU_PROMPT
			+ "I need some help! What are the available commands ?";

	public final static String REQUEST_KEYS = YOU_PROMPT
			+ "I need a list of the keywords. Would you tell me what they are ?";
	
	public final static String HELP_TEXT =  System.lineSeparator() 
			+ "    -------------------- H E L P -------------------- " + System.lineSeparator()
			+ "(1) Type in the NAME of a person, a bot, or a settlement to connect with." + System.lineSeparator()
			+ "(2) Use KEYWORDS or type in a number between 1 and 15 (specific QUESTIONS on a person/bot/vehicle/settlement)."  + System.lineSeparator()		
			+ "(3) Type '/k' or 'key' to see a list of KEYWORDS." + System.lineSeparator() 
			+ "(4) Type 'settlement' to obtain the NAMES of the established settlements." + System.lineSeparator()
			+ "(5) Type 'bye', '/b', 'exit', 'x', 'quit', '/q' to close the chat box." + System.lineSeparator()
			+ "(6) Type '?', 'help', '/?', '/h' for this help page." + System.lineSeparator();

	public final static String HELP_HEIGHT = "(7) Type 'y_' to change the chat box height; '/y1'-> 256 pixels (default) '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels"
			+ System.lineSeparator();

	public final static String KEYWORDS_TEXT = System.lineSeparator() 
			+ "    -------------------- K E Y W O R D S -------------------- " + System.lineSeparator()
			+ "(1) 'where', 'location', 'located', 'task', 'activity', 'action', 'mission', " + System.lineSeparator()
			+ "(2) 'bed', 'quarters', 'building', 'inside', 'outside'," + System.lineSeparator()
			+ "(3) 'settlement', 'settlements', 'associated settlement', 'association', 'home', 'home town',"
			+ System.lineSeparator() // 'buried settlement', 'buried'
			+ "(4) 'vehicle inside', 'vehicle outside', 'vehicle park', 'vehicle settlement'," + System.lineSeparator()
			+ "(5) 'vehicle container', 'vehicle top container'" + System.lineSeparator() 
			+ "    -------------------- N U M E R A L -------------------- " + System.lineSeparator() 
			+ "(6) 1 to 15 are specific QUESTIONS on a person/bot/vehicle/settlement" + System.lineSeparator() 
			+ "    --------------------  M I S C S -------------------- " + System.lineSeparator()
			+ "(7) 'bye', '/b', 'exit', 'x', 'quit', '/q' to close the chat box" + System.lineSeparator()
			+ "(8) 'help', '/h' for the help page" + System.lineSeparator();

	public final static String KEYWORDS_HEIGHT = "(9) '/y1' to reset height to 256 pixels (by default) after closing chat box. '/y2'->512 pixels, '/y3'->768 pixels, '/y4'->1024 pixels"
	+ System.lineSeparator();

	public static String helpText;
		
	public static String keywordText;
			
	/**
	 * The mode of connection. -1 if none, 0 if headless, 1 if gui
	 */
	private static int connectionMode = -1;
	
	public static Person personCache;
	public static Robot robotCache;
	public static Settlement settlementCache;
	public static Unit unitCache;
	public static Vehicle vehicleCache;

//	public static Settlement settlement;
//	public static Building building;
//	public static Equipment equipment;

	public ChatUtils() {
	}
	
	public static String[] clarify(String prompt) {
		String questionText = YOU_PROMPT + "You were mumbling something about....";
		String responseText = null;
		int rand0 = RandomUtil.getRandomInt(4);
		if (rand0 == 0)
			responseText = prompt + " : Could you repeat that?   [/h for help]";
		else if (rand0 == 1)
			responseText = prompt + " : Pardon me?   [/h for help]";
		else if (rand0 == 2)
			responseText = prompt + " : What did you say?   [/h for help]";
		else if (rand0 == 3)
			responseText = prompt + " : I beg your pardon?   [/h for help]";
		else
			responseText = prompt + " : Can you be more specific?   [/h for help]";

		return new String[] { questionText, responseText };
	}

	public static String[] farewell(String respondent) {
		String questionText = YOU_PROMPT + farewellText();// + System.lineSeparator();
		String responseText = respondent + " : " + farewellText();// + System.lineSeparator();
		return new String[] { questionText, responseText };
	}

	public static String farewellText() {

		int r0 = RandomUtil.getRandomInt(6);
		if (r0 == 0)
			return "Bye!";
		else if (r0 == 1)
			return "Farewell!";
		else if (r0 == 2)
			return "Next time!";
		else if (r0 == 3)
			return "Have a nice sol!";
		else if (r0 == 4)
			return "Take it easy!";
		else if (r0 == 5)
			return "Take care!";
		else
			return "See ya!";
	}

	/*
	 * Checks if the user wants to quit chatting
	 */
	public static boolean isQuitting(String text) {
		if (text.equalsIgnoreCase("quit") || text.equalsIgnoreCase("/quit") 
				|| text.equalsIgnoreCase("/q")
//				|| text.equalsIgnoreCase("\\quit")
//				|| text.equalsIgnoreCase("\\q") 

				|| text.equalsIgnoreCase("exit") || text.equalsIgnoreCase("/exit") 
				|| text.equalsIgnoreCase("/x")
//				|| text.equalsIgnoreCase("\\exit")
//				|| text.equalsIgnoreCase("\\x") 

				|| text.equalsIgnoreCase("bye") || text.equalsIgnoreCase("/bye") 
//				|| text.equalsIgnoreCase("\\bye")
				|| text.equalsIgnoreCase("/b")
				) {
			return true;
			
		}
		
		else 
			return false;
	}
	
	// public String checkGreetings(String text) {
	// String result = null;
	// return result;

	/** 
	 * Check if the input string is integer
	 * @param s
	 * @param radix
	 * @return true if the input is an integer
	 */
	public static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	
	/**
	 * Processes a question and return an answer regarding an unit
	 * 
	 * @param text
	 * @return an array of String
	 */
	public static String[] askQuestion(String text) {
//		System.out.println("askQuestion() in ChatUtils");

		String questionText = "";
		StringBuilder responseText = new StringBuilder();
		String name = SYSTEM;
		
		Unit u = null;
		
		if (personCache != null) {
			u = personCache;
			name = personCache.getName();
		} 
		
		else if (robotCache != null) {
			u = robotCache;
			name = robotCache.getName();
		} 
		
		else if (settlementCache != null) {
			u = settlementCache;
			name = settlementCache.getName();
		} 
		
		else if (vehicleCache != null) {
			u = vehicleCache;
			name = vehicleCache.getName();
		}
		
//		System.out.println("name is " + name);
		
		// Case 0 : exit the conversation
		if (isQuitting(text)) {		
			String[] bye = null; 
	
			if (u != null) {
				bye = farewell(name);
				questionText = bye[0];
				responseText.append(bye[1]);
				responseText.append(System.lineSeparator());		
				responseText.append(name);
				
				int rand1 = RandomUtil.getRandomInt(1);

				if (rand1 == 0)
					responseText.append(" has left the conversation.");
				else
					responseText.append(" is disconnected.");
				
				// set personCache and robotCache to null so as to quit the conversation 
				personCache = null;
				robotCache = null;
				settlementCache = null;
				vehicleCache = null;
			}
			
			else {
				bye = farewell(name);
				questionText = bye[0];
				responseText.append(bye[1]);
				responseText.append(System.lineSeparator());
			}

		}

		// Add proposals
//		else if (text.equalsIgnoreCase("/p")) {
//			System.out.println("/p is submitted");
//			questionText = "Below is a list of proposals for your review :";
//			responseText.append(SYSTEM_PROMPT);
//			responseText.append("1. Safety and Health Measures");
//			responseText.append("2. Manufacturing Priority");
//			responseText.append("3. Food Allocation Plan");
//		}
		
		// Add changing the height of the chat box
		// DELETED

		// Case 1: ask about a particular settlement
		else if (settlementCache != null) {

			if (isInteger(text, 10)) {

				int num = Integer.parseUnsignedInt(text, 10);

				if (num == 1) {
					questionText = YOU_PROMPT + "how many beds are there in total ? ";
					responseText.append("The total number of beds is ");
					responseText.append(settlementCache.getPopulationCapacity());

				}

				else if (num == 2) {
					questionText = YOU_PROMPT + "how many beds that have already been designated to a person ? ";
					responseText.append("There are ");
					responseText.append(settlementCache.getTotalNumDesignatedBeds());
					responseText.append(" designated beds. ");

				}

				else if (num == 3) {
					questionText = YOU_PROMPT + "how many beds that are currently NOT occupied ? ";
					responseText.append("There are ");
					responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
					responseText.append(" unoccupied beds. ");

				}

				else if (num == 4) {
					questionText = YOU_PROMPT + "how many beds are currently occupied ? ";
					responseText.append("There are ");
					responseText.append(settlementCache.getSleepers());
					responseText.append(" occupied beds with people sleeping on it at this moment. ");

				}

				else {
					questionText = YOU_PROMPT + "You entered '" + num + "'.";
					responseText.append("Sorry. This number is not assigned to a valid question.");
				}

				// if it's not a integer input
			}

			else if (text.contains("bed") || text.contains("sleep") || text.equalsIgnoreCase("lodging")
					|| text.contains("quarters")) {

				questionText = YOU_PROMPT + "how well are the beds utilized ? ";
				responseText.append("Total number of beds : ");
				responseText.append(settlementCache.getPopulationCapacity());
				responseText.append(System.lineSeparator());
				responseText.append("Desginated beds : ");
				responseText.append(settlementCache.getTotalNumDesignatedBeds());
				responseText.append(System.lineSeparator());
				responseText.append("Unoccupied beds : ");
				responseText.append(settlementCache.getPopulationCapacity() - settlementCache.getSleepers());
				responseText.append(System.lineSeparator());
				responseText.append("Occupied beds : ");
				responseText.append(settlementCache.getSleepers());
				responseText.append(System.lineSeparator());
			}

			else if (text.equalsIgnoreCase("vehicle") || text.equalsIgnoreCase("rover") || text.contains("rover")
					|| text.contains("vehicle")) {

				questionText = YOU_PROMPT + "What are the vehicles in the settlement ? ";
				responseText.append(System.lineSeparator());
				responseText.append("     ----- Rovers/Vehicles Inventory -----");
				responseText.append(System.lineSeparator());
				responseText.append("(1). Total # : ");
				responseText.append(settlementCache.getAllAssociatedVehicles().size());
				responseText.append(System.lineSeparator());
				responseText.append("(2). Total # on Mission : ");
				responseText.append(settlementCache.getMissionVehicles().size());
				responseText.append(System.lineSeparator());
				responseText.append("(3). Total # Parked (NOT on Mission) : ");
				responseText.append(settlementCache.getParkedVehicleNum());
				responseText.append(System.lineSeparator());
				responseText.append("(4). # Cargo Rovers on Mission : ");
				responseText.append(settlementCache.getCargoRovers(2).size());
				responseText.append(System.lineSeparator());
				responseText.append("(5). # Transport Rovers on Mission : ");
				responseText.append(settlementCache.getTransportRovers(2).size());
				responseText.append(System.lineSeparator());
				responseText.append("(6). # Explorer Rovers on Mission : ");
				responseText.append(settlementCache.getExplorerRovers(2).size());
				responseText.append(System.lineSeparator());
				responseText.append("(7). # Light Utility Vehicles (LUVs) on Mission : ");
				responseText.append(settlementCache.getLUVs(2).size());
				responseText.append(System.lineSeparator());
				responseText.append("(8). # Parked Cargo Rovers : ");
				responseText.append(settlementCache.getCargoRovers(1).size());
				responseText.append(System.lineSeparator());
				responseText.append("(9). # Parked Transport Rovers : ");
				responseText.append(settlementCache.getTransportRovers(1).size());
				responseText.append(System.lineSeparator());
				responseText.append("(10). # Parked Explorer Rovers : ");
				responseText.append(settlementCache.getExplorerRovers(1).size());
				responseText.append(System.lineSeparator());
				responseText.append("(11). # Parked Light Utility Vehicles (LUVs) : ");
				responseText.append(settlementCache.getLUVs(1).size());
				responseText.append(System.lineSeparator());
			}

			else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("/k")) {

//				help = true;
				questionText = REQUEST_KEYS;
				if (connectionMode == 0) {
					keywordText = KEYWORDS_TEXT;
				}
				else {
					keywordText = KEYWORDS_TEXT + KEYWORDS_HEIGHT;
				}
				//responseText.append(System.lineSeparator());
				responseText.append(keywordText);

			}

			else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") 
					|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")) {

//				help = true;
				questionText = REQUEST_HELP;
				if (connectionMode == 0) {
					helpText = HELP_TEXT;
				}
				else {
					helpText = HELP_TEXT + HELP_HEIGHT;
				}
				//responseText.append(System.lineSeparator());
				responseText.append(helpText);

			}
			
			else {
				
//				help = true;
				String[] txt = clarify(name);
				questionText = txt[0];
				responseText.append(txt[1]);
			}

		}
		// Case 2: ask to talk to a person or robot
		else if (settlementCache == null) {
			// Note : this is better than personCache != null || robotCache != null since it can
											// incorporate help and other commands

			int num = 0;

			if (isInteger(text, 10))
				num = Integer.parseUnsignedInt(text, 10);

			// Add command "die"
			if (text.equalsIgnoreCase("die")) {

				if (personCache != null) {
					questionText = YOU_PROMPT + "I hereby pronounce you dead.";

					String lastWord = null;

					int rand = RandomUtil.getRandomInt(12);
					// Quotes from http://www.phrases.org.uk/quotes/last-words/suicide-notes.html
					// https://www.goodreads.com/quotes/tag/suicide-note
					if (rand == 0)
						lastWord = "This is all too heartbreaking for me. Farewell, my friend.";
					else if (rand == 1)
						lastWord = "Things just seem to have gone too wrong too many times...";
					else if (rand == 2)
						lastWord = "So I leave this world, where the heart must either break or turn to lead.";
					else if (rand == 3)
						lastWord = "Let's have no sadness——furrowed brow. There's nothing new in dying now. Though living is no newer.";
					else if (rand == 4)
						lastWord = "I myself——in order to escape the disgrace of deposition or capitulation——choose death.";
					else if (rand == 5)
						lastWord = "When all usefulness is over, when one is assured of an unavoidable and imminent death, "
								+ "it is the simplest of human rights to choose a quick and easy death in place of a slow and horrible one. ";
					else if (rand == 6)
						lastWord = "I am going to put myself to sleep now for a bit longer than usual. Call it Eternity.";
					else if (rand == 7)
						lastWord = "All fled——all done, so lift me on the pyre; the feast is over, and the lamps expire.";
					else if (rand == 8)
						lastWord = "No more pain. Wake no more. Nobody owns.";
					else if (rand == 9)
						lastWord = "Dear World, I am leaving because I feel I have lived long enough. I am leaving you with your worries in this sweet cesspool. Good luck.";
					else if (rand == 10)
						lastWord = "This is what I want so don't be sad.";
					else if (rand == 11)
						lastWord = "I don't want to hurt you or anybody so please forget about me. Just try. Find yourself a better friend.";
					else
						lastWord = "They tried to get me——I got them first!";

					responseText.append(lastWord);

					personCache.setLastWord(lastWord);

					personCache.getPhysicalCondition()
							.setDead(new HealthProblem(new Complaint(ComplaintType.SUICIDE), personCache), true);

					personCache = null;
					robotCache = null;
					settlementCache = null;
					vehicleCache = null;
				}
			}
			
			else {

				responseText.append(name);
				responseText.append(": ");
				
				 if (num == 1 || text.toLowerCase().contains("where")) {
					questionText = YOU_PROMPT + "Where are you ?"; // what is your Location Situation [Expert Mode only] ?";
					responseText.append("I'm located at ");
					if (personCache != null) {
						responseText.append(Conversion.capitalize(personCache.getLocationTag().getQuickLocation()));//getLocationSituation().getName()));
					} else if (robotCache != null) {
						responseText.append(Conversion.capitalize(robotCache.getLocationTag().getQuickLocation()));//getLocationSituation().getName()));
					} else if (vehicleCache != null) {
						responseText.append(Conversion.capitalize(vehicleCache.getLocationTag().getQuickLocation()));					
					}
	
				}
	
				else if (num == 2 || text.contains("located") || text.contains("location") && text.contains("situation")) {
					questionText = YOU_PROMPT + "What is your exact location ?";
					LocationStateType stateType = null; 
	
					if (personCache != null) {
						stateType = personCache.getLocationStateType();
						responseText.append("I'm ");
						responseText.append(stateType.getName());
	
						if (personCache.getBuildingLocation() != null) {
							responseText.append(" (");
							responseText.append(personCache.getLocationTag().getExtendedLocations());// .getBuildingLocation().getNickName());
							responseText.append(")");
						}
	
					} else if (robotCache != null) {
						stateType = robotCache.getLocationStateType();
						responseText.append("I'm ");
						responseText.append(stateType.getName());
						responseText.append(" (");
						responseText.append(robotCache.getLocationTag().getExtendedLocations());// .getBuildingLocation().getNickName());
						responseText.append(")");
					}
					
					else if (vehicleCache != null) {
						stateType = vehicleCache.getLocationStateType();
						responseText.append("I'm ");
						responseText.append(stateType.getName());
	
						if (vehicleCache.getBuildingLocation() != null) {
							responseText.append(" (");
							responseText.append(vehicleCache.getLocationTag().getExtendedLocations());// .getBuildingLocation().getNickName());
							responseText.append(")");
						}
					}
			
				}
	
				else if (num == 3 || text.equalsIgnoreCase("task") || text.equalsIgnoreCase("activity")
						|| text.equalsIgnoreCase("action")) {
					questionText = YOU_PROMPT + "What are you doing ?";
					if (personCache != null) {
						responseText.append(personCache.getTaskDescription());
					} else if (robotCache != null) {
						responseText.append(robotCache.getTaskDescription());
					}
	
				}
	
				else if (num == 4 || text.equalsIgnoreCase("mission")) {
	
					// sys = name;
					questionText = YOU_PROMPT + "Are you involved in a particular mission at this moment?";
					Mission mission = null;
					if (personCache != null) {
						mission = personCache.getMind().getMission();
					} else if (robotCache != null) {
						mission = robotCache.getBotMind().getMission();
					} else if (vehicleCache != null) {
						Person p = (Person)vehicleCache.getOperator();
						if (p != null)
							mission = p.getMind().getMission();
	//					else
	//						mission = "Mission data not available.";
					}
	
					if (mission == null)
						responseText.append("No. I'm not. ");
					else
						responseText.append(mission.getDescription());
	
				}
	
				else if (num == 5 || text.equalsIgnoreCase("bed") || text.equalsIgnoreCase("quarter")
						|| text.equalsIgnoreCase("quarters")) {
					questionText = YOU_PROMPT + "Where is your designated quarters/bed ? ";
					Point2D bed = personCache.getBed();
					if (bed == null) {
						if (personCache != null) {
							responseText.append("I haven't got my own private quarters/bed yet.");
						} else if (robotCache != null) {
							responseText.append("I don't need one. My battery can be charged at any robotic station.");
						}
					} else {
						if (personCache != null) {
							Settlement s1 = personCache.getSettlement();
							if (s1 != null) {
								// check to see if a person is on a trading mission
								Settlement s2 = personCache.getAssociatedSettlement();
								if (s2 != null) {
									responseText.append("My designated quarters/bed is at (");
									responseText.append(bed.getX());
									responseText.append(", ");
									responseText.append(bed.getY());
									responseText.append(") in ");
									responseText.append(personCache.getQuarters());
									responseText.append(" at ");
	
									if (s1 == s2) {
										responseText.append(s1);
									}
	
									else {
										// yes, a person is on a trading mission
										responseText.append(s2);
									}
								}
							} else {
								responseText.append("My designated quarters/bed is at (");
								responseText.append(bed.getX());
								responseText.append(", ");
								responseText.append(bed.getY());
								responseText.append(") in ");
								responseText.append(personCache.getQuarters());
								responseText.append(" at ");
							}
						}
	
						else if (robotCache != null) {
							responseText.append("I don't need one. My battery can be charged at any robotic station.");
						}
					}
				}
	
				else if (num == 6 || text.equalsIgnoreCase("inside") || text.equalsIgnoreCase("container")) {
					questionText = YOU_PROMPT + "Are you inside?"; // what is your Container unit [Expert Mode only] ?";
					Unit c = u.getContainerUnit();
					if (c != null) {
						responseText.append("I'm at/in ").append(c.getName());
					} else
						responseText.append("I'm not inside a building or vehicle"); // "I don't have a Container unit. ";
				}
	
				else if (num == 7 || text.equalsIgnoreCase("outside")
						|| text.contains("top") && text.contains("container")) {
					questionText = YOU_PROMPT + "Are you inside?"; // YOU_PROMPT + "what is your Top Container unit [Expert
																	// Mode only] ?";
					Unit tc = u.getTopContainerUnit();
					if (tc != null) {
						responseText.append("I'm in ").append(tc.getName());
					}
	
					else
						responseText.append("I'm nowhere");// don't have a Top Container unit.";
	
				}
	
				else if (num == 8 || text.equalsIgnoreCase("building")) {
					questionText = YOU_PROMPT + "What building are you at ?";
					Settlement s = u.getSettlement();
					if (s != null) {
						// Building b1 = s.getBuildingManager().getBuilding(cache);
						Building b = u.getBuildingLocation();
						if (b != null) {// && b1 != null)
							responseText.append("The building I'm in is ").append(b.getNickName());
							// + " (aka " + b1.getNickName() + ").";
						} else
							responseText.append("I'm not in a building.");
					} else
						responseText.append("I'm not in a building.");
	
				}
	
				else if (num == 9 || text.equalsIgnoreCase("settlement")) {
					questionText = YOU_PROMPT + "What settlement are you at ?";
					Settlement s = u.getSettlement();
					if (s != null) {
						responseText.append("I'm at ").append(s.getName());
					} else
						responseText.append("I'm not inside a settlement");
	
				}
	
				else if (num == 10 || text.equalsIgnoreCase("associated settlement") || text.equalsIgnoreCase("association")
						|| text.equalsIgnoreCase("home") || text.equalsIgnoreCase("home town")
						|| text.equalsIgnoreCase("hometown")) {
					questionText = YOU_PROMPT + "What is your associated settlement ?";
					Settlement s = u.getAssociatedSettlement();
					if (s != null) {
						responseText.append("My associated settlement is ").append(s.getName());
					} else
						responseText.append("I don't have an associated settlement");
				}
	
	//	    	else if (num == 9 || text.equalsIgnoreCase("buried settlement")) {
	//	    		questionText = YOU_PROMPT + "What is his/her buried settlement ?";
	//	    		if personCache.
	//	    		Settlement s = cache.getBuriedSettlement();
	//	    		if (s == null) {
	//	           		responseText = "The buried settlement is " + s.getName();
	//	           		sys = "System : ";
	//	       		}
	//	       		else
	//	       			responseText = "I'm not dead.";
	//	    	}
	
				else if (num == 11 || text.equalsIgnoreCase("vehicle")) {
					questionText = YOU_PROMPT + "What vehicle are you in and where is it ?";
					Vehicle v = u.getVehicle();
					if (v != null) {
						String d = u.getVehicle().getDescription();
						StatusType status = u.getVehicle().getStatus();
						responseText.append("My vehicle is ");
						responseText.append(v.getName());
						responseText.append(" (a ");
						responseText.append(Conversion.capitalize(d));
						responseText.append(" type). It's currently ");
						responseText.append(status.getName().toLowerCase());
						responseText.append(".");
					} else
						responseText.append("I'm not in a vehicle.");
				}
	
				else if (num == 12 || text.equalsIgnoreCase("vehicle inside") || text.equalsIgnoreCase("vehicle container")
						|| text.contains("vehicle") && text.contains("container")) {
					questionText = YOU_PROMPT + "Where is your vehicle at?";// 's container unit ?";
					Vehicle v = personCache.getVehicle();
					if (v != null) {
						Unit c = v.getContainerUnit();
						if (c != null) {
							responseText.append("My vehicle is at ");
							responseText.append(c.getName());
						}
	
						else
							responseText.append("My vehicle is not inside");// doesn't have a container unit.";
	
					} else
						responseText.append("I'm not in a vehicle.");
				}
	
				else if (num == 13 || text.equalsIgnoreCase("vehicle outside")
						|| text.equalsIgnoreCase("vehicle top container")
						|| text.contains("vehicle") && text.contains("top") && text.contains("container")) {
					questionText = YOU_PROMPT + "What is your vehicle located?";// 's top container unit ?";
					Vehicle v = u.getVehicle();
					if (v != null) {
						Unit tc = v.getTopContainerUnit();
						if (tc != null) {
							responseText.append("My vehicle is at ");
							responseText.append(tc.getName());
						} else
							responseText.append("My vehicle is not inside");// doesn't have a top container unit.";
					} else
						responseText.append("I'm not in a vehicle.");
				}
	
				else if (num == 14 || text.contains("vehicle") && text.contains("park")) {
					questionText = YOU_PROMPT + "What building does your vehicle park at ?";
					Vehicle v = u.getVehicle();
					if (v != null) {
						Settlement s = v.getSettlement();
						if (s != null) {
							Building b = s.getBuildingManager().getBuilding(v);
							if (b != null) {
								responseText.append("My vehicle is parked inside ");
								responseText.append(b.getNickName());
							}
	
							else
								responseText.append("My vehicle does not park inside a building/garage");
						} else
							responseText.append("My vehicle is not at a settlement.");
					} else
						responseText.append("I'm not on a vehicle.");
				}
	
				else if (num == 15 || text.contains("vehicle") && text.contains("settlement")) {
					questionText = YOU_PROMPT + "What settlement is your vehicle located at ?";
					Vehicle v = u.getVehicle();
					if (v != null) {
						Settlement s = v.getSettlement();
						if (s != null) {
							responseText.append("My vehicle is at ");
							responseText.append(s.getName());
						} else
							responseText.append("My vehicle is not at a settlement.");
					} else
						responseText.append("I'm not on a vehicle.");
				}
	
				else if (num == 16 || text.contains("sleep hour") || text.contains("bed time")) {
					questionText = YOU_PROMPT + "What is your preferred/usual bed time ?";
	
					int[] twos = ((Person) u).getCircadianClock().getPreferredSleepHours();
					int small = Math.min(twos[0], twos[1]);
					int large = Math.max(twos[0], twos[1]);
	
					responseText.append("My preferred sleep hours are at either " + small + " or " + large + " millisols.");
	
				}
	
				else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("/k")) {
					questionText = REQUEST_KEYS;
					if (connectionMode == 0) {
						keywordText = KEYWORDS_TEXT;
					}
					else {
						keywordText = KEYWORDS_TEXT + KEYWORDS_HEIGHT;
					}
					responseText.append(System.lineSeparator());
					responseText.append(keywordText);
	
				}
	
				else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") 
						|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")) {
					questionText = REQUEST_HELP;
					if (connectionMode == 0) {
						helpText = HELP_TEXT;
					}
					else {
						helpText = HELP_TEXT + HELP_HEIGHT;
					}
					responseText.append(System.lineSeparator());
					responseText.append(helpText);
	
				}
				
				// Add changing the height of the chat box
				// DELETED
				
				else {
					String[] txt = clarify(name);
					questionText = txt[0];
					responseText.append(txt[1]);
				}
			}
		}

		else {
			// set personCache and robotCache to null only if you want to quit the
			// conversation
			String[] txt = clarify(name);
			questionText = txt[0];
			responseText.append(txt[1]);
		}
		
		return new String[] {questionText, responseText.toString()};
		
	}
		
	/*
	 * Parses the text and interprets the contents in the chat box
	 * 
	 * @param input text
	 */
	public static String parseText(String text) {
//		System.out.println("parseText() in ChatUtils");
		StringBuilder responseText = new StringBuilder();
		
		// String SYSTEM_PROMPT = "System : ";
		boolean available = true;
		int nameCase = 0;
		boolean proceed = false;
		
//		Unit unit = null;
		Person person = null;
		Robot robot = null;
		
		// System.out.println("A: text is " + text + ". Running parseText()");
		text = text.trim();
		int len = text.length();

		List<Person> personList = new ArrayList<>();
		List<Robot> robotList = new ArrayList<>();

		// Detect "\" backslash and the name that follows
		if (len >= 3 && text.substring(0, 1).equalsIgnoreCase("\\")) {
			text = text.substring(1, len).trim();
			proceed = true;
		}

		else if (text.equalsIgnoreCase("key") || text.equalsIgnoreCase("/k")) {

			//responseText.append(System.lineSeparator());
			if (connectionMode == 0) {
				keywordText = KEYWORDS_TEXT;
			}
			else {
				keywordText = KEYWORDS_TEXT + KEYWORDS_HEIGHT;
			}
			responseText.append(keywordText);
		}

		else if (text.equalsIgnoreCase("help") || text.equalsIgnoreCase("/h") 
				|| text.equalsIgnoreCase("/?") || text.equalsIgnoreCase("?")) {

			//responseText.append(System.lineSeparator());
			if (connectionMode == 0) {
				helpText = HELP_TEXT;
			}
			else {
				helpText = HELP_TEXT + HELP_HEIGHT;
			}
			responseText.append(helpText);
		}		
		
		// Add proposals
		else if (text.equalsIgnoreCase("/p")) {
//			System.out.println("/p is submitted");
			responseText.append(System.lineSeparator());
			responseText.append(SYSTEM_PROMPT);
			responseText.append("[EXPERIMENTAL & NON-FUNCTIONAL] Below is a list of proposals for your review :");	
			responseText.append(System.lineSeparator());
			responseText.append("1. Safety and Health Measures");
			responseText.append(System.lineSeparator());
			responseText.append("2. Manufacturing Priority");
			responseText.append(System.lineSeparator());
			responseText.append("3. Food Allocation Plan");
			responseText.append(System.lineSeparator());
		}

		// Add asking about settlements in general
		else if (text.toLowerCase().equals("settlement") || text.toLowerCase().equals("settlements")) {

			// questionText = YOU_PROMPT + "What are the names of the settlements ?";

			// Creates an array with the names of all of settlements
			List<Settlement> settlementList = new ArrayList<Settlement>(
					Simulation.instance().getUnitManager().getSettlements());

			int num = settlementList.size();
			String s = "";

			if (num > 2) {
				for (int i = 0; i < num; i++) {
					if (i == num - 2)
						s = s + settlementList.get(i) + ", and ";
					else if (i == num - 1)
						s = s + settlementList.get(i) + ".";
					else
						s = s + settlementList.get(i) + ", ";
				}
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is a total of ");
				responseText.append(num);
				responseText.append(" settlements : ");
				responseText.append(s);
			}

			else if (num == 2) {
				s = settlementList.get(0) + " and " + settlementList.get(1);
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is a total of ");
				responseText.append(num);
				responseText.append(" settlements : ");
				responseText.append(s);
			}

			else if (num == 1) {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There is just one settlement : ");
				responseText.append(settlementList.get(0));
			}

			else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Currently, there is no settlement established on Mars.");
			}

		}

		// Add asking about vehicles in general
		else if (text.toLowerCase().equals("vehicle") || text.toLowerCase().equals("vehicles")) {

			// questionText = YOU_PROMPT + "What are the names of the vehicles ?";

			// Creates an array with the names of all of settlements
			List<Settlement> settlementList = new ArrayList<Settlement>(
					Simulation.instance().getUnitManager().getSettlements());
		
			for (Settlement s : settlementList) {
				Collection<Vehicle> list = s.getAllAssociatedVehicles();
				responseText.append(SYSTEM_PROMPT);
				responseText.append(s);
				responseText.append(" has ");
				responseText.append(list);
				responseText.append(System.lineSeparator()); 			
			}

		}
		
		else if (len >= 5 && text.substring(0, 5).equalsIgnoreCase("hello")
				|| len >= 4 && text.substring(0, 4).equalsIgnoreCase("helo")) {

			if (len > 5) {
				text = text.substring(5, len);
				text = text.trim();
				proceed = true;
			} else if (len > 4) {
				text = text.substring(4, len);
				text = text.trim();
				proceed = true;
			} else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Hello, how can I help?    [/h for help]");
			}
		}

		else if (len >= 3 && text.substring(0, 3).equalsIgnoreCase("hey")) {

			if (len > 3) {
				text = text.substring(3, len);
				text = text.trim();
				proceed = true;
			} else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Hello, how can I help?    [/h for help]");
			}
		}

		else if (len >= 2 && text.substring(0, 2).equalsIgnoreCase("hi")) {

			if (len > 2) {
				text = text.substring(2, len);
				text = text.trim();
				proceed = true;
			} else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("Hello, how can I help?    [/h for help]");
			}
		}

		else if (len >= 2) {
			proceed = true;
		}

		// Part 2 //

		if (len == 0 || text == null || text.length() == 1) {
			responseText.append(clarify(SYSTEM)[1]);

		}

		else if (proceed) { // && text.length() > 1) {
			// System.out.println("B: text is " + text);

			// person and robot
			Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
			while (i.hasNext()) {
				Settlement settlement = i.next();
				// Check if anyone has this name (as first/last name) in any settlements
				// and if he/she is still alive
				if (text.contains("bot") || text.contains("Bot")) {
					// Check if it is a bot
					robotList.addAll(settlement.returnRobotList(text));
				} else {
					personList.addAll(settlement.returnPersonList(text));

				}

				if (personList.size() != 0)
					nameCase = personList.size();
				else
					nameCase = robotList.size();

				// System.out.println("nameCase is " + nameCase);
			}

			// System.out.println("total nameCase is " + nameCase);

			// capitalize the first initial of a name
			text = Conversion.capitalize(text);

			// Case 1: more than one person with that name
			if (nameCase >= 2) {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("There are more than one \"");
				responseText.append(text);
				responseText.append("\". Would you be more specific?");
				// System.out.println(responseText);

				// Case 2: there is one person
			} else if (nameCase == 1) {

				if (!available) {
					// TODO: check if the person is available or not (e.g. if on a mission and comm
					// broke down)
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I'm sorry. ");
					responseText.append(text);
					responseText.append(" is unavailable at this moment");

				} else {
//					System.out.println("personList's size : " + personList.size());
//					System.out.println("personList : " + personList);
					if (!personList.isEmpty()) {
						person = personList.get(0);
						if (person.getPhysicalCondition().isDead()) {
							// Case 4: passed away
							int rand = RandomUtil.getRandomInt(1);
							if (rand == 0) {
								responseText.append(SYSTEM_PROMPT);
								responseText.append("I'm sorry. ");
								responseText.append(text);
								responseText.append(" has passed away and is buried at ");
								responseText.append(person.getBuriedSettlement().getName());
							}
							else {
								responseText.append(SYSTEM_PROMPT);
								responseText.append("Perhaps you don't know that ");
								responseText.append(text);
								responseText.append(" is dead and is buried at ");
								responseText.append(person.getBuriedSettlement().getName());
							}
						} else {
							personCache = person;
//							unitCache = person;
						}
					}

					if (!robotList.isEmpty()) {
						robot = robotList.get(0);
						if (robot.getSystemCondition().isInoperable()) {
							// Case 4: decomissioned
							responseText.append(SYSTEM_PROMPT);
							responseText.append("I'm sorry. ");
							responseText.append(text);
							responseText.append(" has been decomissioned.");
						} else {
							robotCache = robot;
//							unitCache = robot;
						}
					}

					if (robotCache != null) {
						responseText.append(robotCache.getName());
						responseText.append(" : This is ");
						responseText.append(text);
						responseText.append(". ");	
						
					}
					
					else if (personCache != null) {
						responseText.append(personCache.getName());
						responseText.append(" : This is ");
						responseText.append(text);
						responseText.append(". ");
					}
				}

				// Case 3: doesn't exist, check settlement's name
			} else if (nameCase == 0) {

				// System.out.println("nameCase is 0");
				// Match a settlement's name
				if (text.length() >= 2) {
					Iterator<Settlement> j = Simulation.instance().getUnitManager().getSettlements().iterator();
					while (j.hasNext()) {
						Settlement settlement = j.next();
						String s_name = settlement.getName();

						if (s_name.equalsIgnoreCase(text)) {
							// name = "System";
							responseText.append(SYSTEM_PROMPT);
							responseText.append("Yes, what would like to know about \"");
							responseText.append(s_name);
							responseText.append("\" ?");

							settlementCache = settlement;
							// System.out.println("matching settlement name " + s_name);
							break;
						}

						else if (s_name.toLowerCase().contains(text.toLowerCase())
								|| text.toLowerCase().contains(s_name.toLowerCase())) {
							responseText.append(SYSTEM_PROMPT);
							responseText.append("Do you mean \"");
							responseText.append(s_name);
							responseText.append("\" ?");
							// System.out.println("partially matching settlement name " + s_name);
							break;
						}
						 else {
							responseText.append(SYSTEM_PROMPT);
							responseText.append("I do not recognize anyone or any settlements by \"");
							responseText.append(text);
							responseText.append("\".");
						}
						// TODO: check vehicle names

						// TODO: check commander's name

					}
					
				} else {
					responseText.append(SYSTEM_PROMPT);
					responseText.append("I do not recognize anyone or any settlements by \"");
					responseText.append(text);
					responseText.append("\".");
				}
			}

			else {
				responseText.append(SYSTEM_PROMPT);
				responseText.append("I do not recognize anyone or any settlements by \"");
				responseText.append(text);
				responseText.append("\".");
			}
		}

		return responseText.toString();

	}

	public static void setConnectionMode(int value) {
		connectionMode = value;
	}

	public static int getConnectionMode() {
		return connectionMode;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		personCache = null;
		robotCache = null;
		settlementCache = null;
		vehicleCache = null;
	}
}