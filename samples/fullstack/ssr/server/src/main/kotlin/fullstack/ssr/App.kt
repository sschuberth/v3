package fullstack.ssr

import elide.server.*
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.runtime.Micronaut.build
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.fontFamily
import kotlinx.html.tagext.body
import kotlinx.html.tagext.head
import kotlinx.html.title


/** Self-contained application example, which serves an HTML page, with CSS, that says "Hello, Elide!". */
object App {
  /** GET `/`: Controller for index page. */
  @Controller class Index {
    // Serve the page itself.
    @Get("/") suspend fun index() = ssr {
      head {
        title { +"Hello, Elide!" }
        stylesheet("/styles/main.css")
      }
      body {
        injectSSR()
      }
    }

    // Serve styles for the page.
    @Get("/styles/main.css") fun styles() = css {
      rule("body") {
        backgroundColor = Color("#bada55")
      }
      rule("strong") {
        fontFamily = "-apple-system, BlinkMacSystemFont, sans-serif"
      }
    }
  }

  /** Main entrypoint for the application. */
  @JvmStatic fun main(args: Array<String>) {
    build().args(*args).start()
  }
}
