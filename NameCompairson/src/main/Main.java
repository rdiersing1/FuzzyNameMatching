package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import data.Name;
import data.NameData;

public class Main {
	public static void main(String[] args) {
		
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
						System.arraycopy(currLineArr, 1, currLineArr, 1, currLineArr.length - 1);
						nameToSearch = new Name(String.join(" ", currLineArr));
					}
					else {
						setOfNames.add(new Name(currLine));
					}
				}
				currLine = testSetBR.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Name to search: " + nameToSearch.getMainInstance());
		System.out.println("Database size: " + setOfNames.size());
		
		for (Name n : setOfNames) {
			System.out.println(n.getMainInstance() + ": " + nameToSearch.compare(n));
		}
		System.out.println("Done");
	}
}