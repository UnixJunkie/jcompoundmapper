/*
 * Author: Joerg Kurt Wegner, me@joergkurtwegner.eu, @joergkurtwegner (JKW), Steven Osselaer (SO)
 * Copyright 2011, 2012. All rights reserved.
 * 2011-12-14 - JKW - Initial version
 * 2012-01-01 - JKW - Numeric fingerprint properties for allowing fingerprint grouping
 * 2012-10-29 - SO - Reducing code complexity and implementing fingerprint hierarchy
 */
 package de.zbit.jcmapper.io.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.qsar.descriptors.molecular.BCUTDescriptor;
import org.openscience.cdk.qsar.result.DoubleArrayResult;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import de.zbit.jcmapper.fingerprinters.EncodingFingerprint;
import de.zbit.jcmapper.fingerprinters.features.FeatureMap;
import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.io.reader.RandomAccessMDLReader;
import de.zbit.jcmapper.io.writer.feature.SortableFeature;
import de.zbit.jcmapper.tools.progressbar.ProgressBar;
import de.zbit.jcmapper.fingerprinters.topological.features.ECFPFeature;

public class ExporterNumericSQLite implements IExporter {

   @Override
   public void export(
      RandomAccessMDLReader reader,
      EncodingFingerprint fingerprinter,
      String label,
      File outputFile,
      boolean useAromaticFlag
   ) {
      final boolean storeFingerprintSimilarity = true;
      final SQLiteConnection db = new SQLiteConnection(outputFile);
      String fingerprinterName = fingerprinter.getNameOfFingerPrinter();
      fingerprinterName = fingerprinterName.replace(' ', '_');
      fingerprinterName = fingerprinterName.replace('-', '_');
      
      final String tableDictionary = "dictionary" + fingerprinterName;
      final String tableFingerprint = "fingerprint" + fingerprinterName;
      final String tableCompounds = "compounds" + fingerprinterName;
      try {
         db.open(true);
         if(storeFingerprintSimilarity && fingerprinterName.equals("ECFP")){
            db.exec("CREATE TABLE IF NOT EXISTS " + tableDictionary + "(encoding TEXT, fp INTEGER PRIMARY KEY, bc1 REAL, bc2 REAL, bc3 REAL, bc4 REAL, bc5 REAL, bc6 REAL, doublebonds INTEGER, atoms INTEGER, iteration INTEGER, parent INTEGER);");
         } else{
            db.exec("CREATE TABLE IF NOT EXISTS " + tableDictionary + "(encoding TEXT, fp INTEGER PRIMARY KEY);");
         }
         db.exec("CREATE TABLE IF NOT EXISTS " + tableFingerprint + "(compoundnbr INTEGER, fp INTEGER, value REAL);");
         db.exec("CREATE TABLE IF NOT EXISTS " + tableCompounds + "(compoundid TEXT PRIMARY KEY, compoundnbr INTEGER);");
      }
      catch (SQLiteException e) {
         System.out.println(e);
      }
         
      int collisions = 0;
      Long start = System.currentTimeMillis();
      System.out.println("Encoding molecules");
      ProgressBar progressBar = new ProgressBar(reader.getSize());
      SQLiteStatement st = null;
      int cmpdCounter = 1;
      try {
         st = db.prepare("SELECT MAX(compoundnbr) FROM " + tableCompounds);
         st.step();
         cmpdCounter = st.columnInt(0) + 1;
      } 
      catch (SQLiteException e2) {       
         cmpdCounter = 1;
      }

      for (int i = 0; i < reader.getSize(); i++) {
         IAtomContainer mol = reader.getMol(i);
         FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(mol));
         String molLabel = (String) mol.getProperty(label);
         if (molLabel != null) {
            featureMap.setLabel(molLabel);
         } else {
            featureMap.setLabel(ExporterHelper.getMolName(mol) + "_INDEX=" + cmpdCounter);
         }

         String cmpdLabel = featureMap.getLabel();
         String featureString = null;
         int fpInteger = -1;
         double fpValue = 0.0;
         try {
            db.exec("INSERT INTO " + tableCompounds + "(compoundid,compoundnbr) VALUES ('" + cmpdLabel + "','" + cmpdCounter + "');");
            db.exec("BEGIN;");
         } 
         catch (SQLiteException e) {   
            // compound exists, but for which fingerprint routine?
            try {
               st = db.prepare("SELECT compoundnbr FROM " + tableCompounds + " WHERE compoundid = ?");
               st.bind(1, cmpdLabel);
               int cmpdCounter2use=-1;
               while (st.step()) {
                  cmpdCounter2use=st.columnInt(0);
               }
               st = db.prepare("SELECT compoundnbr FROM " + tableFingerprint + " WHERE compoundnbr = ?");
               st.bind(1, cmpdCounter2use);
               while (st.step()) {
                  cmpdCounter2use=st.columnInt(0);
               }
               System.out.println("Compound exists already, skipping " + fingerprinterName + " calculation for '" + cmpdLabel + "' (" + cmpdCounter2use + ")");
               continue;
               //skip this calculation routine and do not add anything to the DB
            } 
            catch (SQLiteException e2) {       
               //All fine compound does not encode this fingerprint
            }
         }
         
         Set<IFeature> featureKeys = featureMap.getKeySet();
         HashMap<Integer, SortableFeature> features = new HashMap<Integer, SortableFeature>();
         for (IFeature feature : featureKeys) {
            int hashCode = feature.hashCode();
            if (features.containsKey(hashCode)) {
               collisions++;
            } else {
               features.put(hashCode, new SortableFeature(feature, useAromaticFlag));
            }
         }
         
         for (Integer hashCode : features.keySet()){
            SortableFeature feature = features.get(hashCode);
            fpInteger = hashCode;
            featureString = feature.getString(useAromaticFlag);
            fpValue = feature.getValue();
            try {
               if(storeFingerprintSimilarity && fingerprinterName.equals("ECFP")){
                  ECFPFeature eFeature = (ECFPFeature)feature.getFeature();
                  IMolecule substructure = eFeature.representedSubstructure();
                  int nAtoms = substructure.getAtomCount();
                  int nBonds = substructure.getBondCount();
                  int iteration = eFeature.getIterationNumber();
                  int parent = eFeature.getParent();
                  int nDoubleBonds = 0;
                  for (int bi = 0; bi < nBonds; bi++) {
                     IBond iBond = substructure.getBond(bi);
                     if (iBond.getOrder() == IBond.Order.DOUBLE) {
                        nDoubleBonds++;
                     }
                  }
                  double fpp[] = getFingerprintProperties(mol, featureString,(ECFPFeature)feature.getFeature());
                  db.exec("INSERT INTO " + tableDictionary + "(encoding, fp, bc1, bc2, bc3, bc4, bc5, bc6,doublebonds,atoms,iteration,parent) VALUES ('" + featureString + "','" + fpInteger + "','" + fpp[0] + "','" + fpp[1] + "','" + fpp[2] + "','" + fpp[3] + "','" + fpp[4] + "','" + fpp[5] + "','"  + nDoubleBonds + "','" + nAtoms + "','" + iteration  + "','" + parent + "');");
               } else{
                  db.exec("INSERT INTO " + tableDictionary + "(encoding, fp) VALUES ('" + featureString + "','" + fpInteger + "');");
               }
            } 
            catch (SQLiteException e) {       
               // skipping duplicates
            }
            try {
               db.exec("INSERT INTO " + tableFingerprint + "(compoundnbr, fp, value) VALUES ('" + cmpdCounter + "','" + fpInteger + "','" + fpValue + "');");
            } 
            catch (SQLiteException e) {       
               System.out.println(e);
            }
         }
         
         try {
            db.exec("COMMIT;");
         } 
         catch (SQLiteException e) {
            System.out.println(e);
         }
         progressBar.DisplayBar();
         cmpdCounter = cmpdCounter + 1;
      }

      Long end = null;
      end = System.currentTimeMillis();
      System.out.println("Time elapsed: " + (end - start) + " ms");
      System.out.println("Collisions:" + collisions);
      db.dispose();
   }

   private double[] getFingerprintProperties(
      IAtomContainer mol,
      String featureString,
      ECFPFeature feature
   ) {
      double fingerprintProperties[] = null;
      IMolecule imol = feature.representedSubstructure();
      ArrayList<Integer> al = new ArrayList<Integer>(); 
      for(IAtom atom: imol.atoms()){
         al.add(new Integer(mol.getAtomNumber(atom)));
      }
      int aindices[] = new int[al.size()];
      for(int io = 0; io<al.size(); io++) {
         aindices[io] = al.get(io).intValue();
      }
      IMolecule imolsub = null;
      try {
         imolsub = (IMolecule) extractSubstructure(mol,aindices);
      } catch (CloneNotSupportedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      BCUTDescriptor bcut = new BCUTDescriptor();
      DoubleArrayResult BCUTvalue = (DoubleArrayResult) ((BCUTDescriptor) bcut).calculate(imolsub).getValue();
      fingerprintProperties = new double[6];
      for (int iii = 0; iii < 6; iii++) {
         fingerprintProperties[iii] = BCUTvalue.get(iii);
      }
      return fingerprintProperties;
   }
   
   // The new CDK provides this AtomContainerManipulator.extractSubstructure method, 
   // so this code snippet might get replaced with the CDK package code in the future
   private static IAtomContainer extractSubstructure(
      IAtomContainer atomContainer,
      int... atomIndices
   ) throws CloneNotSupportedException {
      IAtomContainer substructure = (IAtomContainer) atomContainer.clone();
      int numberOfAtoms = substructure.getAtomCount();
      IAtom[] atoms = new IAtom[numberOfAtoms];
      for (int atomIndex = 0; atomIndex < numberOfAtoms; atomIndex++) {
         atoms[atomIndex] = substructure.getAtom(atomIndex);
      }
      Arrays.sort(atomIndices);
      for (int index = 0; index < numberOfAtoms; index++) {
         if (Arrays.binarySearch(atomIndices, index) < 0) {
            IAtom atom = atoms[index];
            substructure.removeAtomAndConnectedElectronContainers(atom);
         }
     }
     return substructure;
   }
   
}