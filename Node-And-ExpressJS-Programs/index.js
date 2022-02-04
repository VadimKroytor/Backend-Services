/*
---------GEONODE EXAMPLE----------------
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