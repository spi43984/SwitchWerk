package de.piecha.switchwerk.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme
import de.piecha.switchwerk.viewmodel.DeviceWifiProximityStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceWifiProximityIndicatorTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longDeviceNameKeepsAccessibleIndicatorVisible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val deviceName = "A very long device name that must be shortened on a small widget"
        val description = context.getString(R.string.wifi_proximity_nearby)

        composeRule.setContent {
            SwitchWerkTheme {
                DeviceTitle(
                    name = deviceName,
                    wifiProximityStatus = DeviceWifiProximityStatus.NEARBY,
                    isActionRunning = false,
                    maxLines = 1
                )
            }
        }

        composeRule.onNodeWithText(deviceName).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun runningActionIsIncludedInIndicatorDescription() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val description = context.getString(
            R.string.wifi_proximity_action_running,
            context.getString(R.string.wifi_proximity_not_nearby)
        )

        composeRule.setContent {
            SwitchWerkTheme {
                DeviceTitle(
                    name = "Device",
                    wifiProximityStatus = DeviceWifiProximityStatus.NOT_NEARBY,
                    isActionRunning = true,
                    maxLines = 1
                )
            }
        }

        composeRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }
}
