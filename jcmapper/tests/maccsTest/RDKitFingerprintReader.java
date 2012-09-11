package maccsTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RDKitFingerprintReader {
	
	ArrayList<String> fingerprints;
	
	public RDKitFingerprintReader(String source){
		this.read(source);
		
	}
	
	private void read(String source){
		File file = new File(source);
		ArrayList<String> fingerprints = new ArrayList<String>();
		try {
			final BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine())!= null){
				line = line.trim();
				line = line.substring(1);
				fingerprints.add(line);
			}
			this.setFingerprints(fingerprints);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getFingerprints() {
		return fingerprints;
	}

	public void setFingerprints(ArrayList<String> fingerprints) {
		this.fingerprints = fingerprints;
	}
	
	

}
