Fuzzy Name Matching Algorithm:

Once complete this algorithm will be able to search for
a name in a large set of other names, and locate one,
or possibly multiple names, that have a high probability
of matching the name. Or all names, that may not necessarily 
be the same string, but match to the same name. It will also 
display the probability that these names are the same name.
It will indicate if there are no names that have a high probability
of being the same name.

My current approach will separate the name into several blocks or 
components, ie. first name, last name, and possibly middle name. 
The algorithm will then account for name variations of the following list

1. Re-arranging components
2. Abbreviated initials
3. Missing name components
4. Phonetic similarity
5. Titles and honorifics
6. Truncated name components
7. Missing spaces or hyphens
8. Nicknames
10. Spelling differences 

Since this algorithm is only being used to match the authors of wiki papers,
the algorithm may be able to use outside data, such as the set of coauthors
to get better results.
