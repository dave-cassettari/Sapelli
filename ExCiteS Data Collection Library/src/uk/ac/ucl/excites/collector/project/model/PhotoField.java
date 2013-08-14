/**
 * 
 */
package uk.ac.ucl.excites.collector.project.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.excites.collector.project.ui.CollectorUI;
import uk.ac.ucl.excites.util.CollectionUtils;

/**
 * @author Michalis Vitos, mstevens
 *
 */
public class PhotoField extends MediaField
{

	//STATICS--------------------------------------------------------
	static private final String MEDIA_TYPE_JPEG = "PHOTO_JPEG";
	static private final String EXTENSION_JPEG = "jpg";
	
	static public enum FlashMode
	{
		AUTO,
		ON,
		OFF
	}
	
	static public final boolean DEFAULT_USE_NATIVE_APP = false;
	static public final boolean DEFAULT_USE_FRONT_FACING_CAMERA = false;
	static public final FlashMode DEFAULT_FLASH_MODE = FlashMode.AUTO;
	
	//DYNAMICS-------------------------------------------------------
	private boolean useNativeApp;
	private boolean useFrontFacingCamera;
	private FlashMode flashMode;
	
	private String captureButtonImageRelativePath;
	private String approveButtonImageRelativePath;
	private String discardButtonImageRelativePath;
	
	public PhotoField(Form form, String id)
	{
		super(form, id);
		useNativeApp = DEFAULT_USE_NATIVE_APP;
		useFrontFacingCamera = DEFAULT_USE_FRONT_FACING_CAMERA;
		flashMode = DEFAULT_FLASH_MODE;
	}

	@Override
	public void setIn(CollectorUI ui)
	{
		ui.setPhoto(this);
	}

	/**
	 * @return the useFrontFacingCamera
	 */
	public boolean isUseFrontFacingCamera()
	{
		return useFrontFacingCamera;
	}

	/**
	 * @param useFrontFacingCamera the useFrontFacingCamera to set
	 */
	public void setUseFrontFacingCamera(boolean useFrontFacingCamera)
	{
		this.useFrontFacingCamera = useFrontFacingCamera;
	}

	/**
	 * @return the flashMode
	 */
	public FlashMode getFlashMode()
	{
		return flashMode;
	}

	/**
	 * @param flashMode the flashMode to set
	 */
	public void setFlashMode(FlashMode flashMode)
	{
		this.flashMode = flashMode;
	}

	/**
	 * @return the useNativeApp
	 */
	public boolean isUseNativeApp()
	{
		return useNativeApp;
	}

	/**
	 * @param useNativeApp the useNativeApp to set
	 */
	public void setUseNativeApp(boolean useNativeApp)
	{
		this.useNativeApp = useNativeApp;
	}

	/**
	 * @return the captureButtonImageRelativePath
	 */
	public String getCaptureButtonImageRelativePath()
	{
		return captureButtonImageRelativePath;
	}

	/**
	 * @param captureButtonImageRelativePath the captureButtonImageRelativePath to set
	 */
	public void setCaptureButtonImageRelativePath(String captureButtonImageRelativePath)
	{
		this.captureButtonImageRelativePath = captureButtonImageRelativePath;
	}

	/**
	 * @return the approveButtonImageRelativePath
	 */
	public String getApproveButtonImageRelativePath()
	{
		return approveButtonImageRelativePath;
	}

	/**
	 * @param approveButtonImageRelativePath the approveButtonImageRelativePath to set
	 */
	public void setApproveButtonImageRelativePath(String approveButtonImageRelativePath)
	{
		this.approveButtonImageRelativePath = approveButtonImageRelativePath;
	}

	/**
	 * @return the discardButtonImageRelativePath
	 */
	public String getDiscardButtonImageRelativePath()
	{
		return discardButtonImageRelativePath;
	}

	/**
	 * @param discardButtonImageRelativePath the discardButtonImageRelativePath to set
	 */
	public void setDiscardButtonImageRelativePath(String discardButtonImageRelativePath)
	{
		this.discardButtonImageRelativePath = discardButtonImageRelativePath;
	}

	@Override
	public String getMediaType()
	{
		return MEDIA_TYPE_JPEG;
	}

	@Override
	protected String getFileExtension(String mediaType)
	{
		return EXTENSION_JPEG;
	}

	@Override
	public List<File> getFiles(Project project)
	{
		List<File> paths = new ArrayList<File>();
		CollectionUtils.addIgnoreNull(paths, project.getImageFile(captureButtonImageRelativePath));
		CollectionUtils.addIgnoreNull(paths, project.getImageFile(approveButtonImageRelativePath));
		CollectionUtils.addIgnoreNull(paths, project.getImageFile(discardButtonImageRelativePath));
		return paths;
	}
	
}
