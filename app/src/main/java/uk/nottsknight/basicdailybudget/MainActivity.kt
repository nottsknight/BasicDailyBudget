package uk.nottsknight.basicdailybudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import uk.nottsknight.basicdailybudget.ui.SummaryScreen
import uk.nottsknight.basicdailybudget.ui.UpdateScreen
import uk.nottsknight.basicdailybudget.ui.theme.BasicDailyBudgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicDailyBudgetTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = { BdbAppBar() },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    BdbContent(navHostController = navController, innerPadding = innerPadding)
                }
            }
        }
    }
}

private const val PATH_SUMMARY_SCREEN = "summary"
private const val PATH_UPDATE_SCREEN = "update"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BdbAppBar() = TopAppBar(
    title = { Text(stringResource(R.string.appName)) },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary
    ),
)

@Composable
fun BdbContent(navHostController: NavHostController, innerPadding: PaddingValues) {
    Surface(modifier = Modifier.padding(innerPadding)) {
        NavHost(
            navController = navHostController,
            graph = navHostController.createGraph(PATH_SUMMARY_SCREEN) {
                composable(PATH_SUMMARY_SCREEN) {
                    SummaryScreen(onNavToUpdate = {
                        navHostController.navigate(PATH_UPDATE_SCREEN)
                    })
                }

                composable(PATH_UPDATE_SCREEN) {
                    UpdateScreen(onNavToSummary = { navHostController.navigate(PATH_SUMMARY_SCREEN) })
                }
            })
    }
}