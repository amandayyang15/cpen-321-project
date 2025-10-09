package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.ui.theme.LocalSpacing
import com.cpen321.usermanagement.ui.navigation.NavigationStateManager
import android.util.Log

@Composable
fun HomePage(
    navigationStateManager: NavigationStateManager,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Button(
                onClick = { 
                    Log.d("HomePage", "Clicked Create New Project")
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = spacing.small)
            ) {
                Text("Create New Project")
            }
            
            Button(
                onClick = { 
                    Log.d("HomePage", "Join Existing Project")
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = spacing.small)
            ) {
                Text("Join Existing Project")
            }
        }
        
        // Project list below the buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.large)
        ) {
            Text(
                text = "Projects:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = spacing.small)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { 
                        Log.d("HomePage", "Project 1 clicked")
                        navigationStateManager.navigateToProjectView()
                    }
                    .padding(spacing.medium)
            ) {
                Text(
                    text = "Project 1",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
