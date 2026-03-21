package fr.example.tuenolarryjason.disneyfilms.userInterfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.example.tuenolarryjason.disneyfilms.models.UserFilm

@Composable
fun ProfileScreen(
    userEmail: String,
    onLogoutClick: () -> Unit,
    onMyFilmsClick: () -> Unit,
    onWatchedFilmsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mon Profil", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Connecté : $userEmail", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onMyFilmsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Mes Films")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onWatchedFilmsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Done, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Les films vus")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Se déconnecter")
        }
    }
}

@Composable
fun MyFilmsScreen(onFilmClick: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val userFilmsRef = FirebaseDatabase.getInstance().getReference("user_films").child(userId ?: "anonymous")
    
    var allUserFilms by remember { mutableStateOf(listOf<UserFilm>()) }

    LaunchedEffect(userId) {
        userFilmsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UserFilm>()
                for (filmSnapshot in snapshot.children) {
                    val userFilm = filmSnapshot.getValue(UserFilm::class.java)
                    if (userFilm != null) {
                        list.add(userFilm)
                    }
                }
                allUserFilms = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val toWatchFilms = allUserFilms.filter { it.wantToWatch }
    val ownedFilms = allUserFilms.filter { it.ownOnDVD }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Mes Films", style = MaterialTheme.typography.headlineMedium) }

        if (ownedFilms.isNotEmpty()) {
            item { Text("Ma Collection (DVD/Blu-ray)", style = MaterialTheme.typography.titleLarge) }
            items(ownedFilms) { film ->
                ListCardItem(
                    title = film.filmTitle,
                    icon = Icons.Default.Star,
                    onClick = { onFilmClick(film.filmTitle) }
                )
            }
        }

        if (toWatchFilms.isNotEmpty()) {
            item { Text("À regarder", style = MaterialTheme.typography.titleLarge) }
            items(toWatchFilms) { film ->
                ListCardItem(
                    title = film.filmTitle,
                    icon = Icons.Default.Star,
                    onClick = { onFilmClick(film.filmTitle) }
                )
            }
        }

        if (ownedFilms.isEmpty() && toWatchFilms.isEmpty()) {
            item {
                Text("Vous n'avez pas encore ajouté de films à votre collection.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun WatchedFilmsScreen(onFilmClick: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val userFilmsRef = FirebaseDatabase.getInstance().getReference("user_films").child(userId ?: "anonymous")
    
    var watchedFilms by remember { mutableStateOf(listOf<UserFilm>()) }

    LaunchedEffect(userId) {
        userFilmsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<UserFilm>()
                for (filmSnapshot in snapshot.children) {
                    val userFilm = filmSnapshot.getValue(UserFilm::class.java)
                    // Utilise 'watched' qui est le nom dans le modèle
                    if (userFilm?.watched == true) {
                        list.add(userFilm)
                    }
                }
                watchedFilms = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun toggleWatched(film: UserFilm) {
        val safeTitle = film.filmTitle.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
        userFilmsRef.child(safeTitle).child("watched").setValue(!film.watched)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Films vus", style = MaterialTheme.typography.headlineMedium) }

        if (watchedFilms.isEmpty()) {
            item {
                Text("Vous n'avez pas encore marqué de films comme vus.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(watchedFilms) { film ->
                ListCardItem(
                    title = film.filmTitle,
                    icon = Icons.Default.CheckCircle,
                    onClick = { onFilmClick(film.filmTitle) },
                    trailingContent = {
                        IconButton(onClick = { toggleWatched(film) }) {
                            Icon(
                                imageVector = if (film.watched) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                                contentDescription = "Marquer comme vu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}
