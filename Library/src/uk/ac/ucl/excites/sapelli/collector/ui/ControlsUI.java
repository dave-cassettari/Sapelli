/**
 * Sapelli data collection platform: http://sapelli.org
 * 
 * Copyright 2012-2014 University College London - ExCiteS group
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package uk.ac.ucl.excites.sapelli.collector.ui;

import java.util.Arrays;

import uk.ac.ucl.excites.sapelli.collector.control.Controller;
import uk.ac.ucl.excites.sapelli.collector.model.Control;
import uk.ac.ucl.excites.sapelli.collector.model.Form;
import uk.ac.ucl.excites.sapelli.collector.ui.fields.FieldUI;

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
	static public enum State
	{
		HIDDEN,
		SHOWN_DISABLED, // "grayed out"
		SHOWN_ENABLED
	}
	
	// Dynamics------------------------------------------------------
	protected Controller<UI> controller;
	protected UI collectorUI;
	protected boolean enabled;
	
	private Form currentForm;
	private State[] controlStates;
	
	public ControlsUI(Controller<UI> controller, UI collectorUI)
	{
		this.controller = controller;
		this.collectorUI = collectorUI;
		this.enabled = true;
	}
	
	/**
	 * @return a platform-specific UI element (e.g. an Android View instance).
	 */
	protected abstract V getPlatformView();
	
	/**
	 * Updates the controls (showing/hiding/appearance/etc.) for using with the given fieldUI
	 * Note that this fieldUI is expected to match the current field of the Controller.
	 * 
	 * @param fieldUI
	 */
	public void update(FieldUI<?, V, UI> fieldUI)
	{
		// Form change?
		if(fieldUI.field.form != currentForm)
		{
			currentForm = fieldUI.field.form;
			updateForm(currentForm);
		}
		
		// What do we need to show?
		State[] newControlStates = new State[Control.Type.values().length];
		for(Control.Type control : Control.Type.values())
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
	
	public boolean isControlEnabled(Control.Type controlType)
	{
		return enabled && controlStates[controlType.ordinal()] == State.SHOWN_ENABLED;
	}
	
	/**
	 * @param control
	 * @param hardwareKeyPress
	 */
	public void handleControlEvent(Control.Type controlType, boolean hardwareKeyPress)
	{
		if(!isControlEnabled(controlType))
			return;
		
		// Log interaction:
		controller.addLogLine((hardwareKeyPress ? "KEY" : "CLICK") + "_CONTROL_" + controlType.name(), controller.getCurrentField().id);
		
		// Handle event:
		switch(controlType)
		{
			case Back :				
				controller.goBack(true);
				break;
			case Cancel : 
				controller.cancelAndRestartForm();
				break;
			case Forward :
				controller.goForward(true);
				break;
			default : return;
		}
	}
	
	public abstract int getCurrentHeightPx();
	
}
