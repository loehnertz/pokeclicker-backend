package utility

import io.sentry.Sentry
import io.sentry.SentryClient
import io.sentry.SentryClientFactory

object ErrorLogger {
    private const val productionEnvironmentKey = "prod"
    private var sentry: SentryClient? = null

    fun logException(exception: Exception) {
        if (sentry != null) {
            sentry!!.sendException(exception)
        } else {
            println(exception)
        }
    }

    fun setupSentry() {
        if (System.getenv("pokeclicker_environment") == productionEnvironmentKey) {
            Sentry.init(System.getenv("sentry_dsn"))
            sentry = SentryClientFactory.sentryClient()
        }
    }
}
