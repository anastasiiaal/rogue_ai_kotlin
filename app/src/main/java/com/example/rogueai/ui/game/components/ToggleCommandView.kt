package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// --- Helpers -----------------------------------------------------------------

private fun isBinaryStatus(status: String): Boolean {
    return status.equals("active", true) ||
            status.equals("inactive", true) ||
            status.equals("on", true) ||
            status.equals("off", true) ||
            status.equals("true", true) ||
            status.equals("false", true)
}

@Composable
private fun BinaryToggleButton(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (String, String) -> Unit
) {
    val isActive =
        actualStatus.equals("active", true) ||
                actualStatus.equals("on", true) ||
                actualStatus.equals("true", true)

    val label = if (isActive) "Activé ✅" else "Désactivé ❌"
    val action = actions.firstOrNull() ?: "toggle"

    Button(onClick = { onExecuteAction(commandId, action) }) {
        Text(label)
    }
}

// --- API publique ------------------------------------------------------------

/**
 * Commande de type "toggle" (inclut ON_OFF_BUTTON, TOGGLE, CUSTOM_BUTTON).
 *
 * On adapte l’UI selon styleType :
 *  - onoff_button    -> 1 gros bouton On / Off
 *  - custom_button   -> 2 boutons avec labels personnalisés
 *  - toggle (simple) -> 1 bouton binaire ou liste d’actions
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
    val titlePrefix = if (isHighlighted) "👀 " else ""
    val style = styleType.lowercase()

    Column {
        // Titre
        Text(
            text = titlePrefix + name,
            style = MaterialTheme.typography.bodyMedium
        )

//        Text(
//            text = "Type: toggle [$styleType] – État: $actualStatus",
//            style = MaterialTheme.typography.bodySmall
//        )

        if (actions.isEmpty()) {
            Text(
                text = "Aucune action possible 🤔",
                style = MaterialTheme.typography.bodySmall
            )
            return
        }

        when (style) {
            // 1) ON/OFF → bouton "On"/"Off"
            "onoff_button" -> {
                OnOffToggleContent(
                    commandId = commandId,
                    actualStatus = actualStatus,
                    actions = actions,
                    onExecuteAction = onExecuteAction
                )
            }

            // 2) CUSTOM_BUTTON
            "custom_button" -> {
                if (actions.size <= 1 && isBinaryStatus(actualStatus)) {
                    // Cas binaire classique
                    BinaryToggleButton(
                        commandId = commandId,
                        actualStatus = actualStatus,
                        actions = actions,
                        onExecuteAction = onExecuteAction
                    )
                } else {
                    // Vrai mode custom : deux boutons distincts
                    CustomToggleDoubleButtons(
                        commandId = commandId,
                        actualStatus = actualStatus,
                        actions = actions,
                        onExecuteAction = onExecuteAction
                    )
                }
            }

            // 3) toggle simple ou style inconnu
            else -> {
                GenericToggleContent(
                    commandId = commandId,
                    actualStatus = actualStatus,
                    actions = actions,
                    onExecuteAction = onExecuteAction
                )
            }
        }
    }
}

// --- Implémentations privées -------------------------------------------------

/**
 * ON_OFF_BUTTON :
 *   - un seul gros bouton
 *   - texte "On" / "Off" selon l’état actuel
 */
@Composable
private fun OnOffToggleContent(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (String, String) -> Unit
) {
    val action = actions.firstOrNull() ?: "toggle"

    val isOn =
        actualStatus.equals("active", true) ||
                actualStatus.equals("on", true) ||
                actualStatus.equals("true", true)

    val label = if (isOn) "On ✅" else "Off ❌"

    Button(onClick = { onExecuteAction(commandId, action) }) {
        Text(label)
    }
}

/**
 * CUSTOM_BUTTON avec deux actions différentes :
 *   - on affiche 2 boutons côte à côte
 *   - celui qui correspond à actualStatus apparaît "plein"
 */
@Composable
private fun CustomToggleDoubleButtons(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (String, String) -> Unit
) {
    val left = actions.getOrNull(0)
    val right = actions.getOrNull(1)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        left?.let { label ->
            val selected = actualStatus.equals(label, true)
            if (selected)
                Button(onClick = { onExecuteAction(commandId, label) }) { Text(label) }
            else
                OutlinedButton(onClick = { onExecuteAction(commandId, label) }) { Text(label) }
        }

        right?.let { label ->
            val selected = actualStatus.equals(label, true)
            if (selected)
                Button(onClick = { onExecuteAction(commandId, label) }) { Text(label) }
            else
                OutlinedButton(onClick = { onExecuteAction(commandId, label) }) { Text(label) }
        }
    }
}

/**
 * Fallback générique :
 *   - si toggle binaire (1 action + statut binaire) → Activé / Désactivé
 *   - sinon : un bouton "plein" pour la première action, outline pour les autres
 */
@Composable
private fun GenericToggleContent(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (String, String) -> Unit
) {
    // Cas toggle simple → Activé / Désactivé
    if (actions.size <= 1 && isBinaryStatus(actualStatus)) {
        BinaryToggleButton(
            commandId = commandId,
            actualStatus = actualStatus,
            actions = actions,
            onExecuteAction = onExecuteAction
        )
        return
    }

    // Sinon : liste d’actions
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        actions.forEachIndexed { index, action ->
            if (index == 0) {
                Button(onClick = { onExecuteAction(commandId, action) }) { Text(action) }
            } else {
                OutlinedButton(onClick = { onExecuteAction(commandId, action) }) { Text(action) }
            }
        }
    }
}
