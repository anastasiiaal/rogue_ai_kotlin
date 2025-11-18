package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Commande de type "slider".
 *
 * Exemple de payload :
 * {
 *   id: "volume_hallucinations",
 *   name: "Volume hallucinations",
 *   type: "slider",
 *   styleType: "slider",
 *   actual_status: "0",
 *   action_possible: ["1","2",...,"10"]
 * }
 */
@Composable
fun SliderCommandView(
    commandId: String,
    name: String,
    actualStatus: String,
    actions: List<String>,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val titlePrefix = if (isHighlighted) ">>> " else ""

    Column {
        Text(
            text = titlePrefix + name,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Type: slider – Valeur actuelle: $actualStatus",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (actions.isEmpty() && actualStatus.isBlank()) {
            Text(
                text = "Aucune valeur possible 🤔",
                style = MaterialTheme.typography.bodySmall
            )
            return
        }

        Text(
            text = "Choisis une valeur :",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 🔹 1) On prépare la liste complète à afficher :
        //    union(action_possible, actual_status), sans doublons, triée numériquement.
        val allValues = (actions + actualStatus)
            .filter { it.isNotBlank() }
            .distinct()
            .sortedBy { it.toIntOrNull() ?: 0 }

        // 🔹 2) Pour savoir quelles valeurs sont vraiment activables côté backend
        val clickableSet = actions.toSet()

        val chunkSize = 4
        allValues.chunked(chunkSize).forEach { chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chunk.forEach { value ->
                    val isSelected = value == actualStatus
                    val isClickable = clickableSet.contains(value)

                    when {
                        // Bouton de la valeur actuelle (non cliquable)
                        isSelected -> {
                            Button(
                                onClick = { /* rien, déjà sélectionné */ },
                                enabled = false,
                                modifier = Modifier.weight(1f, fill = true)
                            ) {
                                Text(value)
                            }
                        }

                        // Valeur possible selon le backend → bouton cliquable
                        isClickable -> {
                            OutlinedButton(
                                onClick = { onExecuteAction(commandId, value) },
                                modifier = Modifier.weight(1f, fill = true)
                            ) {
                                Text(value)
                            }
                        }

                        // Valeur affichée mais non cliquable (par sécurité)
                        else -> {
                            OutlinedButton(
                                onClick = { /* non autorisé */ },
                                enabled = false,
                                modifier = Modifier.weight(1f, fill = true)
                            ) {
                                Text(value)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
