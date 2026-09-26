package com.beomsoo.sentencelibrary.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beomsoo.sentencelibrary.data.StatusDefinition

@Composable
fun StatusEditorScreen(statuses:List<StatusDefinition>,onSave:(StatusDefinition)->Unit){
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("상태 편집",style=MaterialTheme.typography.headlineMedium);statuses.sortedBy{it.sortOrder}.forEach{s->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(s.name,style=MaterialTheme.typography.titleMedium);Text("${s.defaultProgress}% · ${if(s.adjustable)"조절 가능"else "고정"}")}}}}
}
