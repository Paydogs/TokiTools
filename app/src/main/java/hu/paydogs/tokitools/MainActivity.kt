package hu.paydogs.tokitools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import hu.paydogs.tokitools.ui.TokiToolsApp
import hu.paydogs.tokitools.ui.theme.TokiToolsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLanguage(this).applyDefault()
        enableEdgeToEdge()
        setContent {
            TokiToolsTheme {
                TokiToolsApp()
            }
        }
    }
}
