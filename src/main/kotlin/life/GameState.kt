package life

import java.util.concurrent.ThreadLocalRandom

data class CellCoordinates(val x: Int, val y: Int)

class GameState(
    private val activeCells: Set<CellCoordinates> = setOf(),
    private val cellAges: Map<CellCoordinates, Int> = activeCells.associateWith { 0 }
) {
    fun getActiveCells(): Set<CellCoordinates> = activeCells

    fun getCellAge(cellCoordinates: CellCoordinates): Int {
        return cellAges[cellCoordinates]!!
    }

    fun calculateNewState(): GameState {
        val relevantCells = activeCells.flatMap {
            listOf(
                CellCoordinates(it.x - 1, it.y - 1),
                CellCoordinates(it.x - 1, it.y),
                CellCoordinates(it.x - 1, it.y + 1),
                CellCoordinates(it.x, it.y - 1),
                CellCoordinates(it.x, it.y),
                CellCoordinates(it.x, it.y + 1),
                CellCoordinates(it.x + 1, it.y - 1),
                CellCoordinates(it.x + 1, it.y),
                CellCoordinates(it.x + 1, it.y + 1),
            )
        }.toSet()

        val newActiveCells = relevantCells.flatMap { cell ->
            val allNeighbours = listOf(
                CellCoordinates(cell.x - 1, cell.y - 1),
                CellCoordinates(cell.x - 1, cell.y),
                CellCoordinates(cell.x - 1, cell.y + 1),
                CellCoordinates(cell.x, cell.y - 1),
                CellCoordinates(cell.x, cell.y + 1),
                CellCoordinates(cell.x + 1, cell.y - 1),
                CellCoordinates(cell.x + 1, cell.y),
                CellCoordinates(cell.x + 1, cell.y + 1),
            )
            val activeNeighbours = allNeighbours.intersect(activeCells)
            if (
                activeCells.contains(cell) && (activeNeighbours.size == 2 || activeNeighbours.size == 3)
                || !activeCells.contains(cell) && activeNeighbours.size == 3
            ) {
                listOf(cell)
            } else {
                listOf()
            }
        }.toSet()
        return GameState(
            newActiveCells,
            newActiveCells.associateWith { (cellAges[it]?:-1) + 1 }
        )
    }

    fun activateCells(newActiveCells: Collection<CellCoordinates>) = GameState(
        this.activeCells + newActiveCells,
        this.cellAges + (newActiveCells - activeCells).associateWith { 0 }
    )

    fun activateRandomCellsAround(clickedCell: CellCoordinates): GameState {
        val random = ThreadLocalRandom.current()
        val ns = this.activateCells((1..(4 + random.nextInt(8))).map {
            CellCoordinates(
                clickedCell.x + random.nextInt(5) - 2,
                clickedCell.y + random.nextInt(5) - 2
            )
        }.toSet())
        return ns
    }

    companion object {
        fun random(size: Int, frequency: Double): GameState = GameState(
            (-size..size).flatMap { x ->
                (-size..size).flatMap { y ->
                    if (ThreadLocalRandom.current().nextDouble() < frequency) {
                        listOf(CellCoordinates(x, y))
                    } else {
                        listOf()
                    }
                }
            }.toSet()
        )
    }
}
