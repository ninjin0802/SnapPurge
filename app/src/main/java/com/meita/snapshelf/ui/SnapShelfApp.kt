package com.meita.snapshelf.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meita.snapshelf.core.AppContainer

@Composable
fun SnapShelfApp(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(container))
            HomeScreen(
                viewModel = vm,
                onOpenDetail = { id -> navController.navigate("detail/$id") },
                onOpenDeveloper = { navController.navigate("developer") },
                onOpenDocuments = { navController.navigate("documents") }
            )
        }
        composable("documents") { DocumentOrganizerScreen { navController.popBackStack() } }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val vm: DetailViewModel = viewModel(factory = DetailViewModel.Factory(id, container))
            DetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
        composable("developer") {
            DeveloperScreen(
                onBack = { navController.popBackStack() },
                onOpenPrivacy = { navController.navigate("privacy") },
                onOpenTerms = { navController.navigate("terms") }
            )
        }
        composable("privacy") { LegalScreen("プライバシーポリシー", PrivacyPolicyText) { navController.popBackStack() } }
        composable("terms") { LegalScreen("利用規約", TermsText) { navController.popBackStack() } }
    }
}
