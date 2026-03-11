package fr.example.tuenolarryjason.disneyfilms.userInterfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.example.tuenolarryjason.disneyfilms.models.Film

@Composable
fun FilmListScreen(franchiseName: String, onFilmClick: (String) -> Unit) {
    var films by remember { mutableStateOf(listOf<String>()) }
    val database = FirebaseDatabase.getInstance().getReference("categories")

    LaunchedEffect(franchiseName) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filmTitles = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.child("franchises").children.forEach { franchiseSnapshot ->
                        val currentFranchise = franchiseSnapshot.child("nom").getValue(String::class.java)
                        if (currentFranchise?.trim().equals(franchiseName.trim(), ignoreCase = true)) {
                            // Sous-sagas
                            franchiseSnapshot.child("sous_sagas").children.forEach { sousSaga ->
                                sousSaga.child("films").children.forEach { film ->
                                    film.child("titre").getValue(String::class.java)?.let { filmTitles.add(it) }
                                }
                            }
                            // Direct films
                            franchiseSnapshot.child("films").children.forEach { film ->
                                film.child("titre").getValue(String::class.java)?.let { filmTitles.add(it) }
                            }
                        }
                    }
                }
                films = filmTitles.distinct()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Films : $franchiseName", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        if (films.isEmpty()) {
            Text("Chargement...", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(films) { filmTitle ->
                    ListCardItem(title = filmTitle, icon = Icons.Default.Star, onClick = { onFilmClick(filmTitle) })
                }
            }
        }
    }
}

@Composable
fun FilmDetailScreen(filmTitle: String) {
    var filmDetails by remember { mutableStateOf<Film?>(null) }
    val database = FirebaseDatabase.getInstance().getReference("categories")

    LaunchedEffect(filmTitle) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.child("franchises").children.forEach { franchiseSnapshot ->
                        // Recherche dans sous_sagas
                        franchiseSnapshot.child("sous_sagas").children.forEach { sousSaga ->
                            sousSaga.child("films").children.forEach { filmSnapshot ->
                                if (filmSnapshot.child("titre").getValue(String::class.java) == filmTitle) {
                                    filmDetails = filmSnapshot.getValue(Film::class.java)
                                    return
                                }
                            }
                        }
                        // Recherche en direct
                        franchiseSnapshot.child("films").children.forEach { filmSnapshot ->
                            if (filmSnapshot.child("titre").getValue(String::class.java) == filmTitle) {
                                filmDetails = filmSnapshot.getValue(Film::class.java)
                                return
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Détails du Film", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        filmDetails?.let { film ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(Icons.Default.Star, "Titre", film.titre)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailRow(Icons.Default.DateRange, "Année", film.annee.toString())
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailRow(Icons.Default.MoreVert, "Genre", film.genre)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DetailRow(Icons.Default.Check, "Numéro", film.numero.toString())
                }
            }
        } ?: Text("Chargement des détails...")
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
