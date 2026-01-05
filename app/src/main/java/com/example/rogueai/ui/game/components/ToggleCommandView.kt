package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

// --- piblic API ------------------------------------------------------------

/**
 * View for a TOGGLE command
 * @param commandId ID of the command
 * @param name Name of the command
 * @param styleType Style of the toggle (e.g., "onoff_button", "custom_button")
 * @param actualStatus Current status of the command
 * @param actions List of possible actions for the command
 * @param isHighlighted Whether the command is highlighted
 * @param onExecuteAction Callback when an action is executed
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
        // Title
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
            // 1) ON/OFF → "On"/"Off" button
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
                    // Standard binary toggle
                    BinaryToggleButton(
                        commandId = commandId,
                        actualStatus = actualStatus,
                        actions = actions,
                        onExecuteAction = onExecuteAction
                    )
                } else {
                    // Two different buttons
                    CustomToggleDoubleButtons(
                        commandId = commandId,
                        actualStatus = actualStatus,
                        actions = actions,
                        onExecuteAction = onExecuteAction
                    )
                }
            }

            // 3) Simple toggle
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

// --- Private implementations -------------------------------------------------

/**
 * ON_OFF_BUTTON :
 * - single button showing "On" or "Off" based on actualStatus
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
 * CUSTOM_BUTTON with two actions → two buttons side by side
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
 * Fallback generic toggle content
 */
@Composable
private fun GenericToggleContent(
    commandId: String,
    actualStatus: String,
    actions: List<String>,
    onExecuteAction: (String, String) -> Unit
) {
    // Simple binary toggle
    if (actions.size <= 1 && isBinaryStatus(actualStatus)) {
        BinaryToggleButton(
            commandId = commandId,
            actualStatus = actualStatus,
            actions = actions,
            onExecuteAction = onExecuteAction
        )
        return
    }

    // Else: list all actions as buttons
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
