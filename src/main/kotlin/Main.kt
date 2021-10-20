import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import life.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


@Composable
@Preview
fun App() {
    val cellSizePixels = 40
    val gridColor = Color.Blue
    val cellColor = Color.Blue
    val gameState: MutableState<GameState> = remember { mutableStateOf(GameState.random(20, 0.1)) }
    val pausedState: MutableState<Boolean> = remember { mutableStateOf(false) }

    Executors.newSingleThreadScheduledExecutor {
        val t = Thread(it)
        t.isDaemon = true
        t
    }.scheduleAtFixedRate(
        {
            if (!pausedState.value) {
                gameState.value = gameState.value.calculateNewState()
            }
        },
        1000,
        500,
        TimeUnit.MILLISECONDS
    )

    DesktopMaterialTheme {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset: Offset ->
                        val clickedCell = tapOffset.toCellCoordinates(
                            size.width,
                            size.height,
                            cellSizePixels
                        )
                        val oldState = gameState.value
                        val ns = oldState.activateRandomCellsAround(clickedCell)
                        gameState.value = ns
                    },
                    onDoubleTap = { pausedState.value = !pausedState.value }
                )
            }) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawGrid(canvasWidth, canvasHeight, cellSizePixels, gridColor)
            drawState(gameState.value.getActiveCells(), canvasWidth, canvasHeight, cellSizePixels, cellColor)
        }
    }
}


fun DrawScope.drawState(
    gameState: Set<CellCoordinates>,
    canvasWidth: Float,
    canvasHeight: Float,
    cellSizePixels: Int,
    cellColor: Color
) {
    gameState.forEach {
        val ofs = it.toOffset(canvasWidth, canvasHeight, cellSizePixels)
        val rectOffset = Offset(ofs.x - cellSizePixels / 2, ofs.y - cellSizePixels / 2)
        val rectSize = Size(cellSizePixels.toFloat(), cellSizePixels.toFloat())
        drawRect(
            cellColor,
            rectOffset,
            rectSize
        )
    }
}

private fun DrawScope.drawGrid(
    canvasWidth: Float,
    canvasHeight: Float,
    cellSizePixels: Int,
    gridColor: Color
) {
    val xCenter = canvasWidth / 2
    val yCenter = canvasHeight / 2

    val xCellCount = (canvasWidth / cellSizePixels).toInt()
    val yCellCount = (canvasHeight / cellSizePixels).toInt()

    (-xCellCount - 1..xCellCount).forEach {
        drawLine(
            start = Offset(x = xCenter + it * cellSizePixels + cellSizePixels / 2, y = 0f),
            end = Offset(x = xCenter + it * cellSizePixels + cellSizePixels / 2, y = canvasHeight),
            color = gridColor
        )
    }

    (-yCellCount - 1..yCellCount).forEach {
        drawLine(
            start = Offset(x = 0f, y = yCenter + it * cellSizePixels + cellSizePixels / 2),
            end = Offset(x = canvasWidth, y = yCenter + it * cellSizePixels + cellSizePixels / 2),
            color = gridColor
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

fun CellCoordinates.toOffset(canvasWidth: Float, canvasHeight: Float, cellSizePixels: Int): Offset {
    return Offset((canvasWidth / 2 + x * cellSizePixels), (canvasHeight / 2 - y * cellSizePixels))
}

fun Offset.toCellCoordinates(canvasWidth: Int, canvasHeight: Int, cellSizePixels: Int) = CellCoordinates(
    ((this.x - canvasWidth / 2) / cellSizePixels).roundToInt(),
    ((canvasHeight / 2 - this.y) / cellSizePixels).roundToInt()
)