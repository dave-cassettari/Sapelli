/**
 * 
 */
package uk.ac.ucl.excites.storage.model;

import java.io.IOException;
import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import uk.ac.ucl.excites.storage.io.BitInputStream;
import uk.ac.ucl.excites.storage.io.BitOutputStream;
import uk.ac.ucl.excites.storage.util.IntegerRangeMapping;
import uk.ac.ucl.excites.storage.util.TimeUtils;

/**
 * A column to store DateTime objects (cfr: http://joda-time.sourceforge.net)
 * Supports various ways to configure precision and (hence) size
 * 
 * @author mstevens
 */
public class DateTimeColumn extends Column<DateTime>
{
	
	static private final int QUARTER_OF_AN_HOUR_MS = 15 /*minutes*/ * 60 /*seconds*/ * 1000 /*milliseconds*/;
	static private final int TIMEZONE_QH_OFFSET_SIZE = 7; //bits
	
	/**
	 * Returns a DateTimeColumn that can hold Java-style timestamps, i.e. signed 64 bit integers representing
	 * the number of milliseconds since (or to, if negative) the Java/Unix epoch of 1970/01/1 00:00:00 UTC.
	 * All times are treated as UTC (local timezone is NOT kept).
	 * 
	 * @param name
	 * @param optional
	 * @return
	 */
	static public DateTimeColumn JavaMSTime(String name, boolean optional)
	{
		return new DateTimeColumn(name, new DateTime(Long.MIN_VALUE), 64 /*bits*/, true, false, true, optional);
		/* //Alternative:
		 *  return new DateTimeColumn(name, new DateTime(Long.MIN_VALUE), new DateTime(Long.MAX_VALUE), true, false, true, optional);
		 */
	}
	
	/**
	 * Returns a DateTimeColumn that can hold any millisecond-accurate timestamp in the 21st century, including a local timezone reference.
	 * Takes up 49 bits.
	 * 
	 * @param name
	 * @param optional
	 * @return
	 */
	static public DateTimeColumn Century21(String name, boolean optional)
	{
		return new DateTimeColumn(name, new DateTime(2000, 01, 01, 00, 00, 00), new DateTime(2100, 01, 01, 00, 00, 00), true, true, false, optional);
	}
	
	/**
	 * Returns a DateTimeColumn that can hold any second-accurate timestamp in the 21st century, including a local timezone reference
	 * Takes up 39 bits.
	 * 
	 * @param name
	 * @param optional
	 * @return
	 */
	static public DateTimeColumn Century21NoMS(String name, boolean optional)
	{
		return new DateTimeColumn(name, new DateTime(2000, 01, 01, 00, 00, 00), new DateTime(2100, 01, 01, 00, 00, 00), false, true, false, optional);
	}
	
	/**
	 * Returns a DateTimeColumn that only needs 30 bits.
	 * This is achieved by using second-level accurate (instead of millisecond-level), by not storing the
	 * local timezone, and by limiting the value range to a 34 year window starting on 2008/01/01
	 * (taken because the first Android device was released in 2008).
	 * 
	 * @param name
	 * @param optional
	 * @return
	 */
	static public DateTimeColumn Compact(String name, boolean optional)
	{
		return new DateTimeColumn(name, new DateTime(2008, 01, 01, 00, 00, 00), 30 /*bits*/, false, false, false, optional);
	}
	
	protected IntegerRangeMapping timeMapping;
	protected boolean keepMS;
	protected boolean keepLocalTimezone;
	protected DateTimeFormatter formatter;
	protected boolean strict;

	/**
	 * @param name
	 * @param lowBound earliest allowed DateTime (inclusive)
	 * @param highBound latest allowed DateTime (exclusive)
	 * @param keepMS whether to use millisecond-level (true) or second-level (false) accuracy
	 * @param keepLocalTimezone whether or not to remember to local timezone
	 * @param strictHighBound whether highBound date should be strictly respected (true) or not (false; meaning that the column will accept any DateTime that fits in the allocated number of bits)
	 * @param optional
	 */
	public DateTimeColumn(String name, DateTime lowBound, DateTime highBound, boolean keepMS, boolean keepLocalTimezone, boolean strictHighBound, boolean optional)
	{
		this(	name,
				new IntegerRangeMapping(	Math.round(lowBound.getMillis() / (keepMS ? 1 : 1000d)),
											Math.round(highBound.getMillis() / (keepMS ? 1 : 1000d))),
				keepMS,
				keepLocalTimezone,
				strictHighBound,
				optional);
	}
	
	/**
	 * @param name
	 * @param lowBound earliest allowed DateTime (inclusive)
	 * @param the number of bits to use
	 * @param keepMS whether to use millisecond-level (true) or second-level (false) accuracy
	 * @param keepLocalTimezone whether or not to remember to local timezone
	 * @param strictHighBound whether highBound date should be strictly respected (true) or not (false; meaning that the column will accept any DateTime that fits in the allocated number of bits)
	 * @param optional
	 */
	public DateTimeColumn(String name, DateTime lowBound, int sizeBits, boolean keepMS, boolean keepLocalTimezone, boolean strictHighBound, boolean optional)
	{
		this(	name,
				IntegerRangeMapping.ForSize(	Math.round(lowBound.getMillis() / (keepMS ? 1 : 1000d)),
												sizeBits - (keepLocalTimezone ? TIMEZONE_QH_OFFSET_SIZE : 0)),
				keepMS,
				keepLocalTimezone,
				strictHighBound,
				optional);
	}
	
	private DateTimeColumn(String name, IntegerRangeMapping timeMapping, boolean keepMS, boolean keepLocalTimezone, boolean strictHighBound, boolean optional)
	{
		super(name, optional);
		this.timeMapping = timeMapping;
		this.keepMS = keepMS;
		this.keepLocalTimezone = keepLocalTimezone;
		this.formatter = keepMS ? ISODateTimeFormat.dateTime() : ISODateTimeFormat.dateTimeNoMillis();
		this.strict = strictHighBound;
	}
	
	@Override
	protected DateTime parse(String value) throws ParseException, IllegalArgumentException
	{
		return (keepLocalTimezone ? formatter.withOffsetParsed() : formatter).parseDateTime(value);
	}

	@Override
	protected void write(DateTime value, BitOutputStream bitStream) throws IOException
	{
		timeMapping.write(Math.round(value.getMillis() / (keepMS ? 1 : 1000d)), bitStream);
		if(keepLocalTimezone)
			bitStream.write(getTimeZoneOffsetQH(value), TIMEZONE_QH_OFFSET_SIZE, true);
	}

	@Override
	protected DateTime read(BitInputStream bitStream) throws IOException
	{
		long msSinceJavaEpoch = timeMapping.read(bitStream) * (keepMS ? 1 : 1000);
		return new DateTime(msSinceJavaEpoch, keepLocalTimezone ? getDateTimeZoneFor((int) bitStream.readInteger(TIMEZONE_QH_OFFSET_SIZE, true)) : null);
	}
		

	@Override
	protected void validate(DateTime value) throws IllegalArgumentException
	{
		if(!timeMapping.inRange(Math.round(value.getMillis() / (keepMS ? 1 : 1000d)), strict))
			throw new IllegalArgumentException("The given DateTime (" + formatter.print(value) + ") is outside the allowed range (from " + formatter.print(getLowBound()) + " to " + formatter.print(getHighBound()) + ").");
	}

	@Override
	public boolean isVariableSize()
	{
		return false;
	}

	@Override
	public int getSize()
	{
		return timeMapping.getSize() + (keepLocalTimezone ? TIMEZONE_QH_OFFSET_SIZE : 0);
	}
	
	public DateTime getLowBound()
	{
		return new DateTime(timeMapping.getLowBound() * (keepMS ? 1 : 1000));
	}

	public DateTime getHighBound()
	{
		return new DateTime(timeMapping.getHighBound(strict) * (keepMS ? 1 : 1000));
	}
	
	protected int getTimeZoneOffsetQH(DateTime value)
	{
		return value.getZone().toTimeZone().getRawOffset() / QUARTER_OF_AN_HOUR_MS;
	}
	
	protected DateTimeZone getDateTimeZoneFor(int quarterHourOffset)
	{
		return DateTimeZone.forTimeZone(TimeUtils.getTimeZone(quarterHourOffset * QUARTER_OF_AN_HOUR_MS));
	}

	@Override
	protected String toString(DateTime value)
	{
		return (keepLocalTimezone ? formatter.withZone(value.getZone()) : formatter.withZoneUTC()).print(value);
	}
	
}