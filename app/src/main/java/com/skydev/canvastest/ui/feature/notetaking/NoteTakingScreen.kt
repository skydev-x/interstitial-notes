package com.skydev.canvastest.ui.feature.notetaking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydev.canvastest.domain.model.PointF
import com.skydev.canvastest.domain.model.StrokeData
import com.skydev.canvastest.domain.model.toPath
import com.skydev.canvastest.ui.theme.Accent
import com.skydev.canvastest.ui.theme.AccentSoft
import com.skydev.canvastest.ui.theme.Danger
import com.skydev.canvastest.ui.theme.StrokeColor
import com.skydev.canvastest.ui.theme.Surface0
import com.skydev.canvastest.ui.theme.Surface1
import com.skydev.canvastest.ui.theme.Surface2
import com.skydev.canvastest.ui.theme.Surface3
import com.skydev.canvastest.ui.theme.TextPri
import com.skydev.canvastest.ui.theme.TextSec
import com.skydev.canvastest.ui.utils.isForAll


private val Palette = listOf(
    Color(0xFFF0F0F5), Color(0xFF1A1A2E), Color(0xFF7C6EFA),
    Color(0xFFFA6E6E), Color(0xFF6EFAC3), Color(0xFFFAC76E),
    Color(0xFF6EA8FA), Color(0xFFFA6EC3), Color(0xFF4CAF50),
    Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF00BCD4),
)

private enum class DrawTool(val icon: ImageVector, val label: String) {
    PEN(Icons.Rounded.Edit, "Pen"),
    MARKER(Icons.Rounded.Star, "Marker"),
    ERASER(Icons.Rounded.ShoppingCart, "Eraser"),
    LASSO(Icons.Rounded.Share, "Select"),
}

@Composable
fun NoteTakingScreen(
    modifier: Modifier = Modifier,
    viewModel: NoteTakingViewModel = hiltViewModel(),
    id: String? = null,
    onBack: () -> Unit,
) {
    LaunchedEffect(id) {
        if (id != null) {
            viewModel.load(id)
        }
    }
    val noteUi by viewModel.noteUi.collectAsStateWithLifecycle()
    val strokes by viewModel.strokes.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    NoteTakingUi(
        projectTitle = noteUi?.title ?: run { "Untitled" },
        strokes = strokes,
        onBack = onBack,
        onUndo = viewModel::undo,
        onRedo = viewModel::redo,
        onClear = viewModel::clear,
        canRedo = canRedo,
        onSave = viewModel::persist,
        onShare = {},
        onDelete = {
            viewModel.delete(onBack)
        },
        onRename = viewModel::rename,
        onStrokeComplete = viewModel::onStrokeComplete,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTakingUi(
    projectTitle: String,
    strokes: List<StrokeData>,
    modifier: Modifier = Modifier,
    canRedo: Boolean = false,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit,
    onStrokeComplete: (StrokeData) -> Unit,
    canUndo: Boolean = strokes.isNotEmpty(),
) {
    var strokeColor by remember { mutableStateOf(Color.White) }
    var strokeWidth by remember { mutableFloatStateOf(5f) }
    var activeTool by remember { mutableStateOf(DrawTool.PEN) }
    var colorPanelVisible by remember { mutableStateOf(true) }
    var toolbarVisible by remember { mutableStateOf(true) }
    var widthPanelVisible by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var clearDialogShown by remember { mutableStateOf(false) }

    if (clearDialogShown) {
        ConfirmClearDialog(
            onDismiss = { clearDialogShown = false },
            onConfirm = { clearDialogShown = false; onClear() },
        )
    }

    var renameDialogShown by remember { mutableStateOf(false) }

    if (renameDialogShown) {
        RenameDialog(
            currentTitle = projectTitle,
            onDismiss = { renameDialogShown = false },
            onConfirm = { newTitle ->
                renameDialogShown = false
                onRename(newTitle)
            },
        )
    }

    Scaffold(
        containerColor = Surface0,
        topBar = {
            DrawingTopBar(
                title = projectTitle,
                menuExpanded = menuExpanded,
                onMenuToggle = { menuExpanded = it },
                onBack = onBack,
                onSave = { menuExpanded = false; onSave() },
                onShare = { menuExpanded = false; onShare() },
                onDelete = { menuExpanded = false; onDelete() },
                onRename = {
                    menuExpanded = false
                    renameDialogShown = true
                },
            )
        },
    ) { padding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Surface0),
        ) {

            SPenDrawingCanvas(
                modifier = Modifier.fillMaxSize(),
                strokes = strokes,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                onStrokeComplete = onStrokeComplete,
            )

            AnimatedVisibility(
                visible = colorPanelVisible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
            ) {
                ColorPanel(selected = strokeColor, onSelect = { strokeColor = it })
            }

            AnimatedVisibility(
                visible = widthPanelVisible,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                enter = fadeIn() + slideInHorizontally { -it },
                exit = fadeOut() + slideOutHorizontally { -it },
            ) {
                StrokeWidthPanel(
                    value = strokeWidth,
                    onChange = { strokeWidth = it },
                    color = strokeColor,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RailToggle(
                    icon = Icons.Rounded.ShoppingCart,
                    active = colorPanelVisible,
                    tint = Accent,
                    onClick = { colorPanelVisible = !colorPanelVisible },
                )
                RailToggle(
                    icon = Icons.Rounded.Share,
                    active = widthPanelVisible,
                    onClick = { widthPanelVisible = !widthPanelVisible },
                )
                RailToggle(
                    icon = if (toolbarVisible) Icons.Rounded.KeyboardArrowDown
                    else Icons.Rounded.KeyboardArrowUp,
                    active = toolbarVisible,
                    onClick = { toolbarVisible = !toolbarVisible },
                )
            }

            AnimatedVisibility(
                visible = toolbarVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                BottomToolbar(
                    activeTool = activeTool,
                    onToolSelect = { activeTool = it },
                    canUndo = canUndo,
                    canRedo = canRedo,
                    onUndo = onUndo,
                    onRedo = onRedo,
                    onClear = { clearDialogShown = true },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawingTopBar(
    title: String,
    menuExpanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = TextPri,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPri,
                )
            }
        },
        actions = {
            IconButton(onClick = onSave) {
                Icon(Icons.Rounded.Check, contentDescription = "Save", tint = Accent)
            }
            Box {
                IconButton(onClick = { onMenuToggle(true) }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = TextSec)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuToggle(false) },
                    containerColor = Surface2,
                    shadowElevation = 8.dp,
                ) {
                    StyledMenuItem(Icons.Rounded.Check, "Save", onSave)
                    StyledMenuItem(Icons.Rounded.Share, "Share", onShare)
                    StyledMenuItem(Icons.Rounded.Delete, "Delete", onDelete)
                    HorizontalDivider(
                        color = StrokeColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    StyledMenuItem(Icons.Rounded.Star, "Rename project", {
                        onRename()
                    })
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface1),
    )
}

@Composable
private fun StyledMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = TextPri, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = TextSec,
                modifier = Modifier.size(18.dp)
            )
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(textColor = TextPri),
    )
}

@Composable
private fun ColorPanel(selected: Color, onSelect: (Color) -> Unit) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Surface2,
        shadowElevation = 12.dp,
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(32.dp))
            .border(1.dp, StrokeColor, RoundedCornerShape(32.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Palette.forEach { color ->
                val isSelected = color == selected
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.3f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "colorScale",
                )
                Box(
                    modifier = Modifier
                        .size((20 * scale).dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(2.dp, Accent, CircleShape)
                            else Modifier.border(1.dp, StrokeColor, CircleShape)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onSelect(color) },
                        )
                )
            }
        }
    }
}

@Composable
private fun StrokeWidthPanel(value: Float, onChange: (Float) -> Unit, color: Color) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Surface2,
        shadowElevation = 12.dp,
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .border(1.dp, StrokeColor, RoundedCornerShape(24.dp)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Width", color = TextSec, fontSize = 11.sp, fontWeight = FontWeight.Medium)

            Slider(
                value = value,
                onValueChange = onChange,
                valueRange = 1f..30f,
                modifier = Modifier
                    .height(120.dp)
                    .graphicsLayer { rotationZ = -90f },
                colors = SliderDefaults.colors(
                    thumbColor = Accent,
                    activeTrackColor = Accent,
                    inactiveTrackColor = Surface3,
                ),
            )

            Box(
                modifier = Modifier
                    .size(value.coerceIn(4f, 28f).dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, StrokeColor, CircleShape)
            )

            Text("${value.toInt()}px", color = TextSec, fontSize = 10.sp)
        }
    }
}

@Composable
private fun RailToggle(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    tint: Color = TextSec,
) {
    val bg by animateColorAsState(
        targetValue = if (active) AccentSoft else Surface2,
        animationSpec = tween(200),
        label = "railBg",
    )
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, StrokeColor, RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) Accent else tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun BottomToolbar(
    activeTool: DrawTool,
    onToolSelect: (DrawTool) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Surface1,
        shadowElevation = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(28.dp))
            .border(1.dp, StrokeColor, RoundedCornerShape(28.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolbarAction(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    "Undo",
                    canUndo,
                    onClick = onUndo
                )
                ToolbarAction(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    "Redo",
                    canRedo,
                    onClick = onRedo
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DrawTool.entries.forEach { tool ->
                    ToolChip(
                        tool = tool,
                        selected = tool == activeTool,
                        onClick = { onToolSelect(tool) })
                }
            }

            ToolbarAction(
                Icons.Rounded.AddCircle,
                "Clear",
                canUndo,
                danger = true,
                onClick = onClear
            )
        }
    }
}

@Composable
private fun ToolChip(tool: DrawTool, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (selected) AccentSoft else Color.Transparent,
        animationSpec = tween(200),
        label = "chipBg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Accent else StrokeColor,
        animationSpec = tween(200),
        label = "chipBorder",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.label,
                tint = if (selected) Accent else TextSec,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = tool.label,
                color = if (selected) Accent else TextSec,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun ToolbarAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                !enabled -> TextSec.copy(alpha = 0.3f)
                danger -> Danger
                else -> TextSec
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ConfirmClearDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        titleContentColor = TextPri,
        textContentColor = TextSec,
        iconContentColor = Danger,
        icon = { Icon(Icons.Rounded.ShoppingCart, contentDescription = null) },
        title = { Text("Clear canvas?", fontWeight = FontWeight.SemiBold) },
        text = { Text("All strokes will be permanently removed. This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Clear",
                    color = Danger,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSec) } },
    )
}

@Composable
fun SPenDrawingCanvas(
    modifier: Modifier = Modifier,
    strokes: List<StrokeData>,
    strokeColor: Color = Color.White,
    strokeWidth: Float = 5f,
    onStrokeComplete: (StrokeData) -> Unit,
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints = remember { mutableListOf<PointF>() }
    var invalidate by remember { mutableIntStateOf(0) }

    val renderedPaths = remember(strokes) { strokes.map { it.toPath() } }

    key(strokeColor) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.first()

                            if (change.type.isForAll()) {
                                val pos = change.position
                                when {
                                    change.pressed && currentPath == null -> {
                                        currentPoints = mutableListOf(PointF(pos.x, pos.y))
                                        currentPath = Path().apply {
                                            moveTo(pos.x, pos.y)
                                            lineTo(pos.x + 0.01f, pos.y + 0.01f)
                                        }
                                        invalidate++
                                    }

                                    change.pressed -> {
                                        currentPoints.add(PointF(pos.x, pos.y))
                                        currentPath?.lineTo(pos.x, pos.y)
                                        invalidate++
                                    }

                                    !change.pressed && currentPath != null -> {
                                        onStrokeComplete(
                                            StrokeData(
                                                points = currentPoints.toList(),
                                                color = strokeColor.value.toLong(),
                                                width = strokeWidth,
                                            )
                                        )
                                        currentPath = null
                                        currentPoints = mutableListOf()
                                    }
                                }
                                change.consume()
                            }
                        }
                    }
                }
        ) {
            @Suppress("UNUSED_EXPRESSION") invalidate

            val baseStyle =
                Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

            renderedPaths.forEachIndexed { i, path ->
                drawPath(
                    path = path,
                    color = Color(strokes[i].color.toULong()),
                    style = Stroke(
                        width = strokes[i].width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            currentPath?.let { path ->
                drawPath(path = path, color = strokeColor, style = baseStyle)
            }
        }
    }
}

@Composable
fun RenameDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentTitle) }
    val isValid = text.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        titleContentColor = TextPri,
        textContentColor = TextSec,
        iconContentColor = Accent,
        icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
        title = { Text("Rename note", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("Note title", color = TextSec) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = StrokeColor,
                    focusedTextColor = TextPri,
                    unfocusedTextColor = TextPri,
                    cursorColor = Accent,
                    focusedContainerColor = Surface3,
                    unfocusedContainerColor = Surface3,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (isValid) onConfirm(text.trim()) }
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (isValid) onConfirm(text.trim()) }, enabled = isValid) {
                Text(
                    "Rename",
                    color = if (isValid) Accent else TextSec,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSec) }
        },
    )
}