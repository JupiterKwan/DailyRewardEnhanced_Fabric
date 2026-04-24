# DailyRewardEnhanced - Minecraft Mod for Fabric Server

### Author: JupiterKwan

### CurrentVersion: 1.5

### [![build](https://github.com/JupiterKwan/DailyRewardEnhanced_Fabric/actions/workflows/build.yml/badge.svg)](https://github.com/JupiterKwan/DailyRewardEnhanced_Fabric/actions/workflows/build.yml)

---

## Introduction

Give everyone 1 - 5 random item(s) as reward when they first login everyday. :)

For ```Minecraft version 26.1.2```.

## Usage

Download the Jar Mod from Release, put it into your Fabric server's `````./Mod````` folder, restart your
server and good to go.

## In Game Command

Admin commands are now grouped under ```/dailyreward```:

- ```/dailyreward blacklist list```
- ```/dailyreward blacklist add <itemName>```
- ```/dailyreward blacklist remove <itemName>```
- ```/dailyreward config list```
- ```/dailyreward config add <day> <countMultiplier> <extraDraws>```
- ```/dailyreward config remove <day>```
- ```/dailyreward config reload```

## Config

The mod now stores all runtime configs under ```./config/daily-reward-enhanced/```:

- ```blacklist.json``` reward blacklist entries (all lowercase item ids)
- ```players.json``` player login date + streak data
- ```streak-rewards.json``` streak rule settings (amount multiplier / extra draws)

Legacy files are migrated automatically on server startup.

You can still check ```./daily-reward-enhanced.json``` in the repository as a personal blacklist example.

The example config is below:

```json
[
  "command_block_minecart",
  "recovery_compass"
]
```

Streak rewards example:

```json
{
  "enabled": true,
  "stackByThreshold": true,
  "maxCountMultiplier": 8,
  "maxDrawTimes": 4,
  "rules": [
    {
      "day": 3,
      "countMultiplier": 2,
      "extraDraws": 0
    },
    {
      "day": 7,
      "countMultiplier": 1,
      "extraDraws": 1
    }
  ]
}
```

## Compile

This is a Gradle project made in Intellij Idea with JDK 25. To compile, simply run ```build``` & ```jar```. The output JAR will be located
in ```./build/libs``` folder.

## TODO

- Still thinking...

## Update News

### Version 1.5
- Add support for streak rewards, which can be configured to give players extra reward draws or increase the amount multiplier when they have logged in for a certain number of consecutive days.
- Rewrite the config system to support multiple config files and make it easier to manage different types of configs.
- Now  command ```/dailyreward``` is used to manage both blacklist and streak rewards, with subcommands for listing, adding, removing, and reloading configs.

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



