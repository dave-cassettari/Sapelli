package uk.ac.ucl.excites.transmission.compression;

import uk.ac.ucl.excites.transmission.compression.CompressorFactory.CompressionMode;

public class CompressorResult
{
	
	private CompressionMode mode;
	private byte[] compressedData;
	private float ratio;
	
	/**
	 * @param mode
	 * @param compressedData
	 * @param ratio
	 */
	public CompressorResult(CompressionMode mode, byte[] compressedData, float ratio)
	{
		this.mode = mode;
		this.compressedData = compressedData;
		this.ratio = ratio;
	}

	/**
	 * @return the mode
	 */
	public CompressionMode getMode()
	{
		return mode;
	}

	/**
	 * @return the compressedData
	 */
	public byte[] getCompressedData()
	{
		return compressedData;
	}

	/**
	 * @return the ratio
	 */
	public float getRatio()
	{
		return ratio;
	}
	
	@Override
	public String toString()
	{
		return mode + "-compressed data is " + compressedData.length + " bytes long (" + CompressorFactory.RATIO_FORMAT.format(ratio * 100.0f) + " %)"; 
	}
	
}