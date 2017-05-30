package main;

// To do: fix clustering
// heiarchacal clustering: not as important
// Make a set of unitary tests 
// Detect lists

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
		
		Set<Name> setOfNames = new HashSet<>();
		Map<Name, Integer> namesToClusters = new HashMap<>();
		Map<Set<Name>, Double> nameCompairsons = new HashMap<>();
		ArrayList<Set> nameClusters = new ArrayList<>();
		
		Map<Set<Character>, Set<Name>> namePreparedSets= new HashMap<>(); 
		Map<Name, Set<Set<Name>>> overlappingNamesToSets = new HashMap<>();
		Set<Name> overlappingNames = new HashSet<>();
		
		try {
			// Reads titles set and nicnames set
			FileReader titlesFR = new FileReader("res/titles.txt");
			FileReader nicnamesFR = new FileReader("res/NickNames.txt");
			BufferedReader titlesBR = new BufferedReader(titlesFR);
			BufferedReader nicnamesBR = new BufferedReader(nicnamesFR);
			new NameData(titlesBR, nicnamesBR);
			
			// Writing output to file 
			fw = new FileWriter(args[1]);
			out = new BufferedWriter(fw);
			
		} catch (IOException e) {
			System.out.println("ERROR: One or more files not found");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("ERROR: Not enough command line arguments");
		}
		
		// Grabs names from wiki
		try {
			FileReader nameSetFR = new FileReader(args[0]);
			BufferedReader nameSetBR = new BufferedReader(nameSetFR);
			
			String inLine = null;
			while ((inLine = nameSetBR.readLine()) != null) {
				inLine = inLine.trim();
				String url = "http://wiki.linked.earth/" + inLine.replace(" ", "_");
				setOfNames.add(new Name(inLine, url));
			}
			
			nameSetBR.close();
		} catch (FileNotFoundException e1) {
			System.out.println("ERROR: file " + args[1] + " not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Preprocessing");
		
		// Start Timer
		long startTimePreprocess = System.nanoTime();
		
		// Preprocessing
		// Clusters names so that no two names with no common initials are compared
		for (Name n : setOfNames) {
			String initials = n.getInitials();
			
			Set<Character> iSet = new HashSet<>();
			for (int i = 0; i < initials.length(); ++i) {
				iSet.add(initials.charAt(i));
			}
			
			// iterate over the map to see if we can put the name in a prepared set
			boolean needsNewSet = true;
			for (Map.Entry<Set<Character>, Set<Name>> entry : namePreparedSets.entrySet()) {
				if (entry.getKey().containsAll(iSet)) {			// if we can, do it
					entry.getValue().add(n);
					needsNewSet = false;
					break;
				} 
				
				boolean overlap = false;
				for (int i = 0; i < initials.length(); ++i) {	// if not find overlap
					if (entry.getKey().contains(initials.charAt(i))) {
						overlap = true;
						break;
					}
				}
				if (overlap) {
					overlappingNames.add(n);
					needsNewSet = false;
					break;
				}
			}
			
			if (needsNewSet) {		// make new set if necessary
				Set<Name> newNameSet = new HashSet<>();
				newNameSet.add(n);
				namePreparedSets.put(iSet, newNameSet);
			}
		}
		
		// map the overlapping names to their respective sets
		for (Name n : overlappingNames) {
			
			String initials = n.getInitials();
			boolean valid = false;
			for (Map.Entry<Set<Character>, Set<Name>> entry : namePreparedSets.entrySet()) {
				for (int i = 0; i < initials.length(); ++i) {
					if (entry.getKey().contains(initials.charAt(i))) {
						valid = true;
						if (overlappingNamesToSets.containsKey(n)) {
							overlappingNamesToSets.get(n).add(entry.getValue());
						} else {
							Set<Set<Name>> s = new HashSet<>();
							s.add(entry.getValue());
							overlappingNamesToSets.put(n, s);
						}
						break;
					}
				}
			}
			
			if (!valid) {
				System.out.println("WARNING VALID IS FALSE FOR NAME " + n.getMainInstance());
			}
		}
		
		// End timer
		long endTimePreprocess = System.nanoTime();
		
		System.out.println("Num elements: " + setOfNames.size());
		System.out.println("Num preprocessed sets: " + namePreparedSets.size());
		System.out.println("Num overlaping elements: " + overlappingNames.size());
		
		System.out.println("Preprocessed sets");
		for (Set<Character> cSet : namePreparedSets.keySet()) {
			System.out.println(cSet.toString());
		}
		
		System.out.println("Making name compairsons");
		
		// Start timer
		long startTimeCompare = System.nanoTime();
		
		int numCompairsons = 0;
		// Compare Names
		for (Name n1 : overlappingNames) {
			for (Set<Name> nSet : overlappingNamesToSets.get(n1)) {
				for (Name n2 : nSet) {
					Set<Name> namesToCompare = new HashSet<>();
					namesToCompare.add(n1);
					namesToCompare.add(n2);
					if (!nameCompairsons.containsKey(namesToCompare)) {
						++numCompairsons;
						nameCompairsons.put(namesToCompare, n1.compare(n2));
					}
				}
			}
		}
		
		for (Set<Name> nSet : namePreparedSets.values()) {
			for (Name n1 : nSet) {
				for (Name n2 : nSet) {
					Set<Name> namesToCompare = new HashSet<>();
					namesToCompare.add(n1);
					namesToCompare.add(n2);
					if (!nameCompairsons.containsKey(namesToCompare)) {
						++numCompairsons;
						nameCompairsons.put(namesToCompare, n1.compare(n2));
					}
				}
			}
		}
		
		// End timer
		long endTimeCompare = System.nanoTime();
		
		System.out.println("Clustering");
		
		// Start timer
		long startTimeCluster = System.nanoTime();
		
		// Cluster
		for (Name n1 : overlappingNames) {
			if (!namesToClusters.containsKey(n1)) {
				Set<Name> cluster = new HashSet<>();
				cluster.add(n1);
				for (Set<Name> nSet : overlappingNamesToSets.get(n1)) {
					for (Name n2 : nSet) {
						Set<Name> namesToCompare = new HashSet<>();
						namesToCompare.add(n1);
						namesToCompare.add(n2);
						if (nameCompairsons.get(namesToCompare) > Name.FULL_SIMILARITY_MINIMUM) {
							if (!namesToClusters.containsKey(n2)) {
								cluster.add(n2);
							} else {
								int i = namesToClusters.get(n2);
								nameClusters.get(i).add(n1);
							}
						}
					}
				}
				nameClusters.add(cluster);
				namesToClusters.put(n1, nameClusters.size() - 1);
				for (Name n2 : cluster) {
					namesToClusters.put(n2, nameClusters.size() - 1);
				}
			}
		}
		
		for(Set<Name> nSet : namePreparedSets.values()) {
			for (Name n1 : nSet) {
				if (!nameClusters.contains(n1)) {
					Set<Name> cluster = new HashSet<>();
					cluster.add(n1);
					for (Name n2 : nSet) {
						Set<Name> namesToCompare = new HashSet<>();
						namesToCompare.add(n1);
						namesToCompare.add(n2);
						if (nameCompairsons.get(namesToCompare) > Name.FULL_SIMILARITY_MINIMUM) {
							if (!namesToClusters.containsKey(n2)) {
								cluster.add(n2);
							} else {
								int i = namesToClusters.get(n2);
								nameClusters.get(i).add(n1);
							}
						}
					}
				}
			}
		}
		
		// End time
		long endTimeCluster = System.nanoTime();
		
		try {
			out.write("Matching Names\n\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			int j = 1;
			for (Set<Name> nSet : nameClusters) {
				if (nSet.size() > 1) {
					out.write(j + ". \n"); ++j;
					for (Name n : nSet) {
						out.write(n.getMainInstance() + " : " + n.getLink() + "\n");
					}
					out.write("\n");
				}
			}
			out.write("\n");	
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long runTimePreprocessMS = (endTimePreprocess - startTimePreprocess) / 1_000_000;
		long runTimeCompare = endTimeCompare - startTimeCompare;
		long runTimeClusterMS = (endTimeCluster - startTimeCluster) / 1_000_000;
		long runTimePerCompUS = (runTimeCompare / numCompairsons) / 1_000;
		long runTimeCompareMS = runTimeCompare / 1_000_000;
		
		System.out.println("Runtime stats:");
		System.out.println("Dataset Size: " + setOfNames.size());
		System.out.println("Num compairsons: " + numCompairsons);
		System.out.println("Preprocessing runtime: " + runTimePreprocessMS + "ms");
		System.out.println("Comparison runtime: " + runTimeCompareMS + "ms");
		System.out.println("Runtime per compairson: " + runTimePerCompUS + "us");
		System.out.println("Clustering runtime: " + runTimeClusterMS + "ms");
	}
}
