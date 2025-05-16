package com.example.littledragons.ui.home.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.littledragons.R
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.model.VerifyEmailState
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.delay

private data class Item(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val userRole: List<UserRole>,
    val destination: AppDestination?
)

private val itemsList = listOf(
    Item(
        label = R.string.profile,
        icon = R.drawable.ic_user,
        userRole = listOf(
            UserRole.Parent,
            UserRole.Teacher
        ),
        destination = AppDestination.Profile,
    ),
    Item(
        label = R.string.calendar,
        icon = R.drawable.ic_calendar,
        userRole = listOf(
            UserRole.Parent,
            UserRole.Teacher
        ),
        destination = AppDestination.Events,
    ),
    Item(
        label = R.string.schedule,
        icon = R.drawable.ic_schedule,
        userRole = listOf(
            UserRole.Parent,
            UserRole.Teacher
        ),
        destination = AppDestination.Schedules,
    ),
    Item(
        label = R.string.child_grades,
        icon = R.drawable.ic_grades,
        userRole = listOf(UserRole.Parent),
        destination = AppDestination.Grades,
    ),
    Item(
        label = R.string.grades,
        icon = R.drawable.ic_grades,
        userRole = listOf(UserRole.Teacher),
        destination = AppDestination.Grades,
    ),
    Item(
        label = R.string.support,
        icon = R.drawable.ic_support,
        userRole = listOf(
            UserRole.Parent,
            UserRole.Teacher
        ),
        destination = AppDestination.Support,
    ),
    Item(
        label = R.string.notifications,
        icon = R.drawable.ic_notificatons,
        userRole = listOf(
            UserRole.Parent,
            UserRole.Teacher
        ),
        destination = AppDestination.Notifications,
    ),
)

@Composable
fun MenuList(
    contentPadding: PaddingValues,
    userRole: UserRole?,
    isEmailVerified: VerifyEmailState,
    onClick: (AppDestination) -> Unit,
    onSendEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsFilteredByRole by remember {
        derivedStateOf {
            itemsList.filter { it.userRole.contains(userRole) }
        }
    }
    var enableSendEmailButton by remember { mutableStateOf(true) }

    LaunchedEffect(enableSendEmailButton) {
        if (!enableSendEmailButton) {
            delay(3000)
            enableSendEmailButton = true
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 20.dp
        ),
        modifier = modifier.fillMaxSize(),
    ) {
        when (isEmailVerified) {
            VerifyEmailState.NotVerified -> item(span = { GridItemSpan(2) }) {
                EmailVerifyMessage(
                    enableSendEmailButton = enableSendEmailButton,
                    onSendEmail = {
                        enableSendEmailButton = false
                        onSendEmail()
                    })
            }

            else -> {}
        }
        items(itemsFilteredByRole.size) { it ->
            var item = itemsFilteredByRole[it]
            MenuItem(
                label = stringResource(item.label),
                icon = item.icon
            ) {
                item.destination?.let { onClick(it) }
            }
        }
    }
}

@Composable
private fun MenuItem(
    modifier: Modifier = Modifier,
    label: String,
    @DrawableRes
    icon: Int,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = modifier
            .height(145.dp)
            .clip(CardDefaults.shape)
            .clickable(onClick = onClick),
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    horizontal = 16.dp,
                    vertical = 24.dp
                )
        ) {
            Icon(
                painterResource(icon),
                tint = Color.Unspecified,
                contentDescription = null
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
            )
        }
    }
}

@Composable
private fun EmailVerifyMessage(
    modifier: Modifier = Modifier,
    enableSendEmailButton: Boolean,
    onSendEmail: () -> Unit,
) {
    OutlinedCard(
        border = BorderStroke(
            color = AppPalette.GreenVariant3,
            width = 1.dp
        ),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text(stringResource(R.string.verify_email_message))

            if (enableSendEmailButton) {
                Button(
                    onClick = onSendEmail,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.send_again))
                }
            } else {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.sent))
                }
            }
        }
    }

}

@Preview
@Composable
private fun MenuListPreview() {
    AppTheme {
        MenuList(
            contentPadding = PaddingValues(0.dp),
            userRole = UserRole.Parent,
            isEmailVerified = VerifyEmailState.Verified,
            onClick = {},
            onSendEmail = {}
        )
    }
}

@Preview
@Composable
private fun MenuListNotVerifiedPreview() {
    AppTheme {
        MenuList(
            contentPadding = PaddingValues(0.dp),
            userRole = UserRole.Parent,
            isEmailVerified = VerifyEmailState.NotVerified,
            onClick = {},
            onSendEmail = {}
        )
    }
}