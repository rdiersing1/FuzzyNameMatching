package data;

import java.util.Vector;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.util.EditDistance;

import java.lang.Math;

public class Name {
	private String mainInstance;
	private String initials;
	private String orderedInitials;
	private String orderedBlocksStr;
	private String title;
	
	private String[] blocks;
	private String[] orderedBlocks;
	private Vector< Set<NamePopularityPair> > nicNames;
	
	// ASSUMES THAT NAMEDATA.JAVA HAS ALREADY BEEN INSTANTIATED 
	public Name(String s) {
		mainInstance = s;
		
		// Converts to name blocks w/out periods
		blocks = s.split(" ");
		for (String i : blocks) {
			i.replaceAll(".", "");
		}
		
		// Creates ordered blocks
		orderedBlocks = blocks.clone();
		Arrays.sort(orderedBlocks);
		
		// Creates Initials
		StringBuilder initialsBuilder = new StringBuilder();
		for (String i : blocks) {
			if (i.length() > 0) initialsBuilder.append(i.charAt(0));
		}
		initials = initialsBuilder.toString();
		
		// Creates ordered initials
		StringBuilder oinitialsBuilder = new StringBuilder();
		for (String i : orderedBlocks) {
			if (i.length() > 0) oinitialsBuilder.append(i.charAt(0));
		}
		orderedInitials = oinitialsBuilder.toString();
		
		// Creates ordered blocks string
		StringBuilder oBlockBuilder = new StringBuilder();
		for (String i : orderedBlocks) {
			oBlockBuilder.append(i);
			oBlockBuilder.append(" ");
		}
		oBlockBuilder.deleteCharAt(oBlockBuilder.length() - 1);
		orderedBlocksStr = oBlockBuilder.toString();
		
		// Sets up title
		if (NameData.titles.contains(blocks[0])) {
			title = blocks[0];
		}
		else {
			title = "";
		}
		
		// Sets up nicname vector of possible name sets
		nicNames = new Vector< Set<NamePopularityPair> >();
		for (int i = 0; i < orderedBlocks.length; ++i) {
			if (NameData.getNames(orderedBlocks[i]) == null) {
				nicNames.add(new HashSet<NamePopularityPair>());
			}
			else {
				nicNames.add(NameData.getNames(orderedBlocks[i]));
			}
		}
	}
	
	// Getters
	public String getMainInstance() {
		return mainInstance;
	}
	
	// Comparison algorithms 
	// All algorithms are not case sensitive
	
	// Will return 1.0 if the main instance is a strict match
	private double compareClassic(Name rhs) {
		if (this.mainInstance.equalsIgnoreCase(rhs.mainInstance)) return 1.0;
		return 0.0;
	}
	
	// Will return 1.0 if the title strings match
	private double compareTitles(Name rhs) {
		if (this.title.equalsIgnoreCase(rhs.title)) return 1.0;
		return 0.0;
	}
	
	// Will return 1.0 if the name blocks match even if in wrong order
	private double compareCombination(Name rhs) {
		if (this.orderedBlocksStr.equalsIgnoreCase(rhs.orderedBlocksStr)) return 1.0;
		return 0.0;
	}
	
	// Will return 1.0 if the initials are the same & in the same order
	private double compareInitialsPermutation(Name rhs) {
		if (this.initials.equalsIgnoreCase(rhs.initials)) return 1.0;
		return 0.0;
	}
	
	// Will return 1.0 if the initals are the same even if out of order
	private double compareInitialsCombination(Name rhs) {
		if (this.orderedInitials.equalsIgnoreCase(rhs.orderedInitials)) return 1.0;
		return 0.0;
	}
	
	// Will return Levenshtein distance of the main instance
	private double classicLev(Name rhs) {
		EditDistance ed = new EditDistance();
		return ed.score(this.mainInstance.toLowerCase(), rhs.mainInstance.toLowerCase());
	}
	
	// Will return Levenshtein distance of the initials
	private double initialLev(Name rhs) {
		EditDistance ed = new EditDistance();
		return ed.score(this.initials.toLowerCase(), rhs.initials.toLowerCase());
	}
	
	// Will return the average Levenshtein distance of the ordered name
	// blocks, if one has more name blocks than the other it will not 
	// contribute to the average
	private double aveLev(Name rhs) {
		EditDistance ed = new EditDistance();
		double scoreSum = 0.0;
		int numBlocks = Math.min(rhs.blocks.length, this.blocks.length);
		
		for (int i = 0; i < numBlocks; ++i) {
			scoreSum += ed.score(this.blocks[i], rhs.blocks[i]);
		}
		return scoreSum / numBlocks;
	}
	
	// Will return the Levenshtein distance of the ordered name blocks
	// if one name has more blocks it WILL contribute to the edit distance
	private double orderedLev(Name rhs) {
		EditDistance ed = new EditDistance();
		return ed.score(this.orderedBlocks, rhs.orderedBlocks);
	}
	
	// Prints all local variables
	public void print() {
		System.out.println("mainInstance: " + mainInstance);
		System.out.println("intials: " + initials);
		System.out.println("ordered initials: " + orderedInitials);
		System.out.println("ordered blocks: " + orderedBlocksStr);
		System.out.println("ordered blocks no title: " + orderedBlocks);
		
		System.out.println("block_array: " + String.join("_", blocks));
		System.out.println("ordered block array: " + String.join("_", orderedBlocks));
	}
	
	// Prints all comparison details 
	public void printCompareStats(Name rhs) {
		System.out.println(this.mainInstance + " : " + rhs.mainInstance);
		System.out.println("compare: " + compareClassic(rhs));
		System.out.println("compareTitles: " + compareTitles(rhs));
		System.out.println("compareCombination: " + compareCombination(rhs));
		System.out.println("compareInitialsPermutaution: " + compareInitialsPermutation(rhs));
		System.out.println("compareInitialsCombination: " + compareInitialsCombination(rhs));
		System.out.println("Levenshtine distance classic: " + classicLev(rhs));
		System.out.println("Levenshtine distance of initials: " + initialLev(rhs));
		System.out.println("Levenshtine average distance: " + aveLev(rhs));
		System.out.println("Levenshtine ordered distance: " + orderedLev(rhs));
	}
	
	public double compare(Name rhs) {
		double[] compVals = new double[8];
		// double[] weights = new double[8];
		double totalScore = 0;
		
		compVals[0] = compareClassic(rhs);
		compVals[1] = compareTitles(rhs);
		compVals[2] = compareCombination(rhs);
		compVals[3] = compareInitialsPermutation(rhs);
		compVals[4] = 1 / (classicLev(rhs) + 1);
		compVals[5] = 1 / (initialLev(rhs) + 1);
		compVals[6] = 1 / (aveLev(rhs) + 1);
		compVals[7] = 1 / (orderedLev(rhs) + 1);
		
		for (double d : compVals) {
			totalScore += d;
		}
		return totalScore;
	}
}
