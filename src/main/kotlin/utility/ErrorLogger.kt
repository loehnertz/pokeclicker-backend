package utility

import io.sentry.Sentry

object ErrorLogger {
    private const val productionEnvironmentKey = "prod"

    fun setupSentry() {
        if (System.getenv("pokeclicker_environment") == productionEnvironmentKey) {
            Sentry.init(System.getenv("sentry_dsn"))
        }
    }
}
