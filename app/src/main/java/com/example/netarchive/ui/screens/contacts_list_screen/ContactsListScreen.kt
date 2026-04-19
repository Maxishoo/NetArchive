package com.example.netarchive.ui.screens.contacts_list_screen

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.R
import com.example.netarchive.data.local.db.entity.ContactWithCategories
import com.example.netarchive.data.mapper.toDomain
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

    var animatedList by remember {
        mutableStateOf<List<ContactWithCategories>>(emptyList())
    }
    var isSwapping by remember { mutableStateOf(false) }


    val listState = rememberLazyListState()
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val showSearchFieldState = remember { mutableStateOf(false) }

    val searchFieldOffset by animateDpAsState(
        targetValue = if (showSearchFieldState.value) 60.dp else 0.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "circleOffset"
    )

    val topBarHeight = 90.dp + searchFieldOffset

    Box(modifier = modifier.fillMaxSize()) {

        when (state) {

            is LoadState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
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

                val contactsWithCategories =
                    (state as LoadState.Success<List<ContactWithCategories>>).data

                LaunchedEffect(contactsWithCategories) {
                    if (!isSwapping) {
                        animatedList = contactsWithCategories.toList()
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(top = topBarHeight),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    itemsIndexed(
                        animatedList,
                        key = { _, item -> item.contact.id }
                    ) { _, contactWithCategories ->
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
                                    contactWithCategories.contact.toDomain()
                                )
                            },

                            onVerticalDragStart = {
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        10,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                )
                            },

                            onVerticalDragEnd = { isDragDown ->

                                if (contactWithCategories.contact.pinnedOrder <= 0) return@ContactCard

                                val list = animatedList.toMutableList()

                                val currentIndex = list.indexOfFirst {
                                    it.contact.id == contactWithCategories.contact.id
                                }

                                if (currentIndex == -1) return@ContactCard

                                isSwapping = true

                                if (isDragDown) {

                                    val nextIndex = currentIndex + 1

                                    if (nextIndex < list.size) {

                                        val downItem = list[nextIndex]

                                        if (downItem.contact.pinnedOrder > 0) {

                                            list[currentIndex] = downItem
                                            list[nextIndex] = contactWithCategories

                                            animatedList = list

                                            viewModel.swapPinnedContact(
                                                contactWithCategories.contact.id,
                                                downItem.contact.id
                                            )
                                        }
                                    }

                                } else {

                                    val prevIndex = currentIndex - 1

                                    if (prevIndex >= 0) {

                                        val upItem = list[prevIndex]

                                        if (upItem.contact.pinnedOrder > 0) {

                                            list[currentIndex] = upItem
                                            list[prevIndex] = contactWithCategories

                                            animatedList = list

                                            viewModel.swapPinnedContact(
                                                contactWithCategories.contact.id,
                                                upItem.contact.id
                                            )
                                        }
                                    }
                                }

                                isSwapping = false
                            },

                            onDragSwipeThreshold = {
                                if (vibrator.hasVibrator()) {
                                    vibrator.vibrate(
                                        VibrationEffect.createOneShot(
                                            10,
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )
                                }
                            },

                            onDragEnd = {
                                if (contactWithCategories.contact.pinnedOrder > 0) {
                                    viewModel.unpinContact(contactWithCategories.contact.id)
                                } else {
                                    viewModel.pinContact(contactWithCategories.contact.id)
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
            onCategoryFilterSelected = {
                selectedCategoryId = it
                viewModel.onCategoryFilterSelected(it)
            },
            showSearchFieldState
        )
    }
}