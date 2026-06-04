package de.piecha.switchwerk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.piecha.switchwerk.ui.screens.StartScreen
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SwitchWerkTheme {
                StartScreen()
            }
        }
    }
}
