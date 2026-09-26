package com.beomsoo.sentencelibrary.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.beomsoo.sentencelibrary.data.ReadingRepository
import com.beomsoo.sentencelibrary.ui.article.ArticleScreen
import com.beomsoo.sentencelibrary.ui.dashboard.DashboardScreen
import com.beomsoo.sentencelibrary.ui.export.ExportScreen
import com.beomsoo.sentencelibrary.ui.home.HomeScreen
import com.beomsoo.sentencelibrary.ui.library.PublicationsScreen
import com.beomsoo.sentencelibrary.ui.search.SearchScreen
import com.beomsoo.sentencelibrary.ui.settings.SettingsScreen

private data class Tab(val route:String,val label:String,val icon:androidx.compose.ui.graphics.vector.ImageVector)
private val tabs=listOf(
    Tab("home","홈",Icons.Default.Home), Tab("publications","출판물",Icons.Default.LibraryBooks),
    Tab("dashboard","대시보드",Icons.Default.Insights), Tab("search","검색",Icons.Default.Search)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceLibraryApp(repository:ReadingRepository) {
    val nav=rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route=entry?.destination?.route.orEmpty()
    val selectedTab=tabs.firstOrNull { route.startsWith(it.route) }
    val showBottom=selectedTab!=null
    Scaffold(
        topBar={
            TopAppBar(
                title={ Text("문장 라이브러리") },
                navigationIcon={ if(!showBottom) IconButton(onClick={nav.popBackStack()}) { Icon(Icons.Default.ArrowBack,"뒤로") } },
                actions={
                    IconButton(onClick={nav.navigate("export/all")}) { Icon(Icons.Default.IosShare,"내보내기") }
                    IconButton(onClick={nav.navigate("settings")}) { Icon(Icons.Default.Settings,"설정") }
                }
            )
        },
        bottomBar={ if(showBottom) NavigationBar {
            tabs.forEach { tab -> NavigationBarItem(
                selected=selectedTab?.route==tab.route,
                onClick={nav.navigate(tab.route){ popUpTo(nav.graph.findStartDestination().id){saveState=true}; launchSingleTop=true; restoreState=true }},
                icon={Icon(tab.icon,tab.label)}, label={Text(tab.label)}
            ) }
        }}
    ) { pad ->
        NavHost(navController=nav,startDestination="home",modifier=Modifier.padding(pad)) {
            composable("home") { HomeScreen(repository){ id->nav.navigate("article/$id") } }
            composable("publications?year={year}",arguments=listOf(navArgument("year"){type=NavType.IntType;defaultValue=0})) { back -> PublicationsScreen(repository,initialYear=back.arguments?.getInt("year")?:0,onArticle={nav.navigate("article/$it")}) }
            composable("dashboard") { DashboardScreen(repository,onYear={ year -> nav.navigate("publications?year=$year") }) }
            composable("search") { SearchScreen(repository){nav.navigate("article/$it")} }
            composable("article/{id}") { ArticleScreen(repository,it.arguments?.getString("id").orEmpty(),onExport={id->nav.navigate("export/$id")}) }
            composable("settings") { SettingsScreen(repository,onExport={nav.navigate("export/all")}) }
            composable("export/{id}") { ExportScreen(repository,it.arguments?.getString("id").takeUnless { v->v=="all" }) }
        }
    }
}
