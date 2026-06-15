package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppPolicySection() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(20.dp))
                Text("Charte de la Communauté Mirys ⚖️", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
            
            Text(
                text = "Pour garantir une expérience sécurisée et enrichissante, chaque membre s'engage à respecter nos piliers fondamentaux :",
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 16.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PolicyPoint(Icons.Default.VerifiedUser, "Authenticité", "Soyez vous-même. Les faux profils et l'usurpation d'identité sont strictement interdits.")
                PolicyPoint(Icons.Default.Favorite, "Bienveillance", "Le harcèlement, les discours de haine et la toxicité n'ont pas leur place sur Mirys.")
                PolicyPoint(Icons.Default.Lock, "Confidentialité", "Respectez la vie privée d'autrui. Ne partagez jamais d'informations personnelles sans consentement.")
                PolicyPoint(Icons.Default.SafetyCheck, "Sécurité", "Tout contenu illégal ou dangereux entraînera un bannissement immédiat et définitif.")
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "En utilisant Mirys, vous acceptez nos Conditions Générales et notre Politique de Confidentialité. L'IA de Mirys veille en temps réel au respect de ces règles.",
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun PolicyPoint(icon: ImageVector, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Text(text = desc, fontSize = 10.sp, color = Color.Gray, lineHeight = 14.sp)
        }
    }
}
