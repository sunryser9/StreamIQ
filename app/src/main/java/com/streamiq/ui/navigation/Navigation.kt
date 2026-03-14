package com.streamiq.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.streamiq.ui.screens.*
import com.streamiq.ui.viewmodel.StreamIQViewModel

sealed class Screen(val route: String) {
    object Onboarding  : Screen("onboarding")
    object Dashboard   : Screen("dashboard")
    object LogToday    : Screen("log_today")
    object AddStream   : Screen("add_stream")
    object Analytics   : Screen("analytics")
    object ShareCard   : Screen("share_card")
    object VoiceBot    : Screen("voice_bot")
    object TaxJar      : Screen("tax_jar")
    object StreamScore : Screen("stream_score")
    object Forecast    : Screen("forecast")
    object Insights    : Screen("insights")
    object Export      : Screen("export")
    object Currency    : Screen("currency")
    object Pro         : Screen("pro")
}

@Composable
fun StreamIQNavHost(
    navController: NavHostController,
    viewModel: StreamIQViewModel,
    onToggleTheme: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val startDestination = if (uiState.onboardingComplete) Screen.Dashboard.route
                           else Screen.Onboarding.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(viewModel = viewModel, onComplete = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onLogToday    = { navController.navigate(Screen.LogToday.route) },
                onAddStream   = { navController.navigate(Screen.AddStream.route) },
                onAnalytics   = { navController.navigate(Screen.Analytics.route) },
                onShare       = { navController.navigate(Screen.ShareCard.route) },
                onToggleTheme = onToggleTheme,
                onVoiceBot    = { navController.navigate(Screen.VoiceBot.route) },
                onTaxJar      = { navController.navigate(Screen.TaxJar.route) },
                onStreamScore = { navController.navigate(Screen.StreamScore.route) },
                onForecast    = { navController.navigate(Screen.Forecast.route) },
                onInsights    = { navController.navigate(Screen.Insights.route) },
                onExport      = { navController.navigate(Screen.Export.route) },
                onCurrency    = { navController.navigate(Screen.Currency.route) },
                onPro         = { navController.navigate(Screen.Pro.route) }
            )
        }
        composable(Screen.LogToday.route) {
            LogTodayScreen(viewModel = viewModel, onDone = { navController.popBackStack() })
        }
        composable(Screen.AddStream.route) {
            AddStreamScreen(viewModel = viewModel, onDone = { navController.popBackStack() })
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.ShareCard.route) {
            ShareCardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.VoiceBot.route) {
            VoiceBotScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.TaxJar.route) {
            TaxJarScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.StreamScore.route) {
            StreamScoreScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Forecast.route) {
            ForecastScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Insights.route) {
            InsightsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Export.route) {
            ExportScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Pro.route) {
            ProScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Currency.route) {
            CurrencyScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
