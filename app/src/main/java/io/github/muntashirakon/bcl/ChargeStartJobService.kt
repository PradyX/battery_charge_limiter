// SPDX-License-Identifier: GPL-3.0-or-later

package io.github.muntashirakon.bcl

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * JobScheduler entry point that starts the charge limit when the device begins
 * charging.
 *
 * Manifest-declared power broadcasts are not delivered to apps that are not
 * running on several ROMs (observed on a Samsung Galaxy A7 Lite running an
 * Android 13 GSI: "Background execution not allowed"), so after a normal
 * unplug the limit never restarted when the charger was plugged back in. A
 * persisted job with the charging constraint is run by the system instead and
 * does not depend on the app being alive. It is (re)scheduled whenever the
 * foreground service stops and at boot.
 */
class ChargeStartJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        if (Utils.getSettings(this).getBoolean(Constants.CHARGE_LIMIT_ENABLED, false)) {
            ContextCompat.startForegroundService(this, Intent(this, ForegroundService::class.java))
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}
