/*
---------GEONODE EXAMPLE----------------
receives two URL-encoded parameters lat and lng containing the
GPS coordinates of a place on Earth. If this is the first
time this client has made such a request then the return would
be the text/plain payload “RECEIVED”. If not, then the return
would be the text/plain payload:
The distance from (lat1, lng1) to (lat2, lng2) is: XXX km
where XXX is the geodesic distance between the previous place 
(lat1 and lng1, sent in the last request) and the current one 
(lat2 and lng2, sent in this request). This can continue (to a
3rd, 4th, … places) as long as the requests are made in the same 
session.

1. Run Geo.java or Geo.js on host: "localhost" and port: 4413

2. Run GeoNode.js:
  > node GeoNode.js

3. Paste the following on a web browser:
http://localhost:3000/GeoNode?lat=a&lng=4

Output:
  > Don't understand: a 4

Input:
  > 
  http://localhost:3000/GeoNode?lat=1&lng=2

Output:
  > RECEIVED

Input:
  > 
  http://localhost:3000/GeoNode?lat=3&lng=4

Output:
  > The distance from (1, 2) to (3, 4) is: 314.40295102362484 km

Input:
  > 
  http://localhost:3000/GeoNode?lat=5&lng=10

Output:
  > The distance from (3, 4) to (5, 10) is: 701.6829306883529 km
------------------------------------------
*/


/*
-----------CATALOG EXAMPLE--------------
This service receives one URL parameter id containing an integer 
and returns the id and name of the row corresponding to that id 
in the Category table of the Models_R_US.db database (which resides 
in ~/resources_and_libraries/pkg/sqlite). The response is 
mimetyped as application/json in the response’s content type. If the 
id parameter is missing then the response is an array of json
objects for all rows in the table.

1. Run Catalog.js
  > node Catalog.js

2. Input the following on a web browser:
  > 
  http://localhost:3000/Catalog

Output
  > [{"id":1,"name":"Motorcycles"},
    {"id":2,"name":"Classic Cars"},
    {"id":3,"name":"Trucks and Buses"},
    {"id":4,"name":"Vintage Cars"},
    {"id":5,"name":"Planes"},
    {"id":6,"name":"Ships"},
    {"id":7,"name":"Trains"}]

3. Input: 
  > 
  http://localhost:3000/Catalog?id=5

Output:
  > {"id":5,"name":"Planes"}

4. Input:
  > 
  http://localhost:3000/Catalog?id=10

Output:
  > {"response":"ID does not exist"}

------------------------------------------

*/



/*
---------------EXAMPLE--------------------
This service receives two URL parameters from and to containing the 
start and end addresses of a trip. It returns the optimal distance (in km) 
and time (in minutes) between them given the current traffic conditions. 
This information is received by sending a back-end request to the MapQuest 
Directions API (http://www.mapquestapi.com/directions/v2/route?) with specific
parameters. 

1. Run Trip.js:
    > node Trip.js
    Please note that before you run Trip.js, you must input your own mapQuestKey:
    My mapQuestKey which you can change (I am not sure if mine will work for you)
    is located at dao.js on line 10.
*/


/*
2. Input the following into a web browser:
    > 
    localhost:3000/Trip?from=4700 Keele Street, Toronto&to=Canada's Wonderland, Vaughan, Ontario

Output:
    > {"distance": 9.2232, "time": 14.5}

3. Input:
    > 
    localhost:3000/Trip?from=&to=Canada's Wonderland, Vaughan, Ontario

Output:
    > { "distance": 0, "time": 0 }

4. Input:
    > 
    localhost:3000/Trip?from=Ottawa, Ontario&to=Canada's Wonderland, Vaughan, Ontario

    Output:
    > {"distance": 394.5259, "time": 277.2}

5. Input:
    > 
    localhost:3000/Trip?from=Tokyo, Japan&to=Canada's Wonderland, Vaughan, Ontario

Output:
    > { "distance": 0, "time": 0 }
------------------------------------------

*/

const express = require('express');
const session = require('express-session');
const dao = require('./dao.js');
const app = express();
const port = process.argv[2] || 3000;

app.enable('trust proxy');
app.use(session({
    secret: 'secret',
    resave: true,
    saveUninitialized: true,
    proxy: true
}));


app.get('/GeoNode', async (req, res) => {
    var geoResponse;
    console.log(`request received`);

    let lat = req.query.lat.toString();
    let lng = req.query.lng.toString();

    var doubleRegex = /([+-]?([0-9]+)([.][0-9]+)?)/;

    if (doubleRegex.test(lat) && doubleRegex.test(lng)) {
        if (req.session.storedLat && req.session.storedLng) {
            let geoRequest = req.session.storedLat + " " +
                req.session.storedLng + " " + lat + " " + lng;
            console.log(geoRequest);
            geoResponse = await dao.runGeo("localhost", 4413, geoRequest);
            req.session.storedLatAndLng = req.session.storedLat + ", " + req.session.storedLng;
            req.session.storedLat = lat;
            req.session.storedLng = lng;

            res.send("The distance from (" + req.session.storedLatAndLng +
                ") to (" + lat + ", " + lng + ") is: " + geoResponse + " km");


        }
        else {
            req.session.storedLat = lat;
            req.session.storedLng = lng;
            res.send("RECEIVED");
        }
    }
    else {
        res.send("Don't understand: " + lat + " " + lng);
    }
});





app.get('/Catalog', (req, res) => {
    console.log(`From ${req.ip}, Request ${req.url}`);
    console.log("req.url: " + req.url);
  
    let dbRequest;
    if (req.url === "/Catalog") {
      dbRequest = 'SELECT * FROM Category;';
    }
    if (typeof req.query.id != 'undefined') {
      dbRequest = 'SELECT id,name FROM Category WHERE';
      dbRequest += " id = '" + req.query.id + "'" + ";";
    }
  
    dao.getCatalog(dbRequest, function (rows) {
      let response;
      if (rows.length == 1) {
        response = rows[0];
      }
      else if (rows.length > 1) {
        response = rows;
      }
      else {
        response = {
          "response": "ID does not exist"
        };
  
      }
      res.setHeader('Content-Type', 'application/json');
      res.write(JSON.stringify(response));
      res.end();
  
    });
  
  
  });





  app.get('/Trip', async (req, res) => {
    console.log(`From ${req.ip}, Request ${req.url}`);
    try {

        if (typeof req.query.from != 'undefined' || typeof req.query.to != 'undefined') {
            var response = await dao.getMapQuestAPIResponse(req.query.from, req.query.to);
            res.setHeader('Content-Type', 'application/json');
            res.write(response);
        }
        res.end();
    }
    catch (err) {
        console.log(err);
    }

});

const server = app.listen(port, function () {
    const host = server.address().address;
    const port = server.address().port;
    console.log(`server listening to ${host}:${port}`);
});