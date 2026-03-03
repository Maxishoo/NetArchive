package com.example.netarchive.ui.screens.contacts_list_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.netarchive.R
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.ui.components.cards.ContactCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactListViewModel = hiltViewModel(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isSelectionMode: Boolean = false,
    onContactClick: (Contact) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("Выберите контакт") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
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
                    val contacts = (state as LoadState.Success<List<Contact>>).data

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                            .padding(innerPadding), // Добавляем padding от Scaffold
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(contacts) { contact ->
                            ContactCard(
                                contact = contact,
                                onClick = { onContactClick(contact) }
                            )
                        }
                    }
                }
            }
        }
    }
}