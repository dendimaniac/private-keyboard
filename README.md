# Private Keyboard

## Description
The Private keyboard project will develop a user interface with what one can use a personal phone as keyboard for the device

## Features
* Taking the picture
* Send information to email as a visitor card
* Let user control the application by scaning the QR code inside app

## Prerequisites
* An azure function using signalR service. See [project](https://github.com/private-keyboard-metropolia/private-keyboard-azure-functions)
* A web application interface. See [project](https://github.com/private-keyboard-metropolia/private-keyboard-web-app)

## Installation
* Clone the repo: ` git@github.com:private-keyboard-metropolia/private-keyboard-android-app.git `
* Install 3 modules `activation.jar`, `additionnal.jar` and `mail.jar` in the `libs` folder
* Go to `Data/EmailConfig.java` and change to your sender email.
* Go to `Helpers/QRUtils.java` and change `baseWebAppUrl` to your web application URL.
* Go to `MainActivity.java` and `CustomCameraActivity.java` and change `functionUrl` to your auzre function URL.
* Build and run the project
> The application can run without the third feature if there is no signalR service and web application interface. Running the application without properly install the prerequisites may cause crash problems.

## Concepts implemented
* Azure function with signalR service
* Camera2 API
* Java mail
* Cipher Encryption
* QR code generation

## Contact authors
1. [Vy Nguyen](https://github.com/vynmetropolia)
2. [Nhan Mai](https://github.com/RenMai)
3. [Nhan Nguyen](https://github.com/riofed5)
4. [Quan Dao](https://github.com/dendimaniac)
