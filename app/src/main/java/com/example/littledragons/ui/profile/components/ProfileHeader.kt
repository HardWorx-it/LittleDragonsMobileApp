package com.example.littledragons.ui.profile.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.littledragons.R
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.components.CustomTextField
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.IAppViewModel
import com.example.littledragons.ui.model.ResendEmailVerifyResult
import com.example.littledragons.ui.model.VerifyEmailState
import com.example.littledragons.ui.profile.model.ChangeProfileState
import com.example.littledragons.ui.profile.model.IProfileViewModel
import com.example.littledragons.ui.theme.AppPalette
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProfileHeader(
    onEditAvatar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IProfileViewModel,
    appViewModel: IAppViewModel,
) {
    val state by appViewModel.state.collectAsStateWithLifecycle()

    OutlinedCard(
        border = BorderStroke(
            color = AppPalette.GreenVariant3,
            width = 1.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Avatar(
                onEditAvatar,
                viewModel,
                modifier
            )
            Column {
                CustomTextField(
                    value = if (LocalInspectionMode.current) "Имя Фамилия" else viewModel.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(stringResource(R.string.first_and_last_name)) },
                    isError = viewModel.nameIsNotValid,
                    supportingText = {
                        if (viewModel.nameIsNotValid) {
                            Text(stringResource(R.string.name_field_error))
                        }
                    }
                )
                when (val s = state) {
                    is AppState.Loaded -> {
                        Text(
                            when (s.user.role) {
                                UserRole.Parent -> stringResource(R.string.parent)
                                UserRole.Teacher -> stringResource(R.string.teacher)
                                null -> ""
                            },
                        )
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun Avatar(
    onEditAvatar: () -> Unit,
    viewModel: IProfileViewModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onEditAvatar)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(viewModel.avatar)
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
            contentScale = ContentScale.Companion.Crop,
            modifier = modifier
                .size(
                    width = 75.dp,
                    height = 75.dp
                )
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(color = AppPalette.Avatar)
        )
        FilledIconButton(
            onClick = onEditAvatar,
            modifier = Modifier.Companion
                .size(28.dp)
                .align(Alignment.Companion.BottomEnd)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit),
                modifier = Modifier.Companion.size(18.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ProfileHeaderPreview() {
    AppTheme {
        ProfileHeader(
            appViewModel = object : IAppViewModel {
                override val state: StateFlow<AppState> = MutableStateFlow(
                    AppState.Loaded(
                        user = UserAccount(
                            uid = "",
                            role = UserRole.Teacher,
                            firstName = "Имя",
                            lastName = "Фамилия",
                            email = "",
                        ), child = null
                    )
                )
                override val resendEmailVerifyResult: StateFlow<ResendEmailVerifyResult> =
                    MutableStateFlow(
                        ResendEmailVerifyResult.Initial
                    )
                override val isEmailVerified: StateFlow<VerifyEmailState> = MutableStateFlow(
                    VerifyEmailState.Verified
                )

                override fun load(force: Boolean) {
                }

                override fun resendEmailVerify() {
                }
            },
            viewModel = @SuppressLint("UnrememberedMutableState")
            object : IProfileViewModel {
                override var name by mutableStateOf("")
                override var email by mutableStateOf("")
                override var avatar by mutableStateOf(null)
                override var childName by mutableStateOf("")
                override var childSchoolClass by mutableStateOf("")
                override var nameIsNotValid by mutableStateOf(false)
                override var emailIsNotValid by mutableStateOf(false)
                override val childNameIsNotValid by mutableStateOf(false)
                override val childSchoolClassIsNotValid by mutableStateOf(false)
                override val editState: StateFlow<ChangeProfileState> =
                    MutableStateFlow(ChangeProfileState.Initial)

                override fun updateEmail(value: String) {
                }

                override fun updateAvatar(value: ByteArray) {
                }

                override fun updateName(value: String) {
                }

                override fun updateChildName(value: String) {
                }

                override fun updateChildSchoolClass(value: String) {
                }

                override fun load(user: UserAccount, child: Student?) {
                }

                override fun submit() {}
                override fun logOut() {}
            },
            onEditAvatar = {}
        )
    }
}