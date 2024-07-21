# DailyRewardEnhanced - Minecraft Mod for Fabric Server

---

### Author: JupiterKwan

### CurrentVersion: 1.0.0

---

## Introduction

Give everyone 1 - 5 random item(s) as reward when they first login everyday. :)

For ```Minecraft version 1.21``` and works perfectly on my ```1.21 Fabric Server```. It should work just fine on ```1.21.x```
versions as well (if it exists).

## Usage

Download the Jar Mod from Release, put it into your Fabric server's `````./Mod````` folder, restart your
server and good to go.

## Config

The default config will be generated in ```./config/daily-reward-enhanced.json```, remember to use all lowercase item-id.

You can check out the code folder ```./daily-reward-enhanced.json``` where is saved my personal config file.

The example config is below:

```json
[
  "command_block_minecart",
  "recovery_compass"
]
```

## Compile

This is a Gradle project made in Intellij Idea with JDK 21. To compile, simply run ```build``` & ```jar```. The output JAR will be located
in ```./build/libs``` folder.

---

## Version 1.0.0

### Features

- Migrate Code from Spigot to Fabric. If you are using ```paper/spigot``` server, you can check out [DailyReward_Spigot](https://github.com/JupiterKwan/DailyReward_Spigot).

### TODO

- Luxury roll effect with sound &  ```/Title``` command.
- Add Customized command to add black list item & show reward black list.
- Optimize player's last-login-date to make it remember who has logged in when server shutdown.

