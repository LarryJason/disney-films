package fr.example.tuenolarryjason.disneyfilms.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.example.tuenolarryjason.disneyfilms.ProfileActivity
import fr.example.tuenolarryjason.disneyfilms.ui.theme.DisneyFilmsTheme

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            DisneyFilmsTheme {
                RegisterScreen(
                    onRegisterClick = { email, password ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            // Save user email to database
                                            val database = FirebaseDatabase.getInstance().getReference("users")
                                            database.child(userId).child("email").setValue(email)
                                        }
                                        startActivity(Intent(this, ProfileActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Enregistrement échoué : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onLoginClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Enregistrement", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onRegisterClick(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("S'enregistrer")
        }
        TextButton(onClick = onLoginClick) {
            Text("Déjà un compte ? Se connecter")
        }
    }
}
