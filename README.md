# Chit-Chat-App
 This is a software project, from UoM, mobile development, which is basically implemented a chat application, allowing communication between users.
## Features
- Authentication
  - Login & Loginout: allowing users to successfully enter and withdraw the app, depending on the token modification.
  - Registration: allowing users to input their basic information, like email, password, etc. Additional, safety question has been added to help users find their password.
  - Forget Password: reset password depending on the safety question & answer, which is set in the registration step previously. 
- Sending text message: allowing real-time communication between users, based on firebase.
- Sending images
- Notification: implementing a facility to verify whether a user is online or not; Implementing a push notification facility. The system will send notifications when message-receiver is not online.
- Updating recent conversation: once the conversation has been done, people who users just chat will appear on the main page, and showing the content.
- Video Call(sensor used: Audio, Camera): Using the **agora API** to build this function which could have mutiple people to use this room or two people chat alone. # Notes: install this application first, then, chit-chat app.
- Send Location(Sensor used: Location): User could send his/her location to others, and others could open it in google map.
- Weather Live(Sensor used: Ambient Temperature, Relative Humidity): User could view the live temperature and humidity in the main interface.
## Pre-requisites
To run this project, sdk and [jdk]: https://www.oracle.com/java/technologies/downloads/ will be needed.

![jdk version](https://github.com/MobileGroup-T01-06-1/Chit-Chat-App/blob/master/document%20images/jdk.PNG)

![sdk version](https://github.com/MobileGroup-T01-06-1/Chit-Chat-App/blob/master/document%20images/sdk.PNG)

**Notice: We highly recommend sdk 30 as compile sdk version**


## Preparation
Prepare the Java running environment, then download Android studio and install the SDK. Use the GIT tool to download the team project locally. The Google services JSON file in the project. This project is to design a chat software, so we choose to use the firebase framework to add it to the Android project. The specific steps are as follows: 

First, add the name of the Android package to the Firebase web page, and then debug the signature certificate. Open CMD, enter [keytool - List - V - keystore + file path of debug.keystore], enter the keystore type and keystore provider, and you can get the signing certificate. The next step is to modify the gradle file, which is divided into app level and project level. It mainly involves modifying the SDK version, java version, and some dependent versions.

Then you need to create a database, select the production mode, and then select the time zone (nam8). What you need to do in this part is to modify the rules of the database. This can better protect our database.

The final preparation is to modify virtual devices to multiple devices. This is because the team project is chat software and requires multiple devices to communicate.



## Working Processing(After building prototype 1)
2021/10/11 Building prototype of chit-chat app, adding real time text-communication function.

2021/10/16 Bug, too much blank between sender and receiver message, has been fixed. Annotations have been added.
 
2021/10/17 adding forget password function.

2021/10/18 fixed bugs about forget password and select user.

2021/10/19 implement a facility to show recent conversations, including:

1. showing the last recent message in conversations.
2. updating recent conversations real-time.
3. User-list will update when new message has been arrived.

2021/10/20 adding a kit of floating buttons with extra functions and voice button on the chat bar.

2021/10/26 adding send users' location function.

2021/10/28 Bug fixed: click send button to send message, even users did not input any message.

2021/10/29 implementing a facility to verify whether a user is online or not; Implementing a push notification facility. The system will send notifications when message-receiver is not online.

2021/10/29 design user profile layout.

2021/10/30 adding new sensors of temperature and humidity; Click button updating; UI adjusting.
