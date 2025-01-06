package com.example.codequest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codequest.ui.theme.CodeQuestTheme

class ConsentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeQuestTheme {
                ConsentScreen { agreed ->
                    if (agreed) {
                        // Proceed to Login Activity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Show a message or handle the case where user does not agree
                        Toast.makeText(this, "You must agree to the terms to continue.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun ConsentScreen(onConsent: (Boolean) -> Unit) {
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GDPR Consent Form",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF42A5F5),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = """
                By proceeding, you agree to our terms and conditions. We will process your data 
                according to the guidelines outlined in the privacy policy.
            """.trimIndent(),
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("I agree to the terms and conditions")
        }

        Button(
            onClick = { onConsent(isChecked) },
            enabled = isChecked,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
        ) {
            Text("Agree and Continue", color = Color.White)
        }
    }
}
