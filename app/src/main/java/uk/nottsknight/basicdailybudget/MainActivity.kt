package uk.nottsknight.basicdailybudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import uk.nottsknight.basicdailybudget.ui.SummaryScreen
import uk.nottsknight.basicdailybudget.ui.theme.BasicDailyBudgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicDailyBudgetTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BdbContent(navHostController = navController, innerPadding = innerPadding)
                }
            }
        }
    }
}

private const val PATH_SUMMARY_SCREEN = "summary"

@Composable
fun BdbContent(navHostController: NavHostController, innerPadding: PaddingValues) {
    Surface(modifier = Modifier.padding(innerPadding)) {
        NavHost(
            navController = navHostController,
            graph = navHostController.createGraph(PATH_SUMMARY_SCREEN) {
                composable(PATH_SUMMARY_SCREEN) { SummaryScreen() }
            })
    }
}