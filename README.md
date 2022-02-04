# Backend-Services

Specific details of each service (How to run the program and expected output for particular
input) are outlined in the respective file. 

#### resources_and_libraries
-> contains the external resources required to run several programs. Please note that this 
directory must be located in the home directory if you so choose to run these repository 
programs.

#### TCP-Programs Information
-> This directory contains services implemented in Java over TCP. TCP is a part of the protocol
stack layer responsible for transporting messages between network entities. The TCP 
organizes data in a way that allows secure transmission between the server and the client. These 
programs illustrate the significance of TCP connections in the protocol stack layer.

#### Tomcat-Apache-Programs Information
-> This directory contains services implemented in Java using HTTP and TCP. Session Management,
Open authentication, server-client HTTP connections, and fetching information from a database are
all implemented. Please note that Tomcat Apache v9.0 is used to act as a HTTP web server environment 
in which Java code is allowed to be run. 

#### Node-And-ExpressJS-Programs Information
-> This directory contains services implemented in the NodeJS environment using the ExpressJS
framework. These programs are majorly the Node and ExpressJS counterparts of the Tomcat-Apache
Programs. 

#### Appendix
Appended below, you can find the tables used for the programs in this repository. For example, Auth.java
in TCP-Programs and FAuth.java in Tomcat-Apache-Programs use the Category table for their functionality.
Likewise, the Node-And-ExpressJS program uses the Category table for its functionality. These tables
are a part of the Models_R_US.db database found in 'resources_and_libraries/pkg/sqlite/Models_R_US.db'.


CREATE TABLE IF NOT EXISTS 'Category' (
'id' INTEGER PRIMARY KEY AUTOINCREMENT, 
'name' varchar(50) NOT NULL
);


CREATE TABLE Client (
'id' INTEGER PRIMARY KEY AUTOINCREMENT,
'name' varchar(50) NOT NULL,
'street' varchar(50) NOT NULL,
'city' varchar(50) NOT NULL,
'province' varchar(20) NOT NULL,
'postalCode' varchar(20) NOT NULL,
'salt' char(16) NOT NULL,
'count' integer,
'hash' char(32) not null);
CREATE TABLE Tax (
'province' varchar(32) not null,
'code' varchar(2) not null,
'type' varchar(16) not null,
'PST' double not null,
'GST' double not null
);

