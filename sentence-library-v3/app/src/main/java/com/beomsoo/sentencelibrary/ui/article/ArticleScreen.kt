package com.beomsoo.sentencelibrary.ui.article

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beomsoo.sentencelibrary.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleScreen(repository:ReadingRepository,articleId:String,onExport:(String)->Unit) {
    val articles=repository.articles.collectAsStateWithLifecycle(emptyList()).value;val issues=repository.issues.collectAsStateWithLifecycle(emptyList()).value;val pubs=repository.publications.collectAsStateWithLifecycle(emptyList()).value;val statuses=repository.statuses.collectAsStateWithLifecycle(emptyList()).value;val quotes=repository.quotes.collectAsStateWithLifecycle(emptyList()).value;val tags=repository.tags.collectAsStateWithLifecycle(emptyList()).value;val refs=repository.quoteTagRefs.collectAsStateWithLifecycle(emptyList()).value
    val article=articles.firstOrNull{it.id==articleId} ?: run{Box(Modifier.fillMaxSize().padding(24.dp)){Text("기사를 찾을 수 없습니다.")};return}
    val issue=issues.firstOrNull{it.id==article.issueId};val pub=pubs.firstOrNull{it.id==issue?.publicationId};val scope=rememberCoroutineScope();val context=LocalContext.current
    var summary by remember(article.id){mutableStateOf(article.summary)};var reflection by remember(article.id){mutableStateOf(article.reflection)};var application by remember(article.id){mutableStateOf(article.application)};var addQuote by remember{mutableStateOf(false)};var slider by remember(article.id,article.progress){mutableFloatStateOf(article.progress.toFloat())}
    val articleQuotes=quotes.filter{it.articleId==article.id}
    LazyColumn(contentPadding=PaddingValues(18.dp,18.dp,18.dp,96.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Text("${issue?.year?:""} · ${pub?.name.orEmpty()} · ${issue?.label.orEmpty()}",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary);Text(article.title,style=MaterialTheme.typography.headlineMedium);if(!article.sourceUrl.isNullOrBlank())TextButton(onClick={runCatching{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(article.sourceUrl)))}}){Icon(Icons.Default.OpenInNew,null);Text("공식 출처")}}
        item{ElevatedCard(shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("독서 상태",style=MaterialTheme.typography.titleMedium);FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){statuses.sortedBy{it.sortOrder}.forEach{s->FilterChip(selected=article.statusId==s.id,onClick={scope.launch{repository.setArticleStatus(article.id,s.id)}},label={Text(s.name)})}};val currentStatus=statuses.firstOrNull{it.id==article.statusId};if(currentStatus?.adjustable==true){Text("진행률 ${slider.toInt()}%",style=MaterialTheme.typography.titleSmall);Slider(value=slider,onValueChange={slider=it},onValueChangeFinished={scope.launch{repository.setArticleProgress(article.id,slider.toInt())}},valueRange=1f..99f,steps=97,modifier=Modifier.fillMaxWidth())}else{LinearProgressIndicator(progress={article.progress/100f},modifier=Modifier.fillMaxWidth());Text("${article.progress}%",style=MaterialTheme.typography.bodyMedium)}}}}
        item{Button(onClick={addQuote=true},modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)){Icon(Icons.Default.Add,null);Spacer(Modifier.width(8.dp));Text("문장 추가")}}
        item{Text("저장한 문장 ${articleQuotes.size}",style=MaterialTheme.typography.titleLarge)}
        items(articleQuotes,key={it.id}){q->val qTags=refs.filter{it.quoteId==q.id}.mapNotNull{r->tags.firstOrNull{it.id==r.tagId}?.name};Card(shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text(q.sentence,style=MaterialTheme.typography.bodyLarge);if(q.location.isNotBlank())Text("위치 · ${q.location}",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary);if(qTags.isNotEmpty())Text(qTags.joinToString("  "){"#$it"},style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.secondary);if(q.note.isNotBlank())Text(q.note,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
        item{ElevatedCard(shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("기사 정리",style=MaterialTheme.typography.titleLarge);OutlinedTextField(summary,{summary=it},label={Text("기사 요약")},modifier=Modifier.fillMaxWidth(),minLines=3);OutlinedTextField(reflection,{reflection=it},label={Text("느낀점")},modifier=Modifier.fillMaxWidth(),minLines=3);OutlinedTextField(application,{application=it},label={Text("적용점")},modifier=Modifier.fillMaxWidth(),minLines=3);Button(onClick={scope.launch{repository.setArticleNotes(article.id,summary,reflection,application)}},modifier=Modifier.fillMaxWidth()){Text("기사 정리 저장")}}}}
        item{OutlinedButton(onClick={onExport(article.id)},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Description,null);Spacer(Modifier.width(8.dp));Text("이 기사 A4 / 데이터 내보내기")}}
    }
    if(addQuote)AddQuoteDialog(tags,onDismiss={addQuote=false}){sentence,location,note,selected->scope.launch{repository.addQuote(article.id,sentence,location,note,selected);addQuote=false}}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun AddQuoteDialog(tags:List<Tag>,onDismiss:()->Unit,onSave:(String,String,String,List<String>)->Unit){var sentence by remember{mutableStateOf("")};var location by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};val selected=remember{mutableStateListOf<String>()};AlertDialog(onDismissRequest=onDismiss,title={Text("기억할 문장")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(sentence,{sentence=it},label={Text("문장")},modifier=Modifier.fillMaxWidth(),minLines=3);OutlinedTextField(location,{location=it},label={Text("페이지 · 항 등 위치")},modifier=Modifier.fillMaxWidth());FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){tags.forEach{t->FilterChip(selected=t.id in selected,onClick={if(t.id in selected)selected.remove(t.id)else selected.add(t.id)},label={Text(t.name)})}};OutlinedTextField(note,{note=it},label={Text("내 메모")},modifier=Modifier.fillMaxWidth(),minLines=2)}},confirmButton={Button(onClick={if(sentence.isNotBlank())onSave(sentence,location,note,selected.toList())}){Text("저장")}},dismissButton={TextButton(onClick=onDismiss){Text("취소")}})}
