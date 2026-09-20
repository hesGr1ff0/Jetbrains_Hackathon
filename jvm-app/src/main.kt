import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import wastetrack.ui.scratch.DriveThroughScratchScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        DriveThroughScratchScreen()
    }
}