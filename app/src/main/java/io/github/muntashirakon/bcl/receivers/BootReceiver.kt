package io.github.muntashirakon.bcl.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.topjohnwu.superuser.Shell
import io.github.muntashirakon.bcl.Constants
import io.github.muntashirakon.bcl.Utils

/**
 * Created by Michael on 20.04.2017.
 *
 * Triggered when the phone finished booting.
 * Checks whether power supply is attached and starts the foreground service if necessary.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) {
            return
        }

        val appContext = context.applicationContext
        // Make sure a future plug-in restarts the limit even if the power
        // broadcast is not delivered to the app (see Utils.scheduleChargeStartJob).
        Utils.scheduleChargeStartJob(appContext)
        // Immediately after BOOT_COMPLETED the su daemon is usually not accepting
        // commands yet. Issuing them right away fails silently, which left the limit
        // unenforced whenever the device powered on with the charger already
        // plugged in (no ACTION_POWER_CONNECTED is broadcast in that case, so
        // PowerConnectionReceiver cannot recover it either). Wait for a root shell
        // first, then apply the limit.
        val pendingResult = goAsync()
        Shell.getShell { shell ->
            try {
                if (!shell.isRoot) {
                    return@getShell
                }
                Utils.setVoltageThreshold(null, true, appContext, null)
                Utils.startServiceIfLimitEnabled(appContext)
                Shell.cmd("cat ${Utils.getVoltageFile()}").submit {
                    if (it.out.isNotEmpty()) {
                        Utils.getSettings(appContext).edit()
                            .putString(Constants.DEFAULT_VOLTAGE_LIMIT, it.out[0]).apply()
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
