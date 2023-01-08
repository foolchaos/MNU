package mnu.webdriverwrapper.element

import mnu.webdriverwrapper.driver.Driver
import org.openqa.selenium.By
import org.openqa.selenium.By.ByXPath
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

abstract class BaseElement(
    private val locator: String
) {
    fun click() {
        if (isClickable()) this.getWebElement()?.click()
    }

    fun clickJs() {
        if (isClickable()) Driver.getDriverInstance()?.executeScript("arguments[0].click();", this.getWebElement())
    }

    fun getAttribute(attr: String?): String? {
        return if (isExisting()) this.getWebElement()?.getAttribute(attr) else null
    }

    fun isExisting(): Boolean {
        return checkElement(ExpectedConditions.presenceOfElementLocated(By.xpath(this.locator)))
    }

    fun isVisible(): Boolean {
        return checkElement(ExpectedConditions.visibilityOfElementLocated(By.xpath(this.locator)))
    }

    fun isClickable(): Boolean {
        return checkElement(ExpectedConditions.elementToBeClickable(By.xpath(this.locator)))
    }

    fun isNotDisplayed(): Boolean {
        return try {
            val driver = Driver.getDriverInstance()
            val wait = WebDriverWait(driver, Driver.WAIT_TIME_SEC)
            wait.until(ExpectedConditions.invisibilityOfElementLocated(ByXPath(this.locator)))
            true
        } catch (exc: TimeoutException) {
            false
        }
    }

    fun checkElement(condition: ExpectedCondition<WebElement>): Boolean {
        return try {
            val driver = Driver.getDriverInstance()
            val wait = WebDriverWait(driver, Driver.WAIT_TIME_SEC)
            wait.until(condition)
            true
        } catch (exc: TimeoutException) {
            false
        }
    }

    fun getWebElement(): WebElement? {
        return try {
            val driver = Driver.getDriverInstance()
            val wait = WebDriverWait(driver, Driver.WAIT_TIME_SEC)
            wait.until(ExpectedConditions.presenceOfElementLocated(ByXPath(this.locator)))
        } catch (exc: TimeoutException) {
            null
        }
    }
}