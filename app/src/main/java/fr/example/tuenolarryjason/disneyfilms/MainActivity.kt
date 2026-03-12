package fr.example.tuenolarryjason.disneyfilms

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import fr.example.tuenolarryjason.disneyfilms.auth.LoginActivity
import fr.example.tuenolarryjason.disneyfilms.ui.theme.DisneyFilmsTheme
import fr.example.tuenolarryjason.disneyfilms.userInterfaces.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisneyFilmsTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val context = androidx.compose.ui.platform.LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val canPop = navController.previousBackStackEntry != null
    val rootRoutes = listOf("univers", "categories", "profil")
    val showBackButton = canPop && currentRoute !in rootRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBackButton) {
                TopAppBar(
                    title = { Text("") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    }
                )
            }
        },
        bottomBar = { DisneyBottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "univers",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("univers") {
                UniverseListScreen(onUniverseClick = { name ->
                    navController.navigate("films/${Uri.encode(name)}")
                })
            }
            composable("categories") {
                CategoryListScreen(onCategoryClick = { categoryName ->
                    navController.navigate("franchises/${Uri.encode(categoryName)}")
                })
            }
            composable("franchises/{categoryName}") { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                FranchiseListScreen(categoryName, onFranchiseClick = { franchiseName ->
                    navController.navigate("films/${Uri.encode(franchiseName)}")
                })
            }
            composable("films/{franchiseName}") { backStackEntry ->
                val franchiseName = backStackEntry.arguments?.getString("franchiseName") ?: ""
                FilmListScreen(franchiseName, onFilmClick = { filmTitle ->
                    navController.navigate("film_details/${Uri.encode(filmTitle)}")
                })
            }
            composable("film_details/{filmTitle}") { backStackEntry ->
                val filmTitle = backStackEntry.arguments?.getString("filmTitle") ?: ""
                FilmDetailScreen(filmTitle)
            }
            composable("profil") {
                ProfileScreen(
                    userEmail = auth.currentUser?.email ?: "Inconnu",
                    onLogoutClick = {
                        auth.signOut()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    },
                    onMyFilmsClick = {
                        navController.navigate("mes_films")
                    }
                )
            }
            composable("mes_films") {
                MyFilmsScreen(onFilmClick = { filmTitle ->
                    navController.navigate("film_details/${Uri.encode(filmTitle)}")
                })
            }
        }
    }
}
