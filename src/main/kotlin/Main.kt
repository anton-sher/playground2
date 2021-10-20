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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@Composable
@Preview
fun App() {
    val cellSizePixels = 40
    val gridColor = Color.Blue
    val cellColor = Color.Blue
    val gameState: MutableState<Set<CellCoords>> = remember { mutableStateOf(setOf()) }
    val pausedState: MutableState<Boolean> = remember { mutableStateOf(false) }

    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
        {
            if (!pausedState.value) {
                updateState(gameState)
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
                        gameState.value = gameState.value + CellCoords.fromOffset(
                            tapOffset,
                            size.width,
                            size.height,
                            cellSizePixels
                        )
                    },
                    onDoubleTap = {pausedState.value = !pausedState.value}
                )
            }) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawGrid(canvasWidth, canvasHeight, cellSizePixels, gridColor)
            drawState(gameState.value, canvasWidth, canvasHeight, cellSizePixels, cellColor)
        }
    }
}

private fun updateState(gameState: MutableState<Set<CellCoords>>) {
    gameState.value = calculateNewState(gameState.value)
}

fun calculateNewState(state: Set<CellCoords>): Set<CellCoords> {
    val relevantCells = state.flatMap {
        listOf(
            CellCoords(it.x - 1, it.y - 1),
            CellCoords(it.x - 1, it.y),
            CellCoords(it.x - 1, it.y + 1),
            CellCoords(it.x, it.y - 1),
            CellCoords(it.x, it.y),
            CellCoords(it.x, it.y + 1),
            CellCoords(it.x + 1, it.y - 1),
            CellCoords(it.x + 1, it.y),
            CellCoords(it.x + 1, it.y + 1),
        )
    }.toSet()

    return relevantCells.flatMap { cell ->
        val allNeighbours = listOf(
            CellCoords(cell.x - 1, cell.y - 1),
            CellCoords(cell.x - 1, cell.y),
            CellCoords(cell.x - 1, cell.y + 1),
            CellCoords(cell.x, cell.y - 1),
            CellCoords(cell.x, cell.y + 1),
            CellCoords(cell.x + 1, cell.y - 1),
            CellCoords(cell.x + 1, cell.y),
            CellCoords(cell.x + 1, cell.y + 1),
        )
        val activeNeighbours = allNeighbours.intersect(state)
        if (
            state.contains(cell) && (activeNeighbours.size == 2 || activeNeighbours.size == 3)
            || !state.contains(cell) && activeNeighbours.size == 3
        ) {
            listOf(cell)
        } else {
            listOf()
        }
    }.toSet()
}

data class CellCoords(val x: Int, val y: Int) {
    fun toOffset(canvasWidth: Float, canvasHeight: Float, cellSizePixels: Int): Offset {
        return Offset((canvasWidth / 2 + x * cellSizePixels), (canvasHeight / 2 - y * cellSizePixels))
    }

    companion object {
        fun fromOffset(o: Offset, canvasWidth: Int, canvasHeight: Int, cellSizePixels: Int): CellCoords = CellCoords(
            Math.round((o.x - canvasWidth / 2) / cellSizePixels),
            Math.round((canvasHeight / 2 - o.y) / cellSizePixels)
        )
    }
}

fun DrawScope.drawState(
    gameState: Set<CellCoords>,
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
