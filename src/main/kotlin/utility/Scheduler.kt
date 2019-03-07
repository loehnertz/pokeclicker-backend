package utility

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import service.user.balance.BalanceIncreaser
import service.user.balance.BalanceManager

object Scheduler {
    const val BalanceSyncTimeoutInMinutes: Long = 1
    const val BalanceIncreaseSyncTimeoutInSeconds: Long = 5
    const val BalanceIncreaseTimeoutInSeconds: Long = 1

    fun scheduleRecurringTasks() {
        GlobalScope.launch { BalanceManager.syncAllCurrentBalancesToDatabase() }

        GlobalScope.launch { BalanceIncreaser.increaseBalanceBasedOnIncreaseRateOfEveryUser() }

        GlobalScope.launch { BalanceIncreaser.updateIncreaseRateOfEveryUser() }
    }
}
