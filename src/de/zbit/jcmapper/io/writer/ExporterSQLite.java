/*
 * Author: Joerg Kurt Wegner, me@joergkurtwegner.eu
 * 2011-08
 */
package de.zbit.jcmapper.io.writer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import de.zbit.jcmapper.fingerprinters.EncodingFingerprint;
import de.zbit.jcmapper.fingerprinters.features.FeatureMap;
import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.io.reader.RandomAccessMDLReader;
import de.zbit.jcmapper.io.writer.feature.SortableFeature;
import de.zbit.jcmapper.tools.progressbar.ProgressBar;

public class ExporterSQLite implements IExporter {
 
	@Override
	public void export(RandomAccessMDLReader reader, EncodingFingerprint fingerprinter, String label, File outputFile) {
		// WARNING: This is extremely slow, so beware or try doing some parts in memory
		// For now this scales to any size, since it operates fully on SQL queries, but
		// this slows down things.
		final boolean createPivotedTable= false;
		
		final SQLiteConnection db = new SQLiteConnection(outputFile);
		//avoid special characters in table name
		String fingerprinterName=fingerprinter.getNameOfFingerPrinter();
		fingerprinterName=fingerprinterName.replace(' ', '_');
		fingerprinterName=fingerprinterName.replace('-', '_');
		
		final String tableDictionary="dictionary"+fingerprinterName;
		final String tableFingerprint="fingerprint"+fingerprinterName;
		final String tableCompounds="compounds"+fingerprinterName;
		final String tableFingerprintPivoted="fingerprintpivot"+fingerprinterName;
		try {
			db.open(true);
			//dictionary
			db.exec("DROP TABLE IF EXISTS "+tableDictionary);
			db.exec("CREATE TABLE "+tableDictionary+"(encoding TEXT PRIMARY KEY, fp INTEGER);");
			db.exec("CREATE UNIQUE INDEX Index_"+tableDictionary+"_fp ON "+tableDictionary+"(fp);");
			//fingerprint
			db.exec("DROP TABLE IF EXISTS "+tableFingerprint);
			db.exec("CREATE TABLE "+tableFingerprint+"(compoundid TEXT, fp INTEGER);");
			//compounds
			db.exec("DROP TABLE IF EXISTS "+tableCompounds);
			db.exec("CREATE TABLE "+tableCompounds+"(compoundid TEXT PRIMARY KEY);");
			//fingerprint pivoted
			if(createPivotedTable){
				//just an initial table, the rest will be created dynamically
				db.exec("DROP TABLE IF EXISTS "+tableFingerprintPivoted);
				db.exec("CREATE TABLE "+tableFingerprintPivoted+"(compoundid TEXT PRIMARY KEY);");
			}
		}
		catch (SQLiteException e) 
		{
			// create database(s)
			System.out.println(e);
		}
			
		int collisions = 0;
		java.util.Locale.setDefault(java.util.Locale.ENGLISH);
		DecimalFormat df = new DecimalFormat();

		Long start = System.currentTimeMillis();
		
		///////////////////////
		//Encoding molecules
		System.out.println("Encoding molecules");
		ProgressBar progressBar = new ProgressBar(reader.getSize());
		int fpCounter=1;
		for (int i = 0; i < reader.getSize(); i++) {
			IAtomContainer mol = reader.getMol(i);
			FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(mol));
			String molLabel = (String) mol.getProperty(label);
			if (molLabel != null) {
				featureMap.setLabel(molLabel);
			} else {
				featureMap.setLabel(ExporterHelper.getMolName(mol) + "_INDEX=" + i);
			}

			//IFeature[] keys = (IFeature[]) featureMap.getKeySet().toArray();
			Set<IFeature> keys =   featureMap.getKeySet();
			ArrayList<SortableFeature> Features = new ArrayList<SortableFeature>();

			for (IFeature feature : keys) {
				if (feature instanceof IFeature) {
					Features.add(new SortableFeature(feature));
				}
			}

			String cmpdLabel=featureMap.getLabel();
			String featureString=null;
			int fpInteger=-1;
			int lastUsedIndex = 0;
			Collections.sort(Features);
			try {
				db.exec("INSERT INTO "+tableCompounds+"(compoundid) VALUES ('"+cmpdLabel+"');");
				if(createPivotedTable){
					db.exec("INSERT INTO "+tableFingerprintPivoted+"(compoundid) VALUES ('"+cmpdLabel+"');");
				}
				db.exec("BEGIN;");
			} 
			catch (SQLiteException e) 
			{       
				System.out.println(e);
			}
			String currentFeatureString=null;
			String previousFeatureString=null;
			for (SortableFeature feature : Features) {
				if (feature.getHash() == lastUsedIndex) {
					collisions++;
					continue;
				}
				fpInteger=fpCounter;
				featureString=feature.getString() + ":" + df.format(feature.getValue());
				//skip processing redundant features, aka do not count them up
				currentFeatureString=featureString;
				if(previousFeatureString!=null && currentFeatureString!=null) {
					if (previousFeatureString.equals(currentFeatureString)) {
						//System.out.println(previousFeatureString.equals(currentFeatureString)+" "+previousFeatureString+" "+currentFeatureString);
						//skip redundant features
						continue;
					}
				}
				try {
					db.exec("INSERT INTO "+tableDictionary+"(encoding, fp) VALUES ('"+featureString+"','"+fpInteger+"');");
					//only increment when no error, aka no duplication
					fpCounter++;
				} 
				catch (SQLiteException e) 
				{       
					// skipping duplicates
					SQLiteStatement st = null;
					try {
						//re-assign fpString to a matching one created previously 
						st=db.prepare("SELECT fp FROM "+tableDictionary+" WHERE encoding = ?");
						st.bind(1, featureString);
						while (st.step()) {
							fpInteger=st.columnInt(0);
						}
					} 
					catch (SQLiteException e2) 
					{       
						System.out.println(e2);
					}
					//System.out.println("From previous: encoding='"+featureString+"', fp='"+fpString+"'");
				}
				//System.out.println("Details: encoding='"+featureString+"', fp='"+fpString+"'");
				try {
					db.exec("INSERT INTO "+tableFingerprint+"(compoundid, fp) VALUES ('"+cmpdLabel+"','"+fpInteger+"');");
				} 
				catch (SQLiteException e) 
				{       
					System.out.println(e);
				}
				lastUsedIndex = feature.getHash();
				previousFeatureString=currentFeatureString;
			}
			try {
				db.exec("COMMIT;");
			} 
			catch (SQLiteException e) 
			{       
				System.out.println(e);
			}
			progressBar.DisplayBar();
		}

		Long end = null;
		///////////////////////
		//create pivoted fingerprint table
		if(createPivotedTable){
			end = System.currentTimeMillis();
			System.out.println("Time elapsed: " + (end - start) + " ms");
			System.out.println("Creating fingerprint pivot table");
			try {
				//get all fingerprints, add columns to the pivot table, and initialize them with '0' 
				SQLiteStatement st=db.prepare("SELECT fp FROM "+tableDictionary);
				String fpString=null;
				String fpHead="fp";
				while (st.step()) {
					fpString=fpHead+st.columnInt(0);
					db.exec("ALTER TABLE "+tableFingerprintPivoted+" ADD COLUMN "+fpString+" INTEGER");
				}
				//now initialize with OFF bits to '0' 
				db.exec("BEGIN;");
				st=db.prepare("SELECT fp FROM "+tableDictionary);
				while (st.step()) {
					fpString=fpHead+st.columnString(0);
					SQLiteStatement st2=db.prepare("SELECT compoundid FROM "+tableCompounds);
					String cmpdId=null;
					while (st2.step()) {
						cmpdId=st2.columnString(0);
						db.exec("UPDATE "+tableFingerprintPivoted+" SET "+fpString+" = 0 WHERE compoundid='"+cmpdId+"'");
					}
				}
				db.exec("COMMIT;");
				//now set ON bits to '1' 
				db.exec("BEGIN;");
				st=db.prepare("SELECT compoundid, fp FROM "+tableFingerprint);
				String cmpdId=null;
				while (st.step()) {
					cmpdId=st.columnString(0);
					fpString=fpHead+st.columnString(1);
					db.exec("UPDATE "+tableFingerprintPivoted+" SET "+fpString+" = 1 WHERE compoundid='"+cmpdId+"'");
				}
				db.exec("COMMIT;");
			} 
			catch (SQLiteException e) 
			{       
				System.out.println(e);
			}
		}
		///////////////////////
		//creating table indices
		end = System.currentTimeMillis();
		System.out.println("Time elapsed: " + (end - start) + " ms");
		System.out.println("Creating table indices");
		try {
			db.exec("CREATE INDEX Index_"+tableFingerprint+"_compoundid ON "+tableFingerprint+"(compoundid);");
			db.exec("CREATE INDEX Index_"+tableFingerprint+"_fp ON "+tableFingerprint+"(fp);");
		}
		catch (SQLiteException e) 
		{       
			System.out.println(e);
		}
		end = System.currentTimeMillis();
		System.out.println("Time elapsed: " + (end - start) + " ms");
		System.out.println("Collisions:" + collisions);
		db.dispose();
	}

}