package com.beomsoo.sentencelibrary.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beomsoo.sentencelibrary.data.*
import com.beomsoo.sentencelibrary.sync.OfficialSyncService
import java.time.Year
import kotlinx.coroutines.launch

@Composable
fun PublicationsScreen(repository:ReadingRepository,initialYear:Int=0,onArticle:(String)->Unit) {
    val issues=repository.issues.collectAsStateWithLifecycle(emptyList()).value;val articles=repository.articles.collectAsStateWithLifecycle(emptyList()).value;val publications=repository.publications.collectAsStateWithLifecycle(emptyList()).value;val statuses=repository.statuses.collectAsStateWithLifecycle(emptyList()).value
    val pubById=publications.associateBy{it.id};val statusById=statuses.associateBy{it.id}
    var year by remember(initialYear,issues){mutableIntStateOf(if(initialYear>0)initialYear else issues.maxOfOrNull{it.year}?:Year.now().value)};var filter by remember{mutableStateOf("전체")};val expanded=remember{mutableStateListOf<String>()};val scope=rememberCoroutineScope();var syncText by remember{mutableStateOf("")};var showIssue by remember{mutableStateOf(false)};var articleIssue by remember{mutableStateOf<Issue?>(null)}
    val years=(issues.map{it.year}+(2001..Year.now().value)).distinct().sortedDescending()
    val visible=issues.filter{it.year==year}.filter{issue->if(filter=="전체")true else{val names=articles.filter{it.issueId==issue.id}.mapNotNull{statusById[it.statusId]?.name};when(filter){"완성"->names.isNotEmpty()&&names.all{it=="완성"};"진행중"->names.any{it=="진행중"};"미완"->names.isEmpty()||names.all{it=="미완"};else->true}}}
    Box(Modifier.fillMaxSize()){
        LazyColumn(contentPadding=PaddingValues(18.dp,18.dp,18.dp,96.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{Text("출판물",style=MaterialTheme.typography.headlineMedium);Text("연도 → 호수 → 기사 순서로 빠르게 찾고, 카드 어디를 눌러도 열 수 있습니다.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));FilledTonalButton(onClick={scope.launch{syncText="공식 목차 확인 중…";val r=OfficialSyncService(repository).syncYear(year){d,t->syncText="$d / $t 확인"};syncText="동기화 완료 · 성공 ${r.success} · 실패 ${r.failed}"}}){Icon(Icons.Default.Sync,null);Spacer(Modifier.width(6.dp));Text("$year 공식 동기화")};if(syncText.isNotBlank())Text(syncText,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp))}
            item{LazyRow(horizontalArrangement=Arrangement.spacedBy(7.dp)){lazyItems(years.take(30)){y->FilterChip(selected=y==year,onClick={year=y},label={Text(y.toString())})}};Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("전체","미완","진행중","완성").forEach{s->FilterChip(selected=filter==s,onClick={filter=s},label={Text(s)})}}}
            if(visible.isEmpty())item{Text("이 조건에 해당하는 호수가 없습니다.",modifier=Modifier.padding(20.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)}
            items(visible,key={it.id}){issue->val issueArticles=articles.filter{it.issueId==issue.id}.sortedBy{it.sortOrder};val progress=if(issueArticles.isEmpty())0 else issueArticles.map{it.progress}.average().toInt();val isOpen=issue.id in expanded;ElevatedCard(shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column{Column(Modifier.fillMaxWidth().clickable{if(isOpen)expanded.remove(issue.id)else expanded.add(issue.id)}.padding(18.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text("${year} · ${pubById[issue.publicationId]?.name.orEmpty()}",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);Text(issue.label,style=MaterialTheme.typography.titleLarge)};Text("$progress%",style=MaterialTheme.typography.titleLarge)};Spacer(Modifier.height(10.dp));LinearProgressIndicator(progress={progress/100f},modifier=Modifier.fillMaxWidth());Text("기사 ${issueArticles.count{it.progress==100}}/${issueArticles.size} 완성 · 탭해서 기사별 진행률 열기",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=7.dp))};if(isOpen){HorizontalDivider();issueArticles.forEach{a->ListItem(headlineContent={Text(a.title)},supportingContent={Text("${statusById[a.statusId]?.name?:"미완"} · ${a.progress}%")},modifier=Modifier.clickable{onArticle(a.id)})};TextButton(onClick={articleIssue=issue},modifier=Modifier.padding(horizontal=12.dp,vertical=4.dp)){Icon(Icons.Default.Add,null);Text("기사 직접 추가")}}}}}
        }
        FloatingActionButton(onClick={showIssue=true},modifier=Modifier.padding(20.dp).align(Alignment.BottomEnd)){Icon(Icons.Default.Add,"호수 추가")}
    }
    if(showIssue)AddIssueDialog(publications,onDismiss={showIssue=false}){pub,y,label->scope.launch{repository.addIssue(pub,y,label);showIssue=false}}
    articleIssue?.let{issue->AddArticleDialog(onDismiss={articleIssue=null}){title->scope.launch{repository.addArticle(issue.id,title);articleIssue=null}}}
}

@Composable private fun AddIssueDialog(publications:List<Publication>,onDismiss:()->Unit,onSave:(String,Int,String)->Unit){var pub by remember{mutableStateOf(publications.firstOrNull()?.name?:"파수대—연구용")};var year by remember{mutableStateOf(Year.now().value.toString())};var label by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onDismiss,title={Text("호수 추가")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(pub,{pub=it},label={Text("출판물")});OutlinedTextField(year,{year=it.filter(Char::isDigit)},label={Text("연도")});OutlinedTextField(label,{label=it},label={Text("호수")})}},confirmButton={Button(onClick={if(label.isNotBlank())onSave(pub,year.toIntOrNull()?:Year.now().value,label)}){Text("저장")}},dismissButton={TextButton(onClick=onDismiss){Text("취소")}})}
@Composable private fun AddArticleDialog(onDismiss:()->Unit,onSave:(String)->Unit){var title by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onDismiss,title={Text("기사 추가")},text={OutlinedTextField(title,{title=it},label={Text("기사 제목")})},confirmButton={Button(onClick={if(title.isNotBlank())onSave(title)}){Text("추가")}},dismissButton={TextButton(onClick=onDismiss){Text("취소")}})}
