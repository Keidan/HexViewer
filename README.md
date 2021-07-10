# HexViewer
[![Build Status](https://travis-ci.com/Keidan/HexViewer.svg?branch=master)][travis]
[![CodeFactor](https://www.codefactor.io/repository/github/keidan/hexviewer/badge)][codefactor]
[![Release](https://img.shields.io/github/v/release/Keidan/HexViewer.svg?logo=github)][releases]
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)][license]

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="75">](https://f-droid.org/packages/fr.ralala.hexviewer)

(GPL) Android Hex Viewer is a FREE software.

This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.

This application offers the following features:
*   Opening all files without a corresponding Android application (1).
*   Display of the file in hexadecimal (or plain text) with the possibility to modify the content (in hexadecimal only).
*   Saving the file on the smartphone/tablet (2).
*   Search option in the open file (hexadecimal and plain text modes).

(1) At first, the file can only be saved via "save as" and due to Android permissions reasons, the only way to make it appear in the list of recent files is to reopen it.

(2) Due to file permissions, after a "save as", the list of recent files cannot be updated with the new file location.

_Caution: Opening files that are too large seriously degrades application performance and can suddenly stop the application on low-resource devices._

## Instructions
Download the software :

	mkdir devel
	cd devel
	git clone git://github.com/Keidan/HexViewer.git
	cd HexViewer
 	Use with android studio

## Translations
*   Chinese: [@sr093906](https://github.com/sr093906)
*   English: [@Keidan](https://github.com/Keidan)
*   French: [@Keidan](https://github.com/Keidan)
*   German: [@iNtEgraIR2021](https://github.com/iNtEgraIR2021)
*   Russian: [@OmlineEditor](https://github.com/OmlineEditor)
*   Spanish: [@sguinetti](https://github.com/sguinetti)

Note: In the settings, the list of languages is sorted as follows:
*   en-US: English (always first)
*   Other languages according to the alphabetical order of their codes, i.e.:
    * de-DE: German
    * es-ES: Spanish
    * fr-FR: French
    * ru-RU: Russian
    * zh-CN: Chinese

## Screenshots
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="270px" height="600px" alt="Home screen"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="270px" height="600px" alt="Home screen menu"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="270px" height="600px" alt="Hex display update"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="270px" height="600px" alt="Hex display update & line numbers portrait"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" width="600px" height="270px" alt="Hex display update & ine numbers landscape"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg" width="270px" height="600px" alt="Update mode"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" width="270px" height="600px" alt="Plain display"></p>

## License
[GPLv3](https://github.com/Keidan/HexViewer/blob/master/license.txt)

[travis]: https://travis-ci.com/Keidan/HexViewer
[releases]: https://github.com/Keidan/HexViewer/releases
[codefactor]: https://www.codefactor.io/repository/github/keidan/hexviewer
[license]: https://github.com/Keidan/HexViewer/blob/master/license.txt
