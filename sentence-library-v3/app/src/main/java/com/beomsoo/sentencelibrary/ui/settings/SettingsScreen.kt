package com.beomsoo.sentencelibrary.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beomsoo.sentencelibrary.backup.*
import com.beomsoo.sentencelibrary.data.*
import com.beomsoo.sentencelibrary.domain.ProgressRules
import com.beomsoo.sentencelibrary.recovery.PersistentRecoveryService
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(repository:ReadingRepository,onExport:()->Unit) {
    val statuses=repository.statuses.collectAsStateWithLifecycle(emptyList()).value;val tags=repository.tags.collectAsStateWithLifecycle(emptyList()).value;val snapshots=repository.snapshots.collectAsStateWithLifecycle(emptyList()).value;val context=LocalContext.current;val scope=rememberCoroutineScope();val recovery=remember(repository){PersistentRecoveryService(repository)}
    var message by remember{mutableStateOf("")};var addStatus by remember{mutableStateOf(false)};var addTag by remember{mutableStateOf(false)}
    val importer=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)scope.launch{runCatching{recovery.createSnapshot("가져오기 전 자동 백업");val text=withContext(Dispatchers.IO){context.contentResolver.openInputStream(uri)!!.bufferedReader().use{it.readText()}};val backup=if(text.contains("\"schemaVersion\""))JsonBackupService().decode(text)else V2JsonImporter.toBackupV3(V2JsonImporter.parse(text));repository.replace(backup.toDataSnapshot())}.onSuccess{message="가져오기가 완료되었습니다."}.onFailure{message="가져오기 실패: ${it.message}"}}}
    LazyColumn(contentPadding=PaddingValues(18.dp,18.dp,18.dp,96.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("설정 · 자유도",style=MaterialTheme.typography.headlineMedium);Text("기본은 단순하게, 필요할 때만 원하는 만큼 바꿀 수 있습니다.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        if(message.isNotBlank())item{AssistChip(onClick={message=""},label={Text(message)})}
        item{Section("상태 설정","기본 3개에서 시작해 원하는 상태를 계속 추가할 수 있어요"){statuses.sortedBy{it.sortOrder}.forEach{s->ListItem(headlineContent={Text(s.name)},supportingContent={Text("기본 ${s.defaultProgress}% · ${if(s.adjustable)"직접 조절"else "고정"}${if(s.completed)" · 완료 집계"else ""}")});HorizontalDivider()};TextButton(onClick={addStatus=true}){Icon(Icons.Default.Add,null);Text("상태 추가")}}}
        item{Section("태그 설정","기본 태그도 그대로 고정되지 않습니다"){FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){tags.sortedBy{it.sortOrder}.forEach{AssistChip(onClick={},label={Text(it.name)})}};TextButton(onClick={addTag=true}){Icon(Icons.Default.Add,null);Text("태그 추가")}}}
        item{Section("데이터 이동","앱에 갇히지 않도록 가져오기와 내보내기를 항상 열어둡니다"){Button(onClick=onExport,modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.IosShare,null);Spacer(Modifier.width(8.dp));Text("PDF · DOCX · CSV · JSON 내보내기")};OutlinedButton(onClick={importer.launch(arrayOf("application/json","text/plain","*/*"))},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.FileOpen,null);Spacer(Modifier.width(8.dp));Text("V2 / V3 JSON 가져오기")}}}
        item{Section("복구 센터","설정을 크게 바꾸기 전 복구 지점을 남길 수 있습니다"){Button(onClick={scope.launch{recovery.createSnapshot("수동 스냅샷");message="복구 지점을 만들었습니다."}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.AddTask,null);Text("복구 지점 만들기")};snapshots.take(3).forEach{s->ListItem(headlineContent={Text(s.reason)},supportingContent={Text(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",java.util.Locale.getDefault()).format(java.util.Date(s.createdAt)))},trailingContent={TextButton(onClick={scope.launch{recovery.restoreSnapshot(s.id,true);message="설정만 복원했습니다."}}){Text("설정 복원")}})};OutlinedButton(onClick={scope.launch{val w=recovery.integrityCheck();message=if(w.isEmpty())"데이터 무결성 정상"else w.joinToString(" · ")}},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.HealthAndSafety,null);Text("데이터 무결성 검사")}}}
        item{Section("안전한 기본값 복원","독서 기록은 지우지 않고 기본 상태·태그를 다시 준비합니다"){OutlinedButton(onClick={scope.launch{ProgressRules.defaultStatuses().forEach{repository.upsertStatus(it)};ReadingRepository.DEFAULT_TAGS.forEachIndexed{i,n->repository.upsertTag(Tag("tag-default-$i",n,i))};message="기본 설정을 복원했습니다. 기록은 유지됩니다."}},modifier=Modifier.fillMaxWidth()){Text("기본값 복원")}}}
        item{Text("설정도 JSON 백업에 포함됩니다. 앱을 바꾸더라도 기록 구조를 함께 옮길 수 있습니다.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
    if(addStatus)AddStatusDialog(onDismiss={addStatus=false}){name,progress,adjustable,completed->scope.launch{repository.upsertStatus(StatusDefinition("status-${UUID.randomUUID()}",name,progress,adjustable,completed,true,statuses.size));addStatus=false}}
    if(addTag)AddTagDialog(onDismiss={addTag=false}){name->scope.launch{repository.upsertTag(Tag("tag-${UUID.randomUUID()}",name,tags.size));addTag=false}}
}

@Composable private fun Section(title:String,sub:String,content:@Composable ColumnScope.()->Unit){ElevatedCard(shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,style=MaterialTheme.typography.titleMedium);Text(sub,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);content()}}}
@Composable private fun AddStatusDialog(onDismiss:()->Unit,onSave:(String,Int,Boolean,Boolean)->Unit){var name by remember{mutableStateOf("")};var progress by remember{mutableStateOf("25")};var adjustable by remember{mutableStateOf(true)};var completed by remember{mutableStateOf(false)};AlertDialog(onDismissRequest=onDismiss,title={Text("상태 추가")},text={Column{OutlinedTextField(name,{name=it},label={Text("이름")});OutlinedTextField(progress,{progress=it.filter(Char::isDigit)},label={Text("기본 퍼센트")});Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(adjustable,{adjustable=it});Text("퍼센트 조절");Checkbox(completed,{completed=it});Text("완료 취급")}}},confirmButton={Button(onClick={if(name.isNotBlank())onSave(name,(progress.toIntOrNull()?:25).coerceIn(0,100),adjustable,completed)}){Text("추가")}},dismissButton={TextButton(onClick=onDismiss){Text("취소")}})}
@Composable private fun AddTagDialog(onDismiss:()->Unit,onSave:(String)->Unit){var name by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onDismiss,title={Text("태그 추가")},text={OutlinedTextField(name,{name=it},label={Text("태그 이름")})},confirmButton={Button(onClick={if(name.isNotBlank())onSave(name)}){Text("추가")}},dismissButton={TextButton(onClick=onDismiss){Text("취소")}})}
