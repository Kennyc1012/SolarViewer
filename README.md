# SolarViewer
App to view solar system powered by [Enphase](https://enphase.com/en-us)


<img src="https://raw.githubusercontent.com/Kennyc1012/SolarViewer/master/screenshots/1.png" width="480"/>
<img src="https://raw.githubusercontent.com/Kennyc1012/SolarViewer/master/screenshots/2.png" width="480"/>

# Buidling
To build the app, you need to supply an `APP_ID` and a `USER_ID` in your `gradle.properties` file.

```groovy
android.useAndroidX=true
android.enableJetifier=true
APP_ID="MY_APP_ID"
USER_ID="MY_USER_ID"
```

To create an app id, create an account over at [Enphase's developer site](https://developer.enphase.com/). You can follow this [guide](https://developer.enphase.com/docs/quickstart.html). When your app is created, an `API Key` and an `Authorization URL` will be created.</br>

Once your app is generated, log into your enphase account [here](https://enlighten.enphaseenergy.com/). Once logged in, go to the Authorization URL generated in the above step and allow access.</br>

Once you allow access, go into your settings in your account and scroll all the way to the bottom. Under the `Api Settings` section you should see your application that you gave access to and your user id. </br>

Copy the `API Key` and `user id` into your `gradle.properties` file as shown above. 
