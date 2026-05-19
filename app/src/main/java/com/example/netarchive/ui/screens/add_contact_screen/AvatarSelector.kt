package com.example.netarchive.ui.screens.add_contact_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.netarchive.R

@Composable
fun AvatarSelector(
    avatarUri: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(dimensionResource(id = R.dimen.avatar_size))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(colorResource(id = R.color.avatar_placeholder_background)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri.isNotBlank()) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = stringResource(id = R.string.avatar_content_description),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.avatar_select_content_description),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.avatar_icon_size)),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = stringResource(id = R.string.avatar_change_content_description),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(dimensionResource(id = R.dimen.avatar_edit_icon_size))
                .offset(
                    x = dimensionResource(id = R.dimen.avatar_edit_icon_offset),
                    y = dimensionResource(id = R.dimen.avatar_edit_icon_offset)
                )
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(dimensionResource(id = R.dimen.avatar_edit_icon_padding)),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}