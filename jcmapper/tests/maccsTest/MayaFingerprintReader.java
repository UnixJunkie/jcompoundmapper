package maccsTest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MayaFingerprintReader {
	ArrayList<String> fingerprints;
	
	
	public MayaFingerprintReader(String source){
		this.read(source);
		
	}
	
	
	private void read(String source){
		File file = new File(source);
		ArrayList<String> fingerprints = new ArrayList<String>();
		try {
			final BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			boolean firstLine = true;
			while((line = br.readLine())!= null){
				if (firstLine){
					firstLine=false;
					continue;
				}
				
				line=line.replaceAll("\"", "");
				String [] splittedLine = line.split(";");
				int size = splittedLine.length;
				fingerprints.add(splittedLine[--size]);
				
			}
			this.setFingerprints(fingerprints);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}

	public ArrayList<String> getFingerprints() {
		return fingerprints;
	}

	public void setFingerprints(ArrayList<String> fingerprints) {
		this.fingerprints = fingerprints;
	}
	
	public static void main(String args[]){
		MayaFingerprintReader reader = new MayaFingerprintReader("./resources/ACE_MMMACCSKeysFP.csv");
		ArrayList<String> finger = (ArrayList<String>) reader.fingerprints;
		for (int i = 0; i < finger.size(); i++){
			System.out.println(finger.get(i));
			System.out.println(finger.get(i).length());
		}
	}
	
	
}
