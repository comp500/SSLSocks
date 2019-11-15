# SSLSocks
✨ See [this article](https://hamy.io/post/0011/how-to-run-stunnel-on-your-android-device/) by Hamy for an excellent introduction to the app!

**Now released as a beta on Google Play!** https://play.google.com/store/apps/details?id=link.infra.sslsocks

SSL/TLS tunnel using [stunnel](https://www.stunnel.org/) for Android.

*Note:* Originally intended to be a socks5 VPN through TLS, but I never finished implementing the VPN service.

## Build instructions
1. Install Android Studio
1. Download the stunnel android binary from [stunnel.org](https://www.stunnel.org/index.html), and copy it to /app/src/main/assets/stunnel
1. Build it using Android Studio

## How to use
To edit the configuration, tap the top menu then press Config Editor. Then add your settings according to the [stunnel documentation](https://www.stunnel.org/static/stunnel.html). 

Stunnel should start when you press the start button, and will create a notification while it is being run. If the notification is immediately removed after being created, there was an error, so you will need to check the log (second tab).

Please note that currently the log is only updated when stunnel stops, so you will need to press the stop button to view it. Currently there are also some problems with sending the log to the screen, so make sure you have the app open when you stop it.

### How to configure stunnel
Some example configurations are available in the [stunnel documentation](https://www.stunnel.org/static/stunnel.html#EXAMPLES), and more are given below. Many use cases (e.g. tunnelling SSH or SOCKS over HTTPS) require you to run an stunnel server, which you can download from the stunnel website.

The stunnel binary functions as both a server and a client, as long as you put `client = yes` at the top of your config file when you want to use it as a client. This is set by default in the app.

#### SSH over HTTPS
##### Client

```
[ssh]
accept = 10000
connect = example.com:443
```
Connect to 127.0.0.1:10000 in your SSH client.
##### Server
```
[ssh]
accept = 443
connect = 127.0.0.1:22
```
#### SOCKS
##### Client
```
[rdp]
accept = 9050
connect = example.com:443
```
Connect to 127.0.0.1:9050 in your SOCKS client. Orfox works well for this on android.
##### Server
```
[rdp]
accept = 443
protocol = socks
```
#### RDP
##### Client
```
[rdp]
accept = 3380
connect = example.com:443
```
Connect to 127.0.0.1:3380 in your RDP client.
##### Server
```
[rdp]
accept = 443
connect = 127.0.0.1:3389
```
#### SNI
SNI allows you to have multiple tunnels on one server, as many firewalls only allow port 443.
##### Client
```
[default]
accept = 8080
connect = example.com:443

[ssh]
accept = 10000
connect = example.com:443
sni = ssh.example.com
```
##### Server
```
[default]
accept = 443
connect = 127.0.0.1:8080

[ssh]
sni = default:ssh.example.com
connect = 127.0.0.1:22
```
