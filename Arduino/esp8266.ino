#include <ESP8266WiFi.h>

const char *ssid = "Phx->2.4G";
const char *password = "embedded->1123581321.p";
const char *host = "192.168.1.222";
const int tcpPort = 21567;

String readLine();

void setup()
{
    delay(100);
    Serial.begin(9600);
    Serial.print("Connecting to ");
    Serial.println(ssid);
 
    WiFi.begin(ssid, password);
 
    while (WiFi.status() != WL_CONNECTED)
    {
        delay(100);
        Serial.print(".");
    }
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
}

void loop()
{ 
    if (Serial.available())
    {
        WiFiClient client;
        while (!client.connected())
        {
            if (!client.connect(host, tcpPort))
            {
                Serial.println("connection....");
                //client.stop();
                delay(100);
            }
        }
        Serial.write("c");
        String recv = readLine();
        while (recv != "o")
        {
            if (recv.length())    client.print("{\"source\":\"rfid\",\"id\":\"" + recv + "\"}\r\n");
            recv = readLine();
        }
        client.print("over");
        client.stop();
    }
}

String readLine()
{
    String str;
    while (Serial.available())
    {
        char temp = Serial.read();
        if (temp != '\n') {
          str += char(temp);
          delay(2);
        }//delete temp;
        else break;//delete temp;
    }
    return str;
}
