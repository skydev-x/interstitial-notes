package com.skydev.canvastest.ui.feature.timeline

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.ui.nav.AppRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Palette ───────────────────────────────────────────────────────────────────

private val Bg         = Color(0xFF0A0A0F)
private val Surface1   = Color(0xFF13131A)
private val Surface2   = Color(0xFF1C1C26)
private val Border     = Color(0xFF252532)
private val TextPri    = Color(0xFFF0F0F5)
private val TextSec    = Color(0xFF8080A0)
private val TextTer    = Color(0xFF44445A)
private val Accent     = Color(0xFF7C6EFA)
private val AccentSoft = Color(0x207C6EFA)
private val Mono       = FontFamily.Monospace

// ✅ Single source of truth for all sizing — change here, reflects everywhere
private val CARD_IMAGE_HEIGHT = 160.dp   // fixed image area, ~55% of typical card
private val CARD_GAP          = 16.dp    // uniform gap between every card
private val RAIL_LINE_HEIGHT  = CARD_IMAGE_HEIGHT + CARD_GAP  // connector always matches

private val RailColors = listOf(
    Color(0xFF7C6EFA), Color(0xFF6EFAC3), Color(0xFFFA6E6E),
    Color(0xFF6EA8FA), Color(0xFFFAC76E), Color(0xFFFA6EC3),
)

// ── Screen ────────────────────────────────────────────────────────────────────

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
                            fontFamily = Mono,
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                    // ✅ Fixed spacing via LazyColumn — no per-item padding variation
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

// ── Timeline row ──────────────────────────────────────────────────────────────

@Composable
private fun TimelineRow(
    note: NoteUi,
    index: Int,
    isLast: Boolean,
    railColor: Color,
    onClick: () -> Unit,
) {
    // ✅ No staggered delay — removed index * 55L which caused visible lag
    // when fast-scrolling to items far down the list. Simple instant layout,
    // expand animation still has spring on the content reveal.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── Rail ──────────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(18.dp),
        ) {
            Spacer(Modifier.height(20.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(railColor.copy(alpha = 0.15f)),
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(railColor),
                )
            }

            // ✅ Fixed connector height — never varies with card content
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(RAIL_LINE_HEIGHT)
                        .background(
                            Brush.verticalGradient(
                                listOf(railColor.copy(alpha = 0.5f), Color.Transparent)
                            )
                        ),
                )
            }
        }

        NoteCard(
            note = note,
            onClick = onClick,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Note card ─────────────────────────────────────────────────────────────────

@Composable
private fun NoteCard(
    note: NoteUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, note.id) {
        value = withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "${note.id}_canvas.png")
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Surface1,
        shadowElevation = if (expanded) 12.dp else 4.dp,
        modifier = modifier
            .shadow(if (expanded) 16.dp else 6.dp, RoundedCornerShape(18.dp))
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        Column {

            // ── Image — always CARD_IMAGE_HEIGHT, never resizes ────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CARD_IMAGE_HEIGHT)   // ✅ fixed — same for every card
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Canvas preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Surface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✏️", fontSize = 28.sp)
                    }
                }

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f   to Color.Transparent,
                                0.4f to Color.Transparent,
                                1f   to Bg.copy(alpha = 0.95f),
                            )
                        ),
                )

                // Title + date — always visible, pinned to bottom of image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = note.title.ifBlank { "Untitled" },
                        color = TextPri,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatDate(note.updatedAt),
                        color = TextSec,
                        fontSize = 10.sp,
                        fontFamily = Mono,
                        letterSpacing = 0.3.sp,
                    )
                }

                // Expand toggle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Bg.copy(alpha = 0.75f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { expanded = !expanded },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp
                        else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSec,
                        modifier = Modifier.size(16.dp),
                    )
                }

                if (note.isEmpty) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Surface2.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                    ) {
                        Text("empty", color = TextTer, fontSize = 9.sp, fontFamily = Mono)
                    }
                }
            }

            // ── Expandable section ─────────────────────────────────────────
            // ✅ expandVertically/shrinkVertically — no layout jump, smooth clip
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) + expandVertically(
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                    expandFrom = Alignment.Top,
                ),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(180)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (note.summary.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentSoft)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                "∑",
                                color = Accent,
                                fontSize = 11.sp,
                                fontFamily = Mono,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = note.summary,
                                color = TextPri,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    if (note.transcribedText.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface2)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "// transcribed",
                                color = TextTer,
                                fontSize = 9.sp,
                                fontFamily = Mono,
                                letterSpacing = 0.5.sp,
                            )
                            Text(
                                text = "\"${note.transcribedText}\"",
                                color = TextSec,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                fontFamily = Mono,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }

                    Text(
                        text = "created ${note.createdAt.toFormattedDate()}",
                        color = TextTer,
                        fontSize = 9.sp,
                        fontFamily = Mono,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(onNewNote: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(AccentSoft)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = Accent, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(28.dp))
        Text("nothing here yet", color = TextPri, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, fontFamily = Mono)
        Spacer(Modifier.height(8.dp))
        Text("your notes will appear as a timeline", color = TextSec, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onNewNote,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.height(48.dp),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("create first note", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// ── Formatters ────────────────────────────────────────────────────────────────

private fun formatDate(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L         -> "just now"
        diff < 3_600_000L      -> "${diff / 60_000}m ago"
        diff < 86_400_000L     -> "${diff / 3_600_000}h ago"
        diff < 2 * 86_400_000L -> "yesterday"
        diff < 7 * 86_400_000L -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(ts))
        else                   -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(ts))
    }
}

fun Long.toFormattedDate(): String =
    SimpleDateFormat("hh:mm · d MMM yyyy", Locale.getDefault()).format(Date(this))