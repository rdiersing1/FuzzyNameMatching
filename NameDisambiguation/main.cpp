#include <iostream>
#include <fstream>
#include <set>
#include <vector>
#include <string>
#include <sstream>

#include "Name.h"

using namespace std;

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
	set<string> titles;


	INFS.open("config.txt");	// opens the config file MUST BE CALLED config.txt
	if (!INFS.is_open()) {
		cerr << "ERROR: File 'config.txt' not found!" << endl;
		return 1;
	}

	getFileName(inputFileName, INFS);		// gets the names of the other input files
	getFileName(outputFileName, INFS);
	getFileName(titleFileName, INFS);

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

	INFS.open(inputFileName.c_str());		// opens file with all names
	if (!INFS.is_open()) {
		cerr << "2 ERROR: File '" << inputFileName << "' not found!" << endl;
		return 1;
	}
	OFS.open(outputFileName);

	while (!INFS.eof()) {					// parses names file and normalizes names
		string s;

		getline(INFS, s);
		Name newName(s, titles);
		OFS << newName.toStr() << endl;
	}
	
	INFS.close();
	OFS.close();
}