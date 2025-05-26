package com.example.familybudget.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.familybudget.model.FamilyGroup

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupList(
    groups: List<FamilyGroup>,
    onGroupClick: (FamilyGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGroup by remember { mutableStateOf<FamilyGroup?>(null) }

    Box(modifier = modifier) {
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "У вас пока нет групп",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(groups) { group ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color.Black.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .combinedClickable(
                                onClick = { onGroupClick(group) },
                                onLongClick = { selectedGroup = group }
                            )
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Диалоговое окно с деталями группы
    if (selectedGroup != null) {
        Dialog(
            onDismissRequest = { selectedGroup = null }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedGroup!!.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Создал: ${selectedGroup!!.ownerUsername}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Участники:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    val sortedMembers = selectedGroup!!.members.sortedBy { it.id }
                    sortedMembers.forEach { member ->
                        Text(
                            text = member.username,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { selectedGroup = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
} 