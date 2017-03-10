package data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class NamePopularityPair {
	public String name;
	public double popularity;
	
	public NamePopularityPair (String name, double popularity) {
		this.name = name;
		this.popularity = popularity;
	}
}

public class NameData {
	public static Set<String> titles = new HashSet<>();
	public static Map<String, Set<NamePopularityPair>> nameToNicNames = new HashMap<>();
	public static Map<String, Set<NamePopularityPair>> nicnameToNames = new HashMap<>();
	
	
	public NameData(BufferedReader titleFile, BufferedReader nicnameFile) {
		
		// creates title set
		try {
			String currLine = titleFile.readLine();
			while (currLine != null) {
				titles.add(currLine);						// adds to set of titles
				currLine = titleFile.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error in titleFile parsing");
			System.out.println(e.getMessage());
		}
		
		// creates nic name maps
		try {
			String currLine = nicnameFile.readLine();
			while (currLine != null) {
				String[] line = currLine.split("\\s+");		// splits the string into, 				
				String name = line[0];						// the formal name
				String nicname = line[1];					// the nicname
				double popularity;							// the popularity
				popularity = Double.parseDouble(line[2]);
				NamePopularityPair nicnamePP = new NamePopularityPair(nicname, popularity);
				NamePopularityPair namePP = new NamePopularityPair(name, popularity);
				
				// adds the nicname pair to the map of names to nicnames
				if (nameToNicNames.containsKey(name)) {
					nameToNicNames.get(name).add(nicnamePP);
				}
				else {
					Set<NamePopularityPair> nicNameSet = new HashSet<>();
					nicNameSet.add(nicnamePP);
					nameToNicNames.put(name, nicNameSet);
				}
				
				// adds the name pair to the map of nicnames to names
				if (nicnameToNames.containsKey(nicname)) {
					nicnameToNames.get(nicname).add(namePP);
				}
				else {
					Set<NamePopularityPair> nameSet = new HashSet<>();
					nameSet.add(namePP);
					nicnameToNames.put(nicname, nameSet);
				}
				
				// reads the next line
				currLine = nicnameFile.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error in nicname parsing");
			System.out.println(e.getMessage());
		}	
	}
	
	// gets the set of names that correspond to a nicname
	public static Set<NamePopularityPair> getNames(String name) {
		return nicnameToNames.get(name);
	}
}
