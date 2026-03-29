package com.skydev.canvastest.ui.feature.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.ui.nav.AppRoutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Theme (matches DrawingScreen) ───────────────────────────────────────────

private val Surface0 = Color(0xFF0E0E11)
private val Surface1 = Color(0xFF16161C)
private val Surface2 = Color(0xFF1E1E28)
private val Surface3 = Color(0xFF26262F)
private val StrokeColor = Color(0xFF2E2E3A)
private val TextPri = Color(0xFFF0F0F5)
private val TextSec = Color(0xFF8888A0)
private val TextTer = Color(0xFF55556A)
private val Accent = Color(0xFF7C6EFA)
private val AccentSoft = Color(0x1A7C6EFA)
private val Danger = Color(0xFFFA6E6E)

// Dot colors for the timeline — cycles through these
private val DotColors = listOf(
    Color(0xFF7C6EFA), Color(0xFF6EFAC3), Color(0xFFFA6E6E),
    Color(0xFF6EA8FA), Color(0xFFFAC76E), Color(0xFFFA6EC3),
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    navController: NavController,
    viewModel: TimeLineViewModel = hiltViewModel(),
) {
    val notes = viewModel.notes.collectAsStateWithLifecycle().value
    Scaffold(
        containerColor = Surface0,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Canvas",
                            color = TextPri,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                        )
                        Text(
                            text = if (notes.isEmpty()) "No notes yet"
                            else "${notes.size} note${if (notes.size == 1) "" else "s"}",
                            color = TextSec,
                            fontSize = 12.sp,
                        )
                    }
                },
                actions = {
                    if (notes.isNotEmpty()) {
                        IconButton(
                            onClick = { navController.navigate(AppRoutes.NoteTaking()) }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "New note",
                                tint = Accent,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface0),
            )
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    itemsIndexed(notes, key = { _, item -> item.id }) { index, note ->
                        TimelineItem(
                            note = note,
                            index = index,
                            isLast = index == notes.lastIndex,
                            dotColor = DotColors[index % DotColors.size],
                            onClick = { navController.navigate(AppRoutes.NoteTaking(note.id)) },
                        )
                    }
                }
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(onNewNote: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Decorative ring
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AccentSoft)
                .border(1.dp, StrokeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Nothing here yet",
            color = TextPri,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Your notes will appear as a timeline.\nTap below to create your first one.",
            color = TextSec,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onNewNote,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.height(52.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Create first note",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─── Timeline Item ────────────────────────────────────────────────────────────

@Composable
private fun TimelineItem(
    note: NoteUi,                // your domain model
    index: Int,
    isLast: Boolean,
    dotColor: Color,
    onClick: () -> Unit,
) {
    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Timeline rail (dot + line) ────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(20.dp),
            ) {
                Spacer(Modifier.height(18.dp))

                // Dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .shadow(6.dp, CircleShape),
                )

                // Connector line (hidden for last item)
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(72.dp)         // matches card height roughly
                            .background(
                                Brush.verticalGradient(
                                    listOf(dotColor.copy(alpha = 0.5f), StrokeColor)
                                )
                            ),
                    )
                }
            }

            // ── Note card ─────────────────────────────────────────────────
            NoteCard(
                note = note,
                onClick = onClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (isLast) 0.dp else 12.dp),
            )
        }
    }
}

// ─── Note Card ────────────────────────────────────────────────────────────────

@Composable
private fun NoteCard(
    note: NoteUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Surface1,
        shadowElevation = 4.dp,
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .border(1.dp, StrokeColor, RoundedCornerShape(16.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Title + arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled" },
                    color = TextPri,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = TextTer,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(6.dp))

            // Date + stroke count
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaChip(
                    icon = Icons.Rounded.Done,
                    label = formatDate(note.createdAt),
                )
            }
        }
    }
}

@Composable
private fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextTer,
            modifier = Modifier.size(11.dp),
        )
        Text(label, color = TextSec, fontSize = 11.sp)
    }
}

// ─── Date Formatter ───────────────────────────────────────────────────────────

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        diff < 2 * 86_400_000L -> "Yesterday"
        diff < 7 * 86_400_000L -> SimpleDateFormat("EEEE", Locale.getDefault()).format(
            Date(
                timestamp
            )
        )

        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}