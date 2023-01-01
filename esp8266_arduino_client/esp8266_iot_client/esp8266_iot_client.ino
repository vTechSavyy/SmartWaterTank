#include <ESP8266WiFi.h>        // Include the Wi-Fi library
#include <ESP8266mDNS.h>        // Include the mDNS library
#include <ESP8266HTTPClient.h>  // Include the HTTP client library: 
#include <ArduinoJson.h>        // Include the JSON formatting library
#include <RunningMedian.h>
#include <Thread.h>
#include <Fetch.h>
#include "config.h"

// Declare all the global constants:
float SENSOR_HEIGHT_TANK_1 = 125.0;   // cms  (measured from base of tank)
float SENSOR_HEIGHT_TANK_2 = 125.0;   // cms  (measured from base of tank)

// Define the pins for controlling the relays: 
const int PUMP_1_PIN = D5;
const int PUMP_2_PIN = D6;

// Define Trig and Echo pins for the sensors:
const int trigPin_s1 = D1;
const int echoPin_s1 = D2;

const int trigPin_s2 = D8;
const int echoPin_s2 = D7;

// Create running median filters for the two sensors
RunningMedian buffer_s1 = RunningMedian(30);
RunningMedian buffer_s2 = RunningMedian(30);

// Threads for actuation and data transmission: 
Thread* data_transmission_thread = new Thread();
Thread* actuation_command_thread = new Thread();

// Declare variables for the events: 
String wifi_ssid;

float water_level_tank_1;
float water_level_tank_2;

// Server names and URL's:
String SERVER_BASE_URL     = "https://plum-cockroach-gown.cyclic.app";    // Deployment server//
//String SERVER_BASE_URL   = "https://192.168.1.21:3000";                  // Local testing server//


// Callback function to setup the wifi connection: 
bool setupWifi(const char* ssid, const char* password) {

  WiFi.begin(ssid, password);             // Connect to the network
  Serial.print("Trying to connect to ");
  Serial.print(ssid); Serial.println(" ...");

  int i = 0;
  while (WiFi.status() != WL_CONNECTED && i < 30) { // Wait for the Wi-Fi to connect
    delay(1000);
    Serial.print(++i); Serial.print(' ');
  }
  Serial.println("  ");

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println('\n');
    Serial.println("Connection established!");  
    Serial.print("IP address:\t");
    Serial.println(WiFi.localIP());         // Send the IP address of the ESP8266 to the computer

    if (MDNS.begin("esp8266")) {              // Start the mDNS responder for esp8266.local
      Serial.println("mDNS responder started");
    } else {
      Serial.println("Error setting up MDNS responder!");
    }

    wifi_ssid = ssid;
    return true;
  }
  else
  {
    return false;
  }
}



// Function to trigger and read the ultrasonic sensor data:
float triggerAndReadUltrasonicSensor(const int trigPin, const int echoPin, const int SENSOR_HEIGHT) {

  // Clear the trigPin by setting it LOW:
  digitalWrite(trigPin, LOW);
  delayMicroseconds(5);
  // Trigger the sensor by setting the trigPin high for 10 microseconds:
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  // Read the echoPin, pulseIn() returns the duration (length of the pulse) in microseconds:
  long duration = pulseIn(echoPin, HIGH);
  // Calculate the distance:
  float distance= duration*0.034/2;
  // Calculate the water level (cms):
  return SENSOR_HEIGHT - distance;

}


void transmissionCallback() {

    if (WiFi.status() == WL_CONNECTED) { 

//      Serial.println(" Data transmission callback");/
      const int capacity = JSON_OBJECT_SIZE(5);
      StaticJsonDocument<capacity> esp_data;
  
      // Fill in the data: 
      esp_data["water_level_tank_1"] = buffer_s1.getMedian();
      esp_data["pump_1_status"] = digitalRead(PUMP_1_PIN);
      esp_data["water_level_tank_2"] = buffer_s2.getMedian();
      esp_data["pump_2_status"] = digitalRead(PUMP_2_PIN);
      esp_data["wifi_ssid"] = wifi_ssid;
  
      String esp_data_str;
      serializeJson(esp_data, esp_data_str);

      RequestOptions options;
      options.method = "POST";
      options.headers["Content-Type"] = "application/json";
      options.headers["Content-Length"] = strlen(esp_data_str.c_str());
      options.fingerprint = SHA1_FINGERPRINT;
      options.body = esp_data_str;

      // Making the request
      String DATA_POST_URL = SERVER_BASE_URL + "/api/esp8266data";
      Response response = fetch(DATA_POST_URL.c_str(), options);
//      Serial.println(response);/
    
   }

}

void actutationCommandCallback() {
  
  if (WiFi.status() == WL_CONNECTED) { 

//      Serial.println(" Actuation command callback");/
      RequestOptions options;
      options.method = "GET";
      options.fingerprint = SHA1_FINGERPRINT;

      // Making the request
      String COMMAND_GET_URL = SERVER_BASE_URL + "/api/commands";
      Response response = fetch(COMMAND_GET_URL.c_str(), options);
//      Serial.println(response);/

      const int capacity = JSON_OBJECT_SIZE(3);
      StaticJsonDocument<3*capacity> commands;

        
       // Deserialize the JSON document
      DeserializationError error = deserializeJson(commands, response.body);

      // Test if parsing succeeds.
      if (error) {
        Serial.print("deserializeJson() failed: ");
        Serial.println(error.c_str());
        //return;
      }

      if (commands["pump_1_command"] == "OFF" && digitalRead(PUMP_1_PIN) == LOW)
      {
        digitalWrite(PUMP_1_PIN, HIGH); 
      }

      if (commands["pump_1_command"] == "ON" && digitalRead(PUMP_1_PIN) == HIGH)
      {
        digitalWrite(PUMP_1_PIN, LOW); 
      } 

      if (commands["pump_2_command"] == "OFF" && digitalRead(PUMP_2_PIN) == LOW)
      {
        digitalWrite(PUMP_2_PIN, HIGH); 
      }

      if (commands["pump_2_command"] == "ON" && digitalRead(PUMP_2_PIN) == HIGH)
      {
        digitalWrite(PUMP_2_PIN, LOW); 
      } 
   }
  
}


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  delay(10);
  Serial.println('\n');

  // Setup the wifi connection: 
  bool wifi_res_1 = setupWifi(ssid_1, password_1);

  if (wifi_res_1 == false)
  {
    bool wifi_res_2 = setupWifi(ssid_2, password_2);
  }


  pinMode(trigPin_s1, OUTPUT);
  pinMode(echoPin_s1, INPUT);

  pinMode(trigPin_s2, OUTPUT);
  pinMode(echoPin_s2, INPUT);

  // Initialize the actuation pins:
  pinMode(PUMP_1_PIN, OUTPUT);
  pinMode(PUMP_2_PIN, OUTPUT);
  
  digitalWrite(PUMP_1_PIN, HIGH);
  digitalWrite(PUMP_2_PIN, HIGH);

  data_transmission_thread->onRun(transmissionCallback);
  data_transmission_thread->setInterval(2000);  // milliseconds

  actuation_command_thread->onRun(actutationCommandCallback);
  actuation_command_thread->setInterval(1000);  // milliseconds

}

void loop() {

  if (WiFi.status() != WL_CONNECTED)
  {
    bool wifi_res_1 = setupWifi(ssid_1, password_1);

    if (wifi_res_1 == false)
    {
      bool wifi_res_2 = setupWifi(ssid_2, password_2);
    }
  }

  // put your main code here, to run repeatedly:
  water_level_tank_1 = triggerAndReadUltrasonicSensor(trigPin_s1, echoPin_s1, SENSOR_HEIGHT_TANK_1);
  water_level_tank_2 = triggerAndReadUltrasonicSensor(trigPin_s2, echoPin_s2, SENSOR_HEIGHT_TANK_2);

  // Add sensor data to the buffers:
  buffer_s1.add(water_level_tank_1);
  buffer_s2.add(water_level_tank_2);

  // Transmit the current data to the server
  if(data_transmission_thread->shouldRun() && WiFi.status() == WL_CONNECTED) 
    data_transmission_thread->run();  

  // Get the latest commands from the server
  if(actuation_command_thread->shouldRun() && WiFi.status() == WL_CONNECTED) 
    actuation_command_thread->run(); 
  
  // Set sample rate to ~100Hz: 
  delay(100);

  // Print the water levels for debugging
  Serial.print("Pump #1 status is");
  Serial.println(digitalRead(PUMP_1_PIN));
  Serial.print("Pump #2 status is");
  Serial.println(digitalRead(PUMP_2_PIN));
//  Serial.print(" Water level tank #1 is ");
//  Serial.println(water_level_tank_1);
//
//  Serial.print(" Water level tank #2 is ");
//  Serial.println(water_level_tank_2);
}
