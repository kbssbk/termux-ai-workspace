package com.beomsoo.sentencelibrary.ui.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beomsoo.sentencelibrary.data.RecoverySnapshot

@Composable
fun RecoveryScreen(snapshots:List<RecoverySnapshot>,onSettingsRestore:(String)->Unit,onFullRestore:(String)->Unit){
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("복구 센터",style=MaterialTheme.typography.headlineMedium);Text("잘못 설정해도 독서 기록을 잃지 않도록 복구 지점을 관리합니다.",color=MaterialTheme.colorScheme.onSurfaceVariant);snapshots.forEach{s->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(s.reason,style=MaterialTheme.typography.titleMedium);Row{TextButton(onClick={onSettingsRestore(s.id)}){Text("설정만")};TextButton(onClick={onFullRestore(s.id)}){Text("전체 복원")}}}}}}
}
