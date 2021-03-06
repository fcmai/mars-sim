/**
 * Mars Simulation Project
 * UnitWindow.java
 * @version 3.1.0 2017-09-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

import com.alee.laf.desktoppane.WebInternalFrame;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideTabbedPane;

/**
 * The UnitWindow is the base window for displaying units.
 */
public abstract class UnitWindow extends WebInternalFrame { // ModalInternalFrame

	// private static final int BLUR_SIZE = 7;

	public static final int WIDTH = 480;// 512;
	public static final int HEIGHT = 625;//605;

	// private BufferedImage image;
	public static final String USER = Msg.getString("icon.user");
	private static final String TOWN = Msg.getString("icon.town");
	private static final String JOB = Msg.getString("icon.job");
	private static final String ROLE = Msg.getString("icon.role");
	private static final String SHIFT = Msg.getString("icon.shift");

	// private static final String TITLE = Msg.getString("icon.title");
	private static final String ONE_SPACE = " ";
	private static final String TWO_SPACES = "  ";
	private static final String DEAD = "Dead";
	private static final String SHIFT_FROM = " Shift :  (From ";
	private static final String TO = " to ";
	private static final String MILLISOLS = " millisols)";
	private static final String SHIFT_ANYTIME = " Shift :  Anytime";
	private static final String ONE_SPACE_SHIFT = " Shift";
	// private static final String STATUS = "Status (click to open/close)";
	// private static final String DETAILS = "Details";
	// private static final String STATUS_ICON = Msg.getString("icon.status");
	// private static final String DETAILS_ICON = Msg.getString("icon.details");

	// Data members
	private int themeCache = -1;

	private String oldRoleString = "", oldJobString = "", oldTownString = "";
	private ShiftType oldShiftType = null;
	private WebLabel townLabel;
	private WebLabel jobLabel;
	private WebLabel roleLabel;
	private WebLabel shiftLabel;

	private WebPanel statusPanel;
	/** The tab panels. */
	private Collection<TabPanel> tabPanels;
	/** The center panel. */
	// private JTabbedPane tabPanel;
	private JideTabbedPane tabPanel;
	// private JTabbedPane tabPanel;

	/** Main window. */
	protected MainDesktopPane desktop;
	/** Unit for this window. */
	protected Unit unit;
//	protected SlidePaneFactory factory;
	protected MainScene mainScene;

	/**
	 * Constructor
	 *
	 * @param desktop        the main desktop panel.
	 * @param unit           the unit for this window.
	 * @param hasDescription true if unit description is to be displayed.
	 */
	public UnitWindow(MainDesktopPane desktop, Unit unit, boolean hasDescription) {
		// Use JInternalFrame constructor
		super(unit.getName(), false, true, false, true);

		// Initialize data members
		this.desktop = desktop;
		mainScene = desktop.getMainScene();
		this.unit = unit;

		this.setMaximumSize(new Dimension(WIDTH, HEIGHT));
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		tabPanels = new ArrayList<TabPanel>();

		// Create main panel
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());// setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create status panel
		statusPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));

		mainPane.add(statusPanel, BorderLayout.NORTH);

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = ONE_SPACE + Conversion.capitalize(unit.getShortenedName()) + ONE_SPACE;
		
		if (unit instanceof Person) {
			statusPanel.setPreferredSize(new Dimension(WIDTH / 8, 60));
		}

		int theme = 0;
		if (mainScene != null) {
			theme = MainScene.getTheme();
			if (themeCache != theme) {
				themeCache = theme;
				// pale blue : Color(198, 217, 217)) = new Color(0xC6D9D9)
				// pale grey : Color(214,217,223) = D6D9DF
				// pale mud : (193, 191, 157) = C1BF9D
			}
		}

		else
			theme = 7;

		if (unit instanceof Person) {

			WebLabel nameLabel = new WebLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
			nameLabel.setMinimumSize(new Dimension(80, 60));

			Font font = null;

			if (MainScene.OS.contains("linux")) {
				new Font("DIALOG", Font.BOLD, 8);
			} else {
				new Font("DIALOG", Font.BOLD, 10);
			}
			nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);// .CENTER_ALIGNMENT);
			nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
			nameLabel.setFont(font);
			nameLabel.setVerticalTextPosition(WebLabel.BOTTOM);
			nameLabel.setHorizontalTextPosition(WebLabel.CENTER);

			statusPanel.add(nameLabel);

			// Create description label if necessary.
			if (hasDescription) {

				WebLabel townIconLabel = new WebLabel();
				TooltipManager.setTooltip(townIconLabel, "Associated Settlement", TooltipWay.down);
				setImage(TOWN, townIconLabel);

				WebLabel jobIconLabel = new WebLabel();
				TooltipManager.setTooltip(jobIconLabel, "Job", TooltipWay.down);
				setImage(JOB, jobIconLabel);

				WebLabel roleIconLabel = new WebLabel();
				TooltipManager.setTooltip(roleIconLabel, "Role", TooltipWay.down);
				setImage(ROLE, roleIconLabel);

				WebLabel shiftIconLabel = new WebLabel();
				TooltipManager.setTooltip(shiftIconLabel, "Work Shift", TooltipWay.down);
				setImage(SHIFT, shiftIconLabel);

				WebPanel townPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				WebPanel jobPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				WebPanel rolePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				WebPanel shiftPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

				townLabel = new WebLabel();
				townLabel.setFont(font);

				jobLabel = new WebLabel();
				jobLabel.setFont(font);

				roleLabel = new WebLabel();
				roleLabel.setFont(font);

				shiftLabel = new WebLabel();
				shiftLabel.setFont(font);

				statusUpdate();

				townPanel.add(townIconLabel);
				townPanel.add(townLabel);
				townPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				jobPanel.add(jobIconLabel);
				jobPanel.add(jobLabel);
				jobPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				rolePanel.add(roleIconLabel);
				rolePanel.add(roleLabel);
				rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				shiftPanel.add(shiftIconLabel);
				shiftPanel.add(shiftLabel);
				shiftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

				WebPanel rowPanel = new WebPanel(new GridLayout(2, 2, 0, 0));
				// rowPanel.setBorder(new MarsPanelBorder());

				rowPanel.add(townPanel);// , FlowLayout.LEFT);
				rowPanel.add(rolePanel);// , FlowLayout.LEFT);
				rowPanel.add(shiftPanel);// , FlowLayout.LEFT);
				rowPanel.add(jobPanel);// , FlowLayout.LEFT);

				statusPanel.add(rowPanel);
				rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

//	            }
			}

			// factory.add(centerPanel, DETAILS, getImage(DETAILS_ICON), true);
		}

		tabPanel = new JideTabbedPane();
		tabPanel.setPreferredSize(new Dimension(WIDTH - 15, 512));
		tabPanel.setBorder(null);

		tabPanel.setBoldActiveTab(true);
		tabPanel.setScrollSelectedTabOnWheel(true);
		tabPanel.setTabShape(JideTabbedPane.SHAPE_WINDOWS_SELECTED);
		
		if (MainScene.getTheme() == 7) {
			LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
			tabPanel.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003); // COLOR_THEME_VSNET);
		} else {
			LookAndFeelFactory.installJideExtension(LookAndFeelFactory.VSNET_STYLE);
			tabPanel.setColorTheme(JideTabbedPane.COLOR_THEME_VSNET);
		}
		// Setting foreground color for tab text.
		tabPanel.setForeground(Color.DARK_GRAY);
		tabPanel.setTabPlacement(JideTabbedPane.LEFT);

		WebPanel centerPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		centerPanel.add(tabPanel);
		centerPanel.setPreferredSize(new Dimension(WIDTH - 5, 512));

		// update();

		mainPane.add(centerPanel, BorderLayout.CENTER);
		// add focusListener to play sounds and alert users of critical conditions.

		// TODO: disabled in SVN while in development
		// this.addInternalFrameListener(new
		// UniversalUnitWindowListener(UnitInspector.getGlobalInstance()));
	}

	/**
	 * Sets weather image.
	 */
	public void setImage(String imageLocation, WebLabel label) {
		ImageIcon imageIcon = ImageLoader.getNewIcon(imageLocation);
		label.setIcon(imageIcon);
	}

	public Image getImage(String imageLocation) {
	
		return ImageLoader.getNewIcon(imageLocation).getImage();
	}

	/*
	 * Updates the status of an unit
	 */
	public void statusUpdate() {

		Person p = (Person) unit;

		String townString = null;

		if (p.getPhysicalCondition().isDead())
			townString = DEAD;// + p.getBuriedSettlement();
		else
			townString = Conversion.capitalize(unit.getDescription());
		// System.out.println("Description is : " + text);
		if (!oldTownString.equals(townString)) {
			oldJobString = townString;
			if (townString.length() > 40)
				townString = townString.substring(0, 40);
			townLabel.setText(TWO_SPACES + townString);// , JLabel.CENTER);
		}

		String jobString = p.getMind().getJob().getName(p.getGender());
		if (!oldJobString.equals(jobString)) {
			oldJobString = jobString;
			jobLabel.setText(TWO_SPACES + jobString);// , JLabel.CENTER);
		}

		String roleString = p.getRole().getType().getName();
		if (!oldRoleString.equals(roleString)) {
			oldRoleString = roleString;
			roleLabel.setText(TWO_SPACES + roleString);
		}

		ShiftType newShiftType = p.getTaskSchedule().getShiftType();
		if (oldShiftType != newShiftType) {
			oldShiftType = newShiftType;
			shiftLabel.setText(TWO_SPACES + newShiftType.getName());
			TooltipManager.setTooltip(shiftLabel, newShiftType.getName() + getTimePeriod(newShiftType),
					TooltipWay.down);
		}
	}

	public String getTimePeriod(ShiftType shiftType) {
		String time = null;
		if (shiftType == ShiftType.A)
			time = SHIFT_FROM + TaskSchedule.A_START + TO + TaskSchedule.A_END + MILLISOLS;
		else if (shiftType == ShiftType.B)
			time = SHIFT_FROM + TaskSchedule.B_START + TO + TaskSchedule.B_END + MILLISOLS;
		else if (shiftType == ShiftType.X)
			time = SHIFT_FROM + TaskSchedule.X_START + TO + TaskSchedule.Y_END + MILLISOLS;
		else if (shiftType == ShiftType.Y)
			time = SHIFT_FROM + TaskSchedule.Y_START + TO + TaskSchedule.Y_END + MILLISOLS;
		else if (shiftType == ShiftType.Z)
			time = SHIFT_FROM + TaskSchedule.Z_START + TO + TaskSchedule.Z_END + MILLISOLS;
		else if (shiftType == ShiftType.ON_CALL)
			time = SHIFT_ANYTIME;
		else
			time = ONE_SPACE_SHIFT;
		return time;
	}

	/**
	 * Adds a tab panel to the center panel.
	 *
	 * @param panel the tab panel to add.
	 */
	protected final void addTabPanel(TabPanel panel) {
		if (!tabPanels.contains(panel)) {
			tabPanels.add(panel);
		}
	}

	protected final void addTopPanel(TabPanel panel) {
		if (!tabPanels.contains(panel)) {
			tabPanels.add(panel);
			statusPanel.add(panel, BorderLayout.CENTER);
		}
	}

	protected void sortTabPanels() {
		tabPanels.stream().sorted((t1, t2) -> t2.getTabTitle().compareTo(t1.getTabTitle()));
		tabPanels.forEach(panel -> {
			tabPanel.addTab(panel.getTabTitle(), panel.getTabIcon(), panel, null);// panel.getTabToolTip());
		});

	}

	/**
	 * Gets the unit for this window.
	 *
	 * @return unit
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * Updates this window.
	 */
	public void update() {
		// Update each of the tab panels.
		for (TabPanel tabPanel : tabPanels) {
			if (tabPanel.isVisible()) {// && isShowing()) {
				SwingUtilities.invokeLater(() -> 
					tabPanel.update()
				);
//				tabPanel.updateUI();
//			tabPanel.validate();
			}
		}

		if (unit instanceof Person) {
			statusUpdate();
		}

	}
	
	@Override
    public String getName() {
    	return unit.getName() +"'s unit window";
    }
    
	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {
		statusPanel = null;
		if (tabPanels != null)
			tabPanels.clear();
		tabPanels = null;
		tabPanel = null;
		oldShiftType = null;
		townLabel = null;
		jobLabel = null;
		roleLabel = null;
		shiftLabel = null;
		desktop = null;
		unit = null;
		mainScene = null;
	}
}