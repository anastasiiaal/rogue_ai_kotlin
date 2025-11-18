package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Commande de type "toggle" (inclut ON_OFF_BUTTON, TOGGLE, CUSTOM_BUTTON).
 *
 * On adapte l’UI selon styleType :
 *  - onoff_button    -> 1 gros bouton Activer / Désactiver
 *  - custom_button   -> 2 boutons avec labels personnalisés
 *  - toggle (simple) -> 1 bouton par action_possible
 */
@Composable
fun ToggleCommandView(
    commandId: String,
    name: String,
    styleType: String,
    actualStatus: String,
    actions: List<String>,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val titlePrefix = if (isHighlighted) ">>> " else ""
    val style = styleType.lowercase()

    Column {
        // Titre
        Text(
            text = titlePrefix + name,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Type: toggle [$styleType] – État: $actualStatus",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (actions.isEmpty()) {
            Text(
                text = "Aucune action possible 🤔",
                style = MaterialTheme.typography.bodySmall
            )
            return
        }

        when (style) {
            "onoff_button" -> OnOffToggleContent(
                commandId = commandId,
                actualStatus = actualStatus,
                actions = actions,
                onExecuteAction = onExecuteAction
            )

            "custom_button" -> CustomToggleContent(
                commandId = commandId,
                actualStatus = actualStatus,
                actions = actions,
                onExecuteAction = onExecuteAction
            )

            else -> GenericToggleContent(
                commandId = commandId,
                actions = actions,
                onExecuteAction = onExecuteAction
            )
        }
    }
}

/**
 * ON_OFF_BUTTON :
 *   - un seul gros bouton
 *   - texte "Activer" ou "Désactiver" selon actualStatus
 *   - on envoie la première action_possible (souvent "toggle")
 */
@Composable
private fun OnOffToggleContent(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val primaryAction = actions.firstOrNull() ?: "toggle"
    val isOn = actualStatus.equals("active", ignoreCase = true) ||
            actualStatus.equals("on", ignoreCase = true) ||
            actualStatus.equals("true", ignoreCase = true)

    val label = if (isOn) "Désactiver" else "Activer"

    Button(onClick = { onExecuteAction(commandId, primaryAction) }) {
        Text(label)
    }
}

/**
 * CUSTOM_BUTTON :
 *   - on suppose 2 actions possibles (gauche / droite)
 *   - on affiche 2 boutons côte à côte avec ces labels
 *   - celui qui correspond à actualStatus apparaît "plein", l’autre en outline
 */
@Composable
private fun CustomToggleContent(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val left = actions.getOrNull(0)
    val right = actions.getOrNull(1)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        left?.let { label ->
            val isSelected = actualStatus.equals(label, ignoreCase = true)

            if (isSelected) {
                Button(onClick = { onExecuteAction(commandId, label) }) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { onExecuteAction(commandId, label) }) {
                    Text(label)
                }
            }
        }

        right?.let { label ->
            val isSelected = actualStatus.equals(label, ignoreCase = true)

            if (isSelected) {
                Button(onClick = { onExecuteAction(commandId, label) }) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { onExecuteAction(commandId, label) }) {
                    Text(label)
                }
            }
        }
    }
}

/**
 * Fallback générique :
 *   - un bouton "plein" pour la première action
 *   - des boutons outline pour les actions suivantes
 */
@Composable
private fun GenericToggleContent(
    commandId: String,
    actions: List<String>,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    actions.forEachIndexed { index, action ->
        Spacer(modifier = Modifier.height(4.dp))

        if (index == 0) {
            Button(onClick = { onExecuteAction(commandId, action) }) {
                Text(action)
            }
        } else {
            OutlinedButton(onClick = { onExecuteAction(commandId, action) }) {
                Text(action)
            }
        }
    }
}
