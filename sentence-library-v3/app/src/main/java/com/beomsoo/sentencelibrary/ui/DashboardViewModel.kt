package com.beomsoo.sentencelibrary.ui

import com.beomsoo.sentencelibrary.data.ArticleSummary
import com.beomsoo.sentencelibrary.domain.ProgressRules

data class DashboardYearRow(val year:Int, val total:Int, val completed:Int, val inProgress:Int, val progress:Int)

class DashboardViewModel(private val items: List<ArticleSummary>) {
    val resumeItem: ArticleSummary? = items.filter { it.status == "진행중" }.maxByOrNull { it.updatedAt }
        ?: items.filter { it.progress < 100 }.maxByOrNull { it.updatedAt }
    val overallProgress: Int = ProgressRules.average(items.map { it.progress })
    val completedCount: Int = items.count { it.progress >= 100 || it.status == "완성" }
    val inProgressCount: Int = items.count { it.status == "진행중" }
    val unfinishedCount: Int = items.size - completedCount - inProgressCount
    val yearRows: List<DashboardYearRow> = items.groupBy { it.year }.map { (year, rows) ->
        DashboardYearRow(year, rows.size, rows.count { it.progress >= 100 || it.status=="완성" }, rows.count { it.status=="진행중" }, ProgressRules.average(rows.map { it.progress }))
    }.sortedByDescending { it.year }
}
