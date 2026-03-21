package fr.example.tuenolarryjason.disneyfilms.userInterfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun UniverseListScreen(onUniverseClick: (String) -> Unit) {
    var universes by remember { mutableStateOf(listOf<String>()) }
    val database = FirebaseDatabase.getInstance().getReference("categories")

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val universeSet = mutableSetOf<String>()
                for (categorySnapshot in snapshot.children) {
                    categorySnapshot.child("franchises").children.forEach { franchiseSnapshot ->
                        franchiseSnapshot.child("nom").getValue(String::class.java)?.let { universeSet.add(it) }
                    }
                }
                universes = universeSet.toList().sorted()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Les Univers Disney", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(universes) { universe ->
                ListCardItem(title = universe, icon = null, onClick = { onUniverseClick(universe) })
            }
        }
    }
}
