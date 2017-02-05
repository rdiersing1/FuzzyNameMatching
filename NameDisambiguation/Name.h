#ifndef NAME_H_
#define NAME_H_

#include <string>
#include <set>
#include <vector>
#include <map>

class Name {

public:
	Name(std::string, const std::set<std::string> &, const std::map<std::string, std::set<std::string> > &);
	std::string toStr() const;

private:
	std::vector< std::string > nameBlocks;
	std::string title;

	std::vector<bool> abbreviated;
	std::vector< std::set<std::string> > possibleNames;
	bool hasNickName;
};


#endif // !NAME_H_
