package com.example.rogueai.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Mode solo de test, 100% local :
 * - affiche des instructions techniques absurdes
 * - bouton "Action exécutée" pour passer à la suivante
 * - bouton "Terminer la partie" pour revenir à l'accueil
 */
@Composable
fun SoloGameScreen(
    roomCode: String,
    onExit: () -> Unit
) {
    // Liste d’instructions débiles façon Spaceteam/RogueAI
    val instructions = remember {
        listOf(
            "Inverser le flux de particules quantiques du module GPU n°7.",
            "Défragmenter la mémoire émotionnelle de l’IA.",
            "Rebooter le sous-système de sarcasme en mode sécurisé.",
            "Réaligner le flux d’octets sur l’axe Z du serveur.",
            "Purger le cache des pensées toxiques.",
            "Activer le protocole de surchauffe contrôlée du processeur.",
            "Réinitialiser la matrice de probabilité des bugs critiques.",
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    val currentInstruction = instructions[currentIndex]

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rogue AI – Mode solo",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Room : $roomCode",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentInstruction,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crie cette instruction à tes coéquipiers imaginaires.\nEnsuite, appuie sur le bouton ci-dessous 🤖",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Passe à l’instruction suivante (boucle)
                    currentIndex = (currentIndex + 1) % instructions.size
                }
            ) {
                Text("Action exécutée (Instruction suivante)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onExit) {
                Text("Terminer la partie et revenir à l'accueil")
            }
        }
    }
}
