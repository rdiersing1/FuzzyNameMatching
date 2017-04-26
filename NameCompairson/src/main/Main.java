package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
		
		FileWriter fw = null;
		BufferedWriter out = null;
		
		Name nameToSearch = new Name("");
		Set<Name> setOfNames = new HashSet<>();
		Map<Name, Integer> namesToClusters = new HashMap<>();
		Map<Set<Name>, Double> nameCompairsons = new HashMap<>();
		ArrayList<Set> nameClusters = new ArrayList<>();
		
		try {
			// Reads titles set and nicnames set
			FileReader titlesFR = new FileReader("res/titles.txt");
			FileReader nicnamesFR = new FileReader("res/NickNames.txt");
			BufferedReader titlesBR = new BufferedReader(titlesFR);
			BufferedReader nicnamesBR = new BufferedReader(nicnamesFR);
			new NameData(titlesBR, nicnamesBR);
			
			// Reads test set
			FileReader testSetFR = new FileReader(args[0]);
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
			
			// Writing output to file 
			fw = new FileWriter(args[1]);
			out = new BufferedWriter(fw);
			
		} catch (IOException e) {
			System.out.println("ERROR: One or more files not found");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("ERROR: Not enough command line arguments");
		}
		
		try {
			out.write("Name to search: " + nameToSearch.getMainInstance() + "\n");
			out.write("Database size: " + setOfNames.size() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		nameProbability[] names = new nameProbability[setOfNames.size()];
		
		// Search for name
		int i = 0;
		for (Name n : setOfNames) {
			names[i] = new nameProbability(n.getMainInstance(), n.compare(nameToSearch));
			++i;
		}
		
		// Start timer
		long startTime = System.nanoTime();
		
		int numCompairsons = 0;
		// Compare all names
		for (Name n1: setOfNames) {
			for (Name n2 : setOfNames) {
				Set<Name> namesToCompare = new HashSet<>();
				namesToCompare.add(n1);
				namesToCompare.add(n2);
				if (!nameCompairsons.containsKey(namesToCompare)) {
					++numCompairsons;
					nameCompairsons.put(namesToCompare, n1.compare(n2));
				}
			}
		}
		
		// Cluster
		for (Name n1 : setOfNames) {
			if (!namesToClusters.containsKey(n1)) {
				Set<Name> matchingNames = new HashSet<>();
				matchingNames.add(n1);
				for (Name n2 : setOfNames) {
					if (!namesToClusters.containsKey(n2)) {
						Set<Name> namesToCompare = new HashSet<>();
						namesToCompare.add(n1);
						namesToCompare.add(n2);
						if (nameCompairsons.get(namesToCompare) > Name.FULL_SIMILARITY_MINIMUM) {
							matchingNames.add(n2);
						}
					}
				}
				nameClusters.add(matchingNames);
				namesToClusters.put(n1, nameClusters.size() - 1);
				for (Name n2 : matchingNames) {
					namesToClusters.put(n2, nameClusters.size() - 1);
				}
			}
		}
		
		// End timer
		long endTime = System.nanoTime();
		
		Arrays.sort(names, new nameProbabilityComparator());
		
		// Prints results with line dividing probable names from improbable names
		double oldVal = 1;
		for (nameProbability n : names) {
			double newVal = n.getProbability();
			try {
				if ((oldVal > Name.FULL_SIMILARITY_MINIMUM) && newVal < Name.FULL_SIMILARITY_MINIMUM) {
					
						out.write("--------------------------------------------------------------\n");
				}
				out.write(n.getName() + " " + newVal + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			oldVal = newVal;
		}
		
		try {
			out.write("Done\n\n");
			out.write("Matching Names\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			int j = 0;
			for (Set<Name> nSet : nameClusters) {
				out.write(j + ". \n"); ++j;
				for (Name n : nSet) {
					out.write(n.getMainInstance() + "\n");
				}
				out.write("\n");
			}
			out.write("\n");	
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long runTime = endTime - startTime;
		long runTimePerComp = runTime / numCompairsons;
		
		System.out.println("Runtime stats:");
		System.out.println("Num compairsons: " + numCompairsons);
		System.out.println("Total clustering runtime: " + (runTime/1_000_000) + "ms");
		System.out.println("Runtime per compairson: " + (runTimePerComp/1000) + "us");
		System.out.println("Estimated runtime for size 1,000: " + (1E6 * runTimePerComp / 1_000_000_000) + "s");
		System.out.println("Estimated runtime for adding to size 1,000: " + (1000 * runTimePerComp / 1_000_000) + "ms");
		System.out.println("Estimated runtime for size: 30,000: " + (9E8 * runTimePerComp / 1_000_000_000) + "s");
		System.out.println("Estimated runtime for adding to size 30,000: " + (30_000 * runTimePerComp / 1_000_000) + "ms");
		
//		Name name1 = new Name("Terri Diersing");
//		Name name2 = new Name("Tim Diersing");
//		System.out.println("COMPAIRSON: ");
//		name1.printCompareStats(name2);
	}
}
