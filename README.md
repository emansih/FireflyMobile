### Firefly III(Android)

[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/packages/xyz.hisname.fireflyiii/)
           

An unofficial Android client for [Firefly III](https://github.com/firefly-iii/firefly-iii), written in Kotlin. 

### Disclaimer
This software is using Firefly III APIs. It is under heavy development and it might work in unexpected ways. In order to use this app, you must first [setup](https://firefly-iii.readthedocs.io/en/latest/installation/server.html) a [Firefly III](https://firefly-iii.org/) instance. 

Other alternative include [this software](https://github.com/mconway/firefly-app/) written by mconway using Ionic framework. 

### Authentication

Check the [wiki page](https://github.com/emansih/FireflyMobile/wiki/Authentication) for more info


### Features

✔ Support Kitkat and up!

✔ Offline: View offline data

✔ Automation: Add data non-interactively allowing powerful automation. Read the [wiki](https://github.com/emansih/FireflyMobile/wiki/Automation-via-Android-Intents) for more info.

✔ Reporting: Financial reports give you overview of your spending and income 


### Running the Software

#### Building

With your device plugged into your PC and ADB enabled
```bash
git clone https://github.com/emansih/FireflyMobile.git
cd FireflyMobile
./gradlew clean installDebug
```

Please note that `assembleGithubRelease` task requires you to have 3 system environment variables. They are 
*FireflyKeystoreAlias*, *FireflyKeystoreFile* and *FireflyKeystorePassword*


#### Pre-built APK

An APK is provided in the [release page](https://github.com/emansih/FireflyMobile/releases) for convienent purposes. All APKs
are signed with the same release key, this ensure that the APK comes from me and your financial data will not be exposed to nefarious actors. The APK SHA256 public key is `40:F2:02:B8:CC:D1:68:87:56:8A:F7:9E:27:44:5B:E1:82:51:CC:B9:1E:89:08:8B:04:3D:2F:35:A2:0D:C3:8F`. All commits in this repo are signed with my GPG key and the public key can be found on [keybase](https://keybase.io/hisname/pgp_keys.asc) and [Github](https://api.github.com/users/emansih/gpg_keys)


To get latest update notifications, add [this RSS feed](https://github.com/emansih/FireflyMobile/releases.atom). 

It's also available via F-Droid [here](https://f-droid.org/packages/xyz.hisname.fireflyiii/). I have also added my own FDroid repo for users with Privileged Extension. For more info, take a look at the [wiki](https://github.com/emansih/FireflyMobile/wiki/FDroid)

If there is enough interest, I might upload it to Google Play Store.

### Screenshots

| Dashboard | Piggy Bank | Adding Piggy Bank  | Piggy Bank Details |
| :-: | :-: | :-: | :-: |
| ![1](art/screenshot1.png) | ![3](art/screenshot3.png) | ![4](art/screenshot4.png) | ![5](art/screenshot5.png) |

| Transactions | Bills | Login | Report | Bills Details
| :-: | :-: | :-: | :-: | :-: |
| ![2](art/screenshot2.png) | ![6](art/screenshot6.png) | ![7](art/screenshot7.png) | ![8](art/screenshot8.png) | ![9](art/screenshot9.png)


Thanks to [Screener - Better Screenshots](https://play.google.com/store/apps/details?id=de.toastcode.screener&hl=en) for the device frame!

Icons in this app are taken from:
1. [Flaticon](https://www.flaticon.com/free-icon/piggy-bank-with-dollar-coin_21239)
2. [Material Design Icons](https://materialdesignicons.com)
3. [Android Iconics](https://github.com/mikepenz/Android-Iconics)
4. App icon taken from Firefly III server(favicon). [Original source](https://www.kissclipart.com/dinero-no-png-clipart-service-751-05-jh4t51/download-clipart.html)

### Known Limitations / bugs
Heh... I left this to the last.

Please see [this](https://github.com/emansih/FireflyMobile/projects/1) for updates. 

### License
```
    Copyright (C) 2018 Daniel Quah(emansih)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
