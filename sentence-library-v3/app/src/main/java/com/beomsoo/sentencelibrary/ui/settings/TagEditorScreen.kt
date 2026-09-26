package com.beomsoo.sentencelibrary.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beomsoo.sentencelibrary.data.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditorScreen(tags:List<Tag>,onSave:(Tag)->Unit){
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("태그 편집",style=MaterialTheme.typography.headlineMedium);FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){tags.sortedBy{it.sortOrder}.forEach{AssistChip(onClick={},label={Text(it.name)})}}}
}
