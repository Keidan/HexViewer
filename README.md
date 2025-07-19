# HexViewer
[![Build Status](https://github.com/Keidan/HexViewer/actions/workflows/build.yml/badge.svg)][build]
[![Release](https://img.shields.io/github/v/release/Keidan/HexViewer.svg?logo=github)][releases]
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)][license]
[![Tests](https://keidan.testspace.com/spaces/154614/badge?token=1703ccc98e9b749c34f17b691e15d58ce0789f38)][tests]
[![Issues](https://keidan.testspace.com/spaces/154614/metrics/192721/badge?token=5f710af9a9e6285910122d933e899b47c31eeb22)][issues]
[![Weblate](https://hosted.weblate.org/widgets/hexviewer/-/svg-badge.svg)][weblate]
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=bugs)][sonarcloud]
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=code_smells)][sonarcloud]
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=duplicated_lines_density)][sonarcloud]
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=vulnerabilities)][sonarcloud]
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=sqale_rating)][sonarcloud]
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=reliability_rating)][sonarcloud]
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=security_rating)][sonarcloud]
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=sqale_index)][sonarcloud]
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=Keidan_HexViewer&metric=ncloc)][sonarcloud]


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
	
## Contributions

### Bug fixes

You can simply report a problem via an issue, or even provide pull requests with fixes for a bug you discovered.

About the style of the code: Try to respect the original style as much as possible.

### Translations
You are invited to provide translations for the app. But there are some rules.
* Please translate only if you're **familiar with the nomenclature of the Android system**. If you're not sure, check the official documentation and see how it should be done.
* Contribute only if you commit to translating the **majority** of the strings. No one wants an application that is a mix of English and native language. I may remove translations, if they are not updated on new string resources.
* So that I can integrate it correctly, please give me the translation of the new language.

Translations can be provided via [Weblate][weblate].

Note: In the settings, the list of languages is sorted as follows (according to the alphabetical order of their codes):
| Code | Language | Contributors |
| ---- | -------- | ------------ |
| ar | Arabic | [@sec.xyx](https://hosted.weblate.org/user/sec.xyx/), [@amro](https://hosted.weblate.org/user/amro/) |
| de-DE | German | [@iNtEgraIR2021](https://github.com/iNtEgraIR2021), [@eUgEntOptIc44](https://github.com/eUgEntOptIc44), (+DeepL) |
| en-US | English | [@Keidan](https://github.com/Keidan), [@comradekingu](https://github.com/comradekingu) |
| es-ES | Spanish | [@sguinetti](https://github.com/sguinetti), [@gallegonovato](https://hosted.weblate.org/user/gallegonovato/), (+DeepL) |
| fr-FR | French | [@Keidan](https://github.com/Keidan), [@Edanas](https://hosted.weblate.org/user/Edanas/) |
| hi | Hindi | [@Shopimisrel](https://github.com/Scrambled777) |
| hu-HU | Hungarian | [@SziaTomi](https://github.com/SziaTomi) |
| in-ID | Indonesian | [@Velorixen](https://github.com/Velorixen) |
| it-IT | Italian | [@eUgEntOptIc44](https://github.com/eUgEntOptIc44) |
| iw | Hebrew | [@Shopimisrel](https://github.com/Shopimisrel) |
| ja-JP | Japanese | [@gnuhead-chieb](https://github.com/gnuhead-chieb) |
| nb-NO | Norwegian Bokmål | [@comradekingu](https://github.com/comradekingu) |
| nl-NL | Dutch (Nederlands) | [@AudeBelgraver](https://hosted.weblate.org/user/AudeBelgraver/) |
| pt-BR | Portuguese (Brazil) | [@Bardock88](https://github.com/Bardock88), [@pedrolinharesmoreira](https://github.com/pedrolinharesmoreira) |
| pt-PT | Portuguese | [@SantosSi](https://github.com/SantosSi) |
| ru-RU | Russian | [@OmlineEditor](https://github.com/OmlineEditor), [@AHOHNMYC](https://github.com/AHOHNMYC), [@racingrebel](https://github.com/racingrebel), [@Usland123](https://github.com/Usland123) |
| ta-IN | Tamil | [@TamilNeram](https://github.com/TamilNeram) |
| tr-TR | Turkish | [@ersen0](https://github.com/ersen0) |
| vi-VN | Vietnamese | [@unbiaseduser](https://github.com/unbiaseduser) |
| zh-CN | Chinese | [@sr093906](https://github.com/sr093906), [@alchemillatruth](https://hosted.weblate.org/user/alchemillatruth/), [@hamburger1024](https://hosted.weblate.org/user/hamburger1024/), [@inworless](https://github.com/inworless) |

The current status of the translations is as follows:

[![Weblate](https://hosted.weblate.org/widgets/hexviewer/-/strings/multi-auto.svg)][weblate]

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
[license]: https://github.com/Keidan/HexViewer/blob/master/license.txt
[tests]: https://keidan.testspace.com/spaces/154614?utm_campaign=badge&utm_medium=referral&utm_source=test
[issues]: https://keidan.testspace.com/spaces/154614/current/Issues?utm_campaign=badge&utm_medium=referral&utm_source=issues
[weblate]: https://hosted.weblate.org/engage/hexviewer/
[sonarcloud]: https://sonarcloud.io/summary/new_code?id=Keidan_HexViewer
