package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommandCard(
    type: String,
    styleType: String,
    id: String,
    name: String,
    actualStatus: String,
    actions: List<String>,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            when (type) {
                "slider" -> {
                    SliderCommandView(
                        commandId = id,
                        name = name,
                        actualStatus = actualStatus,
                        actions = actions,
                        isHighlighted = isHighlighted,
                        onExecuteAction = onExecuteAction
                    )
                }

                "toggle" -> {
                    ToggleCommandView(
                        commandId = id,
                        name = name,
                        styleType = styleType,
                        actualStatus = actualStatus,
                        actions = actions,
                        isHighlighted = isHighlighted,
                        onExecuteAction = onExecuteAction
                    )
                }

                else -> {
                    ToggleCommandView(
                        commandId = id,
                        name = "$name (type: $type)",
                        styleType = styleType,
                        actualStatus = actualStatus,
                        actions = actions,
                        isHighlighted = isHighlighted,
                        onExecuteAction = onExecuteAction
                    )
                }
            }
        }
    }
}