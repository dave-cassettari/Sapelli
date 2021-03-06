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

package uk.ac.ucl.excites.sapelli.collector.ui.items;

import uk.ac.ucl.excites.sapelli.collector.util.ScreenMetrics;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

/**
 * An abstract class representing items contained within an {@link uk.ac.ucl.excites.sapelli.collector.ui.ItemPickerView}.
 * 
 * @author mstevens
 *
 * @param <I> self-bound generic parameter used to refer to subclass type. All subclasses *must* bind this to themselves! E.g.: {@code class SubItem extends Item<SubItem>}
 */
public abstract class Item<I extends Item<I>>
{
	
	// STATIC -----------------------------------------------------------------
	static public final float DEFAULT_PADDING_DIP = 3.0f;
	static public final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
	
	// DYNAMIC ----------------------------------------------------------------
	protected final Integer id;
	protected float paddingDip = DEFAULT_PADDING_DIP;
	protected int backgroundColor = DEFAULT_BACKGROUND_COLOR;
	protected boolean visible = true;
	protected String description;
	
	private View view = null; // cached view instance, to allow for recycling
	
	/**
	 * @param id (may be null)
	 */
	public Item(Integer id)
	{
		this.id = id;
	}
	
	public View getView(Context context)
	{
		return getView(context, true); // recycle views by default
	}
	
	/**
	 * @param context
	 * @param recycle whether or not to recycle previously instantiate view instance
	 * @return
	 */
	public View getView(Context context, boolean recycle)
	{
		if(view == null || !recycle)
			view = createView(context, recycle);
		
		// (Re)apply properties:
		applyProperties(view);
		
		return view;
	}
	
	public void applyProperties(View view)
	{
		// Set padding (same all round):
		int paddingPx = ScreenMetrics.ConvertDipToPx(view.getContext(), paddingDip);
		view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
		
		// Set background color:
		view.setBackgroundColor(backgroundColor);
		
		// Set the description used for accessibility support:
		if(description != null)
			view.setContentDescription(description);
		
		// Set view visibility:
		view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}
	
	public void invalidateView()
	{
		view = null;
	}
	
	/**
	 * @param context
	 * @param recycleChildren only relevant to composite subclasses like {@link LayeredItem}
	 * @return
	 */
	protected abstract View createView(Context context, boolean recycleChildren);

	/**
	 * @param visible
	 * @return the Item itself
	 */
	@SuppressWarnings("unchecked")
	public I setVisibility(boolean visible)
	{
		this.visible = visible;
		return (I) this;
	}
	
	public boolean isVisible()
	{
		return visible;
	}
	
	/**
	 * @return the paddingDip
	 */
	public float getPaddingDip()
	{
		return paddingDip;
	}

	/**
	 * @param paddingDip the paddingDip to set
	 * @return the Item itself
	 */
	@SuppressWarnings("unchecked")
	public I setPaddingDip(float paddingDip)
	{
		this.paddingDip = paddingDip;
		return (I) this;
	}

	public boolean hasID()
	{
		return id != null;
	}
	
	public Integer getID()
	{
		return id;
	}

	/**
	 * @return the backgroundColor
	 */
	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	/**
	 * @param backgroundColor the backgroundColor to set
	 * @return the Item itself
	 */
	@SuppressWarnings("unchecked")
	public I setBackgroundColor(int backgroundColor)
	{
		this.backgroundColor = backgroundColor;
		return (I) this;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description the description to set
	 * @return the Item itself
	 */
	@SuppressWarnings("unchecked")
	public I setDescription(String description)
	{
		this.description = description;
		return (I) this;
	}
	
}
