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

package uk.ac.ucl.excites.sapelli.collector;

import java.io.File;

import uk.ac.ucl.excites.sapelli.collector.db.CollectorRecordStoreUpgrader;
import uk.ac.ucl.excites.sapelli.collector.db.CollectorPreferences;
import uk.ac.ucl.excites.sapelli.collector.db.ProjectRecordStore;
import uk.ac.ucl.excites.sapelli.collector.db.ProjectStore;
import uk.ac.ucl.excites.sapelli.collector.io.AndroidFileStorageProvider;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageException;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageProvider;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageRemovedException;
import uk.ac.ucl.excites.sapelli.collector.io.FileStorageUnavailableException;
import uk.ac.ucl.excites.sapelli.collector.util.CrashReporter;
import uk.ac.ucl.excites.sapelli.collector.util.ProjectRunHelpers;
import uk.ac.ucl.excites.sapelli.shared.db.StoreHandle;
import uk.ac.ucl.excites.sapelli.shared.db.exceptions.DBException;
import uk.ac.ucl.excites.sapelli.shared.io.FileHelpers;
import uk.ac.ucl.excites.sapelli.shared.util.TimeUtils;
import uk.ac.ucl.excites.sapelli.shared.util.android.Debug;
import uk.ac.ucl.excites.sapelli.shared.util.android.DeviceControl;
import uk.ac.ucl.excites.sapelli.storage.db.RecordStore;
import uk.ac.ucl.excites.sapelli.storage.db.sql.sqlite.android.AndroidSQLiteRecordStore;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;

import com.crashlytics.android.Crashlytics;

/**
 * Application App to keep the db4o object throughout the life-cycle of the Collector
 * 
 * @author Michalis Vitos, mstevens
 * 
 */
public class CollectorApp extends Application
{

	// STATICS------------------------------------------------------------
	static protected final String TAG = "CollectorApp";
	
	static private final String DATABASE_BASENAME = "Sapelli";
	static private final String DEMO_PREFIX = "Demo_";
	
	static private final String CRASHLYTICS_VERSION_INFO = "VERSION_INFO";
	static private final String CRASHLYTICS_BUILD_INFO = "BUILD_INFO";
	static public final String CRASHLYTICS_DEVICE_ID_CRC32 = "SAPELLI_DEVICE_ID_CRC32";
	static public final String CRASHLYTICS_DEVICE_ID_MD5 = "SAPELLI_DEVICE_ID_MD5";
	static public final String PROPERTY_LAST_PROJECT = "SAPELLI_LAST_RUNNING_PROJECT"; // used as a System property as well as on Crashlytics
	public static enum StorageStatus
	{
		UNKNOWN, STORAGE_OK, STORAGE_UNAVAILABLE, STORAGE_REMOVED
	}
	
	// DYNAMICS-----------------------------------------------------------
	private BuildInfo buildInfo;
	
	private CollectorPreferences preferences;
	
	public final CollectorClient collectorClient = new AndroidCollectorClient();
	
	// Files storage:
	private FileStorageProvider fileStorageProvider;
	private FileStorageException fileStorageException = null;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// Build info:
		this.buildInfo = BuildInfo.GetInstance(getApplicationContext());
		
		Debug.d("CollectorApp started.\nBuild info:\n" + buildInfo.getAllInfo());

		// Start Crashlytics for bugs reporting
		if(!BuildConfig.DEBUG)
		{
			Crashlytics.start(this);
			Crashlytics.setString(CRASHLYTICS_VERSION_INFO, buildInfo.getNameAndVersion() + " [" + buildInfo.getExtraVersionInfo() + "]");
			Crashlytics.setString(CRASHLYTICS_BUILD_INFO, buildInfo.getBuildInfo());
		}
		
		// Get collector preferences:
		preferences = new CollectorPreferences(getApplicationContext());
		
		// Initialise file storage:
		try {
			this.fileStorageProvider = initialiseFileStorage(); // throws FileStorageException
		}
		catch(FileStorageException fse)
		{
			this.fileStorageException = fse; // postpone throwing until getFileStorageProvider() is called!
		}
		
		// Set up a CrashReporter (will use dumps folder):
		if(fileStorageProvider != null)
			Thread.setDefaultUncaughtExceptionHandler(new CrashReporter(fileStorageProvider, getResources().getString(R.string.app_name)));

		// Create shortcut to Sapelli Collector on Home Screen:
		if(preferences.isFirstInstallation())
		{
			// Create shortcut
			ProjectRunHelpers.createCollectorShortcut(getApplicationContext());
			// Set first installation to false
			preferences.setFirstInstallation(false);
		}
	}
	
	/**
	 * @return
	 * @throws FileStorageException
	 */
	private FileStorageProvider initialiseFileStorage() throws FileStorageException
	{
		File sapelliFolder = null;
		
		// Try to get Sapelli folder path from preferences:
		try
		{
			sapelliFolder = new File(preferences.getSapelliFolderPath());
		}
		catch(NullPointerException npe) {}

		// Did we get the folder path from preferences? ...
		if(sapelliFolder == null)
		{	// No: first installation or reset
			
			// Find appropriate files dir (using application-specific folder, which is removed upon app uninstall!):
			File[] paths = DeviceControl.getExternalFilesDirs(this, null);
			if(paths != null && paths.length != 0)
			{
				// We count backwards because we prefer secondary external storage (which is likely to be on an SD card rather unremovable memory)
				for(int p = paths.length - 1; p >= 0; p--)
					if(isMountedReadableWritableDir(paths[p]))
					{
						sapelliFolder = paths[p];
						break;
					}
			}

			// Do we have a path?
			if(sapelliFolder != null)
				// Yes: store it in the preferences:
				preferences.setSapelliFolder(sapelliFolder.getAbsolutePath());
			else
				// No :-(
				throw new FileStorageUnavailableException();
		}
		else
		{	// Yes, we got path from preferences, check if it is available ...
			if(!isMountedReadableWritableDir(sapelliFolder)) // (will also attempt to create the directory if it doesn't exist)
				// No :-(
				throw new FileStorageRemovedException(sapelliFolder.getAbsolutePath());
		}

		// If we get here this means we have a non-null sapelliFolder object representing an accessible path...
		
		// Try to get the Android Downloads folder...
		File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		if(!isMountedReadableWritableDir(downloadsFolder)) // check if we can access it (will also attempt to create the directory if it doesn't exist)
			// No :-(
			throw new FileStorageException("Cannot access downloads folder: " + downloadsFolder.getAbsolutePath());
		
		// Return path provider
		return new AndroidFileStorageProvider(sapelliFolder, downloadsFolder); // Android specific subclass of FileStorageProvider, which generates .nomedia files
	}
	
	/**
	 * Returns a FileStorageProvider when file storage is available or throws an FileStorageUnavailableException or an FileStorageRemovedException if it is not
	 * 
	 * @return a PathProvider object
	 * @throws FileStorageException
	 */
	public FileStorageProvider getFileStorageProvider() throws FileStorageException
	{
		if(fileStorageProvider != null && fileStorageException == null)
			return fileStorageProvider;
		if(fileStorageException != null)
			throw fileStorageException;
		else
			throw new FileStorageUnavailableException(); // this shouldn't happen
	}
	
	/**
	 * @return the preferences
	 */
	public CollectorPreferences getPreferences()
	{
		return preferences;
	}

	/**
	 * Check if a directory is on a mounted storage and writable/readable
	 * 
	 * @param dir
	 * @return
	 * @throws FileStorageException
	 */
	private boolean isMountedReadableWritableDir(File dir) throws FileStorageException
	{
		try
		{
			return	// Null check:
					(dir != null)
					// Try to create the directory if it is not there
					&& FileHelpers.createDirectory(dir)
					/* Check storage state, accepting both MEDIA_MOUNTED and MEDIA_UNKNOWN.
					 * 	The MEDIA_UNKNOWN state occurs when a path isn't backed by known storage media; e.g. the SD Card on
					 * the Samsung Xcover 2 (the detection of which we have to force in DeviceControl#getExternalFilesDirs()). */
					&& (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(dir)) || EnvironmentCompat.MEDIA_UNKNOWN.equals(EnvironmentCompat.getStorageState(dir)))
					// Check whether we have read & write access to the directory:
					&& FileHelpers.isReadableWritableDirectory(dir);
		}
		catch(Exception e)
		{
			throw new FileStorageException("Unable to create or determine status of directory: " + (dir != null ? dir.getAbsolutePath() : "null"), e);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Debug.d(newConfig.toString());
	}
	
	public BuildInfo getBuildInfo()
	{
		return buildInfo;
	}

	/**
	 * Returns a prefix to be used on storage identifiers (DB4O filenames, SharedPref's names, etc.) when in demo mode
	 * (if not in demo mode the prefix is empty).
	 * The goal is to separate demo-mode storage from non-demo-mode installations and previous demo installations.
	 * 
	 * @return
	 */
	public String getDemoPrefix()
	{
		return (buildInfo.isDemoBuild() ? DEMO_PREFIX + FileHelpers.makeValidFileName(TimeUtils.getTimestampForFileName(buildInfo.getTimeStamp())) : "");
	}

	@Override
	public void onLowMemory()
	{
		super.onLowMemory();
		Debug.d("onLowMemory() called!");
	}

	@Override
	public void onTerminate()
	{
		super.onTerminate();
		// This method is for use in emulated process environments. It will never be called on
		// a production Android device, where processes are removed by simply killing them; no
		// user code (including this callback) is executed when doing so.
		Debug.d("Should never be called!");
	}

	/**
	 * @return handles for all Stores that need to be backed-up
	 */
	public StoreHandle<?>[] getStoreHandlesForBackup()
	{
		return new StoreHandle[] { 	collectorClient.recordStoreHandle,
									// TODO add handle(s) for transmission store(s) here
									collectorClient.projectStoreHandle };
	}
	
	/**
	 * @author mstevens
	 *
	 */
	private class AndroidCollectorClient extends CollectorClient
	{

		@Override
		protected RecordStore createRecordStore() throws DBException
		{
			return new AndroidSQLiteRecordStore(this, CollectorApp.this, getFileStorageProvider().getDBFolder(true), getDemoPrefix() /*will be "" if not in demo mode*/ + DATABASE_BASENAME, CURRENT_COLLECTOR_RECORDSTORE_VERSION, new CollectorRecordStoreUpgrader(this));
			//return new DB4ORecordStore(this, getFileStorageProvider().getDBFolder(true), getDemoPrefix() /*will be "" if not in demo mode*/ + DATABASE_BASENAME);
		}

		@Override
		protected ProjectStore createProjectStore() throws DBException
		{
			return new ProjectRecordStore(this, getFileStorageProvider());
			//return new PrefProjectStore(CollectorApp.this, getFileStorageProvider(), getDemoPrefix());
			//return new DB4OProjectStore(getFileStorageProvider().getDBFolder(true), getDemoPrefix() /*will be "" if not in demo mode*/ + "ProjectStore");
		}

	}
	
}
