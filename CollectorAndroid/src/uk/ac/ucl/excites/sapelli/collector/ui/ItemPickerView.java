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

import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.excites.sapelli.collector.R;
import uk.ac.ucl.excites.sapelli.collector.ui.items.Item;
import uk.ac.ucl.excites.sapelli.collector.ui.items.TextItem;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

/**
 * @author Julia, Michalis Vitos, mstevens
 * 
 */
public class ItemPickerView extends GridView
{

	// Statics:
	static public final int DEFAULT_HEIGHT_PX = 100;
	static public final int DEFAULT_WIDTH_PX = 100;
	
	static private final String TAG = "PickerView";
	
	// Dynamics:
	protected LayoutParams itemLayoutParams = new LayoutParams(DEFAULT_WIDTH_PX, DEFAULT_HEIGHT_PX);
	protected boolean recycleViews;
	
	public ItemPickerView(Context context)
	{
		this(context, true); // allow recycled views by default
	}
	
	public ItemPickerView(Context context, boolean recycleViews)
	{
		super(context);
		this.recycleViews = recycleViews;
		
		// Set adapter:
		setAdapter(new PickerAdapter());
		
		// This is needed to hide the border when an picker item is pressed and to calculate the borders more appropriately
		setSelector(R.drawable.projectlist_selector);
	}
	
	public PickerAdapter getAdapter()
	{
		return (PickerAdapter) super.getAdapter();
	}
	
	/**
	 * @return the recycleViews
	 */
	public boolean isRecycleViews()
	{
		return recycleViews;
	}

	/**
	 * @param recycleViews the recycleViews to set
	 */
	public void setRecycleViews(boolean recycleViews)
	{
		this.recycleViews = recycleViews;
	}

	/**
	 * @param widthPx the widthPx to set
	 * @param heightPx the heightPx to set
	 */
	public void setItemDimensionsPx(int widthPx, int heightPx)
	{
		this.itemLayoutParams = new LayoutParams(widthPx, heightPx);
	}
	
	/**
	 * @author mstevens, Julia
	 *
	 */
	public class PickerAdapter extends BaseAdapter
	{

		private final List<Item<?>> items;
		
		public PickerAdapter()
		{
			this.items = new ArrayList<Item<?>>();
		}
		
		public void addItem(Item<?> item)
		{
			items.add(item);
			notifyDataSetChanged();
		}
		
		public void clear()
		{
			items.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount()
		{
			return items.size();
		}

		@Override
		public Item<?> getItem(int position)
		{
			if(position < 0 || position >= items.size())
				return null; // prevent IndexOutOfBoundsException (should never happen)
			return items.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			Item<?> item = getItem(position);
			if(item != null && item.hasID())
				return (long) item.getID();
			return position;
		}

		/**
		 * Create a new ImageView for each item referenced by the Adapter
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// Get item:
			Item<?> item = getItem(position); // should never be null, but we check anyway below
			
			// Try recycling convertView:
			if(item != null && recycleViews && convertView != null && convertView.getId() == getItemId(position))
			{
				item.applyProperties(convertView); // in case item properties (e.g. visibility) have changed in the meantime
				return convertView;
			}
			// Get view from item:
			else
			{
				if(item == null) // this really shouldn't ever happen ;-)
				{
					item = new TextItem(getContext().getString(R.string.error)).setBackgroundColor(Color.RED);
					Log.e(TAG, "getView(): got null item at position " + position + "!");
				}
					
				// Create the view:
				View view = item.getView(getContext(), recycleViews);
				
				// Set id:
				view.setId((int) getItemId(position));
				
				// Set layout params (width & height):
				view.setLayoutParams(itemLayoutParams);

				// Return view:
				return view;
			}
		}
	}
	
}
