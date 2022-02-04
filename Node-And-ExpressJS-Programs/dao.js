const os = require('os');
const path = require('path');
const sqlite3 = require('sqlite3');

const dbfile = 'resources_and_libraries/pkg/sqlite/Models_R_US.db';
const dbpath = path.join(os.homedir(), ...dbfile.split('/'));
const db = new (sqlite3.verbose()).Database(dbpath);
const http = require('http');

var mapQuestKey = "eq4cXPFbfIRteZlr2dvAOaCnSfT29xmR";

module.exports = {

    getCatalog(statement, success, failure = console.log) {

        db.all(statement, (err, rows) => {
            if (err == null) {
                success(rows);
            } else {
                failure(err);
            }
        });
    },


    runGeo(host, port, request) {

        return new Promise((resolve, reject) => {
            try {
                const { createConnection } = require('net');
                const client = createConnection(port, host);
                let connection;
                client.on('connect', () => {
                    connection = `${client.remoteAddress}:${client.remotePort}`;
                    console.log(`Connected to ${connection}`);

                    client.write(request);
                    client.on('data', data => {
                        resolve(data.toString());
                    });

                    client.on('close', () => {

                        console.log(`Disconnected from ${connection}`)
                    });

                    client.on('end', () => {

                        client.end();
                    });


                });
            }
            catch (error) {
                console.log(`Error when connecting ${connection}.`);
                reject(err);
            }
        });

    },

    getMapQuestAPIResponse(from, to) {
        return new Promise((resolve, reject) => {
            try {
                var url = ("http://www.mapquestapi.com/" +
                    "directions/v2/route?key=" + mapQuestKey +
                    "&from=" + encodeURIComponent(from) + "&to=" + encodeURIComponent(to) +
                    "&unit=k&timeType=1&useTraffic=true");
                var chunk = "";
                http.get(url, response => {
                    let data = "";

                    response.on("data", result => {
                        try {
                            chunk += result;
                            chunk = JSON.parse(chunk);
                            if (chunk.info.statuscode !== "0" && chunk.info.messages[0] === "Unable to calculate route." || chunk.info.messages[0] === "At least two locations must be provided." ||
                                chunk.info.messages[0] === "We are unable to route with the given locations.") {
                                data += "{ \"distance\": 0, \"time\": 0 }";
                            }
                            if (typeof chunk.route.realTime != 'undefined' && typeof chunk.route.distance != 'undefined') {
                                let timeInMin = (parseFloat(chunk.route.realTime.toString()) / 60).toFixed(1).toString();
                                data += "{\"distance\": " + chunk.route.distance
                                    + ", \"time\": " + timeInMin + "}";
                            }
                        }
                        catch (err) {
                            console.log(err);
                        }
                    });
                    response.on("end", () => {
                        console.log(data);
                        resolve(data);
                    });
                    response.on("error", err => {
                        console.log("Error: " + err.message);
                        reject(err);
                    });
                });


            }
            catch (err) {
                console.log(err);
            }
        });
    }
};

