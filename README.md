# FraudulentDetector
===================================

This project demonstrates how to use Camera2
API and FirebaseVision API to detect and extract texts from images. 

Introduction
------------
This project is to show the use case of quick deposit on Android device. Suppose user wants to take a photo of a check and deposits it to his/her checking account. The photo cannot be an image of dog, cat, laptop, chair, floor, etc. It needs to a check image. However to validate it is a check image, all the work is done on server side nowadays and this project is to use a tensorflowlite model running on Android device to classify the image. With this, 75% of the fake images will be blocked in front end and server doesn't need to handle those http request with fake images.

Pre-requisites
--------------

- Android SDK 27
- Android Build Tools v27.0.2
- Android Support Repository


Getting Started
---------------

This project uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.


License
-------

It is free, and may be redistributed under the terms specified in the [LICENSE](https://github.com/DreamSkyMXDan/Machine-Learning-Project/blob/master/project/LICENSE) file.
