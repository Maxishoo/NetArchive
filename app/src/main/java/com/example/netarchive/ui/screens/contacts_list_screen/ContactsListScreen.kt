package com.example.netarchive.ui.screens.contacts_list_screen

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.R
import com.example.netarchive.data.local.db.entity.ContactWithCategories
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.ui.components.cards.ContactCard

@Composable
fun ContactListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactListViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onContactClick: (Contact) -> Unit = {},
    isSelectionMode: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val showSearchFieldState = remember { mutableStateOf(false) }
    val searchFieldOffset by animateDpAsState(
        targetValue = if (showSearchFieldState.value) 60.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "circleOffset"
    )

    val topBarHeight = 90.dp + searchFieldOffset

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (state) {
            is LoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is LoadState.Error -> {
                Text(
                    text = stringResource(R.string.error_contacts_load),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is LoadState.Empty -> {
                Text(
                    text = stringResource(R.string.contacts_empty),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is LoadState.Success -> {
                val contactsWithCategories = (state as LoadState.Success<List<ContactWithCategories>>).data

                LaunchedEffect(contactsWithCategories.size) {
                    listState.scrollToItem(0)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(top = topBarHeight),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(contactsWithCategories, key = { it.contact.id }) { contactWithCategories ->
                        Log.e("Cont12", "all!  ${contactWithCategories.contact.pinnedOrder}")
                        ContactCard(
                            contact = Contact(
                                id = contactWithCategories.contact.id,
                                username = contactWithCategories.contact.username,
                                phone = contactWithCategories.contact.phone,
                                telegram = contactWithCategories.contact.telegram,
                                max = contactWithCategories.contact.max,
                                email = contactWithCategories.contact.email,
                                job = contactWithCategories.contact.job,
                                avatar = contactWithCategories.contact.avatar,
                                pinnedOrder = contactWithCategories.contact.pinnedOrder
                            ),
                            categories = contactWithCategories.categories,
                            onClick = {
                                onContactClick(
                                    Contact(
                                        id = contactWithCategories.contact.id,
                                        username = contactWithCategories.contact.username,
                                        phone = contactWithCategories.contact.phone,
                                        telegram = contactWithCategories.contact.telegram,
                                        max = contactWithCategories.contact.max,
                                        email = contactWithCategories.contact.email,
                                        job = contactWithCategories.contact.job,
                                        avatar = contactWithCategories.contact.avatar,
                                        pinnedOrder = contactWithCategories.contact.pinnedOrder
                                    )
                                )
                            },
                            onDragEnd = {
                                if (contactWithCategories.contact.pinnedOrder > 0) {
                                    viewModel.unpinContact(contactWithCategories.contact.id)
                                } else {
                                    viewModel.pinContact(contactWithCategories.contact.id)
                                }
                            },
                            onDragSwipeThreshold = {
                            if (vibrator.hasVibrator()) {
                                vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                        }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        ContactsTopBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                viewModel.onSearchQueryChange(it)
            },
            isSelectionMode = isSelectionMode,
            allCategories = allCategories,
            selectedCategoryId = selectedCategoryId,
            onCategoryFilterSelected = { categoryId ->
                selectedCategoryId = categoryId
                viewModel.onCategoryFilterSelected(categoryId)
            },
            showSearchFieldState
        )
    }
}