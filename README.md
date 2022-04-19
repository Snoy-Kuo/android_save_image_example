# save_image_example

An Android example App that shows how to load, save and share images.<br/>
There are the main featues:

 - Load image from
   - Internet
   - Resources folder
   - Assets folder
 - Save image to
   - App folder
   - System Pictures folder
   - System Download folder
 - Share image to other Apps using
   - Intent Resolver
   - Sharesheet

## Tested platforms
 - [x] Android 13 (API Tiramisu)   
 - [x] Android 12L (API 32, S_V2)   
 - [x] Android 12 (API 31, S)   
 - [x] Android 11 (API 30, R)   
 - [x] Android 10 (API 29, Q)   
 - [x] Android 9 (API 28, P)   
 - [x] Android 8.1 (API 27, O_MR1)   
 - [x] Android 8 (API 26, O)   
 - [x] Android 7.1.1 (API 25, N_MR1)   
 - [x] Android 7 (API 24, N)
 - [x] Android 6 (API 23, M)
 - [x] Android 5.1 (API 22, LOLLIPOP_MR1)   

## Dev env

 - macOS 12.2.1 (Monterey) x64
 - Android Studio Bumblebee Patch 2
 - Android SDK version 32
 - JDK: 11
 - Gradle: 7.2
 - Kotlin: 1.6.10

 ## References

 - [Support loading images from AssetManager](https://github.com/coil-kt/coil/issues/10)
 - [onRequestPermissionsResult() is deprecated. Are there any alternatives?](https://stackoverflow.com/questions/66551781/android-onrequestpermissionsresult-is-deprecated-are-there-any-alternatives)
 - [How to save file to external storage in Android 10 and Above](https://medium.com/@thuat26/how-to-save-file-to-external-storage-in-android-10-and-above-a644f9293df2)
 - [android share image in ImageView without saving in sd card](https://microeducate.tech/android-share-image-in-imageview-without-saving-in-sd-card/)
 - [How To Share An Image From Your Android App Without Exposing It To The Gallery](https://medium.com/tech-takeaways/how-to-share-an-image-from-your-android-app-without-exposing-it-to-the-gallery-e9a7a214eb2c)
 - [Sending simple data to other apps](https://developer.android.com/training/sharing/send)

 ## Libraries

 - [coil](https://github.com/coil-kt/coil)
 - [lifecycle-runtime-ktx](https://developer.android.com/jetpack/androidx/releases/lifecycle)


 ## Todos

 - move action events and states to vm.