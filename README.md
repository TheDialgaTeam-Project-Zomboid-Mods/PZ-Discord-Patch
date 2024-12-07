<div id="top"></div>

# Project Zomboid Discord Patch

<summary>Table of Contents</summary>
<ol>
    <li><a href="#about-this-mod">About This Mod</a></li>
    <li><a href="#built-instructions">Built Instructions</a></li>
    <li><a href="#installation">Installation</a></li>
</ol>

## About This Mod

This mod upgrade the existing javacord 2.0.17 to javacord 3.8.0.

<p align="right">(<a href="#top">back to top</a>)</p>

## Built Instructions

This project is build with [gradle](https://gradle.org/install/).

1. Clone / Download the git repository source code. <br />
Git CLI: `git clone https://github.com/TheDialgaTeam-Project-Zomboid-Mods/PZ-Discord-Patch.git`

2. Edit `build.gradle.kts` rootDir to your Project Zomboid Dedicated Server location. <br />
For Windows: `val rootDir = "C:/Program Files (x86)/Steam/steamapps/common/Project Zomboid Dedicated Server/"`

3. Build the program with `gradle`. <br />
CLI: `gradlew shadowJar`

<p align="right">(<a href="#top">back to top</a>)</p>

## Installation

Note: This require root access to your Project Zomboid Dedicated Server directory. If you are using a hosting provider that host the PZ server for you, you are unable to do the installation as you can't change the classpath.

1. Copy `PZDiscordPatch-1.0.0.jar` into Project Zomboid Dedicated Server's java directory. <br />
For Windows: `C:/Program Files (x86)/Steam/steamapps/common/Project Zomboid Dedicated Server/java/`

2. Edit the classpath to include `java/PZDiscordPatch-1.0.0.jar` at the beginning. (For json file, please put it before `java/.` entry)

3. Restart your server and check the console. If done right, it should complain about missing Log4j2 as Project Zomboid uses a custom Logger. (`org.javacord.core.util.logging.ExceptionLoggerDelegateImpl No Log4j2 compatible logger was found. Using default Javacord implementation!`)

Note that you are required to enable the ___Message Content Intent___ for the bot that you have created. Otherwise discord module will not run.

<p align="right">(<a href="#top">back to top</a>)</p>
