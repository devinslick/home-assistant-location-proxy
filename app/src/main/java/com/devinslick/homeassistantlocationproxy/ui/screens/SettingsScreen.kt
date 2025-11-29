package com.devinslick.homeassistantlocationproxy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devinslick.homeassistantlocationproxy.ui.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel, onClose: () -> Unit) {
    val baseUrl by viewModel.haBaseUrl.collectAsState()
    val token by viewModel.haToken.collectAsState()
    val entityId by viewModel.entityId.collectAsState()
    val pollingInterval by viewModel.pollingInterval.collectAsState()

    val baseUrlState = remember { mutableStateOf(baseUrl ?: "") }
    val tokenState = remember { mutableStateOf(token ?: "") }
    val entityState = remember { mutableStateOf(entityId ?: "") }

    LaunchedEffect(baseUrl) { baseUrlState.value = baseUrl ?: "" }
    LaunchedEffect(token) { tokenState.value = token ?: "" }
    LaunchedEffect(entityId) { entityState.value = entityId ?: "" }

    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top, modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(value = baseUrlState.value, onValueChange = { baseUrlState.value = it }, label = { Text("HA Base URL") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tokenState.value, onValueChange = { tokenState.value = it }, label = { Text("HA Token") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(value = entityState.value, onValueChange = { entityState.value = it }, label = { Text("Entity ID") }, modifier = Modifier.fillMaxWidth())

            Text(text = "Polling interval: ${pollingInterval}s")
            Slider(value = pollingInterval.toFloat(), onValueChange = { viewModel.updatePollingInterval(it.toLong()) }, valueRange = 5f..300f, modifier = Modifier.fillMaxWidth())

            Button(onClick = {
                viewModel.updateBaseUrl(baseUrlState.value.ifBlank { null })
                viewModel.updateToken(tokenState.value.ifBlank { null })
                viewModel.updateEntityId(entityState.value.ifBlank { null })
                onClose()
            }, modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Save")
            }
            Button(onClick = { viewModel.clearToken() }, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = "Clear Token")
            }
        }
    }
}
