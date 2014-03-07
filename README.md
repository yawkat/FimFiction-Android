FimFiction-Android
==================

This is an Android client for basic functions of the [FimFiction](http://www.fimfiction.net) website. It utilizes my [FimFiction-API](https://github.com/yawkat/FimFiction-Java).

It's usual development time is early morning and it was not originally intended to be read by others so please forgive the bad code quality and poor documentation.

Aims
----

- Stability: This app is made for the poor mobile internet I often have and provides a reliable access to the FimFiction website. It will handle connection issues appropriately.
- Speed: This app caches a lot of data locally. This includes cover art (only downloaded on wifi) and the story EPUBs themselves.

Setup
-----

- The default, un-customizable cache location is /storage/extSdCard/FimFiction . If you do not have storage at that location you will need to edit it manually (at.yawk.fimfiction.android.Constants).
- The icon must be downloaded to res/drawable/icon.png . I use the FimFiction logo but decided against uploading it here for obvious reasons.

Dependencies:
- FimFiction-Java
    - commons-lang
    - commons-logging
    - commons-codec
    - httpclient
    - httpcore
    - tagsoup
- Google Gson
- Google Guava
