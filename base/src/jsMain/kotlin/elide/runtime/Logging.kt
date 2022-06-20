package elide.runtime

import kotlin.reflect.KClass


/** Describes an expected class which is able to produce [Logger] instances as a factory. */
@Suppress("MemberVisibilityCanBePrivate", "unused") actual class Logging {
  companion object {
    // Singleton logging manager instance.
    private val singleton = Logging()

    /** @return Generic root logger. */
    fun acquire(): elide.runtime.js.Logger = root()

    /** @return Logger created, or resolved, for the [target] Kotlin class. */
    fun of(target: KClass<*>): elide.runtime.js.Logger = named(
      target.simpleName ?: ""
    )

    /** @return Logger resolved at the root name. */
    fun root(): elide.runtime.js.Logger = named(
      ""
    )

    /** @return Logger created for the specified [name]. */
    fun named(name: String): elide.runtime.js.Logger {
      return if (name.isEmpty() || name.isBlank()) {
        singleton.logger() as elide.runtime.js.Logger
      } else {
        singleton.logger(name) as elide.runtime.js.Logger
      }
    }

    // @TODO(sgammon): logging control JS-side
    internal fun isEnabled(level: LogLevel) = true

    // Static log sender.
    internal fun sendLog(level: LogLevel, messages: List<Any>, levelChecked: Boolean) {
      val enabled = levelChecked || isEnabled(level)
      if (enabled) {
        level.resolve().invoke(messages.toTypedArray())
      }
    }
  }

  /**
   * Acquire a [Logger] for the given logger [name], which can be any identifying string; in JVM circumstances, the full
   * class name of the subject which is sending the logs is usually used.
   *
   * @param name Name of the logger to create and return.
   * @return Desired logger.
   */
  actual fun logger(name: String): Logger {
    return elide.runtime.js.Logger(name)
  }

  /**
   * Acquire a root [Logger] which is unnamed, or uses an empty string value (`""`) for the logger name.
   *
   * @return Root logger.
   */
  actual fun logger(): Logger {
    return elide.runtime.js.Logger(null)
  }
}
