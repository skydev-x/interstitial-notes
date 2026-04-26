package com.skydev.canvastest.ui.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.skydev.canvastest.ui.feature.timeline.components.EmptyState
import com.skydev.canvastest.ui.feature.timeline.components.TimelineRow
import com.skydev.canvastest.ui.nav.AppRoutes
import com.skydev.canvastest.ui.theme.Accent
import com.skydev.canvastest.ui.theme.Bg
import com.skydev.canvastest.ui.theme.TextPri
import com.skydev.canvastest.ui.theme.TextSec

val CARD_IMAGE_HEIGHT = 160.dp
val CARD_GAP = 16.dp
val RAIL_LINE_HEIGHT = CARD_IMAGE_HEIGHT + CARD_GAP

private val RailColors = listOf(
    Color(0xFF7C6EFA), Color(0xFF6EFAC3), Color(0xFFFA6E6E),
    Color(0xFF6EA8FA), Color(0xFFFAC76E), Color(0xFFFA6EC3),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    navController: NavController,
    viewModel: TimeLineViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onRefresh() }

    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Canvas",
                            color = TextPri,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.8).sp,
                        )
                        Text(
                            if (notes.isEmpty()) "no notes yet"
                            else "${notes.size} note${if (notes.size == 1) "" else "s"}",
                            color = TextSec,
                            fontSize = 11.sp,
                        )
                    }
                },
                actions = {
                    if (notes.isNotEmpty()) {
                        IconButton(onClick = { navController.navigate(AppRoutes.NoteTaking()) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "New note", tint = Accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notes.isEmpty()) {
                EmptyState(
                    onNewNote = { navController.navigate(AppRoutes.NoteTaking()) },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CARD_GAP),
                ) {
                    itemsIndexed(notes, key = { _, n -> n.id }) { index, note ->
                        TimelineRow(
                            note = note,
                            index = index,
                            isLast = index == notes.lastIndex,
                            railColor = RailColors[index % RailColors.size],
                            onClick = { navController.navigate(AppRoutes.NoteTaking(note.id)) },
                        )
                    }
                }
            }
        }
    }
}