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

package uk.ac.ucl.excites.sapelli.storage.db.sql.sqlite.types;

import uk.ac.ucl.excites.sapelli.storage.db.sql.SQLRecordStore.TypeMapping;
import uk.ac.ucl.excites.sapelli.storage.db.sql.sqlite.SQLiteRecordStore;
import uk.ac.ucl.excites.sapelli.storage.model.Schema;
import uk.ac.ucl.excites.sapelli.storage.model.columns.BooleanColumn;
import uk.ac.ucl.excites.sapelli.storage.util.ColumnPointer;


/**
 * @author mstevens
 *
 */
public class SQLiteBooleanColumn extends SQLiteIntegerColumn<Boolean>
{

	static public final String SQLITE_DATA_TYPE = "BOOLEAN";
	
	static private final TypeMapping<Long, Boolean> boolIntMapping = new TypeMapping<Long, Boolean>()
	{
		
		@Override
		public Boolean toSapelliType(Long value)
		{
			return value == 1;
		}
		
		@Override
		public Long toSQLType(Boolean value)
		{
			return value ? 1l : 0l;
		}
	};
	
	/**
	 * @param store
	 * @param constraint
	 * @param sourceSchema
	 * @param sourceColumn
	 */
	public SQLiteBooleanColumn(SQLiteRecordStore store, String constraint, Schema sourceSchema, BooleanColumn sourceColumn)
	{
		super(store, SQLITE_DATA_TYPE, constraint, sourceSchema, sourceColumn, boolIntMapping);
	}

	/**
	 * @param store
	 * @param name
	 * @param constraint
	 * @param sourceColumnPointer
	 */
	public SQLiteBooleanColumn(SQLiteRecordStore store, String name, String constraint, ColumnPointer sourceColumnPointer)
	{
		super(store, name, SQLITE_DATA_TYPE, constraint, sourceColumnPointer, boolIntMapping);
	}
	
}