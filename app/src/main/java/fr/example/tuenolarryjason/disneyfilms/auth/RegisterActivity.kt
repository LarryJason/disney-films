package fr.example.tuenolarryjason.disneyfilms.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var passwordVisible by remember { mutableStateOf(false) }

    // Couleurs
    val backgroundColor = Color(0xFF1B1B2F)
    val inputBackground = Color(0xFF252542)
    val primaryRed = Color(0xFFE94560)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))


        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Email", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryRed,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = inputBackground,
                unfocusedContainerColor = inputBackground,
                cursorColor = primaryRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Mot de passe", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Cacher" else "Montrer", color = Color.Gray)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryRed,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = inputBackground,
                unfocusedContainerColor = inputBackground,
                cursorColor = primaryRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))



        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onRegisterClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Sign up", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vous avez déjà un compte?", color = Color.Gray)
            TextButton(onClick = onLoginClick) {
                Text("Sign in", color = primaryRed)
            }
        }
    }
}
