package life

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GameStateTest : StringSpec({
    "should turn a stick" {
        val state1 = GameState(
            setOf(
                CellCoordinates(0, -1),
                CellCoordinates(0, 0),
                CellCoordinates(0, 1),
            )
        )

        val state2 = state1.calculateNewState()
        state2.getActiveCells() shouldBe setOf(
            CellCoordinates(-1, 0),
            CellCoordinates(0, 0),
            CellCoordinates(1, 0),
        )
    }

    "should update age" {
        val state1 = GameState(
            setOf(
                CellCoordinates(0, -1),
                CellCoordinates(0, 0),
                CellCoordinates(0, 1),
            )
        )

        val state2 = state1.calculateNewState()
        state2.getActiveCells() shouldBe setOf(
            CellCoordinates(-1, 0),
            CellCoordinates(0, 0),
            CellCoordinates(1, 0),
        )

        state2.getCellAge(CellCoordinates(-1, 0)) shouldBe 0
        state2.getCellAge(CellCoordinates(1, 0)) shouldBe 0
        state2.getCellAge(CellCoordinates(0, 0)) shouldBe 1
    }
})
