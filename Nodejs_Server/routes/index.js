const express = require("express");
const router = express.Router();
const moment = require('moment');
const fs = require('fs');
var admin = require("firebase-admin");
var serviceAccount = require("../automaticwatertank-firebase-adminsdk-yrnhh-6232c82ce4.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://automaticwatertank.firebaseio.com"
});

var registrationToken = 'ca098N39Rd2Yj9-SEItTzG:APA91bGd_8Ug4meAMe1MgZqdVQ79QRuqOpwLAYsMfOIhEHIGfEPkfZjF2kWoCKoZ4F_HdpAonAwJtQfEQUf5tZkZ4p36opWmS_e_AnaO_PMkPzL6H4vBNmiuj50Unjz60O6BFEnm3qLA';

const ESP8266_FILE_NAME = "esp8266_waterproof_sensor_data.txt";

// Params: 
var SENSOR_HEIGHT_TANK_1 = 125.0;  // cms
var LOWER_THRESH_TANK_1 = 40.0;  // cms
var UPPER_THRESH_TANK_1 = 100.0;  // cms

var SENSOR_HEIGHT_TANK_2 = 125.0;  // cms
var LOWER_THRESH_TANK_2 = 40.0;  // cms
var UPPER_THRESH_TANK_2 = 100.0;  // cms

// var UTC_OFFSET = 5.5;  // hours (UTC to IST)
// var MORNING_START_TIME = '08:00';  // HH:mm
// var MORNING_END_TIME = '10:00';    // HH:mm
// var EVENING_START_TIME = '17:00';  // HH:mm
// var EVENING_END_TIME = '19:00';    // HH:mm
// var CLIENT_OVERRIDE_RECEIVED = true;
// var system_mode = 0;

var pump_1_command = "WT_OFF";
var pump_2_command = "WT_OFF";

// Variables: 
var water_level_tank_1 = 0.0;
var water_level_tank_2 = 0.0;
var pump_1_status = 0;
var pump_2_status = 0;

const fd = fs.openSync(ESP8266_FILE_NAME, 'w');
var file_pos = 0

// Initialize the data array: 
fs.writeSync(fd, "[", file_pos++, 'utf8');



router.post("/esp8266data", (req, res) => {

    water_level_tank_1 = req.body.water_level_tank_1;
    // water_level_tank_2 = req.body.water_level_tank_2;
    pump_1_status = 1 - req.body.pump_1_status;
    // pump_2_status = 1 - req.body.pump_2_status;


    var jsonData = {
        "t": moment().utc().format('HH:mm:ss'),
        "level": water_level_tank_1
    }


    var buf = new Buffer(JSON.stringify(jsonData) + ",");
    fs.writeSync(fd, buf, 0, buf.length, file_pos);

    file_pos = file_pos + buf.length

    // Print to screen: 
    console.log(" Tank #1 level is: ", water_level_tank_1);
    // console.log(" Tank #2 level is: ", water_level_tank_2);
    console.log(" Pump #1  is: ", pump_1_status ? "ON" : "OFF");
    // console.log(" Pump #2  is: ", pump_2_status ? "ON" : "OFF");
    console.log(" Pump #1  command is: ", pump_1_command);
    // console.log(" Pump #2  command is: ", pump_2_command);
    console.log(" --------------------------------------- ");

    res.status(200).json({ message: " ESP8266 data received" });
});


router.post("/events", (req, res) => {

    const event_component = req.body.component;
    const event_type = req.body.type;

    // if (event_component === 'PUMP_1') {
    //     pump_1_command = event_type;
    // }

    // if (event_component === 'PUMP_2') {
    //     pump_2_command = event_type;
    // }

    // Print to screen: 
    console.log(" Event type : ", event_type, " occured for ", event_component);
    console.log(" --------------------------------------- ");

    var payload = {
        notification: {
            title: "This is a Notification",
            body: `Event type : , ${event_type} , " occured for  ${event_component}`
        }
    };

    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    admin.messaging().sendToDevice(registrationToken, payload, options)
        .then(function (response) {
            console.log("Successfully sent message to Firebase :", response);
        })
        .catch(function (error) {
            console.log("Error sending message to Firebase:", error);
        });

    res.status(200).json({ message: " Event data received. Notification sent" });
});

router.get("/params", (req, res) => {

    res.status(200).json({
        SENSOR_HEIGHT_TANK_1,
        LOWER_THRESH_TANK_1,
        UPPER_THRESH_TANK_1,
        SENSOR_HEIGHT_TANK_2,
        LOWER_THRESH_TANK_2,
        UPPER_THRESH_TANK_2,

    })
});

router.get("/commands", (req, res) => {

    res.status(200).json({
        pump_1_command,
        pump_2_command
    });


    // // If in automatic mode then check the current time: 
    // let curr_time = moment().utc();

    // curr_time.add(UTC_OFFSET, 'h');

    // let morn_start_time = moment.utc(curr_time.format('YYYY-MM-DDT') + MORNING_START_TIME, 'YYYY-MM-DDTHH:mm');
    // let morn_end_time = moment.utc(curr_time.format('YYYY-MM-DDT') + MORNING_END_TIME, 'YYYY-MM-DDTHH:mm');

    // let eve_start_time = moment.utc(curr_time.format('YYYY-MM-DDT') + EVENING_START_TIME, 'YYYY-MM-DDTHH:mm');
    // let eve_end_time = moment.utc(curr_time.format('YYYY-MM-DDT') + EVENING_END_TIME, 'YYYY-MM-DDTHH:mm');

    // let morn_awake = curr_time > morn_start_time && curr_time < morn_end_time;
    // let eve_awake = curr_time > eve_start_time && curr_time < eve_end_time;

    // // console.log(" Current IST time is : ", curr_time.format('YYYY-MM-DDTHH:mm'));

    // if (morn_awake || eve_awake) {
    //     system_mode = 1;
    // }
    // else {
    //     system_mode = 0
    // }


    // res.status(200).json({
    //     system_mode
    // })


});

router.get("/status", async (req, res) => {

    await new Promise(resolve => setTimeout(resolve, 1000));

    console.log(" Finshed waiting...")

    res.status(200).json({
        water_level_tank_1: "INVALID",
        water_level_tank_2: "INVALID",
        pump_1_status,
        pump_2_status,
        pump_1_command,
        pump_2_command

    });


})

router.get("/time", (req, res) => {

    let curr_time = moment().utc();

    console.log(" Android time is : " + curr_time.format('HH:mm:ss'))

    res.status(200).json({
        "time": curr_time.format('HH:mm:ss')
    });
});

router.post("/commands", (req, res) => {

    if (req.body.pump_1) {
        console.log(' Received command for pump #1 ', req.body.pump_1);
        pump_1_command = req.body.pump_1;
        res.status(200).json({ msg: "Pump #1 command set to " + pump_1_command });
    }

    if (req.body.pump_2) {
        console.log(' Received command for pump #2 ', req.body.pump_2);
        pump_2_command = req.body.pump_2;
        res.status(200).json({ msg: "Pump #2 command set to " + pump_2_command });
    }

});

module.exports = router;