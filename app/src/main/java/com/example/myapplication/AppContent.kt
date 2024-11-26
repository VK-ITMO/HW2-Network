package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val catsPhotoController = CatsPhotoController()
private val catsGifController = CatsGifController()


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppContent() {

    val linksSaver = Saver<MutableList<String>, List<String>>(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )

    val statesSaver = Saver<MutableList<Boolean>, List<Boolean>>(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )

    val links = rememberSaveable(saver = linksSaver) {
        mutableStateListOf()
    }

    val imageLoaded = rememberSaveable(saver = statesSaver) {
        mutableStateListOf()
    }

    val imageError = rememberSaveable(saver = statesSaver) {
        mutableStateListOf()
    }

    var fullScreenLoaderIndex by remember { mutableIntStateOf(-1) }

    val scope = rememberCoroutineScope()

    var showErrorMessage by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 12.dp)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.Blue)
                                .padding(10.dp),
                        ) {
                            Text(
                                text = "CATS",
                                fontSize = 32.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                items(items = links, key = null) { imageLink ->
                    val index = links.indexOf(imageLink)
                    if (fullScreenLoaderIndex == -1 || fullScreenLoaderIndex == index) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .padding(8.dp).clickable {
                                    if (imageLoaded[index]) {
                                        fullScreenLoaderIndex = if (fullScreenLoaderIndex == index) -1 else index
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageError.getOrNull(index) == true) {

                                ErrorLoadingPhoto(Modifier.align(Alignment.Center),
                                    retryLoad = {
                                        imageError[index] = false
                                    },
                                    deletePhoto = {
                                        imageError.removeAt(index)
                                        imageLoaded.removeAt(index)
                                        links.removeAt(index)
                                    })

                            } else {
                                if (imageLoaded.getOrNull(index) == false) {
                                    CircularProgressIndicator()
                                }


                                AsyncImage(
                                    model = imageLink,
                                    contentDescription = null,
                                    onSuccess = { imageLoaded[index] = true },
                                    onError = { imageError[index] = true },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }

            Row(
                modifier = Modifier
                    .background(Color.Gray)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val newPhotos = catsPhotoController.getPhoto()
                            if (newPhotos.isSuccess) {
//                                Log.i("LoadPic", "Success from api, 10 picture taken")
                                for (photo in newPhotos.getOrNull()!!) {
                                    links.add(photo.url)
                                    imageLoaded.add(false)
                                    imageError.add(false)

                                }
//                                Log.i("LoadPic", "${links.size} Now in Links")
                            } else {
                                showErrorMessage = true
//                                Log.e("LoadPic", "No Success from api. ${newPhotos.exceptionOrNull().toString()}")

                            }
                        }
                    },
                ) {
                    Text(text = "New Photo", textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.width(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val newGifs = catsGifController.getGifs()
                            if (newGifs.isSuccess) {
//                                Log.i("LoadPic", "Successs from api, 1 gif taken")
                                links.add("https://cataas.com/cat/${newGifs.getOrNull()!!.id}")
                                imageLoaded.add(false)
                                imageError.add(false)
                            //                                Log.i("LoadPic", "${links.size} Now in Links")
                            } else {
                                showErrorMessage = true

//                                Log.e("LoadPic", "No Success from api. ${newGifs.exceptionOrNull().toString()}")
                            }
                        }
                    },
                ) {
                    Text(text = "New GIF", textAlign = TextAlign.Center)
                }
            }
        }
        if (showErrorMessage) {
            ErrorApiRequest()
            LaunchedEffect(Unit) {
                delay(3000)
                showErrorMessage = false
            }
        }
    }
}

@Composable
fun ErrorLoadingPhoto(
    modifier: Modifier = Modifier,
    retryLoad: () -> Unit,
    deletePhoto: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(
            "Connection error.\nTry it later.",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                modifier = Modifier,
                onClick = retryLoad
            ) {
                Text("Retry")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                modifier = Modifier,
                onClick = deletePhoto
            ) {
                Text("Delete")
            }


        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ErrorApiRequest() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Cyan),
            contentAlignment = Alignment.Center,

            ) {
            Text(
                modifier = Modifier.padding(12.dp),
                text = "An error occurred while accessing the network.\nPlease try again later.",
                textAlign = TextAlign.Center,
                fontSize = 30.sp,

            )
        }
    }
}