package com.example.littledragons.ui.home.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.littledragons.R
import com.example.littledragons.model.decodeBase64String
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme

@Composable
fun ProfileHeader(
    firstName: String,
    lastName: String,
    avatar: String?
) {
    ProvideTextStyle(
        MaterialTheme.typography.bodyMedium.copy(fontSize = 32.sp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(end = 60.dp)
        ) {
            Column {
                Text(firstName)
                Text(lastName)
            }
            AvatarImage(avatar = avatar)
        }
    }
}

@Composable
private fun AvatarImage(avatar: String?, modifier: Modifier = Modifier) {
    val imageByteArray by remember {
        derivedStateOf {
            decodeBase64String(avatar)
        }
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageByteArray)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.avatar_placeholder),
        fallback = painterResource(R.drawable.avatar_placeholder),
        error = painterResource(R.drawable.avatar_placeholder),
        onError = {
            Log.d(
                "AvatarImage",
                "Unable to load profile avatar image",
                it.result.throwable
            )
        },
        contentDescription = stringResource(R.string.avatar),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(width = 62.dp, height = 62.dp)
            .clip(CircleShape)
            .background(color = AppPalette.Avatar)
            .border(2.dp, color = AppPalette.White, shape = CircleShape),
    )
}

@Preview
@Composable
private fun ProfileHeaderPreview() {
    AppTheme {
        ProfileHeader(
            firstName = "Имя",
            lastName = "Фамилия",
            avatar = ""
        )
    }
}