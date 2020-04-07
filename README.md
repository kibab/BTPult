# BTPult
Androd-based Bluetooth remote control for the Arduino-powered robot


## Using the application

 * Build it and install on the phone.
 * Launch and press "Rescan", the list of paired Bluetooth devices will be displayed.
 * Select a device that corresponds to the Bluetooth module installed on the robot. I have tested it with HC-05 Bluetooth module connected to Arduino's serial port.
 * On the next screen press "Connect". The app will try to establish Bluetooth connection with the paired device.
 * Once the connection is ready, whatever is received from the device is displayed in the log viewer in the upper part of the screen. You can operate direction buttons and a speed lever.
 
## Robot control protocol
The protocol is super simple. It's basically just three bytes where the first byte is a command, the second byte is an argument, and finally there is an "\n" :-)

Commands:
 * `MF / MB / ML / MR` for moving forward / backward / left / right.
 * `S0...S9` for setting the speed.
 * `P_` for ping
 
 ## Arduino code
 The working Arduino code is located at https://github.com/kibab/robobagger.
 
 ## Extending the app
 It's Open Source! So feel free to fork and add / remove commands and implement your own protocol / whatever. The app provides you with a basic BT connection handling.
