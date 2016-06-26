workload-android
========

Android app for workload monitoring of students at TU Dresden.

* Written by Konstantin Schubert
* Email: konstantin@schubert.fr
* Website: konstantinschubert.com
* *Do not hesitate to contact me with questions.*
* *I am always interested in new projects.*


This repository containes the code for the workload Android app which communicates with the workload web API.
The workload web API is part of the [`workload`](https://github.com/KonstantinSchubert/workload) repository.

## Technology

The app is written as standard, native Android app. The programming language is Java.

## Project Structure and Software Architecture

The project structure follows the standard structure of an [Android Studio](https://developer.android.com/studio/index.html) project. 

The folder `android/workload` contains the directory `app`, 
with the actual Android app and the folder `MPChartLib` with a copy of the 
open-source charting library [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart/tree/master/MPChartLib).

The Java classes for the app are defined in `/android/workload/app/src/main/java/com/gmail/konstantin/schubert/workload/`.
There is a subfolder for Activity classes and a subfolder for [Adapter](https://developer.android.com/reference/android/widget/Adapter.html) classes. 

The app uses a custom [content provider](https://developer.android.com/guide/topics/providers/content-providers.html) called SurveyContentProvider,
which uses an SQL database to store all data. This database is synched with the web service via the web-API using an Android  sync adapter.
The sync adapter in turn makes use of an Android authenticator to manage its authentication with the web API. 
All classes related to the sync process are contained in the [`sync` folder](https://github.com/KonstantinSchubert/workload-android/tree/master/android/workload/app/src/main/java/com/gmail/konstantin/schubert/workload/sync).
The architecture and implementation of these classes is explained in [this android tutorial](https://developer.android.com/training/sync-adapters/index.html) which I used when writing the app.

The synchronization logic defines what entries will be synched under which conditions and how conflicts are resolved.
It is documented [here](documentation/SyncLogic.md).


## Installation

Just install from the play store: https://play.google.com/store/apps/details?id=com.gmail.konstantin.schubert.workload

