/**
 * 
 */
package uk.ac.ucl.excites.transmission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import uk.ac.ucl.excites.storage.model.Column;
import uk.ac.ucl.excites.storage.model.Record;
import uk.ac.ucl.excites.storage.model.Schema;

/**
 * @author mstevens
 * 
 */
public abstract class Transmission
{

	protected DateTime sentAt = null;
	protected DateTime receivedAt = null;
	protected DateTime confirmationSentAt = null;
	protected DateTime confirmationReceivedAt = null;
	
	protected Schema schema;
	protected Set<Column<?>> columnsToFactorOut;
	protected Map<Column<?>, Object> factoredOutValues = null;
	protected List<Record> records;

	public Transmission(Schema schema)
	{
		this(schema, new HashSet<Column<?>>());
	}
	
	public Transmission(Schema schema, Set<Column<?>> columnsToFactorOut)
	{
		this.schema = schema;
		for(Column<?> c : columnsToFactorOut)
			if(schema.getColumnIndex(c) == Schema.UNKNOWN_COLUMN_INDEX)
				throw new IllegalArgumentException(c.toString() + " does not belong to the given schema.");
		this.columnsToFactorOut = columnsToFactorOut;
		this.factoredOutValues = new HashMap<Column<?>, Object>();
		this.records = new ArrayList<Record>();
	}
	
	public abstract boolean addRecord(Record record) throws Exception;
	
	public List<Record> getRecords()
	{
		return records;
	}
	
	public abstract void send() throws Exception;
	
	public boolean isEmpty()
	{
		return records.size() == 0;
	}
	
	public boolean isSent()
	{
		return sentAt != null;
	}

	public boolean isReceived()
	{
		return receivedAt != null;
	}
	
}