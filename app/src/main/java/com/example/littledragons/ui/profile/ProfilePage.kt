package com.example.littledragons.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.littledragons.R
import com.example.littledragons.model.getPhotoThumbnail
import com.example.littledragons.model.readBitmapToBytes
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.model.types.Student
import com.example.littledragons.model.types.UserAccount
import com.example.littledragons.model.types.UserRole
import com.example.littledragons.ui.activityViewModel
import com.example.littledragons.ui.components.BackButton
import com.example.littledragons.ui.components.CustomScaffold
import com.example.littledragons.ui.components.CustomTopAppBar
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.model.AppState
import com.example.littledragons.ui.model.AppViewModel
import com.example.littledragons.ui.model.IAppViewModel
import com.example.littledragons.ui.model.ResendEmailVerifyResult
import com.example.littledragons.ui.model.VerifyEmailState
import com.example.littledragons.ui.navigateToRedirectAuth
import com.example.littledragons.ui.profile.components.AddSchoolDialog
import com.example.littledragons.ui.profile.components.AddStudentDialog
import com.example.littledragons.ui.profile.components.ProfileForm
import com.example.littledragons.ui.profile.components.ProfileHeader
import com.example.littledragons.ui.profile.model.AddSchoolClassState
import com.example.littledragons.ui.profile.model.AddSchoolClassViewModel
import com.example.littledragons.ui.profile.model.AddStudentState
import com.example.littledragons.ui.profile.model.AddStudentViewModel
import com.example.littledragons.ui.profile.model.ChangeProfileState
import com.example.littledragons.ui.profile.model.IAddSchoolClassViewModel
import com.example.littledragons.ui.profile.model.IAddStudentViewModel
import com.example.littledragons.ui.profile.model.IProfileViewModel
import com.example.littledragons.ui.profile.model.ProfileViewModel
import com.example.littledragons.ui.routes.AppDestination
import com.example.littledragons.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    appViewModel: AppViewModel = activityViewModel(),
    addSchoolClassViewModel: AddSchoolClassViewModel = hiltViewModel(),
    addStudentViewModel: AddStudentViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope();
    val snackbarHostState = remember { SnackbarHostState() }
    val appState by appViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(appState) {
        when (val s = appState) {
            is AppState.Loaded -> viewModel.load(user = s.user, child = s.child)
            else -> {}
        }
    }

    val context = LocalContext.current
    // Диалог выбора фото профила
    val pickPhoto = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            // Конвертирование фото в превью меньшего размера
            getPhotoThumbnail(
                context,
                uri
            ).let {
                // Сохранение в base64 для Firestore
                val bytes = it?.readBitmapToBytes()
                if (bytes == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.save_profile_avatar_failed)
                        )
                    }
                } else {
                    viewModel.updateAvatar(bytes)
                }
            }
        }
    }

    Page(
        onBack = {
            when (viewModel.editState.value) {
                ChangeProfileState.InProgress, ChangeProfileState.Success ->
                    appViewModel.load(force = true)

                else -> {}
            }
            navController.navigateUp()
        },
        viewModel = viewModel,
        appViewModel = appViewModel,
        addSchoolClassViewModel = addSchoolClassViewModel,
        addStudentViewModel = addStudentViewModel,
        onChooseAvatarDialog = {
            pickPhoto.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        },
        onNavigate = {
            when (it) {
                AppDestination.Initial -> navController.navigateToRedirectAuth()
                else -> navController.navigate(it)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: IProfileViewModel,
    appViewModel: IAppViewModel,
    addSchoolClassViewModel: IAddSchoolClassViewModel,
    addStudentViewModel: IAddStudentViewModel,
    onChooseAvatarDialog: () -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val state by appViewModel.state.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val addSchoolClassState by addSchoolClassViewModel.state.collectAsStateWithLifecycle()
    val addStudentState by addStudentViewModel.state.collectAsStateWithLifecycle()

    var showAddSchoolDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(
        state,
        onNavigate
    ) {
        when (state) {
            AppState.Failed.NotAuthorized, AppState.Failed.NotRegistered ->
                onNavigate(AppDestination.Initial)

            else -> {}
        }
    }

    LaunchedEffect(editState) {
        when (editState) {
            ChangeProfileState.Failed.ReauthenticateRequired -> {
                val result = snackbarHostState.showSnackbar(
                    context.getString(R.string.reauthenticate_required),
                    actionLabel = context.getString(R.string.logout)
                )
                when (result) {
                    SnackbarResult.Dismissed -> {}
                    SnackbarResult.ActionPerformed -> viewModel.logOut()
                }
            }

            is ChangeProfileState.Failed.Error -> snackbarHostState.showSnackbar(context.getString(R.string.update_profile_failed))

            ChangeProfileState.Success -> {
                snackbarHostState.showSnackbar(context.getString(R.string.profile_update_success))
                appViewModel.load(force = true)
            }

            ChangeProfileState.Failed.ChildNotFound -> snackbarHostState.showSnackbar(
                context.getString(
                    R.string.update_profile_child_not_found
                )
            )

            else -> {}
        }
    }

    LaunchedEffect(addSchoolClassState) {
        when (addSchoolClassState) {
            is AddSchoolClassState.Failed -> snackbarHostState.showSnackbar(context.getString(R.string.add_school_class_failed))
            AddSchoolClassState.InProgress -> showAddSchoolDialog = false
            AddSchoolClassState.Success -> snackbarHostState.showSnackbar(context.getString(R.string.add_school_class_success))
            else -> {}
        }
    }

    LaunchedEffect(addStudentState) {
        when (addStudentState) {
            is AddStudentState.Failed.Error -> snackbarHostState.showSnackbar("Не удалось добавить ученика")
            AddStudentState.Failed.SchoolClassNotFound -> snackbarHostState.showSnackbar("Не удалось добавить ученика: класс не существует")
            AddStudentState.InProgress -> showAddStudentDialog = false
            AddStudentState.Success -> snackbarHostState.showSnackbar("Ученик успешно добавлен")
            else -> {}
        }
    }

    CustomScaffold(
        topBar = {
            CustomTopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(onClick = {
                        viewModel.logOut()
                        onNavigate(AppDestination.Initial)
                    }) {
                        Icon(
                            painterResource(R.drawable.ic_logout_24px),
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        when (val s = state) {
            is AppState.Failed.Error -> Error(onRefresh = appViewModel::load)
            is AppState.Loaded -> ProfileForm(
                viewModel = viewModel,
                appViewModel = appViewModel,
                header = {
                    ProfileHeader(
                        onEditAvatar = onChooseAvatarDialog,
                        viewModel = viewModel,
                        appViewModel = appViewModel,
                    )
                },
                buttons = {
                    val buttonColors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    val modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()

                    Button(
                        colors = buttonColors,
                        onClick = viewModel::submit,
                        enabled = when (editState) {
                            ChangeProfileState.InProgress -> false
                            else -> true
                        },
                        modifier = modifier,
                    ) {
                        when (editState) {
                            ChangeProfileState.InProgress ->
                                CircularProgressIndicator(modifier = Modifier.padding(8.dp))

                            else -> Text(stringResource(R.string.save))
                        }
                    }

                    if (s.user.role == UserRole.Teacher) {
                        Button(
                            colors = buttonColors,
                            onClick = { showAddStudentDialog = true },
                            modifier = modifier,
                        ) {
                            when (addStudentState) {
                                AddStudentState.InProgress ->
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))

                                else -> Text(stringResource(R.string.add_student))
                            }
                        }

                        Button(
                            colors = buttonColors,
                            onClick = { showAddSchoolDialog = true },
                            modifier = modifier,
                        ) {
                            when (addSchoolClassState) {
                                AddSchoolClassState.InProgress ->
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))

                                else -> Text(stringResource(R.string.add_school_class))
                            }
                        }
                    }
                },
                modifier = Modifier.padding(contentPadding)
            )

            else -> Loading()
        }
    }

    if (showAddSchoolDialog) {
        AddSchoolDialog(
            onDismissRequest = {
                showAddSchoolDialog = false
            },
            viewModel = addSchoolClassViewModel
        )
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismissRequest = {
                showAddStudentDialog = false
            },
            viewModel = addStudentViewModel
        )
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        FetchError(
            label = { Text(stringResource(R.string.failed_to_fetch_profile)) },
            onRefresh = onRefresh
        )
    }
}


@Preview
@Composable
private fun ProfilePagePreview() {
    AppTheme {
        Page(
            appViewModel = object : IAppViewModel {
                override val state: StateFlow<AppState> = MutableStateFlow(
                    AppState.Loaded(
                        user = UserAccount(role = UserRole.Parent),
                        child = Student(firstName = "Ученик", lastName = "Ученик", classId = "5А")
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
            viewModel = object : IProfileViewModel {
                override val name: String = ""
                override val email: String = ""
                override val avatar: ByteArray? = byteArrayOf()
                override val childName: String = ""
                override val childSchoolClass: String = ""
                override val nameIsNotValid: Boolean = false
                override val emailIsNotValid: Boolean = false
                override val childNameIsNotValid: Boolean = false
                override val childSchoolClassIsNotValid: Boolean = false
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
            addSchoolClassViewModel = object : IAddSchoolClassViewModel {
                override val name: String = ""
                override val nameIsNotValid: Boolean = false
                override val state: StateFlow<AddSchoolClassState> =
                    MutableStateFlow(AddSchoolClassState.Initial)

                override fun updateName(value: String) {
                }

                override fun submit() {
                }
            },
            addStudentViewModel = object : IAddStudentViewModel {
                override val name: String = ""
                override val schoolClassId: SchoolClassId = ""
                override val nameIsNotValid: Boolean = false
                override val schoolClassIdIsNotValid: Boolean = false
                override val state: StateFlow<AddStudentState> =
                    MutableStateFlow(AddStudentState.Initial)

                override fun updateName(value: String) {
                }

                override fun updateSchoolClassId(value: String) {
                }

                override fun submit() {
                }
            },
            onChooseAvatarDialog = {},
            onNavigate = {},
            onBack = {}
        )
    }
}