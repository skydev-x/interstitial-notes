package com.skydev.canvastest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import com.skydev.canvastest.ui.theme.CanvasTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SPenDrawingCanvas(
                        modifier = Modifier.padding(innerPadding)
                    ){

                    }
                }
            }
        }
    }
}


@Composable
fun SPenDrawingCanvas(
    modifier: Modifier = Modifier,
    onSaveRequested: (Path) -> Unit
) {
    val paths = remember { mutableStateListOf<Path>() }

    // A snapshot-aware holder for the live path.
    // 'invalidate' is a dummy counter — incrementing it tells Compose
    // that currentPath's contents have changed and a redraw is needed.
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var invalidate by remember { mutableIntStateOf(0) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()

                        if (change.type == PointerType.Stylus) {
                            val pos = change.position

                            when {
                                change.pressed && currentPath == null -> {
                                    // Stroke start — create path and do first moveTo + lineTo
                                    // so even a tap produces a visible dot
                                    currentPath = Path().apply {
                                        moveTo(pos.x, pos.y)
                                        lineTo(pos.x + 0.01f, pos.y + 0.01f) // ensures dot on tap
                                    }
                                    invalidate++
                                }

                                change.pressed -> {
                                    // Stroke continue
                                    currentPath?.lineTo(pos.x, pos.y)
                                    invalidate++ // <-- this triggers recomposition each move
                                }

                                !change.pressed && currentPath != null -> {
                                    // Stroke end — commit to finished paths
                                    paths.add(currentPath!!)
                                    currentPath = null
                                    // No need to increment invalidate; paths list change triggers redraw
                                }
                            }
                            change.consume()
                        }
                    }
                }
            }
    ) {
        // Suppress unused-read warning — reading invalidate here ensures
        // the Canvas lambda re-executes on every pointer move
        @Suppress("UNUSED_EXPRESSION")
        invalidate
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        currentPath?.let { path ->
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}