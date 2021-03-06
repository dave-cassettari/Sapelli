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

package uk.ac.ucl.excites.sapelli.storage.queries;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ucl.excites.sapelli.storage.model.Record;
import uk.ac.ucl.excites.sapelli.storage.model.Schema;
import uk.ac.ucl.excites.sapelli.storage.queries.constraints.Constraint;
import uk.ac.ucl.excites.sapelli.storage.queries.constraints.ConstraintVisitor;

/**
 * @author mstevens
 *
 */
public class Source extends Constraint
{

	// STATICS ------------------------------------------------------
	static private final boolean BY_INCLUSION = true;
	static private final boolean BY_EXCLUSION = !BY_INCLUSION;
	
	static public final Source ANY = new Source(Collections.<Schema> emptySet(), BY_INCLUSION);
	
	static public Source From(Schema schema)
	{
		if(schema == null)
			return ANY;
		return From(Collections.singleton(schema));
	}
	
	static public Source From(Schema... schemata)
	{
		if(schemata == null || schemata.length == 0)
			return ANY;
		return new Source(new HashSet<Schema>(Arrays.asList(schemata)), BY_INCLUSION);
	}
	
	static public Source From(Collection<Schema> schemata)
	{
		if(schemata == null || schemata.isEmpty())
			return ANY;
		return new Source(schemata, BY_INCLUSION);
	}
	
	static public Source NotFrom(Schema schema)
	{
		if(schema == null)
			return ANY;
		return NotFrom(Collections.singleton(schema));
	}
	
	static public Source NotFrom(Schema... schemata)
	{
		if(schemata == null || schemata.length == 0)
			return ANY;
		return new Source(new HashSet<Schema>(Arrays.asList(schemata)), BY_EXCLUSION);
	}
	
	static public Source NotFrom(Collection<Schema> schemata)
	{
		if(schemata == null || schemata.isEmpty())
			return ANY;
		return new Source(schemata, BY_EXCLUSION);
	}

	// DYNAMICS -----------------------------------------------------
	private final Set<Schema> schemata;
	private final boolean inclusion;
	
	/**
	 * @param schemata
	 * @param inclusion
	 */
	private Source(Collection<Schema> schemata, boolean inclusion)
	{
		this.schemata = schemata instanceof Set<?> ? (Set<Schema>) schemata : new HashSet<Schema>(schemata);
		this.inclusion = inclusion;
	}
	
	public boolean isAny()
	{
		return schemata.isEmpty();
	}

	/**
	 * @return the schemata
	 */
	public Set<Schema> getSchemata()
	{
		return schemata;
	}

	/**
	 * @return the from
	 */
	public boolean isByInclusion()
	{
		return inclusion;
	}
	
	/**
	 * @return the from
	 */
	public boolean isByExclusion()
	{
		return !inclusion;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.sapelli.storage.queries.constraints.Constraint#reduce()
	 */
	@Override
	public Constraint reduce()
	{
		return isAny() ? null : this;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.sapelli.storage.queries.constraints.Constraint#negate()
	 */
	@Override
	public Constraint negate()
	{
		return new Source(schemata, !inclusion).reduce();
	}

	/**
	 * Compares record schema which schemata in the source.
	 * The call to {@link Set#contains(Object)} uses the expensive {@link Schema#equals(Object)} method to compare pairs of schemata, but the search itself is O(log2(n)).
	 * 
	 * @see uk.ac.ucl.excites.sapelli.storage.queries.constraints.Constraint#_isValid(uk.ac.ucl.excites.sapelli.storage.model.Record)
	 */
	@Override
	protected boolean _isValid(Record record)
	{
		return schemata.isEmpty() || inclusion == schemata.contains(record.getSchema());
	}
	
	/**
	 * Alternative isValid() method which allows by-passing the expensive schema comparison and use a cheaper (but less) secure implementation.
	 * When {@code fullSchemaCompare} is false a  cheaper schema pair comparison will be used, but the search itself is O(n).
	 * 
	 * @param record
	 * @param fullSchemaCompare
	 * @return
	 */
	public boolean isValid(Record record, boolean fullSchemaCompare)
	{
		if(fullSchemaCompare)
			return isValid(record); // uses _isValid() above
		else if(record == null)
			return false;
		else if(schemata.isEmpty())
			return true;
		else
		{	// Uses cheaper schema pair comparison, but the search is O(n)
			boolean contains = false;
			for(Schema sourceSchema : schemata)
				if(record.getSchema().equals(sourceSchema, false, false, false))
				{
					contains = true;
					break;
				}
			return inclusion == contains;
		}
	}
	
	@Override
	public void accept(ConstraintVisitor visitor) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Source#accept(ConstraintVisitor) is not implemented");
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj instanceof Source)
		{
			Source other = (Source) obj;
			return this.schemata.equals(other.schemata) && this.inclusion == other.inclusion;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 1;
		hash = 31 * hash + schemata.hashCode();
		hash = 31 * hash + (inclusion ? 0 : 1);
		return hash;
	}
	
}
