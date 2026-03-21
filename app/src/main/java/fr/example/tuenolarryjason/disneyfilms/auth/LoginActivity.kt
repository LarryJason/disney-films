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
import fr.example.tuenolarryjason.disneyfilms.MainActivity
import fr.example.tuenolarryjason.disneyfilms.ui.theme.DisneyFilmsTheme

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            DisneyFilmsTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Connexion échouée : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            text = "Log In",
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

        // Password Field
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

            }

        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryRed),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Log in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vous n'avez pas de compte?", color = Color.Gray)
            TextButton(onClick = onRegisterClick) {
                Text("Sign up", color = primaryRed )
            }
        }
    }
}
