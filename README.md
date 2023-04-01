# Snapload
Snapchat tool that allows users to send gallery images and videos or custom stickers to friends using Snapchat's SDK.

Inspo taken from [Chickencam](https://github.com/DarDur-Chickenbone/ChickenCam2020-Android), although all chickencam was, was [Snapchat's sample app](https://github.com/Snapchat/creative-kit-sample/tree/main/android) 1:1

**Not bannable**

## Features
  - Select custom stickers dynamically from gallery
  - Media that doesn't match the Snapchat's 9:16 resolution will be presented with a cropping ability in the app. (or auto-crop), thus removing any black borders you might encounter
  - Add a Lens along with your uploaded image
  - Videos auto compressed and split (to comply with Snapchat's restrictions)
    - Shared media must be 300 MB or smaller.
    - Videos must be 60 seconds or shorter.
    - Videos that are longer than 10 seconds are split up into multiple Snaps of 10 seconds or less.




## How to install
1. Create an account at kit.snapchat.com
2. Create app and scroll to development enviroment. Inside android package type "com.snapchat.kit.chickencam" and switch it on.
4. Open the project and go into AndroidManifest.xml. Inside on Line 24 paste the Auth Code found under where you typed the package name in Snapkit.
5. Go into Snapkit and add your username.
6. Build APK and that's it 
