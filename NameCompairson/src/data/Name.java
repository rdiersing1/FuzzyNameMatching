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
	private String[] orderedNameBlocks;
	private Vector< Set<NamePopularityPair> > nicNames;
	private Set<Name> coauthors;
	
	// Constants
	private static final double SIMILARITY_MINIMUM = 0.4;
	// Weights
	private static final double COMPARE_CLASSIC_WEIGHT = 1;
	private static final double COMPARE_TITLES_WEIGHT = 2;
	private static final double COMPARE_COMBINATION_WEIGHT = 0;
	private static final double COMPARE_INITIALS_PERM_WEIGHT = 0;
	private static final double COMPARE_INITIALS_COMBO_WEIGHT = 2;
	private static final double CLASSIC_LEV_WEIGHT = 0;
	private static final double INITIALS_LEV_WEIGHT = 0;
	private static final double AVE_LEV_WEIGHT = 0;
	private static final double ORDERED_LEV_WEIGHT = 1;
	private static final double BBBCOMPARE_WEIGHT = 13;
	private static final double SUBSTR_INTIIALS_WEIGHT = 1;
	private static final double COMMON_INTIIALS_WEIGHT = 9;

	// ASSUMES THAT NAMEDATA.JAVA HAS ALREADY BEEN INSTANTIATED 
	public Name(String s) {
		// converts to lowercase and removes caps
		mainInstance = s.toLowerCase();
		mainInstance = mainInstance.replace(".", "");
		
		// Converts to name blocks
		String[] tempBlocks;
		tempBlocks = mainInstance.split(" ");
		
		// Sets up title
		if (NameData.titles.contains(tempBlocks[0])) {
			title = tempBlocks[0];
			// erases title from blocks
			blocks = Arrays.copyOfRange(tempBlocks, 1, tempBlocks.length);

		}
		else {
			title = "";
			blocks = tempBlocks;
		}
		
		// Creates ordered blocks
		orderedBlocks = blocks.clone();
		Arrays.sort(orderedBlocks);
		
		// Creates Initials
		int initialBlocks = 0;
		StringBuilder initialsBuilder = new StringBuilder();
		for (String i : blocks) {
			if (i.length() > 0) initialsBuilder.append(i.charAt(0));
			if (i.length() == 1) ++initialBlocks;
		}
		initials = initialsBuilder.toString();
		
		// Creates non initial blocks
		orderedNameBlocks = new String[blocks.length - initialBlocks];
		int k = 0;
		for (String i : orderedBlocks) {
			if (i.length() > 1) {
				orderedNameBlocks[k] = i;
				++k;
			}
		}
		
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
		if (oBlockBuilder.length() > 0) oBlockBuilder.deleteCharAt(oBlockBuilder.length() - 1);
		orderedBlocksStr = oBlockBuilder.toString();
		
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
		if (this.title.length() == 0 && rhs.title.length() == 0) return 1.0;
		if (this.title.length() == 0 || rhs.title.length() == 0) return 0.5;
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
		return ed.score(this.mainInstance, rhs.mainInstance);
	}
	
	// Will return Levenshtein distance of the initials
	private double initialLev(Name rhs) {
		EditDistance ed = new EditDistance();
		return ed.score(this.initials, rhs.initials);
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
	
	// Will return the number of initials in common over the max number of initials
	private double commonInitials(Name rhs) {
		int index = 0;
		int commonInitials = 0;
		
		for (int i = 0; i < orderedInitials.length(); ++i) {
			for (int j = index; j < rhs.orderedInitials.length(); ++j) {
				if (orderedInitials.charAt(i) == rhs.orderedInitials.charAt(j)) {
					index = j + 1;
					++commonInitials;
					break;
				}
			}
		}
		
		return  ( (double) commonInitials)/(Math.min(orderedInitials.length(), rhs.orderedInitials.length()));
	}
	
	// returns 1 if the one of the initials is a substring of the other
	private double substrInitials(Name rhs) {
		if (this.initials.contains(rhs.initials) && rhs.initials.length() > 0) {
			return this.initials.length()/rhs.initials.length();
		}
		if (rhs.initials.contains(this.initials) && this.initials.length() > 0) {
			return rhs.initials.length()/this.initials.length();
		}
		return 0.0;
	}
	
	// compares two specific name blocks
	public double compareBlock(String lhs, String rhs) {
		
		// Finds the edit distance
		EditDistance ed = new EditDistance();
		double currScore = ed.score(lhs, rhs);
		
		// handles the case of an empty name block
		if (lhs.isEmpty() || rhs.isEmpty()) return 0;
		
		// If the initials are the same the score is increased
		if (lhs.charAt(0) != rhs.charAt(0)) currScore += 3;
		currScore = 1 / (1 + currScore);
		
		// If the name is a substring of another name the score is increased 
		if (lhs.contains(rhs) || rhs.contains(lhs)) {
			// this function halves the distance between the score and one
			currScore =  1 - ((1 - currScore) / 2);
		}
		
		return currScore;
	}
	
	// Block by block comparison 
	private double BBBSimilarityCompareison(Name rhs) {
		int similarNames = 0;
		int similarNameSum = 0;
		double score = 0;
		
		int totalNamesLhs = orderedNameBlocks.length;
		int totalNamesRhs = rhs.orderedNameBlocks.length;

		for (String name : orderedNameBlocks) {
			// Case of empty rhs name
			if (totalNamesRhs == 0) break;
			
			// gets an array of the score between the curr lhs block and all rhs blocks
			double[] blockScores = new double[totalNamesRhs];
			for (int i = 0; i < rhs.orderedNameBlocks.length; ++i) {
				//System.out.println("Compairing blocks: " + name + ", " + rhs.orderedNameBlocks[i]);
				blockScores[i] = compareBlock(name, rhs.orderedNameBlocks[i]);
			}
			
			// Finds maximum value & positon of max value
			double max = blockScores[0];
			for (int i = 0; i < blockScores.length; ++i) {
				if (blockScores[i] > max) {
					max = blockScores[i];
				}
			}
			
			// increments Score
			// TODO: account for similar names that have already been used
			if (max >= SIMILARITY_MINIMUM) {
				++similarNames; 
				similarNameSum += max;
			}
		}
		
		if (similarNames == 0) score = 0;
		else {
			score = similarNameSum/((double) similarNames);
			
//			if ((similarNames == totalNamesRhs) && (similarNames == totalNamesLhs)) {
//				// this function halves the distance between the score and one
//				score =  1 - ((1 - score) / 2);
//			}
		}
		return score;
	}
	
	// Co-author comparison
	public double coAuthorCompare(Name rhs) {
		int numInCommon = 0;
		for (Name i : coauthors) {
			for (Name j : rhs.coauthors) {
				if (i.compare(j) > Name.SIMILARITY_MINIMUM) {
					++numInCommon;
				}
			}
		}
		return ((double) numInCommon / coauthors.size());
	}
	
	// Prints all local variables
	public void print() {
		System.out.println("mainInstance: " + mainInstance);
		System.out.println("intials: " + initials);
		System.out.println("ordered initials: " + orderedInitials);
		System.out.println("ordered blocks: " + orderedBlocksStr);
		System.out.println("title: " + title);
		
		System.out.println("block_array: " + String.join("_", blocks));
		System.out.println("ordered block array: " + String.join("_", orderedBlocks));
	}
	
	// Prints all comparison details 
	public void printCompareStats(Name rhs) {
		System.out.println(this.mainInstance + " : " + rhs.mainInstance);
		System.out.println("compare: " + compareClassic(rhs) * COMPARE_CLASSIC_WEIGHT + "/" + COMPARE_CLASSIC_WEIGHT);
		System.out.println("compareTitles: " + compareTitles(rhs) * COMPARE_TITLES_WEIGHT + "/" + COMPARE_TITLES_WEIGHT);
		System.out.println("compareCombination: " + compareCombination(rhs) * COMPARE_COMBINATION_WEIGHT + "/" + COMPARE_COMBINATION_WEIGHT);
		System.out.println("compareInitialsPermutaution: " + compareInitialsPermutation(rhs) * COMPARE_INITIALS_PERM_WEIGHT + "/" + COMPARE_INITIALS_PERM_WEIGHT);
		System.out.println("compareInitialsCombination: " + compareInitialsCombination(rhs) * COMPARE_INITIALS_COMBO_WEIGHT + "/" + COMPARE_INITIALS_COMBO_WEIGHT);
		System.out.println("Levenshtine distance classic: " + (1 / (classicLev(rhs) + 1)) * CLASSIC_LEV_WEIGHT + "/" + CLASSIC_LEV_WEIGHT);
		System.out.println("Levenshtine distance of initials: " + (1 / (initialLev(rhs) + 1)) * INITIALS_LEV_WEIGHT + "/" + INITIALS_LEV_WEIGHT);
		System.out.println("Levenshtine average distance: " + (1 / (initialLev(rhs) + 1)) * AVE_LEV_WEIGHT + "/" + AVE_LEV_WEIGHT);
		System.out.println("Levenshtine ordered distance: " + (1 / (orderedLev(rhs) + 1)) * ORDERED_LEV_WEIGHT + "/" + ORDERED_LEV_WEIGHT);
		System.out.println("BBBCompare: " + BBBSimilarityCompareison(rhs) * BBBCOMPARE_WEIGHT + "/" + BBBCOMPARE_WEIGHT);
		System.out.println("substrInitials: " + substrInitials(rhs) * SUBSTR_INTIIALS_WEIGHT + "/" + SUBSTR_INTIIALS_WEIGHT);
		System.out.println("Common initials: " + commonInitials(rhs) * COMMON_INTIIALS_WEIGHT + "/" + COMMON_INTIIALS_WEIGHT);
		System.out.println("Full comparison: " + compare(rhs));
	}
	
	// Linear combination of all other comparisons
	public double compare(Name rhs) {
		double[] compVals = new double[12];
		double[] weights = new double[12];
		double totalScore = 0;
		double weightSum = 0;
		
		weights[0] = COMPARE_CLASSIC_WEIGHT;
		weights[1] = COMPARE_TITLES_WEIGHT;
		weights[2] = COMPARE_COMBINATION_WEIGHT;
		weights[3] = COMPARE_INITIALS_PERM_WEIGHT;
		weights[4] = COMPARE_INITIALS_COMBO_WEIGHT;
		weights[5] = CLASSIC_LEV_WEIGHT;
		weights[6] = INITIALS_LEV_WEIGHT;
		weights[7] = AVE_LEV_WEIGHT;
		weights[8] = ORDERED_LEV_WEIGHT;
		weights[9] = BBBCOMPARE_WEIGHT;
		weights[10] = SUBSTR_INTIIALS_WEIGHT;
		weights[11] = COMMON_INTIIALS_WEIGHT;
		
		for (double d : weights) {
			weightSum += d;
		}
		
		compVals[0] = compareClassic(rhs) * weights[0];
		compVals[1] = compareTitles(rhs) * weights[1];
		compVals[2] = compareCombination(rhs) * weights[2];
		compVals[3] = compareInitialsPermutation(rhs) * weights[3];
		compVals[4] = compareInitialsCombination(rhs) * weights[4];
		compVals[5] = 1 / (classicLev(rhs) + 1) * weights[5];
		compVals[6] = 1 / (initialLev(rhs) + 1) * weights[6];
		compVals[7] = 1 / (aveLev(rhs) + 1) * weights[7];
		compVals[8] = 1 / (orderedLev(rhs) + 1) * weights[8];
		compVals[9] = BBBSimilarityCompareison(rhs) * weights[9];
		compVals[10] = substrInitials(rhs) * weights[10];
		compVals[11] = commonInitials(rhs) * weights[11];
		
		for (double d : compVals) {
			totalScore += d;
		}
		
		// Special conditions:
		// If initials match perfectly add 1/3 the dist between score and 1
		if (orderedInitials == rhs.orderedInitials) {
			totalScore += ((1 - totalScore) / 3);
		}
		
		return totalScore/weightSum;
	}
}
