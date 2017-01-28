#ifndef NAME_H_
#define NAME_H_

#include <string>
#include <set>
#include <vector>

class Name {

public:
	Name(std::string, const std::set<std::string> &);
	std::string toStr() const;

private:
	std::vector< std::string > nameBlocks;
	std::string title;

	std::vector<bool> abbreviated;

};


#endif // !NAME_H_
