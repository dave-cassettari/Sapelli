/**
 * 
 */
package uk.ac.ucl.excites.collector.project.ui;

/**
 * @author mstevens
 *
 */
public class ButtonsState
{
	
	private boolean backShown;
	private boolean cancelShown;
	private boolean forwardShown;
	
	/**
	 * @param backShown
	 * @param cancelShown
	 * @param forwardShown
	 */
	public ButtonsState(boolean backShown, boolean cancelShown, boolean forwardShown)
	{
		this.backShown = backShown;
		this.cancelShown = cancelShown;
		this.forwardShown = forwardShown;
	}

	public int getNumberOfButtonsShown()
	{
		return (backShown ? 1 : 0) + (cancelShown ? 1 : 0) + (forwardShown ? 1 : 0);
	}

	/**
	 * @return the backShown
	 */
	public boolean isBackShown()
	{
		return backShown;
	}

	/**
	 * @return the cancelShown
	 */
	public boolean isCancelShown()
	{
		return cancelShown;
	}

	/**
	 * @return the forwardShown
	 */
	public boolean isForwardShown()
	{
		return forwardShown;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof ButtonsState))
			return false;
		ButtonsState another = (ButtonsState) o;
		return backShown == another.backShown && cancelShown == another.cancelShown && forwardShown == another.forwardShown;
	}
	
}