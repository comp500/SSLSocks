# SSLSocks
SSL + socks5 VPN using [stunnel](https://www.stunnel.org/) for Android.

Allows you to easily set up stunnel clientside with android, and tunnel your phone's internet connection through it. This system should bypass most blocks if you have an unblocked domain, as it looks exactly like normal HTTPS traffic.

**Note: Currently does not run as a VPN, only as a HTTPS tunnel!**

## Build instructions
1. Install Android Studio
1. Download the stunnel android binary from [stunnel.org](https://www.stunnel.org/index.html), and copy it to /app/src/main/assets/stunnel
1. Build it using Android Studio

## How to use
To edit the configuration, tap the top menu then press Config Editor. Then add your settings according to the [stunnel documentation](https://www.stunnel.org/static/stunnel.html). 

Stunnel should start when you press the start button, and will create a notification while it is being run. If the notification is immediately removed after being created, there was an error, so you will need to check the log (second tab).

Please note that currently the log is only updated when stunnel stops, so you will need to press the stop button to view it. Currently there are also some problems with sending the log to the screen, so make sure you have the app open when you stop it.
