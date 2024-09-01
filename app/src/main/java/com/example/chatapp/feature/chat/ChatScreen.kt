package com.example.chatapp.feature.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chatapp.R
import com.example.chatapp.model.Message
import com.example.chatapp.ui.theme.DarkGrey
import com.example.chatapp.ui.theme.purple
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ChatScreen(navController: NavController, channelId: String) {
    Scaffold(containerColor = Color.Black) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val viewModel: ChatViewModel = hiltViewModel()
            LaunchedEffect(key1 = true) {
                viewModel.listenForMessages(channelId)
            }
            val messages = viewModel.messages.collectAsState()
            ChatMessages(messages = messages.value, onSendMessage = { messages ->
                viewModel.sendMessage(channelId, messages)
            })
        }
    }

}


@Composable
fun ChatMessages(
    messages: List<Message>, onSendMessage: (String) -> Unit
) {

    val hideKeyboardController = LocalSoftwareKeyboardController.current

    val meg = remember {
        mutableStateOf("")
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(DarkGrey)
                //horizontalArrangement = Arrangement.SpaceBetween,
                .padding(8.dp), verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = {

                meg.value = ""
            }) {
                Image(
                    painter = painterResource(id = R.drawable.img_2),
                    contentDescription = "attachment"
                )

            }

            TextField(
                value = meg.value,
                onValueChange = { meg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hideKeyboardController?.hide()
                }),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = DarkGrey,
                    unfocusedContainerColor = DarkGrey,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White,
                    unfocusedLabelColor = Color.White
                )

            )
            IconButton(onClick = {
                onSendMessage(meg.value)
                meg.value = ""
            }) {
                Image(painter = painterResource(id = R.drawable.img_1), contentDescription = "Send")

            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val bubbleColor = if (isCurrentUser) {
        purple
    } else {
        DarkGrey
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)


    ) {
        val alignment = if (isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd

        Row(

            modifier = Modifier
                .padding(8.dp)
                .align(alignment),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCurrentUser) {
                Image(
                    painter = painterResource(id = R.drawable.img_4),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = message.message.trim(),
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = bubbleColor, shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)

            )
        }


    }


}


