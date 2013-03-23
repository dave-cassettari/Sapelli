/**
 * 
 */
package uk.ac.ucl.excites.transmission.sms.text;

import org.joda.time.DateTime;

import uk.ac.ucl.excites.transmission.sms.Message;
import uk.ac.ucl.excites.transmission.sms.SMSAgent;
import uk.ac.ucl.excites.transmission.sms.SMSTransmission;

/**
 * @author mstevens
 *
 */
public class TextMessage extends Message
{

	public static final int MAX_SINGLE_PART_LENGTH = 160;
	
	private String content;
	private boolean multiPart = false;
	
	/**
	 * To be called on the sending side.
	 * 
	 * @param receiver
	 * @param transmission
	 * @param partNumber
	 * @param totalParts
	 * @param payload
	 */
	public TextMessage(SMSAgent receiver, SMSTransmission transmission, int partNumber, int totalParts, byte[] payload)
	{
		super(receiver, transmission, partNumber, totalParts);
		//TODO encode bytes in 7bit SMS text format and then call setText()
	}

	/**
	 * To be called on the receiving side.
	 * 
	 * @param sender
	 * @param text
	 */
	public TextMessage(SMSAgent sender, String text)
	{
		super(sender, new DateTime() /*received NOW*/);
		this.content = text;
	}
	
	protected void setText(String text)
	{
		this.content = text;
		if(text.length() > MAX_SINGLE_PART_LENGTH)
			multiPart = true;
	}
	
	public String getText()
	{
		return content;
	}
	
	public boolean isMultiPart()
	{
		return multiPart;
	}
	
	@Override
	public byte[] getPayload()
	{
		//TODO decode to bytes
		return new byte[140];
	}

	@Override
	public void send()
	{
		transmission.getSMSService().send(this);
	}

}
