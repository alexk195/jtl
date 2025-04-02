// compile with "g++ main.cpp -o example2"
#include <iostream>
int main(int argc, char**argv) {
	// define variables
	std::string Name;
	int Age;
	// set from std::cin
	std::cout << "Type in Name :";
	std::cin  >> Name;
	std::cout << "Type in Age :";
	std::cin  >> Age;
	// print them
	std::cout << "Your data:" << std::endl;

	std::cout << "Name:" << Name << std::endl;
	std::cout << "Age:" << Age << std::endl;
	return 0;
}
