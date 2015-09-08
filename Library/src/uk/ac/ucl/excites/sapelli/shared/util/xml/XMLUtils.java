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

package uk.ac.ucl.excites.sapelli.shared.util.xml;

import uk.ac.ucl.excites.sapelli.shared.util.StringUtils;


/**
 * A class with helpful methods for dealing with XML
 * 
 * @author mstevens
 * 
 */
public class XMLUtils
{
	
	static public String header(String encoding, boolean v11)
	{
		return "<?xml version=\"1." + (v11 ? "1" : "0") + "\" encoding=\"" + encoding + "\"?>";
	}
	
	static public String header()
	{
		return header("UTF-8", false);
	}
	
	/**
	 * Returns an XML comment string with the given text and the given number of
	 * tabs in front
	 * 
	 * @param text
	 * @return xml comment String
	 */
	static public String comment(String text)
	{
		return comment(text, 0);
	}
	
	/**
	 * Returns an XML comment string with the given text and the given number of
	 * tabs in front
	 * 
	 * @param text
	 * @param tabs  number of tabs to insert before comment start
	 * @return xml comment String
	 */
	static public String comment(String text, int tabs)
	{
		return StringUtils.addTabsFront("<!-- " + escapeCharacters(text) + " -->", tabs);
	}

	/**
	 * Replaces reserved XML characters with escapes
	 * 
	 * @param input
	 *            a String to process
	 * @return the same String but with reserved XML characters escaped
	 */
	static public String escapeCharacters(String input)
	{
		if(input == null)
			return input;
		input.replace("&", "&amp;");
		input.replace("<", "&lt;");
		input.replace(">", "&gt;");
		input.replace("\"", "&quot;");
		input.replace("'", "&apos;");
		return input;
	}
	
	/**
	 * Uses {@link XMLChar} & {@link XML11Char}, taken from Apache Xerces2 Java Parser (currently included code files are taken from v2.11.0),
	 * which is licensed under Apache License, Version 2.0.
	 * 
	 * @param tagName
	 * @param v11 whether XML v1.1 (true) or v1.0 is used
	 * @return
	 * 
	 * @see <a href="https://xerces.apache.org/xerces2-j/javadocs/xerces2/org/apache/xerces/util/XMLChar.html#isValidName(java.lang.String)">org.apache.xerces.util.XMLChar#isValidName(java.lang.String)</a>
	 * @see <a href="https://xerces.apache.org/xerces2-j/javadocs/xerces2/org/apache/xerces/util/XML11Char.html#isValidName(java.lang.String)">org.apache.xerces.util.XML11Char#isXML11ValidName(java.lang.String)</a>
	 */
	static public boolean isValidName(String tagName, boolean v11)
	{
		if (v11)
		{
			return XML11Char.isXML11ValidName(tagName);
		}

		return XMLChar.isValidName(tagName);
	}
}
