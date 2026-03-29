package com.skydev.canvastest.ui.feature.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.skydev.canvastest.ui.nav.AppRoutes
import com.skydev.canvastest.ui.theme.Surface2


@Composable
fun TimelineScreen(
    navController: NavController,
    viewModel: TimeLineViewModel = hiltViewModel()
) {

    LaunchedEffect(key1 = true) {
        viewModel.loadNotes()
    }

    val notes = viewModel.state.collectAsStateWithLifecycle().value.items

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Surface2)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(notes) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clickable {
                            navController.navigate(AppRoutes.NoteTaking)
                        }
                        .padding(16.dp)
                ){
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${it.createdAt}"
                    )
                }
            }
        }
    }
}