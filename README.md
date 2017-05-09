# EMapper

EMapper provides a visual representation of the universe of New Eden in the online massively-multiplayer game EVE-Online. 
A* path-finding algorithm is used to provide route information between two solar-system nodes within the network.

## Prerequisites

Required jar files

```
Google - guava - https://github.com/google/guava
Graphstream - http://graphstream-project.org/
Sqlite jdbc - https://bitbucket.org/xerial/sqlite-jdbc/downloads/

```
Required sqlite database file for EVE data Static data export (SDE) made available by CCP, conversion available by fuzzwork.co.uk. The sqlite db file must be named "EData.db" in the top level directory.
```
https://www.fuzzwork.co.uk/dump/
```

## Screenshots
![alt text](/screenshots/EMapper1.PNG "Opening application")
![alt text](/screenshots/EMapper2.PNG "Example route")

## License

This code is provided under the Academic Academic Free License v. 3.0.
For details, please see the http://www.opensource.org/licenses/AFL-3.0.

## Acknowledgments

* Thomas Bierhance for the AutoCompletion combobox class
* CCP for making EVEs Static Data Export(SDE) public https://developers.eveonline.com/resource/resources
* https://www.fuzzwork.co.uk/dump/ for SDE reformatting
* Graphstream Project which this application is based around

### CCP copyright notice
© 2014 CCP hf. All rights reserved. "EVE", "EVE Online", "CCP", and all related logos and images are trademarks or registered trademarks of CCP hf.
