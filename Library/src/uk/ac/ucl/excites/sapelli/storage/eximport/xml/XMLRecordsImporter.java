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

package uk.ac.ucl.excites.sapelli.storage.eximport.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.SAXException;

import uk.ac.ucl.excites.sapelli.shared.util.StringUtils;
import uk.ac.ucl.excites.sapelli.shared.util.xml.DocumentParser;
import uk.ac.ucl.excites.sapelli.shared.util.xml.XMLAttributes;
import uk.ac.ucl.excites.sapelli.storage.StorageClient;
import uk.ac.ucl.excites.sapelli.storage.eximport.Importer;
import uk.ac.ucl.excites.sapelli.storage.eximport.xml.XMLRecordsExporter.CompositeMode;
import uk.ac.ucl.excites.sapelli.storage.model.Column;
import uk.ac.ucl.excites.sapelli.storage.model.Record;
import uk.ac.ucl.excites.sapelli.storage.model.RecordColumn;
import uk.ac.ucl.excites.sapelli.storage.model.Schema;
import uk.ac.ucl.excites.sapelli.storage.model.VirtualColumn;
import uk.ac.ucl.excites.sapelli.storage.model.columns.LocationColumn;
import uk.ac.ucl.excites.sapelli.storage.model.columns.StringColumn;
import uk.ac.ucl.excites.sapelli.storage.types.Location;
import uk.ac.ucl.excites.sapelli.storage.util.UnknownModelException;

/**
 * XML {@link DocumentParser} that imports {@link Record}s from XML files generated by the current and previous versions of {@link XMLRecordsExporter}.
 * 
 * <p>Supported formats:<br/>
 * 	- v1.x exports, with both v1.x versions of the {@link Location} serialisation format (see {@link Location#parseV1X(String)}).<br/>
 *  - All 3 {@link CompositeMode}s supported by {@link XMLRecordsExporter}: {@link CompositeMode#String}, {@link CompositeMode#Flat} & {@link CompositeMode#Nested} 
 * 
 * @author mstevens
 */
public class XMLRecordsImporter extends DocumentParser implements Importer
{

	protected StorageClient client;
	protected Record currentRecord;
	protected boolean v1xExport;
	protected Stack<Column<?>> columnStack;
	protected List<Record> records;

	public XMLRecordsImporter(StorageClient client)
	{
		super();
		this.client = client;
		columnStack = new Stack<Column<?>>();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.excites.sapelli.storage.eximport.xml.Importer#importFrom(java.io.File)
	 */
	@Override
	public List<Record> importFrom(File xmlFile) throws UnknownModelException, IndexOutOfBoundsException, Exception
	{
		records = new ArrayList<Record>();
		columnStack.clear();
		parse(open(xmlFile));
		return records;
	}

	@Override
	public void startDocument() throws SAXException
	{
		// does nothing (for now)
	}

	@Override
	public void parseStartElement(String uri, String localName, String qName, XMLAttributes attributes) throws UnknownModelException, IndexOutOfBoundsException, SAXException
	{
		// <RecordsExport>
		if(qName.equals(XMLRecordsExporter.TAG_RECORDS_EXPORT))
		{
			// do nothing
		}
		// <Record>
		else if(qName.equals(Record.TAG_RECORD))
		{
			if(currentRecord != null)
				throw new SAXException("Records cannot be nested!");
			
			Schema schema = null;
			try
			{
				if(attributes.contains(Schema.V1X_ATTRIBUTE_SCHEMA_ID))
				{	//This file contains records exported by Sapelli v1.x
					int schemaID = attributes.getRequiredInteger(Record.TAG_RECORD, Schema.V1X_ATTRIBUTE_SCHEMA_ID, "because this is a v1.x record");
					int schemaVersion = attributes.getInteger(Schema.V1X_ATTRIBUTE_SCHEMA_VERSION, Schema.V1X_DEFAULT_SCHEMA_VERSION);
					schema = client.getSchemaV1(schemaID, schemaVersion);
					v1xExport = true;
				}
				else
				{
					long modelID = attributes.getRequiredLong(Record.TAG_RECORD, Schema.ATTRIBUTE_MODEL_ID);
					int modelSchemaNo = attributes.getRequiredInteger(Record.TAG_RECORD, Schema.ATTRIBUTE_MODEL_SCHEMA_NUMBER);
					String schemaName = attributes.getString(Schema.ATTRIBUTE_SCHEMA_NAME, null, false, false);
					schema = client.getSchema(modelID, modelSchemaNo, schemaName);
					v1xExport = false;
				}
			}
			catch(IllegalArgumentException iae)
			{
				new SAXException("This is not a valid Sapelli XML record export (missing model/schema identification attribute(s))");
			}
			if(schema != null)
				currentRecord = schema.createRecord();
			// TODO transmission? sent/received
		}
		// Record columns:
		else if(currentRecord != null)
		{
			Record record = currentRecord;
			for(String colName : qName.split("\\" + RecordColumn.QUALIFIED_NAME_SEPARATOR))
			{
				// Deal with previous (record)column:
				if(!columnStack.isEmpty())
				{
					RecordColumn<?> recCol = ((RecordColumn<?>) columnStack.peek());
					// Create subrecord instance:
					if(!recCol.isValueSet(record))
						recCol.storeObject(record, recCol.getNewRecord());
					// Set subrecord as record:
					record = recCol.retrieveValue(record);
				}
				// Deal with current column:
				Column<?> col = record.getSchema().getColumn(colName, true);
				if(col == null)
					addWarning("Skipping column " + colName + " because it does not exist in " + record.getSchema().toString());
				else if(col instanceof VirtualColumn)
				{
					//addWarning("Skipping virtual column " + colName);
					col = null;
				}
				columnStack.push(col); // even when null! (to deal with unrecognised columns)
				if(col == null)
					break;
			}
		}
		// <?>
		else
			addWarning("Ignored unrecognised or invalidly placed element \"" + qName + "\".");
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		// Reached leaf value string...
		if(currentRecord != null && !columnStack.isEmpty() && columnStack.peek() != null)
		{
			// Get the column at the top of the columnStack:
			Column<?> column = columnStack.peek();
			
			// Get the (sub)record corresponding to the column:
			Record record = currentRecord;
			for(Column<?> col : columnStack)
				if(col instanceof RecordColumn && col != column)
					record = ((RecordColumn<?>) col).retrieveValue(record);
			
			// Get string representation of column value:
			String valueString = new String(ch, start, length);
			// String checks:
			if(!(column instanceof StringColumn)) // Note: We may want to try and do this without so many instanceof checks...
			{
				valueString = valueString.trim();
				if(valueString.isEmpty())
					return; // unless the column is a StringColumn the empty String should be treated as null so there is no value to set. We return here to avoid errors when setting null values on non-optional columns.	
			}
			// else --> don't trim here & allow empty String (because the values of StringColumns are not quoted in XML exports and empty string is not the same as null in this case)!
			
			// Parse & store value:
			try
			{
				if(v1xExport && column instanceof LocationColumn)
					// Backwards compatibility with old location formats:
					column.storeObject(record, Location.parseV1X(valueString));
				else
				{
					if(column instanceof StringColumn)
						/* The values of StringColumns exported to XML are not quoted, so we store them 'as is'.
						 * Parsing them with parseAndStoreValue() would cause empty string to be treated as null
						 * and non-empty String would cause and Exception to be thrown because parseAndStoreValue()
						 * would invoke StringColumn#parse() which expects a quoted String.
						 * 
						 * See XMLRecordExporter#visit(ColumnPointer) for the corresponding export logic.
						 * Note that there we used (column.getType() == String.class) instead of an instanceof
						 * check. This is to deal with virtual columns with a StringColumn as target. Here we
						 * don't need to bother with that because virtual columns are always skipped upon
						 * importing (see above). */
						column.storeObject(record, valueString);
					else
						column.parseAndStoreValue(record, valueString);
				}
			}
			catch(Exception e)
			{
				addWarning(e.getClass().getName() + " upon parsing value (" + valueString + ") for " + column.toString() + " \"" + column.getName() + "\"" + (e.getMessage() != null ? ", cause: " + e.getMessage() : "."));
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		// </Record>
		if(qName.equals(Record.TAG_RECORD))
		{
			records.add(currentRecord);
			currentRecord = null;
		}
		// Record columns:
		else if(currentRecord != null && !columnStack.isEmpty())
		{
			for(int c = 0; c <= StringUtils.countOccurances(qName, RecordColumn.QUALIFIED_NAME_SEPARATOR); c++)
				columnStack.pop();
		}
	}

	@Override
	public void endDocument() throws SAXException
	{
		// does nothing for now
	}

}
