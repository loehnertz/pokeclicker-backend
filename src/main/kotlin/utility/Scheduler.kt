package utility

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.user.balance.BalanceIncreaser
import service.user.balance.BalanceManager
import java.util.concurrent.TimeUnit

object Scheduler {
    private const val BalanceSyncTimeoutInMinutes: Long = 1
    private const val BalanceIncreaseSyncTimeoutInMinutes: Long = 1
    const val BalanceIncreaseTimeoutInMinutes: Long = 1

    fun scheduleRecurringTasks() {
        GlobalScope.launch {
            while (true) {
                BalanceManager.syncAllCurrentBalancesToDatabase()
                delay(TimeUnit.MINUTES.toMillis(BalanceSyncTimeoutInMinutes))
            }
        }

        GlobalScope.launch {
            while (true) {
                BalanceIncreaser.increaseBalanceBasedOnIncreaseRateOfEveryUser()
                delay(TimeUnit.MINUTES.toMillis(BalanceIncreaseTimeoutInMinutes))
            }
        }

        GlobalScope.launch {
            while (true) {
                BalanceIncreaser.updateIncreaseRateOfEveryUser()
                delay(TimeUnit.MINUTES.toMillis(BalanceIncreaseSyncTimeoutInMinutes))
            }
        }
    }
}
