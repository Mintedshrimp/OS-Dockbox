package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.HeaderBar
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun header_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HeaderBar(title = "OS Dockbox", statusText = "SESSION LIVE", version = "v0.1.1")
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/header.png")
    }
}
