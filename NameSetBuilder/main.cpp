#include <iostream>
#include <string>
#include <fstream>
#include <sstream>

using namespace std;

// Cleans up set of names from wikipedia 
int main() {
	ifstream INFS;
	ofstream OFS;
	
	INFS.open("NameSet.txt");		// file MUST BE CALLED NameSet.txt
	if (!INFS.is_open()) {
		cerr << "Error File Not Found!" << endl;
		return 1;
	}
	OFS.open("CleanNameSet.txt");

	while (!INFS.eof()) {			// parses file
		string line;
		string pName;
		string s;
		getline(INFS, line);
		stringstream ss(line);
		bool valid = true;

		while (!ss.eof()) {			// parses each name
			ss >> s;
			for (int i = 0; i < s.size(); ++i) {
				if (s.at(i) == '[') {	// does not include names with [
					valid = false;
				}
			}

			if (!valid) {
				break;
			}
			else if (s.at(0) == '(') {	// ends when it gets to a (
				break;
			}
			pName = pName + s + ' ';
		}

		if (valid) {
			if (pName.at(pName.size() - 2) == ',') {	// assumes size of at least 2
				pName.pop_back();
				pName.pop_back();
			}
			OFS << pName << endl;
		}
	}

	INFS.close();
	OFS.close();
}