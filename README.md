# DailyRewardEnhanced - Minecraft Mod for Fabric Server

### Author: JupiterKwan

### CurrentVersion: 1.4

### [![build](https://github.com/JupiterKwan/DailyRewardEnhanced_Fabric/actions/workflows/build.yml/badge.svg)](https://github.com/JupiterKwan/DailyRewardEnhanced_Fabric/actions/workflows/build.yml)

---

## Introduction

Give everyone 1 - 5 random item(s) as reward when they first login everyday. :)

For ```Minecraft version 26.1.2```.

## Usage

Download the Jar Mod from Release, put it into your Fabric server's `````./Mod````` folder, restart your
server and good to go.

## In Game Command

Current has three commands for this plugin: ```/rewardBlackList```, ```/rewardBlackListAdd <itemName>``` and ```/rewardBlackListDelete <itemName>```. The delete command lets admins fix mistaken blacklist entries.

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

## TODO

- Still thinking...

## Update News

### Version 1.4
- Support 26.1.2.
- Add Chinese and English language support.
- Fix some bugs.

### Version 1.3
- Support 1.21.10.

### Version 1.2

- Support to display multi-language item name.
- Optimize player's last-login-date to make it remember who has logged in when server shutdown.

### Version 1.1

- Add luxury roll effect with sound &  ```/Title``` command.
- Add Customized command to add black list item & show reward black list.
- Fix some bugs.

### Version 1.0

#### Features

- Migrate Code from Spigot to Fabric. If you are using ```paper/spigot``` server, you can check out [DailyReward_Spigot](https://github.com/JupiterKwan/DailyReward_Spigot).



