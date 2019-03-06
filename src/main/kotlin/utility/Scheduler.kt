package utility

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.user.balance.BalanceManager
import java.util.concurrent.TimeUnit

object Scheduler {
    private const val BalanceSyncTimeoutInMinutes: Long = 1

    fun scheduleRecurringTasks() {
        GlobalScope.launch {
            while (true) {
                BalanceManager.syncAllCurrentBalancesToDatabase()
                delay(TimeUnit.MINUTES.toMillis(BalanceSyncTimeoutInMinutes))
            }
        }
    }
}
