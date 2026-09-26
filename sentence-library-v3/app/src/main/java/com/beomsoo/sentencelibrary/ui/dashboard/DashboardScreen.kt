package com.beomsoo.sentencelibrary.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beomsoo.sentencelibrary.data.ReadingRepository
import com.beomsoo.sentencelibrary.ui.DashboardViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(repository:ReadingRepository,onYear:(Int)->Unit) {
    val summaries=repository.articleSummaries.collectAsStateWithLifecycle(emptyList()).value
    val model=DashboardViewModel(summaries)
    val remaining=summaries.count { it.progress<100 }
    val estimate=LocalDate.now().plusDays(remaining.toLong())
    LazyColumn(contentPadding=PaddingValues(18.dp,18.dp,18.dp,96.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("전체 진행 대시보드",style=MaterialTheme.typography.headlineMedium); Text("몇 년이 지나도 한눈에 전체 흐름을 확인할 수 있어요.",color=MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            ElevatedCard(shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("전체 진행률",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary); Text("${model.overallProgress}%",style=MaterialTheme.typography.displaySmall); LinearProgressIndicator(progress={model.overallProgress/100f},modifier=Modifier.fillMaxWidth())
            }}
        }
        item { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) { Metric("전체",summaries.size,Modifier.weight(1f));Metric("완성",model.completedCount,Modifier.weight(1f));Metric("진행중",model.inProgressCount,Modifier.weight(1f));Metric("미완",model.unfinishedCount,Modifier.weight(1f)) } }
        item { Card(shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("하루 1기사 기준",style=MaterialTheme.typography.labelLarge); Text("남은 ${remaining}개 · 예상 완료 $estimate",style=MaterialTheme.typography.bodyLarge) } } }
        item { Text("연도별 진행률",style=MaterialTheme.typography.titleLarge) }
        items(model.yearRows,key={it.year}) { row ->
            Card(shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth().clickable{onYear(row.year)}) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("${row.year}",style=MaterialTheme.typography.titleMedium);Text("${row.progress}%")}
                LinearProgressIndicator(progress={row.progress/100f},modifier=Modifier.fillMaxWidth())
                Text("완성 ${row.completed} · 진행중 ${row.inProgress} · 전체 ${row.total}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }}
        }
    }
}
@Composable private fun Metric(label:String,value:Int,modifier:Modifier){ Card(modifier,shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(10.dp)){Text(label,style=MaterialTheme.typography.labelSmall);Text(value.toString(),style=MaterialTheme.typography.titleLarge)}} }
