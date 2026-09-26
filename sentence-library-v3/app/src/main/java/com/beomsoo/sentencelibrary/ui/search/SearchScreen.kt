package com.beomsoo.sentencelibrary.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beomsoo.sentencelibrary.data.ReadingRepository

@Composable
fun SearchScreen(repository:ReadingRepository,onArticle:(String)->Unit) {
    val summaries=repository.articleSummaries.collectAsStateWithLifecycle(emptyList()).value
    val quotes=repository.quotes.collectAsStateWithLifecycle(emptyList()).value
    val tags=repository.tags.collectAsStateWithLifecycle(emptyList()).value
    val refs=repository.quoteTagRefs.collectAsStateWithLifecycle(emptyList()).value
    var query by remember { mutableStateOf("") }; var status by remember { mutableStateOf("전체") }
    val q=query.trim().lowercase()
    val articles=summaries.filter { (status=="전체"||it.status==status) && (q.isBlank() || "${it.title} ${it.publicationName} ${it.issueLabel} ${it.year}".lowercase().contains(q)) }
    val quoteHits=quotes.filter { quote ->
        val tagNames=refs.filter { it.quoteId==quote.id }.mapNotNull { r->tags.firstOrNull { it.id==r.tagId }?.name }
        q.isNotBlank() && "${quote.sentence} ${quote.note} ${quote.location} ${tagNames.joinToString(" ")}".lowercase().contains(q)
    }
    LazyColumn(contentPadding=PaddingValues(18.dp,18.dp,18.dp,96.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("검색 · 다시 찾기",style=MaterialTheme.typography.headlineMedium); Text("기사, 저장 문장, 태그, 위치와 메모까지 한 번에 검색합니다.",color=MaterialTheme.colorScheme.onSurfaceVariant) }
        item { OutlinedTextField(query,{query=it},leadingIcon={Icon(Icons.Default.Search,null)},label={Text("검색어")},singleLine=true,modifier=Modifier.fillMaxWidth()) }
        item { Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("전체","미완","진행중","완성").forEach{s->FilterChip(selected=status==s,onClick={status=s},label={Text(s)})}} }
        item { Text("기사 ${articles.size} · 문장 ${quoteHits.size}",style=MaterialTheme.typography.labelLarge) }
        items(articles.take(100),key={"a-${it.id}"}) { item -> Card(Modifier.fillMaxWidth().clickable{onArticle(item.id)},shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text(item.title,style=MaterialTheme.typography.titleMedium);Text("${item.year} · ${item.issueLabel} · ${item.status} ${item.progress}%",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}} }
        items(quoteHits.take(100),key={"q-${it.id}"}) { quote -> Card(Modifier.fillMaxWidth().clickable{onArticle(quote.articleId)},shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text(quote.sentence);if(quote.location.isNotBlank())Text(quote.location,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary);if(quote.note.isNotBlank())Text(quote.note,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}} }
    }
}
