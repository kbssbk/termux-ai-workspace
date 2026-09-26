package com.beomsoo.sentencelibrary.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beomsoo.sentencelibrary.data.ArticleSummary
import com.beomsoo.sentencelibrary.data.ReadingRepository
import com.beomsoo.sentencelibrary.ui.DashboardViewModel

@Composable
fun HomeScreen(repository:ReadingRepository,onArticle:(String)->Unit) {
    val summaries=repository.articleSummaries.collectAsStateWithLifecycle(emptyList()).value
    val model=DashboardViewModel(summaries)
    LazyColumn(modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item {
            Text("오늘도 이어서 읽어볼까요?",style=MaterialTheme.typography.headlineMedium)
            Text("한 번 기록한 내용은 로컬에 계속 쌓이고, 언제든 다른 형식으로 꺼낼 수 있어요.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            val item=model.resumeItem
            ElevatedCard(shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth().then(if(item!=null) Modifier.clickable{onArticle(item.id)} else Modifier)) {
                Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("이어 읽기",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                    if(item==null) Text("진행 중인 기사가 아직 없습니다.",style=MaterialTheme.typography.titleMedium)
                    else {
                        Text(item.title,style=MaterialTheme.typography.titleLarge)
                        Text("${item.year} · ${item.publicationName} · ${item.issueLabel}",color=MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(progress={item.progress/100f},modifier=Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(item.status); Text("${item.progress}%") }
                        Button(onClick={onArticle(item.id)}) { Text("기사 작업공간 열기"); Spacer(Modifier.width(6.dp)); Icon(Icons.Default.ArrowForward,null) }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Metric("전체",summaries.size.toString(),Modifier.weight(1f)); Metric("완성",model.completedCount.toString(),Modifier.weight(1f)); Metric("진행중",model.inProgressCount.toString(),Modifier.weight(1f))
            }
        }
        item { Text("최근 기사",style=MaterialTheme.typography.titleLarge) }
        items(summaries.sortedByDescending { it.updatedAt }.take(6),key={it.id}) { ArticleRow(it,onArticle) }
    }
}

@Composable private fun Metric(label:String,value:String,modifier:Modifier){
    Card(modifier=modifier,shape=RoundedCornerShape(18.dp)) { Column(Modifier.padding(14.dp)) { Text(label,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant); Text(value,style=MaterialTheme.typography.headlineSmall) } }
}
@Composable private fun ArticleRow(item:ArticleSummary,onArticle:(String)->Unit){
    Card(Modifier.fillMaxWidth().clickable{onArticle(item.id)},shape=RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(item.title,style=MaterialTheme.typography.titleMedium); Spacer(Modifier.height(5.dp)); Text("${item.year} · ${item.issueLabel} · ${item.status} ${item.progress}%",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } }
}
