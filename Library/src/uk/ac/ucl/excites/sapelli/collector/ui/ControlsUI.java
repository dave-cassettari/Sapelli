package uk.ac.ucl.excites.sapelli.collector.ui;

import java.util.Arrays;

import uk.ac.ucl.excites.sapelli.collector.control.Controller;
import uk.ac.ucl.excites.sapelli.collector.model.Form;

/**
 * Abstract class to represent the controls UI (i.e. back/cancel/fwd buttons, maybe others later)
 * 
 * @author mstevens
 *
 * @param <V>
 * @param <UI>
 */
public abstract class ControlsUI<V, UI extends CollectorUI<V, UI>>
{
	
	// Statics-------------------------------------------------------
	static public enum Control
	{
		BACK,
		//UP,
		CANCEL,
		FORWARD
	}
	
	static public enum State
	{
		HIDDEN,
		SHOWN_DISABLED, // "grayed out"
		SHOWN_ENABLED
	}
	
	// Dynamics------------------------------------------------------
	protected Controller controller;
	protected UI collectorUI;
	protected boolean enabled;
	
	private Form currentForm;
	private State[] controlStates;
	
	public ControlsUI(Controller controller, UI collectorUI)
	{
		this.controller = controller;
		this.collectorUI = collectorUI;
		this.enabled = true;
	}
	
	/**
	 * @return a platform-specific UI element (e.g. an Android View instance).
	 */
	protected abstract V getPlatformView();
	
	public void update(FieldUI<?, V, UI> fieldUI)
	{
		// Form change?
		if(currentForm != fieldUI.getField().getForm())
		{
			currentForm = fieldUI.getField().getForm();
			updateForm(currentForm);
		}
		
		// What do we need to show?
		State[] newControlStates = new State[Control.values().length];
		for(Control control : ControlsUI.Control.values())
			newControlStates[control.ordinal()] = fieldUI.getControlState(control); // takes into account the current FormMode
		
		// Is this different from the currently shown controls?
		if(!Arrays.equals(newControlStates, controlStates))
		{
			controlStates = newControlStates;
			updateControlStates(controlStates);
		}
	}
	
	protected abstract void updateForm(Form newForm);
	
	protected abstract void updateControlStates(State[] newControlStates);
	
	public void disable()
	{
		enabled = false;
	}
	
	public void enable()
	{
		enabled = true;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public boolean isControlEnabled(Control control)
	{
		return enabled && controlStates[control.ordinal()] == State.SHOWN_ENABLED;
	}
	
	/**
	 * @param control
	 * @param hardwareKeyPress
	 */
	public void handleControlEvent(Control control, boolean hardwareKeyPress)
	{
		if(!isControlEnabled(control))
			return;
		
		// Log interaction:
		controller.addLogLine((hardwareKeyPress ? "KEY" : "CLICK") + "_CONTROL_" + control.name(), controller.getCurrentField().getID());
		
		// Handle event:
		switch(control)
		{
			case BACK :				
				controller.goBack(true);
				break;
			case CANCEL : 
				controller.cancelAndRestartForm();
				break;
			case FORWARD :
				controller.goForward(true);
				break;
			default : return;
		}
	}
	
	public abstract int getCurrentHeightPx();
	
}
