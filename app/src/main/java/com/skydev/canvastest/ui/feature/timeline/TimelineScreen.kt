package com.skydev.canvastest.ui.feature.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.skydev.canvastest.ui.nav.AppRoutes


@Composable
fun TimelineScreen(
    navController: NavController
) {

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Red)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(5) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Color.Green)
                        .padding(16.dp)
                        .clickable {
                            navController.navigate(AppRoutes.NoteTaking)
                        })
            }
        }
    }
}