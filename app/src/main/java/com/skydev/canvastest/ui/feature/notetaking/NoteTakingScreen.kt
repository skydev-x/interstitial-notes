package com.skydev.canvastest.ui.feature.notetaking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydev.canvastest.domain.model.PointF
import com.skydev.canvastest.domain.model.StrokeData
import com.skydev.canvastest.domain.model.toPath
import com.skydev.canvastest.ui.feature.notetaking.component.ColorPickerRow
import com.skydev.canvastest.ui.utils.isForAll


@Composable
fun NoteTakingScreen(
    viewModel: NoteTakingViewModel,
    modifier: Modifier = Modifier
) {
    val strokes = viewModel.strokes.collectAsStateWithLifecycle().value
    Box(
        modifier = modifier
    ) {

        var color by remember { mutableStateOf(Color.White) }

        SPenDrawingCanvas(
            modifier = Modifier.fillMaxSize(),
            strokes = strokes,
            strokeColor = color,
            strokeWidth = 5f,
        ) {
            viewModel.onStrokeComplete(it)
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = viewModel::undo,
                enabled = strokes.isNotEmpty()
            ) { Text("Undo") }

            Button(
                onClick = viewModel::clear,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = strokes.isNotEmpty()
            ) { Text("Clear") }
        }
        ColorPickerRow(modifier = Modifier.align(Alignment.TopCenter)) {
            color = it
        }
    }
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

    val renderedPaths = remember(strokes) {
        strokes.map { it.toPath() }
    }

    val colorValue by remember(strokeColor) {
        derivedStateOf {
            strokeColor.value.toLong()
        }
    }

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
                                                color = colorValue,
                                                width = strokeWidth
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
            val style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            renderedPaths.forEachIndexed { i, path ->
                drawPath(
                    path = path,
                    color = Color(strokes[i].color.toULong()),
                    style = Stroke(
                        width = strokes[i].width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            currentPath?.let { path ->
                drawPath(path = path, color = strokeColor, style = style)
            }
        }
    }
}
