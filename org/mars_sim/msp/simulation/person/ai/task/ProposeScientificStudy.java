/**
 * Mars Simulation Project
 * ProposeScientificStudy.java
 * @version 2.87 2009-07-07
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.science.Science;
import org.mars_sim.msp.simulation.science.ScienceUtil;
import org.mars_sim.msp.simulation.science.ScientificStudy;
import org.mars_sim.msp.simulation.science.ScientificStudyManager;

/**
 * A task for proposing a new scientific study.
 */
public class ProposeScientificStudy extends Task implements Serializable {

    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ProposeScientificStudy";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = 0D;
    
    // Task phase.
    private static final String PROPOSAL_PHASE = "Writing Study Proposal";
    
    private ScientificStudy study; // The scientific study to propose.
    
    /**
     * Constructor.
     * @param person the person performing the task.
     * @throws Exception if error constructing task.
     */
    public ProposeScientificStudy(Person person) throws Exception {
        super("Proposing a scientific study", person, false, true, STRESS_MODIFIER, 
                true, RandomUtil.getRandomDouble(100D));
        
        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        study = manager.getOngoingPrimaryStudy(person);
        if (study == null) {
            // Create new scientific study.
            Job job = person.getMind().getJob();
            Science science = ScienceUtil.getAssociatedScience(job);
            if (science != null) {
                String skillName = ScienceUtil.getAssociatedSkill(science);
                int level = person.getMind().getSkillManager().getSkillLevel(skillName);
                study = manager.createScientificStudy(person, science, level);
            }
            else {
                logger.log(Level.SEVERE, "Person's job: " + job.getName() + " not scientist.");
                endTask();
            }
        }
        
        if (study != null) setDescription("Proposing a " + study.getScience().getName() + " study");
        
        // Initialize phase
        addPhase(PROPOSAL_PHASE);
        setPhase(PROPOSAL_PHASE);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
        ScientificStudy study = manager.getOngoingPrimaryStudy(person);
        if (study != null) {
            
            // Check if study is in proposal phase.
            if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
                
                // Increase probability if person's current job is related to study's science.
                Job job = person.getMind().getJob();
                Science science = study.getScience();
                if ((job != null) && ScienceUtil.getAssociatedScience(job).equals(science)) result = 100D;
                else result = 10D;
            }
        }
        else {
            
            // Probability of starting a new scientific study.
            
            // Check if scientist job.
            Job job = person.getMind().getJob();
            Science science = ScienceUtil.getAssociatedScience(job);
            if (science != null) result = 1D;
        }
        
        return result;
    }
    
    /**
     * Performs the writing study proposal phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double proposingPhase(double time) throws Exception {
        
        if (!study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) endTask();
        if (isDone()) return time;
        
        // Determine amount of effective work time based on science skill.
        double workTime = time;
        int scienceSkill = getEffectiveSkillLevel();
        if (scienceSkill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) scienceSkill);
        
        study.addProposalWorkTime(workTime);
        
        // Add experience
        addExperience(time);
        
        return 0D;
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to relevant science skill
        // 1 base experience point per 25 millisols of proposal writing time.
        double newPoints = time / 25D;
        
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
            NaturalAttributeManager.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        
        String skillName = ScienceUtil.getAssociatedSkill(study.getScience());
        person.getMind().getSkillManager().addExperience(skillName, newPoints);
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> skills = new ArrayList<String>(1);
        skills.add(ScienceUtil.getAssociatedSkill(study.getScience()));
        return skills;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        String skillName = ScienceUtil.getAssociatedSkill(study.getScience());
        return manager.getEffectiveSkillLevel(skillName);
    }

    @Override
    protected double performMappedPhase(double time) throws Exception {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (PROPOSAL_PHASE.equals(getPhase())) return proposingPhase(time);
        else return time;
    }
}