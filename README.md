Hello and welcome to my little app. I wanted to make a dashboard so i could easily see and control things on my homeassistant server by using some old android devices I had laying around. 
Ive tested this on Samsung Galaxy S7, S8, S9 and S24 Ultra and they all seem to be working just fine!!

To use the app first either download the code and build with android studio and install on your device (I believe the app works down to android 7) or download and install the APK (Note the apk is not the most up to date version)
Open the app and go to the settings page. Scroll down until you get to "server connection configuration"
This is where you will enter your Home Assistant IP and port number (e.g. 192.168.1.2:8123) Make sure you put the port at the end of the IP address or it wont connect to Home Assistant
Next add your Long-lived access token. This can be created by going to your user settings in Home Assistant, Security and scrolling to the bottom and clicking "create token" Copy the token into the app.
Press "apply and re-connect" and you should be connected the Home Assistant. You can test the connection below in the network diagnostics section. press "run verification ping trace" to test the connection.
You should be able to go to the Lights page and any lights connected to Home Assistant should appear. 
You can press on the name of the light for more controls for the lights. you can also rename the device by pressing and holding the name of the light.


Settings


There are a few options in the settings.
First off is system appearace. Here you can manually choose a system theme. or turn on automatic themes.
The automatic things work from the dawn dusk sensors from Home Assistant. you get the option of 2 themes in this mode, one for day and one for night.

Below that you have the option to customise the 2 macro buttons on the dashboard. You can change the name of the macros and add devices that you wish to toggle when you press the button

Near the bottom of the settings you have the Display controls. Here you have the option to keep the screen awake so you dont have to unlock your device everytime you want to interacte with the app
There is also Screen burn protection added that you will want to turn on if you have the previous setting enabled
Below that it night mode. This is mainly for if you have the device set up in a bedroom this allows you to choose times to have the screen go black and turn back on with the abilty to choose the duration the screen stays on 
after an interaction and finally a button to black out the screen. This feature works best on a device with an OLED display as it just puts a black layer over the window and turns the brightness down as low as it goes.
a limitation of android and not being able to unlock the device for an app.

Next you have the connection status and at the very bottom is an option to view all entites Home Assistant is passing to the app with its state

Dashboard

At the current moment theres not much going on with this page. you have your 2 macro buttons and a screen that cycles through some system information. I will be making changes to this page to allow you to customise what is shown



Lights


This page lists all the lights found by Home Assistant and gives you option to turn them on and off from this view.
You can press on the name of the light for more options.
in order these are:
Light status
Power toggle
Light Brightness
Colour Temp (this doesnt work at the moment)
Sleep timer (Will only count down while on the selected light at the moment)
RGB controls (this will still appear when your light doesnt have RGB capabilties at the moment)


Climate


At the current moment this is a mock up view and doesnt do anything with Home Assistant
This will be implemented at somepoint soon

Setup Note for Home Assistant:
For this dashboard to communicate with your global settings, create these two helpers in Home Assistant under Settings > Devices & Services > Helpers:

An Input Boolean named heating_master_switch

An Input Number named heating_master_target_temp (Set its range from 15 to 30 with 0.5 steps)


Security

Same as climate. just a mock up at the minute


Ive mainly been working on the back end of the app and settings/Customastion
I will be adding things based on what i have in my own home and what i would like the app to do for me
At the moment the app is a glorified light switch but at least i can turn my smart lights on and off without having the try to connect to the network when i turn them back on,
shout at my smart speaker or go onto the bulbs app/ home assistant app to turn a light on or off.
I currently have an S8 amd S9 on the walls to be able to control my lights and so far it works well
Please feel free to download and edit the code as you feel fit for your personal home assistant needs

Please do not redisrubute this app as your own. 
