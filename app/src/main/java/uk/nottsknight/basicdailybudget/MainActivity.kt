package uk.nottsknight.basicdailybudget

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import uk.nottsknight.basicdailybudget.ui.NewAccountScreen
import uk.nottsknight.basicdailybudget.ui.SummaryScreen
import uk.nottsknight.basicdailybudget.ui.UpdateScreen
import uk.nottsknight.basicdailybudget.ui.theme.AppTheme

val Context.preferences: DataStore<Preferences> by preferencesDataStore(name = "bdb_prefs")

private const val PATH_MAIN = "main_screen"
private const val PATH_NEW_ACCOUNT = "new_account_screen"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val snackHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()

                Scaffold(
                    topBar = { BdbAppBar() }, modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackHostState) }
                ) { innerPadding ->
                    BdbContent(
                        navController = navController,
                        snackHost = snackHostState,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BdbAppBar() = TopAppBar(
    title = { Text(stringResource(R.string.appName)) },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BdbContent(
    navController: NavHostController,
    snackHost: SnackbarHostState,
    innerPadding: PaddingValues
) {

    Surface(modifier = Modifier.padding(innerPadding)) {
        NavHost(navController = navController, graph = navController.createGraph(PATH_MAIN) {
            composable(PATH_MAIN) {
                Column {
                    var selectedIndex by remember { mutableIntStateOf(0) }
                    PrimaryTabRow(selectedTabIndex = selectedIndex) {
                        Tab(selected = selectedIndex == 0,
                            onClick = { selectedIndex = 0 },
                            text = { Text(stringResource(R.string.summaryTabLbl)) })

                        Tab(selected = selectedIndex == 1,
                            onClick = { selectedIndex = 1 },
                            text = { Text(stringResource(R.string.incomeTabLbl)) })
                    }

                    when (selectedIndex) {
                        0 -> SummaryScreen(snackHost) { navController.navigate(PATH_NEW_ACCOUNT) }
                        1 -> UpdateScreen(snackHost)
                    }
                }
            }

            composable(PATH_NEW_ACCOUNT) { NewAccountScreen { navController.navigate(PATH_MAIN) } }
        })
    }
}