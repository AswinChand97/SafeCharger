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

* Use of rate of charge to calculate the estimated time of 85% charge. (The logic has been commented). Need to use [Shared preferences](https://developer.android.com/training/data-storage/shared-preferences) - This is done :)
* There is no UI as of now. (Just the instructions of the usage :P). So atleast a simple UI needs to be shown about the efficient usage of battery or tips.
* The notification should be something like an alarm or a call which seeks the attention of the user. The notification should also have a `turn-off` option. - This is partially done now.

The above list may grow with time. 

PS : I am not an android developer. So there will be lot of code issues. Please feel free to suggest the corrections and contribute to this simple project. 

