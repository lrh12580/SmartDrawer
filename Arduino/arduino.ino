#include <SoftwareSerial.h>

SoftwareSerial wifi(10, 11); // RX, TX
SoftwareSerial rfid(8, 9);
int i = 0;
char buffer[3];

void setup() {
  Serial.begin(115200);
  while (!Serial) {
    ;
  }
  Serial.println("Goodnight moon!");
  rfid.begin(115200);
  wifi.begin(9600);
  delay(100);
}

void loop() {
  rfid.listen();
  if (rfid.available()) {
    rfid.write(0xBB);
    rfid.write(0x17);
    rfid.write(0x02);
    rfid.write((byte)0x00);
    rfid.write((byte)0x00);
    rfid.write(0x19);
    rfid.write(0x0D);
    rfid.write(0x0A);
    String str = readLine();
    Serial.print(str);//检测软串口输入的内容
    
    delay(10);
    wifi.print(str);
    if (i > 5) {
      i = 0;
      wifi.write("o\n");
      delay(10);
      delay(10000);
    }
  }
}

String readLine()//由于.read()函数每次读取一个字节，所以写了这个函数读取多位
{
  String str;
  while (rfid.available())
  {
    int s = int(rfid.read());
    String temp = inttohex(s);
    if (temp == "45" || temp == "53" || temp == "57" || temp == "49" || temp == "61") {
      str = str + s + "\n";
    }
      
    delay(3);
  }
  i++;
  return str;
}

char *inttohex(int aa)
{
  if (aa / 16 < 10)   //计算十位，并转换成字符
    buffer[0] = aa / 16 + '0';
  else
    buffer[0] = aa / 16 - 10 + 'A';
  if (aa % 16 < 10)   //计算个位，并转换成字符
    buffer[1] = aa % 16 + '0';
  else
    buffer[1] = aa % 16 - 10 + 'A';
  buffer[2] = '\0';   //字符串结束标志
  return (buffer);
}
