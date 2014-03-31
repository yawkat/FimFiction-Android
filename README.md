FimFiction-Android
==================

This is an Android client for basic functions of the [FimFiction](http://www.fimfiction.net) website. It utilizes my [FimFiction-API](https://github.com/yawkat/FimFiction-Java).

It's usual development time is early morning and it was not originally intended to be read by others so please forgive the bad code quality and poor documentation.

Aims
----

- Stability: This app is made for the poor mobile internet I often have and provides a reliable access to the FimFiction website. It will handle connection issues appropriately.
- Speed: This app caches a lot of data locally. This includes cover art (only downloaded on wifi) and the story EPUBs themselves.

Download
--------

[![Download](http://ci.yawk.at/job/FimFiction-Android/badge/icon)](http://ci.yawk.at/job/FimFiction-Android)

Manual Setup
-----

- A different icon can be placed in res/drawable/icon.png . The default is a basic placeholder that doesn't look particulary good but will work for testing purposes.
- To compile using maven, run `mvn clean package android:deploy` with your device connected. Your android SDK location must be set in the ANDROID_HOME environment variable. You can find more information on that at [the maven-android-plugin tutorial](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted).
