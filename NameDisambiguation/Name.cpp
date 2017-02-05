#include "Name.h"

#include <sstream>
#include <algorithm>
#include <iostream>
#include <map>

using namespace std;

void removePeriods(string &s) {			// Removes the periods from the string
	string retString;
	retString.reserve(s.size());			
	for (int i = 0; i < s.size(); ++i) {
		if (s.at(i) != '.') {
			retString.push_back(s.at(i));
		}
	}
	s = retString;
}

void removeSpaces(string &s) {			// Removes the spaces from the string
	string retString;
	retString.reserve(s.size());
	for (int i = 0; i < s.size(); ++i) {
		if (s.at(i) != ' ') {
			retString.push_back(s.at(i));
		}
	}
	s = retString;
}

string Name::toStr() const {		// outputs a string of the name in the correct format
	stringstream returnSS;
	string s;

	if (title.size() != 0) {		// checks case where there is a title
		returnSS << title << ". ";
	}

	for (unsigned i = 0; i < nameBlocks.size(); ++i) {		// prints all of the blocks
		returnSS << nameBlocks.at(i);
		if (abbreviated.at(i)) {	// prints a period if the name is abreviated
			returnSS << '.';
		}
		returnSS << ' ';			// prints a space after every name block (even the last one)
	}

	returnSS << "\t {Title: ";		// prints title info
	if (title.size() != 0) {
		returnSS << title;
	}
	else {
		returnSS << "None";
	}

	returnSS << ", Name blocks count: " << nameBlocks.size();	// Prints name block count

	int numAbbreviatedComponents = 0;
	for (int i = 0; i < abbreviated.size(); ++i) {
		if (abbreviated.at(i)) {
			++numAbbreviatedComponents;
		}
	}
	returnSS << ", Abbreviated components count: " << numAbbreviatedComponents  << '}';		// Prints abrivated component count

	if (hasNickName) {
		returnSS << " [";
		
		for (unsigned i = 0; i < possibleNames.size(); ++i) {
			if (possibleNames.at(i).size() != 0) {
				returnSS << "Nickname: " << nameBlocks.at(i);
				returnSS << ", Possible Names:";
				for (set<string>::iterator it = possibleNames.at(i).begin(); it != possibleNames.at(i).end(); ++it) {
					returnSS << *it << ' ';
				}
			}
		}

		returnSS << ']';
	}

	getline(returnSS, s);
	return s;
}

Name::Name(string s, const set<string> &titles, const map<string, set<string> > &nickNames) {	// constructs a name, sorts name blocks, notes abriviations, and 
	stringstream ss(s);
	hasNickName = false;

	while (!ss.eof()) {					// parses the string
		string temp;
		ss >> temp;
		removePeriods(temp);			// Removes periods to put string in proper format
		if (titles.count(temp) != 0) {
			title = temp;				// Checks if the name block is a title (based on list of "all" titles in titles.txt)
		}
		else if (temp.size() != 0) {	// Ensures no name block is empty
			removeSpaces(temp);			// Ensures no name block contains spaces
			nameBlocks.push_back(temp);
		}
	}

	sort(nameBlocks.begin(), nameBlocks.end());			// Sorts the name blocks alphabetically 

	possibleNames.resize(nameBlocks.size());

	for (unsigned i = 0; i < nameBlocks.size(); ++i) {	
		if (nickNames.count(nameBlocks.at(i)) != 0) {	// Stores the possible nicknames for a name
			hasNickName = true;
			cerr << "Has a nickname" << endl;
			possibleNames.at(i) = nickNames.find(nameBlocks.at(i))->second;
		}

		if (nameBlocks.at(i).size() == 1) {				// Stores which names are abbreviated in vector of bools
			abbreviated.push_back(true);
		}
		else {
			abbreviated.push_back(false);
		}
	}
}