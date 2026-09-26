package com.beomsoo.sentencelibrary.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beomsoo.sentencelibrary.data.CustomFieldDefinition

@Composable
fun CustomFieldEditorScreen(fields:List<CustomFieldDefinition>){
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("사용자 정의 필드",style=MaterialTheme.typography.headlineMedium);Text("페이지, 항, 중요도, 재독 예정일처럼 필요한 필드를 추가할 수 있도록 데이터 구조를 열어두었습니다.",color=MaterialTheme.colorScheme.onSurfaceVariant);fields.forEach{ListItem(headlineContent={Text(it.name)},supportingContent={Text("${it.target} · ${it.type}")})}}
}
