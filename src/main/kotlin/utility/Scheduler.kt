package utility

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import service.user.BalanceManager
import java.util.concurrent.TimeUnit

object Scheduler {
    private const val BalanceSyncTimeoutInMinutes: Long = 1

    fun scheduleRecurringTasks() {
        runBlocking {
            launch {
                while (true) {
                    BalanceManager.syncAllCurrentBalancesToDatabase()
                    delay(TimeUnit.MINUTES.toMillis(BalanceSyncTimeoutInMinutes))
                }
            }
        }
    }
}
