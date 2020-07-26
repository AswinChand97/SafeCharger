# ChargeStatusChecker
This is a simple android application to maintain healthy battery life. 
It has been advised by some technology experts that the phone should not be  charged to a complete charge cycle repeatedly in order to maintain a good battery life.
Also, this is just a small idea which emerged when I heard the shocking news of my friend's phone exploded near his head after a complete charge.
Fortunately, nothing happened to him.

The following is the working of the application : 
* The application will be always idle
* When the android phone goes to the charging state, a background thread will be fired which checks the battery level in an optimized way.
* The application will trigger a notification sound when the charge reaches 85%.

The application needs a lot of improvements. Only the basic functionality is ready as of now. I will just list down the things need to be done : 

* There is no UI as of now. (Just the instructions of the usage :P). So atleast a simple UI needs to be shown about the efficient usage of battery or tips.
* The application should also have a `turn-off` option for the notification.
* For devices having android 8 and above (mostly all the devices), the user may have to perform an additional step in the settings to receive the notification, if the application is not running. Unfortunately, only few [broacast receiver intent exceptions](https://developer.android.com/guide/components/broadcast-exceptions) are provided by Android for API level 26 and above and the required ACTION_POWER_CONNECTED intent is not available. This should be fixed.
* Also, due to the above point, the target SDK version must be set to 25 to receive the intent in the devices having android 8 and above now. Due to this, we could not upload the application to Google Play Store. Thus, we need to identify some means to receive the ACTION_POWER_CONNECTED intent.

The above list may grow with time. 

PS : I am not an android developer. So there will be lot of code issues. Please feel free to suggest the corrections and contribute to this simple project. 

