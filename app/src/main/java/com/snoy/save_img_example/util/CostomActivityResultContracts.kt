package com.snoy.save_img_example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat

/**
 * An [ActivityResultContract] to [request a permission][Activity.requestPermissions]
 */
class RequestPermissionResultContract(var onPermissionGranted: () -> Unit={}) : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String): Intent {
        return RequestMultiplePermissions.createIntent(arrayOf(input))
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if (intent == null || resultCode != Activity.RESULT_OK) return false
        val grantResults =
            intent.getIntArrayExtra(RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS)
        val bRet = if (grantResults == null || grantResults.isEmpty()) false else grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (bRet){
            onPermissionGranted()
        }
        return bRet
    }

    override fun getSynchronousResult(
        context: Context, input: String?
    ): SynchronousResult<Boolean>? {
        return if (input == null) {
            SynchronousResult(false)
        } else if (ContextCompat.checkSelfPermission(context, input)
            == PackageManager.PERMISSION_GRANTED
        ) {
            SynchronousResult(true)
        } else {
            // proceed with permission request
            null
        }
    }
}

/**
 * An [ActivityResultContract] to [request permissions][Activity.requestPermissions]
 */
class RequestMultiplePermissions :
    ActivityResultContract<Array<String?>, Map<String?, Boolean>>() {
    override fun createIntent(context: Context, input: Array<String?>): Intent {
        return createIntent(input)
    }

    override fun getSynchronousResult(
        context: Context, input: Array<String?>?
    ): SynchronousResult<Map<String?, Boolean>>? {
        if (input == null || input.isEmpty()) {
            return SynchronousResult(emptyMap())
        }
        val grantState: MutableMap<String?, Boolean> = ArrayMap()
        var allGranted = true
        for (permission in input) {
            val granted = (ContextCompat.checkSelfPermission(
                context,
                permission!!
            )
                    == PackageManager.PERMISSION_GRANTED)
            grantState[permission] = granted
            if (!granted) allGranted = false
        }
        return if (allGranted) {
            SynchronousResult(grantState)
        } else null
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Map<String?, Boolean> {
        if (resultCode != Activity.RESULT_OK) return emptyMap()
        if (intent == null) return emptyMap()
        val permissions =
            intent.getStringArrayExtra(EXTRA_PERMISSIONS)
        val grantResults =
            intent.getIntArrayExtra(RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS)
        if (grantResults == null || permissions == null) return emptyMap()
        val result: MutableMap<String?, Boolean> = HashMap()
        var i = 0
        val size = permissions.size
        while (i < size) {
            result[permissions[i]] = grantResults[i] == PackageManager.PERMISSION_GRANTED
            i++
        }
        return result
    }

    companion object {
        /**
         * An [Intent] action for making a permission request via a regular
         * [Activity.startActivityForResult] API.
         *
         * Caller must provide a `String[]` extra [.EXTRA_PERMISSIONS]
         *
         * Result will be delivered via [Activity.onActivityResult] with
         * `String[]` [.EXTRA_PERMISSIONS] and `int[]`
         * [.EXTRA_PERMISSION_GRANT_RESULTS], similar to
         * [Activity.onRequestPermissionsResult]
         *
         * @see Activity.requestPermissions
         * @see Activity.onRequestPermissionsResult
         */
        const val ACTION_REQUEST_PERMISSIONS =
            "androidx.activity.result.contract.action.REQUEST_PERMISSIONS"

        /**
         * Key for the extra containing all the requested permissions.
         *
         * @see .ACTION_REQUEST_PERMISSIONS
         */
        const val EXTRA_PERMISSIONS = "androidx.activity.result.contract.extra.PERMISSIONS"

        /**
         * Key for the extra containing whether permissions were granted.
         *
         * @see .ACTION_REQUEST_PERMISSIONS
         */
        const val EXTRA_PERMISSION_GRANT_RESULTS =
            "androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS"

        fun createIntent(input: Array<String?>): Intent {
            return Intent(ACTION_REQUEST_PERMISSIONS).putExtra(
                EXTRA_PERMISSIONS,
                input
            )
        }
    }
}
