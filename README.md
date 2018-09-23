### Firefly III(Android)

An unofficial Android client for [Firefly III](https://github.com/firefly-iii/firefly-iii), written in Kotlin. 

This software is under heavy development and should be considered alpha quality as it might work in unexpected ways. 

Other alternative include [this software](https://github.com/mconway/firefly-app/) written by mconway using Ionic framework. 

Icons in this app are taken from:
1. [Flaticon](https://www.flaticon.com/free-icon/piggy-bank-with-dollar-coin_21239)
2. [Material Design Icons](https://materialdesignicons.com)

### Authentication
In order to start using this software, you have to create a new client in your Firefly Instance. 
1. Login to your Firefly instance
2. Click on `Options`, then `profile`
3. Scroll down to `OAuth Clients` section and click on `Create New Client`
4. Use any name and ensure you are using `http://empty` as your redirect URL. 
5. Click `save`
6. Copy the client secret that you have just created
7. Paste it into the mobile app login screen under `secret` field

| Ensure you use http://empty as your redirect URL | Copy this    | Paste it on your Android device    |
| ------------------------------ | ------------------------------ | ------------------------------ |
| ![1](art/firefly-web-oauth-dialog.png) | ![2](art/firefly-web-oauth-string.png) | ![3](art/firefly-mobile-oauth.jpg) |


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
