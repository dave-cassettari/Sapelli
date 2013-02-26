/**
 * 
 */
package uk.ac.ucl.excites.collector.project.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import uk.ac.ucl.excites.collector.project.model.Project;
import uk.ac.ucl.excites.collector.project.util.DuplicateException;
import uk.ac.ucl.excites.storage.model.Schema;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;

/**
 * @author mstevens, julia, Michalis Vitos
 * 
 */
public final class DataAccess
{

	//Statics----------------------------------------------
	static private final String TAG = "DATA ACCESS";
	static private final String DATABASE_NAME = "ExCiteS.db4o";
	static private final int PROJECT_ACTIVATION_DEPTH = 500;
	static private DataAccess INSTANCE = null;

	static public DataAccess getInstance(String dbFolderPath)
	{
		if(INSTANCE == null || INSTANCE.dbFolderPath != dbFolderPath)
			INSTANCE = new DataAccess(dbFolderPath);
		return INSTANCE;
	}

	//Dynamics---------------------------------------------
	private String dbFolderPath;
	private ObjectContainer db;
	
	private DataAccess(String dbFolderPath)

	{
		try
		{
			this.dbFolderPath = dbFolderPath;
			openDB(); //open the database!
			Log.d(TAG, "Opened new database connection in file: " + getDbPath());
		}
		catch(Exception e)
		{
			Log.e(TAG, "Unable to open database");
		}
	}
	
	/**
	 * (Re)Opens the database
	 */
	public void openDB()
	{
		if(db != null)
		{
			//Log.w(TAG, "Database is already open.");
			return;
		}
		this.db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getDbPath());
	}
	
	/**
	 * Closes the database. IT can be reopend with openDB().
	 */
	public void closeDB()
	{
		db.close();
		db = null;
		Log.d(TAG, "Closed database connection");
	}

	/**
	 * Returns the file where the DB is saved
	 * 
	 * @return
	 */
	public String getDbPath()
	{
		return dbFolderPath + File.separator + DATABASE_NAME;
	}

	/**
	 * Copy Database File to the destination
	 * 
	 * @param dstFilePath
	 */
	public void copyDBtoSD(String dstFilePath)
	{
		File srcFile = new File(getDbPath());
		File destFile = new File(dstFilePath);
		try
		{
			copyFile(srcFile, destFile);
		}
		catch(IOException e)
		{
			Log.e(TAG, "Unable to copy database: " + e.toString());
		}
	}

	/**
	 * Method to Copy a file
	 * 
	 * @param srcFile
	 * @param dstFile
	 * @throws IOException
	 */
	private void copyFile(File srcFile, File dstFile) throws IOException
	{
		File directory = new File(dstFile.getParentFile().getAbsolutePath());
		directory.mkdirs();

		if(!dstFile.exists())
		{
			dstFile.createNewFile();
		}

		InputStream in = new FileInputStream(srcFile);
		OutputStream out = new FileOutputStream(dstFile);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * @param schema
	 */
	public void store(Schema schema)
	{
		db.store(schema);
	}

	/**
	 * Retrieves all schemata
	 * 
	 * @return
	 */
	public List<Schema> retrieveSchemata()
	{
		List<Schema> result = db.query(Schema.class);
		return result;
	}

	/**
	 * @param id
	 * @param version
	 * @return
	 */
	@SuppressWarnings("serial")
	public Schema retrieveSchema(final int id, final int version)
	{
		@SuppressWarnings("serial")
		ObjectSet<Schema> result = db.query(new Predicate<Schema>()
		{
			public boolean match(Schema schema)
			{
				return schema.getID() == id && schema.getVersion() == version;
			}
		});

		if(result.hasNext())
			return result.next();
		else
			return null;
	}

	/**
	 * @param project
	 */
	public void store(Project project) throws DuplicateException
	{
		if(retrieveProject(project.getName()) != null)
			throw new DuplicateException("There is already a project named \"" + project.getName() + "\"!");
		db.store(project);
	}

	/**
	 * Retrieves all projects
	 * 
	 * @return
	 */
	public List<Project> retrieveProjects()
	{
		final List<Project> result = db.queryByExample(Project.class);
		for(Project p : result)
			db.activate(p, PROJECT_ACTIVATION_DEPTH);
		return result;
	}

	/**
	 * Retrieves specific Project
	 * 
	 * @return null if project was not found
	 */
	public Project retrieveProject(String projectName)
	{
		Project theExample = new Project(projectName);
		final List<Project> result = db.queryByExample(theExample);
		if(result.isEmpty())
			return null;
		else
		{
			Project p = result.get(0);
			db.activate(p, PROJECT_ACTIVATION_DEPTH);
			return p;
		}
	}

	/**
	 * Delete specific project
	 * 
	 * @return
	 */
	public void deleteProject(Project project)
	{
		db.delete(project);
	}
	
}