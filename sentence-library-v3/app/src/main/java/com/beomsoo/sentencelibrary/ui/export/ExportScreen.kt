package com.beomsoo.sentencelibrary.ui.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.beomsoo.sentencelibrary.backup.*
import com.beomsoo.sentencelibrary.data.DataSnapshot
import com.beomsoo.sentencelibrary.data.ReadingRepository
import com.beomsoo.sentencelibrary.export.*
import kotlinx.coroutines.launch

@Composable
fun ExportScreen(repository:ReadingRepository,articleId:String?) {
    val context=LocalContext.current; val scope=rememberCoroutineScope(); var pending by remember{mutableStateOf<ByteArray?>(null)};var message by remember{mutableStateOf("")}
    fun save(uri:android.net.Uri?){ if(uri!=null) runCatching{context.contentResolver.openOutputStream(uri)?.use{it.write(pending?:byteArrayOf())}}.onSuccess{message="저장했습니다."}.onFailure{message="저장 실패: ${it.message}"} }
    val pdf=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf"),::save)
    val docx=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),::save)
    val csv=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip"),::save)
    val json=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json"),::save)
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text(if(articleId==null)"전체 기록 내보내기" else "기사 내보내기",style=MaterialTheme.typography.headlineMedium)
        Text("A4 문서용 PDF/DOCX와, 다른 앱으로 옮길 수 있는 CSV/JSON을 분리해 제공합니다.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        ElevatedCard(shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("A4 출력 구성",style=MaterialTheme.typography.titleMedium); Text("출판물 · 호수 · 기사 → 상태/진행률 → 요약 → 느낀점 → 적용점 → 저장 문장/위치/태그/메모",style=MaterialTheme.typography.bodyMedium);Text("긴 문장은 자동 줄바꿈·페이지 나눔 처리됩니다.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }}
        ExportButton(Icons.Default.Description,"PDF · A4 인쇄/보관") { scope.launch { val snap=repository.snapshot(); val arts=exportArticles(snap,articleId);pending=PdfExportService().createMany(arts);pdf.launch(fileName(articleId,"pdf")) } }
        ExportButton(Icons.Default.Article,"DOCX · 나중에 편집") { scope.launch { val snap=repository.snapshot(); val arts=exportArticles(snap,articleId);pending=DocxExportService().createMany(arts);docx.launch(fileName(articleId,"docx")) } }
        ExportButton(Icons.Default.TableChart,"CSV · 스프레드시트용") { scope.launch { val backup=repository.snapshot().toBackupV3();pending=CsvExportService().exportZip(backup);csv.launch(fileName(articleId,"csv.zip")) } }
        ExportButton(Icons.Default.DataObject,"JSON · 완전 백업/이동") { scope.launch { val backup=repository.snapshot().toBackupV3();pending=JsonBackupService().encode(backup).toByteArray();json.launch(fileName(articleId,"json")) } }
        if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.primary)
        Spacer(Modifier.weight(1f)); Text("JSON/CSV에는 앱 내부 경로나 기기 고유 ID를 넣지 않습니다. 앱이 없어져도 기록을 꺼낼 수 있는 형식입니다.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun ExportButton(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,onClick:()->Unit){ Button(onClick=onClick,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),shape=RoundedCornerShape(16.dp)){Icon(icon,null);Spacer(Modifier.width(8.dp));Text(label)} }
private fun fileName(articleId:String?,ext:String)="문장라이브러리_${if(articleId==null)"전체" else "기사"}_${java.time.LocalDate.now()}.$ext"

private fun exportArticles(snap:DataSnapshot, articleId:String?):List<ExportArticle> {
    val pubs=snap.publications.associateBy{it.id}; val issues=snap.issues.associateBy{it.id}; val statuses=snap.statuses.associateBy{it.id};val tags=snap.tags.associateBy{it.id};val refs=snap.quoteTagRefs.groupBy{it.quoteId};val quotes=snap.quotes.groupBy{it.articleId}
    return snap.articles.filter { articleId==null || it.id==articleId }.sortedWith(compareByDescending<com.beomsoo.sentencelibrary.data.Article>{issues[it.issueId]?.year?:0}.thenBy{it.sortOrder}).map { a ->
        val issue=issues[a.issueId]; ExportArticle(
            publication=pubs[issue?.publicationId]?.name.orEmpty(),issue="${issue?.year ?: ""}년 ${issue?.label.orEmpty()}",title=a.title,status=statuses[a.statusId]?.name ?: a.statusId,progress=a.progress,summary=a.summary,reflection=a.reflection,application=a.application,
            quotes=quotes[a.id].orEmpty().map { q -> ExportQuote(q.sentence,q.location,refs[q.id].orEmpty().mapNotNull{r->tags[r.tagId]?.name},q.note) }
        )
    }
}
