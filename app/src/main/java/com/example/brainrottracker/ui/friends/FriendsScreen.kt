package com.example.brainrottracker.ui.friends

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.data.model.FriendProfile
import com.example.brainrottracker.data.model.FriendRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    var searchCode by remember { mutableStateOf("") }

    // Show snackbar for action messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Friends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Sidebar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = {
                        Text(
                            "My Friends",
                            fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Requests",
                                fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                            )
                            if (state.pendingRequests.isNotEmpty()) {
                                Badge { Text("${state.pendingRequests.size}") }
                            }
                        }
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                    text = {
                        Text(
                            "Add Friend",
                            fontWeight = if (pagerState.currentPage == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> FriendsListTab(
                        friends = state.friends,
                        isLoading = state.isLoading,
                        onRemove = { viewModel.removeFriend(it) },
                        onRefresh = { viewModel.loadFriends() },
                    )
                    1 -> RequestsTab(
                        pendingRequests = state.pendingRequests,
                        sentRequests = state.sentRequests,
                        onAccept = { viewModel.acceptRequest(it) },
                        onDecline = { viewModel.declineRequest(it) },
                    )
                    2 -> AddFriendTab(
                        searchCode = searchCode,
                        onSearchCodeChange = {
                            searchCode = it.uppercase()
                            viewModel.clearSearchResult()
                        },
                        onSearch = { viewModel.searchByCode(searchCode) },
                        isSearching = state.isSearching,
                        searchResult = state.searchResult?.let {
                            Triple(it.userId, it.name, it.friendCode)
                        },
                        searchError = state.searchError,
                        onSendRequest = { userId ->
                            viewModel.sendRequest(userId)
                            searchCode = ""
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendsListTab(
    friends: List<FriendProfile>,
    isLoading: Boolean,
    onRemove: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (friends.isEmpty()) {
        Box(
            Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No friends yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Go to the Add Friend tab and share your code!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "${friends.size} friend${if (friends.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(friends, key = { it.userId }) { friend ->
            FriendCard(friend = friend, onRemove = { onRemove(friend.userId) })
        }
    }
}

@Composable
private fun FriendCard(friend: FriendProfile, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(friend.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        friend.friendCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove friend",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestsTab(
    pendingRequests: List<FriendRequest>,
    sentRequests: List<FriendRequest>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (pendingRequests.isNotEmpty()) {
            item {
                Text(
                    "Incoming Requests",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            items(pendingRequests, key = { "pending-${it.userId}" }) { req ->
                RequestCard(
                    name = req.name,
                    code = req.friendCode,
                    isPending = true,
                    onAccept = { onAccept(req.userId) },
                    onDecline = { onDecline(req.userId) },
                )
            }
        }

        if (sentRequests.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Sent Requests",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            items(sentRequests, key = { "sent-${it.userId}" }) { req ->
                RequestCard(
                    name = req.name,
                    code = req.friendCode,
                    isPending = false,
                )
            }
        }

        if (pendingRequests.isEmpty() && sentRequests.isEmpty()) {
            item {
                Box(
                    Modifier.fillParentMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No pending requests",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    name: String,
    code: String,
    isPending: Boolean,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            if (isPending && onAccept != null && onDecline != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDecline) {
                        Icon(Icons.Default.Close, contentDescription = "Decline", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Text(
                    "Pending",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun AddFriendTab(
    searchCode: String,
    onSearchCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    searchResult: Triple<String, String, String>?,  // userId, name, friendCode
    searchError: String?,
    onSendRequest: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Add by Friend Code",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Enter a friend's code (e.g. BRT-X7K2) to send them a request.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )

        OutlinedTextField(
            value = searchCode,
            onValueChange = onSearchCodeChange,
            label = { Text("Friend Code") },
            placeholder = { Text("BRT-XXXX") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape = RoundedCornerShape(12.dp),
        )

        Button(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth(),
            enabled = searchCode.length >= 4 && !isSearching,
            shape = RoundedCornerShape(12.dp),
        ) {
            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
            }
            Text("Search")
        }

        searchError?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    it,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        searchResult?.let { (userId, name, code) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = { onSendRequest(userId) },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }
        }
    }
}
