# HexViewer
[![Build Status](https://github.com/Keidan/HexViewer/actions/workflows/build.yml/badge.svg)][build]
[![CodeFactor](https://www.codefactor.io/repository/github/keidan/hexviewer/badge)][codefactor]
[![Release](https://img.shields.io/github/v/release/Keidan/HexViewer.svg?logo=github)][releases]
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)][license]
[![Tests](https://keidan.testspace.com/spaces/154614/badge?token=1703ccc98e9b749c34f17b691e15d58ce0789f38)][tests]
[![Issues](https://keidan.testspace.com/spaces/154614/metrics/192721/badge?token=5f710af9a9e6285910122d933e899b47c31eeb22)][issues]
[![Weblate](https://hosted.weblate.org/widgets/hexviewer/-/svg-badge.svg)][weblate]

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

**WARNING:** This application cannot open files larger than ~20 MB on recent smartphones and less for older smartphones.

:star2: A special thanks to [@OmlineEditor](https://github.com/OmlineEditor) for her patience and help :pray:, without her this application would not be at this level :blush:.

## Instructions
Download the software :

	mkdir devel
	cd devel
	git clone git://github.com/Keidan/HexViewer.git
	cd HexViewer
 	Use with android studio

## Translations
*   Chinese: [@sr093906](https://github.com/sr093906), [@alchemillatruth](https://hosted.weblate.org/user/alchemillatruth/)
*   English: [@Keidan](https://github.com/Keidan), [@comradekingu](https://github.com/comradekingu)
*   French: [@Keidan](https://github.com/Keidan), [@Edanas](https://hosted.weblate.org/user/Edanas/)
*   German: [@iNtEgraIR2021](https://github.com/iNtEgraIR2021)
*   Japanese: [@gnuhead-chieb](https://github.com/gnuhead-chieb)
*   Norwegian Bokmål: [@comradekingu](https://github.com/comradekingu)
*   Russian: [@OmlineEditor](https://github.com/OmlineEditor)
*   Spanish: [@sguinetti](https://github.com/sguinetti)
*   Turkish: [@ersen0](https://github.com/ersen0)

Note: In the settings, the list of languages is sorted as follows:
*   en-US: English (always first)
*   Other languages according to the alphabetical order of their codes, i.e.:
    * de-DE: German
    * es-ES: Spanish
    * fr-FR: French
    * ja-JP: Japanese
    * nb-NO: Norwegian Bokmål
    * ru-RU: Russian
    * tr-TR: Turkish
    * zh-CN: Chinese
	
Check out [CONTRIBUTING.md](CONTRIBUTING.md), if you're interested in participating.

## Screenshots
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="270px" height="600px" alt="Home screen"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="270px" height="600px" alt="Home screen menu"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" width="270px" height="600px" alt="Hex display update"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" width="270px" height="600px" alt="Hex display update & line numbers portrait"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" width="600px" height="270px" alt="Hex display update & ine numbers landscape"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg" width="270px" height="600px" alt="Update mode"></p>
<p align="center"><img src="fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" width="270px" height="600px" alt="Plain display"></p>

## License
[GNU GPL v3 or later](https://github.com/Keidan/HexViewer/blob/master/license.txt)

[build]: https://github.com/Keidan/HexViewer/actions
[releases]: https://github.com/Keidan/HexViewer/releases
[codefactor]: https://www.codefactor.io/repository/github/keidan/hexviewer
[license]: https://github.com/Keidan/HexViewer/blob/master/license.txt
[tests]: https://keidan.testspace.com/spaces/154614?utm_campaign=badge&utm_medium=referral&utm_source=test
[issues]: https://keidan.testspace.com/spaces/154614/current/Issues?utm_campaign=badge&utm_medium=referral&utm_source=issues
[weblate]: https://hosted.weblate.org/engage/hexviewer/