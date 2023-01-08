package mnu.webdriverwrapper.driver

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.RemoteWebDriver

object Driver {
    private var BROWSER_NAME: DriverType = DriverType.CHROME
    var WAIT_TIME_SEC: Long = 30

    private var driverInstance: RemoteWebDriver? = null

    enum class DriverType {
        CHROME,
        FIREFOX
    }

    fun getDriverInstance(): RemoteWebDriver? {
        driverInstance = driverInstance ?: when (BROWSER_NAME) {
            DriverType.CHROME -> {
                WebDriverManager.chromedriver().setup()
                ChromeDriver()
            }
            DriverType.FIREFOX -> {
                WebDriverManager.firefoxdriver().setup()
                FirefoxDriver()
            }
        }

        return driverInstance
    }

    fun clearDriverInstance() {
        driverInstance!!.manage().deleteAllCookies()
        driverInstance!!.quit()
        driverInstance = null
    }

    fun waitMs() {
        waitMs(100)
    }

    fun waitMs(millis: Int) {
        try {
            Thread.sleep(millis.toLong())
        } catch (ignored: InterruptedException) {
        }
    }
}