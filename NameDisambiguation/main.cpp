#include <iostream>
#include <fstream>
#include <set>
#include <vector>
#include <string>
#include <sstream>
#include <map>

#include "Name.h"

using namespace std;

void stdCapitalization(string &s) {
	if (s.size() != 0) {
		s.at(0) = toupper(s.at(0));
	}
	for (int i = 1; i < s.size(); ++i) {
		s.at(i) = tolower(s.at(i));
	}
}

void getFileName(string &fileName, ifstream &INFS) {
	string s;
	getline(INFS, s);
	stringstream ss(s);
	ss >> s;
	ss >> fileName;
}

// This algorithem currently takes an input of a set of names and outputs 
// the set of names properly formatted

int main() {
	ifstream INFS;
	ofstream OFS;
	string inputFileName;
	string outputFileName;
	string titleFileName;
	string nickNameFileName;
	set<string> titles;
	map<string, set<string> > nickNames;


	INFS.open("config.txt");	// opens the config file MUST BE CALLED config.txt
	if (!INFS.is_open()) {
		cerr << "ERROR: File 'config.txt' not found!" << endl;
		return 1;
	}

	getFileName(inputFileName, INFS);		// gets the names of the other input files (in order of the config file)
	getFileName(outputFileName, INFS);
	getFileName(titleFileName, INFS);
	getFileName(nickNameFileName, INFS);

	INFS.close();

	INFS.open(titleFileName.c_str());		// opens file with all known titles 
	if (!INFS.is_open()) {
		cerr << "1 ERROR: File '" << titleFileName << "' not found!" << endl;
		return 1;
	}

	while (!INFS.eof()) {					// makes set of those titles
		string title;
		string s;
		stringstream ss;

		getline(INFS, s);
		ss.str(s);
		ss >> title;
		titles.insert(title);
	}

	INFS.close();

	INFS.open(nickNameFileName.c_str());		// opens file with all nicknames
	if (!INFS.is_open()) {
		cerr << "2 ERROR: File '" << nickNameFileName << "' not found!" << endl;
		return 1;
	}

	while (!INFS.eof()) {
		string name;
		string nickName;
		string line;

		getline(INFS, line);
		stringstream ss(line);
		ss >> nickName >> name;

		stdCapitalization(name);
		stdCapitalization(nickName);

		if (nickNames.count(nickName) != 0) {
			map<string, set<string> >::iterator it = nickNames.find(nickName);
			it->second.insert(name);
		}
		else {
			pair<string, set<string> > newNode;
			set<string> namesSet;
			namesSet.insert(name);
			newNode.first = nickName;
			newNode.second = namesSet;
			nickNames.insert(newNode);
		}
	}
	cerr << "Finshed making map" << endl;

	INFS.close();

	INFS.open(inputFileName.c_str());		// opens file with all names
	if (!INFS.is_open()) {
		cerr << "2 ERROR: File '" << inputFileName << "' not found!" << endl;
		return 1;
	}
	OFS.open(outputFileName);

	while (!INFS.eof()) {					// parses names file and normalizes names
		string s;

		getline(INFS, s);
		Name newName(s, titles, nickNames);
		OFS << newName.toStr() << endl;
	}
	
	INFS.close();
	OFS.close();
}