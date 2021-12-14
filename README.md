# Chat app
# A fully working chat application for Android written in Java
## phase 1 - data messages
[![Build Status](https://travis-ci.org/element-io/svg-tags.svg?branch=master)](https://travis-ci.org/element-io/svg-tags)
***
chat app is an android application that as the name implies, allows its users to comunicate with other chat app members
**The application is developed by one inexperienced computer science graduate and is for demonstration purposes only. as it stands for today december 2021, the app shouldn't be used as a main communication program.**
***
## Features

- privacy focuesed!
    * locally stored data (outside of user profile)
        * the data that is stored on googles servers, such as user profile, email, password is nessecery to provide the authentication and message sending functions. your profile data includs the image you provided is stored on googles servers!
        * files, images and voice messages are being uploaded first to google storage and then downloaded at the recipients. once all recipients downloaded the files, they should be deleted from googles storage
        * an alternative, a local one bulit by me (the app developer) is on its way to replace the apps dependency on googles firebase services but it will take time
    * no need to provide your phone number
    * no ads and no spying
    * open source (you can validate those claims)
- supports single user conversations, group conversations and even sms conversations
    * user approval is needed to read and send sms messages (carrier charges may applie)
    * color (colour) coded conversations to easally tell apart different types of conversations
    * capability to mute, block,delete and pin conversation
    * each conversation has its own profile with stats about it
    * search conversations by conversation name or conversation participants names
- supports different messages types and actions
    * text,voice and image messages can be sent from the app
    * messages can be edited and deleted at any time
    * contact read validation - the message sender is notified about the message status (waiting, sent, delivered and read)
    * messages can be stared and searched for
- supports users profiles
    * can view different user profiles
    * sign up with email and password
    * mute and block users in group conversation without effecting the group
- Android
    * creates notifcations for each message based on your preferences
    * can reply from notification
    * using fcm to send messages in a reliable way
        * working on self implamintation to not use googles service
    * material design focused
        * will be updated to support matirial U in android 12
- new features are being added every majour update
    * dropbox backup is on its way (and googles drive backup is right around the corner)!
    * text assist - writing a message list? the numbers will be written automaticaly, saving you time
    * you are welcome to request!

***
## Images from the app
- conversations
<img src="https://github.com/Hanan2412/Chat/blob/master/chatAppImages/conversations.png" width="540" height="1080" />

- conversation
<img src="https://github.com/Hanan2412/Chat/blob/master/chatAppImages/conversation.png" width="540" height="1080" />

- settings
<img src="https://github.com/Hanan2412/Chat/blob/master/chatAppImages/settings.png" width="540" height="1080" />

- user profile
<img src="https://github.com/Hanan2412/Chat/blob/master/chatAppImages/user%20profile.png" width="540" height="1080" />

***

## The technology behind the app

- [Android](https://developer.android.com/) - The android sdk
- [Retrofit](https://square.github.io/retrofit/) - to send messages
- [Picasso](https://square.github.io/picasso/) - easy way to download picuters
- [Firebase](https://firebase.google.com/) - those technologies will be replaced in the future with home made ones
    * [Firebase messaging serive (fcm)](https://firebase.google.com/docs/cloud-messaging/http-server-ref) - a free, unlimited way of sending messages between users
    * [Firebase database](https://firebase.google.com/docs/database) - a limited free database to store user information - the profile they create
    * [Firebase authentication](https://firebase.google.com/docs/auth) - easy way to authenticate users on login and to transfer data between them
- [material design](https://material.io/design) - implements matirial design for consistent and modern look


## How to install?
The app is developed to support android 8 and above - any device released in 2019 and later should run the app just fine.
download the apk to your android device and run it

## Development!

### Road map
| chat | development start (estimated) |development finished (estimated)|version|
|------|---------------------------------|------------------------------|-------|
|android app - phase 1|april 2021|febuery 2022|0.7 
|android app - phase 2|febuery 2022|december 2022| 
|backend|| |
|android wear|||
|web interface||
|windows|||

the road map is not final and **will** probably change

want to help? great! contact me at dorfmanhanan@gmail.com
