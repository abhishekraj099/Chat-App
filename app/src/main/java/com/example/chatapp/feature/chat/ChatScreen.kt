package com.example.chatapp.feature.chat

import android.Manifest
import android.net.Uri
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatapp.R
import com.example.chatapp.model.Message
import com.example.chatapp.ui.theme.DarkGrey
import com.example.chatapp.ui.theme.purple
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult as rememberLauncherForActivityResult1

@Composable
fun ChatScreen(navController: NavController, channelId: String,channelName: String) {
    Scaffold(
        containerColor = Color.Black
    ) {
        val viewModel: ChatViewModel = hiltViewModel()

        val chooseDialog = remember {
            mutableStateOf(false)
        }

        val cameraImageUri = remember {
            mutableStateOf<Uri?>(null)
        }

        val cameraImageLauncher = rememberLauncherForActivityResult1(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success) {
                cameraImageUri.value?.let {
                    // send image to server
                    viewModel.sendImageMessage(it, channelId)
                }
            }
        }
// implementaion of pick image from gallery
        val imageLauncher = rememberLauncherForActivityResult1(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                viewModel.sendImageMessage(it, channelId)

            }


        }

        fun createImageUri(): Uri {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = ContextCompat.getExternalFilesDirs(
                navController.context, Environment.DIRECTORY_PICTURES
            ).first()
            return FileProvider.getUriForFile(navController.context,
                "${navController.context.packageName}.provider",
                File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                    cameraImageUri.value = Uri.fromFile(this)
                })
        }

        val permissionLauncher = rememberLauncherForActivityResult1(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                cameraImageLauncher.launch(createImageUri())

            }

        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
//            val viewModel: ChatViewModel = hiltViewModel()
            LaunchedEffect(key1 = true) {
                viewModel.listenForMessages(channelId)
            }
            val messages = viewModel.messages.collectAsState()
            ChatMessages(messages = messages.value, onSendMessage = { messages ->
                viewModel.sendMessage(channelId, messages)
            }, onImageClicked = {
                chooseDialog.value = true
            }

            )
        }



        if (chooseDialog.value) {
            ContentSelectionDialog(
                onCameraSelected = {
                    chooseDialog.value = false
                    if (navController.context.checkSelfPermission(Manifest.permission.CAMERA)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraImageLauncher.launch(createImageUri())
                    } else {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.CAMERA)


                    }
                },
                onGallerySelected = {
                    chooseDialog.value = false
                    imageLauncher.launch("image/*")
                })
        }
    }


}

@Composable
fun ContentSelectionDialog(onCameraSelected: () -> Unit, onGallerySelected: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onCameraSelected) {
                Text(text = "Camera", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onGallerySelected) {
                Text(text = "Gallery")
            }
        },
        title = { Text(text = "Select Image?") },
        text = { Text(text = "Select an image from camera or gallery") })
}


@Composable
fun ChatMessages(
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onImageClicked: () -> Unit
) {

    val hideKeyboardController = LocalSoftwareKeyboardController.current

    val meg = remember {
        mutableStateOf("")
    }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGrey)
                //horizontalArrangement = Arrangement.SpaceBetween,
                .padding(8.dp), verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = {

                meg.value = ""
                onImageClicked()
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
                    painter = painterResource(id = R.drawable.img_6),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .background(
                        color = bubbleColor, shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                if (message.imageUrl != null) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp),
                        contentScale = ContentScale.Crop // fit
                    )
                } else {
                    Text(text = message.message?.trim() ?: "", color = Color.White)
                }
            }

        }

    }
}


