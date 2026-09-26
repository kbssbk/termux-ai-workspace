package com.beomsoo.sentencelibrary.domain

import com.beomsoo.sentencelibrary.data.StatusDefinition
import kotlin.math.roundToInt

object ProgressRules {
    fun defaultStatuses(): List<StatusDefinition> = listOf(
        StatusDefinition("status-unfinished", "미완", 0, false, false, true, 0),
        StatusDefinition("status-progress", "진행중", 25, true, false, true, 1),
        StatusDefinition("status-done", "완성", 100, false, true, true, 2)
    )

    fun normalize(status: StatusDefinition, requested: Int): Int = when {
        status.completed -> 100
        !status.adjustable -> status.defaultProgress.coerceIn(0, 100)
        else -> requested.coerceIn(1, 99)
    }

    fun average(values: List<Int>): Int = if (values.isEmpty()) 0 else values.map { it.coerceIn(0, 100) }.average().roundToInt()
}
