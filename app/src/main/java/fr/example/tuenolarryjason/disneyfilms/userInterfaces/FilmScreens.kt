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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp

import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.example.tuenolarryjason.disneyfilms.models.Film
import fr.example.tuenolarryjason.disneyfilms.models.UserFilm

@Composable
fun FilmListScreen(franchiseName: String, onFilmClick: (String) -> Unit) {
    var films by remember { mutableStateOf(listOf<Film>()) }
    val database = FirebaseDatabase.getInstance().getReference("categories")

    LaunchedEffect(franchiseName) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filmList = mutableListOf<Film>()
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.child("franchises").children.forEach { franchiseSnapshot ->
                        val currentFranchise = franchiseSnapshot.child("nom").getValue(String::class.java)
                        if (currentFranchise?.trim().equals(franchiseName.trim(), ignoreCase = true)) {
                            franchiseSnapshot.child("sous_sagas").children.forEach { sousSaga ->
                                sousSaga.child("films").children.forEach { filmSnapshot ->
                                    filmSnapshot.getValue(Film::class.java)?.let { filmList.add(it) }
                                }
                            }
                            franchiseSnapshot.child("films").children.forEach { filmSnapshot ->
                                filmSnapshot.getValue(Film::class.java)?.let { filmList.add(it) }
                            }
                        }
                    }
                }
                films = filmList.distinctBy { it.titre }
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
                items(films) { film ->
                    ListCardItem(
                        title = film.titre,
                        subtitle = "Année : ${film.annee}",
                        icon = Icons.Default.Star,
                        onClick = { onFilmClick(film.titre) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilmDetailScreen(filmTitle: String) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val database = FirebaseDatabase.getInstance().getReference()
    val allUserFilmsRef = database.child("user_films")
    val currentUserRef = allUserFilmsRef.child(userId ?: "anonymous")
    val usersRef = database.child("users")

    var filmDetails by remember { mutableStateOf<Film?>(null) }
    var userFilmStatus by remember { mutableStateOf(UserFilm(filmTitle)) }
    var usersInterestedEmails by remember { mutableStateOf(listOf<String>()) }

    val safeTitle = filmTitle.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")

    LaunchedEffect(filmTitle) {
        // Film info
        database.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.child("franchises").children.forEach { franchiseSnapshot ->
                        franchiseSnapshot.child("sous_sagas").children.forEach { sousSaga ->
                            sousSaga.child("films").children.forEach { filmSnapshot ->
                                if (filmSnapshot.child("titre").getValue(String::class.java) == filmTitle) {
                                    filmDetails = filmSnapshot.getValue(Film::class.java)
                                    return
                                }
                            }
                        }
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

        // Current user status
        currentUserRef.child(safeTitle).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(UserFilm::class.java)?.let { userFilmStatus = it }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Get interested users emails
        allUserFilmsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userIds = mutableListOf<String>()
                for (userSnapshot in snapshot.children) {
                    if (userSnapshot.key == userId) continue
                    val filmStatus = userSnapshot.child(safeTitle).getValue(UserFilm::class.java)
                    if (filmStatus?.ownOnDVD == true && filmStatus.wantToGetRidOf == true) {
                        userSnapshot.key?.let { userIds.add(it) }
                    }
                }

                if (userIds.isNotEmpty()) {
                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnap: DataSnapshot) {
                            val emails = mutableListOf<String>()
                            userIds.forEach { id ->
                                val email = userSnap.child(id).child("email").getValue(String::class.java)
                                if (email != null) emails.add(email)
                                else emails.add("Utilisateur $id")
                            }
                            usersInterestedEmails = emails
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                } else {
                    usersInterestedEmails = emptyList()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun updateStatus(newStatus: UserFilm) {
        currentUserRef.child(safeTitle).setValue(newStatus)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Détails du Film", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            filmDetails?.let { film ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(Icons.Default.Star, "Titre", film.titre)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow(Icons.Default.DateRange, "Année", film.annee.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow(Icons.Default.Info, "Genre", film.genre)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow(Icons.Default.Done, "Numéro", film.numero.toString())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Ma Collection", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(
                            label = "Vu",
                            icon = Icons.Default.Face,
                            selected = userFilmStatus.watched,
                            onSelectedChange = { updateStatus(userFilmStatus.copy(watched = it)) },
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(
                            label = "À regarder",
                            icon = Icons.Default.Refresh,
                            selected = userFilmStatus.wantToWatch,
                            onSelectedChange = { updateStatus(userFilmStatus.copy(wantToWatch = it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(
                            label = "DVD/Blu-ray",
                            icon = Icons.Default.Star,
                            selected = userFilmStatus.ownOnDVD,
                            onSelectedChange = { updateStatus(userFilmStatus.copy(ownOnDVD = it)) },
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(
                            label = "À supprimer",
                            icon = Icons.Default.Delete,
                            selected = userFilmStatus.wantToGetRidOf,
                            onSelectedChange = { updateStatus(userFilmStatus.copy(wantToGetRidOf = it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (usersInterestedEmails.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Disponibilité (Occasion)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ces utilisateurs souhaitent s'en débarrasser :", style = MaterialTheme.typography.bodySmall)
                }
            } ?: Text("Chargement des détails...")
        }

        if (usersInterestedEmails.isNotEmpty()) {
            items(usersInterestedEmails) { email ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(email, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
        modifier = modifier
    )
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
