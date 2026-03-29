package com.skydev.canvastest.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.skydev.canvastest.ui.feature.notetaking.NoteTakingScreen
import com.skydev.canvastest.ui.feature.notetaking.NoteTakingViewModel
import com.skydev.canvastest.ui.feature.timeline.TimelineScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    viewModel: NoteTakingViewModel
) {
    val navController = rememberNavController()

    NavHost(
        startDestination = AppRoutes.Timeline,
        navController = navController,
        modifier = modifier
    ) {

        composable<AppRoutes.NoteTaking> {
            val id = it.toRoute<AppRoutes.NoteTaking>().id
            NoteTakingScreen(
                viewModel = viewModel,
                id = id,
            ) {
                navController.navigateUp()
            }
        }

        composable<AppRoutes.Timeline> {
            TimelineScreen(navController)
        }

    }
}