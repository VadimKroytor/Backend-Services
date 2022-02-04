# Backend-Services

-> Specific details of each service (How to run the program and expected output for particular
input) are outlined in the respective file. 

#### resources_and_libraries directory information
-> contains the external resources required to run several programs. These external resources
were put in .gitignore for the sake of following best practice. As such, the database many 
programs in this repository use was the only resource stored in this folder. Apache Tomcat 
Version 9.0.8 and Apache Derby 10.14.2.0 were downloaded and used for these repo programs.
Please note that this directory must be located in the home directory if you so choose to 
run these repository programs (due to the nature of the programs hard-coded directory access).

#### TCP-Programs Information
-> This directory contains services implemented in Java over TCP. TCP is a part of the protocol
stack layer responsible for transporting messages between network entities. The TCP 
organizes data in a way that allows secure transmission between the server and the client. These 
programs illustrate the significance of TCP connections in the protocol stack layer.

#### Tomcat-Apache-Programs Information
-> This directory contains services implemented in Java using HTTP and TCP. Session Management,
Open authentication, server-client HTTP connections, and fetching information from a database are
all implemented. Please note that Tomcat Apache v9.0 is used to act as a HTTP web server environment 
in which Java code is allowed to be run. Also, please note that web.xml is used to map the ports and 
InetAddress of the respective programs. web.xml is used instead of manually changing the individual 
network addresses on every designated program for the sake of efficiency and organization. 

#### Node-And-ExpressJS-Programs Information
-> This directory contains services implemented in the NodeJS environment using the ExpressJS
framework. These programs are majorly the Node and ExpressJS counterparts of the Tomcat-Apache
Programs. 

#### Appendix
-> Node v14.18.3 and npm v8.3.0 were used for these programs.


Appended below, you can find the tables scemas (from the database used) for which the programs utilized. 
For example, Auth.java in TCP-Programs and FAuth.java in Tomcat-Apache-Programs use the Category table 
for their functionality. Likewise, the Node-And-ExpressJS program uses the Category table for its 
functionality. These tables are a part of the Models_R_US.db database found in 
'resources_and_libraries/pkg/sqlite/Models_R_US.db'.


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


-> The following github repo was used as a reference to implement these services. 
https://github.com/EECS4413-2021F
