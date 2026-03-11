package fr.example.tuenolarryjason.disneyfilms.userInterfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
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
fun CategoryListScreen(onCategoryClick: (String) -> Unit) {
    var categories by remember { mutableStateOf(listOf<String>()) }
    val database = FirebaseDatabase.getInstance().getReference("categories")

    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = snapshot.children.mapNotNull { it.child("categorie").getValue(String::class.java) }
                categories = categoryList.distinct().sorted()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Catégories", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { category ->
                ListCardItem(title = category, icon = Icons.Default.Menu, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
fun FranchiseListScreen(categoryName: String, onFranchiseClick: (String) -> Unit) {
    var franchises by remember { mutableStateOf(listOf<String>()) }
    val database = FirebaseDatabase.getInstance().getReference("categories")

    LaunchedEffect(categoryName) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val franchiseList = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    if (categorySnapshot.child("categorie").getValue(String::class.java)?.trim().equals(categoryName.trim(), ignoreCase = true)) {
                        categorySnapshot.child("franchises").children.forEach { franchiseSnapshot ->
                            franchiseSnapshot.child("nom").getValue(String::class.java)?.let { franchiseList.add(it) }
                        }
                    }
                }
                franchises = franchiseList.sorted()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Franchises de $categoryName", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(franchises) { franchise ->
                ListCardItem(title = franchise, icon = Icons.Default.Place, onClick = { onFranchiseClick(franchise) })
            }
        }
    }
}
