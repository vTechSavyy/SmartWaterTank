#include <ESP8266WiFi.h>        // Include the Wi-Fi library
#include <ESP8266mDNS.h>        // Include the mDNS library
#include <ESP8266HTTPClient.h>  // Include the HTTP client library: 
#include <ArduinoJson.h>        // Include the JSON formatting library
#include <RunningMedian.h>
#include <Thread.h>


// Define the pins for controlling the relays: 
const int PUMP_1_PIN = D5;
const int PUMP_2_PIN = D6;

// Define Trig and Echo pins for the sensors:
const int trigPin_s1 = D1;
const int echoPin_s1 = D2;

const int trigPin_s2 = D3;
const int echoPin_s2 = D4;


RunningMedian buffer_s1 = RunningMedian(20);
RunningMedian buffer_s2 = RunningMedian(20);

// Threads for actuation and data transmission: 
Thread* data_transmission_thread = new Thread();
Thread* actuation_command_thread = new Thread();
Thread* param_request_thread = new Thread();

// Declare the HTTP client:
HTTPClient http;

// Declare variables for the events: 
String event_component, event_type;
bool send_event_data_to_server;

float water_level_tank_1;
float water_level_tank_2;

//const char* ssid     = "Airtel-B310-B566";             // The SSID (name) of the Wi-Fi network you want to connect to
//const char* password = "********";                     // The password of the Wi-Fi network

const char* ssid     = "PereiraHome-Jio-2.4GHz";         // The SSID (name) of the Wi-Fi network you want to connect to
const char* password = "JGAJSC84";                       // The password of the Wi-Fi network

//String SERVER_BASE_URL = "https://plum-cockroach-gown.cyclic.app";

String SERVER_BASE_URL = "http://192.168.29.224:3000";   // Local testing server


// Callback function to setup the wifi connection: 
void setupWifi() {

  WiFi.begin(ssid, password);             // Connect to the network
  Serial.print("Connecting to ");
  Serial.print(ssid); Serial.println(" ...");

  int i = 0;
  while (WiFi.status() != WL_CONNECTED) { // Wait for the Wi-Fi to connect
    delay(1000);
    Serial.print(++i); Serial.print(' ');
  }

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
  return distance;

}


void transmissionCallback() {

    if (WiFi.status() == WL_CONNECTED) { 

      http.begin(SERVER_BASE_URL + "/api/esp8266data");
      http.addHeader("Content-Type", "application/json");
  
      const int capacity = JSON_OBJECT_SIZE(5);
      StaticJsonDocument<capacity> esp_data;
  
      // Fill in the data: 
      esp_data["water_level_tank_1"] = buffer_s1.getMedian();
      esp_data["pump_1_status"] = digitalRead(PUMP_1_PIN);
      esp_data["water_level_tank_2"] = buffer_s2.getMedian();
      esp_data["pump_2_status"] = digitalRead(PUMP_2_PIN);
  
      String esp_data_str;
      serializeJson(esp_data, esp_data_str);
  
      int esp_data_res_code = http.POST(esp_data_str);
  
      if (esp_data_res_code > 0){
        Serial.println("Successful data transmission");
      }
      else {
        Serial.println("Failed to transmit");
        Serial.println(esp_data_res_code);
      }
  
      //Todo: Check the response code:
      http.end();  // Close connection
    
   }

}

void actutationCommandCallback() {
  
  if (WiFi.status() == WL_CONNECTED) { 

    Serial.println(" Actuation command callback");

    http.begin(SERVER_BASE_URL + "/api/commands");

    const int capacity = JSON_OBJECT_SIZE(3);
    StaticJsonDocument<3*capacity> commands;

    int command_res_code = http.GET();

    //Check for the returning code
    if (command_res_code > 0) { 
        
       // Deserialize the JSON document
      DeserializationError error = deserializeJson(commands, http.getString());

      // Test if parsing succeeds.
      if (error) {
        Serial.print("deserializeJson() failed: ");
        Serial.println(error.c_str());
        //return;
      }

      if (commands["pump_1_command"] == "WT_OFF" && digitalRead(PUMP_1_PIN) == LOW)
      {
        digitalWrite(PUMP_1_PIN, HIGH); 
      }

      if (commands["pump_1_command"] == "WT_ON" && digitalRead(PUMP_1_PIN) == HIGH)
      {
        Serial.println("Pump 1 command is WT_ON. Setting pin to low");
        digitalWrite(PUMP_1_PIN, LOW); 
      }   
   }

    http.end();  // Close connection
    
   }
  
}


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  delay(10);
  Serial.println('\n');

  // Setup the wifi connection: 
//  setupWifi();/

  pinMode(trigPin_s1, OUTPUT);
  pinMode(echoPin_s1, INPUT);

  pinMode(trigPin_s2, OUTPUT);
  pinMode(echoPin_s2, INPUT);

  // Initialize the actuation pins:
  pinMode(PUMP_1_PIN, OUTPUT);
  pinMode(PUMP_2_PIN, OUTPUT);
  
  digitalWrite(PUMP_1_PIN, HIGH);
  digitalWrite(PUMP_2_PIN, HIGH);

}

void loop() {

  // put your main code here
  delay(10000);

  Serial.println(" Starting motor #1 for 3 seconds");
  digitalWrite(PUMP_1_PIN, LOW);
  delay(3000);
  digitalWrite(PUMP_1_PIN, HIGH);

  delay(5000);

  Serial.println(" Starting motor #2 for 3 seconds");
  digitalWrite(PUMP_2_PIN, LOW);
  delay(3000);
  digitalWrite(PUMP_2_PIN, HIGH);

  
  // Set sample rate to ~10Hz: 
  delay(5000);
  
}
