package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import data.Name;
import data.NameData;

class nameProbability {
	
	String name;
	double probability;
	
	public nameProbability(String name, double probability) {
		this.name = name;
		this.probability = probability;
	}
	
	public String getName() {
		return this.name;
	}
	public double getProbability() {
		return this.probability;
	}
}

class nameProbabilityComparator implements Comparator<nameProbability> {

	@Override
	public int compare(nameProbability arg0, nameProbability arg1) {
		double ret;
		if (arg1 != null && arg0 != null ) {
			ret = arg1.getProbability() - arg0.getProbability();
		}
		else {
			ret = 0;
		}
		if (ret == 0) return 0;
		if (ret > 0) return 1;
		return -1;
	}
}

public class Main {
	public static void main(String[] args) {
		
		System.out.println("Starting up...");
		
		Name nameToSearch = new Name("");
		Set<Name> setOfNames = new HashSet<>();
		
		try {
			// Reads titles set and nicnames set
			FileReader titlesFR = new FileReader("res/titles.txt");
			FileReader nicnamesFR = new FileReader("res/NickNames.txt");
			BufferedReader titlesBR = new BufferedReader(titlesFR);
			BufferedReader nicnamesBR = new BufferedReader(nicnamesFR);
			new NameData(titlesBR, nicnamesBR);
			
			// Reads test set
			FileReader testSetFR = new FileReader("res/testSet.txt");
			BufferedReader testSetBR = new BufferedReader(testSetFR);
			
			String currLine = testSetBR.readLine();
			while (currLine != null) {
				String[] currLineArr = currLine.split(" ");
				if (currLineArr.length > 0) {
					if (currLineArr[0].equalsIgnoreCase("SEARCH_FOR:") && currLineArr.length > 1) {
						currLineArr = Arrays.copyOfRange(currLineArr, 1, currLineArr.length);
						nameToSearch = new Name(String.join(" ", currLineArr));
					}
					else {
						setOfNames.add(new Name(currLine));
					}
				}
				currLine = testSetBR.readLine();
			}
		} catch (IOException e) {
			System.out.println("ERROR: One or more files not found");
			e.printStackTrace();
		}
		
		System.out.println("Name to search: " + nameToSearch.getMainInstance());
		System.out.println("Database size: " + setOfNames.size());
		
		nameProbability[] names = new nameProbability[setOfNames.size()];
		
		int i = 0;
		for (Name n : setOfNames) {
			names[i] = new nameProbability(n.getMainInstance(), n.compare(nameToSearch));
			++i;
		}
		
		Arrays.sort(names, new nameProbabilityComparator());
		
		for (nameProbability n : names) {
			System.out.println(n.getName() + " " + n.getProbability());
		}
		
		System.out.println("Done");
		System.out.println("");
		System.out.println("");
		
		Name name1 = new Name("Robert Diersing");
		Name name2 = new Name("Robert L D");
		System.out.println("COMPAIRSON: ");
		name1.printCompareStats(name2);
	}
}
