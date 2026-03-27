package com.malcolm.ecomapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.malcolm.ecomapp.api.*
import dev.jeziellago.compose.markdown.MarkdownText
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Message(val text: String, val isUser: Boolean)

class MainActivity : ComponentActivity() {

    private val api = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:9090/") 
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChatApi::class.java)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ChatScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen() {
        var messages by remember { mutableStateOf(listOf(Message("Hi! I'm your AI assistant. How can I help you?", false))) }
        var inputText by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Ecom AI Mobile", style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            bottomBar = {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask about products...") },
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val userMsg = inputText
                                    inputText = ""
                                    messages = messages + Message(userMsg, true)
                                    scope.launch {
                                        try {
                                            val res = api.chat(ChatRequest(userMsg))
                                            messages = messages + Message(res.response, false)
                                        } catch (e: Exception) {
                                            messages = messages + Message("Network Error: Check if backend is running on port 9090", false)
                                        }
                                    }
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
            }
        }
    }

    @Composable
    fun ChatBubble(msg: Message) {
        val alignment = if (msg.isUser) Alignment.End else Alignment.Start
        val color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
        val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
            Surface(
                color = color,
                shape = RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp, 
                    bottomStart = if (msg.isUser) 16.dp else 4.dp, 
                    bottomEnd = if (msg.isUser) 4.dp else 16.dp
                ),
                tonalElevation = 2.dp
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    MarkdownText(
                        markdown = msg.text,
                        color = textColor
                    )
                }
            }
            Text(
                text = if (msg.isUser) "You" else "Assistant",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
