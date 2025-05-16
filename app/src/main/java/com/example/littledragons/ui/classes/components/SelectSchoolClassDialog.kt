package com.example.littledragons.ui.classes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.littledragons.R
import com.example.littledragons.model.types.SchoolClass
import com.example.littledragons.model.types.SchoolClassId
import com.example.littledragons.ui.classes.model.SchoolClassesState
import com.example.littledragons.ui.components.FetchError
import com.example.littledragons.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSchoolClassDialog(
    onDismissRequest: () -> Unit,
    onSelect: (SchoolClassId) -> Unit,
    state: SchoolClassesState,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
    ) {
        Surface(
            modifier = modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            when (val s = state) {
                is SchoolClassesState.Failed -> Error()
                SchoolClassesState.Initial, SchoolClassesState.Loading -> Loading()
                is SchoolClassesState.Loaded -> Body(
                    classes = s.classes,
                    onSelect = onSelect,
                    onDismissRequest = onDismissRequest,
                )
            }
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        FetchError(
            label = { Text(stringResource(R.string.fetch_school_classes_error)) },
        )
    }
}

@Composable
private fun Body(
    classes: List<SchoolClass>,
    onSelect: (SchoolClassId) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        LazyColumn {
            items(classes.size) {
                ListItem(headlineContent = {
                    Text(
                        classes[it].name ?: stringResource(R.string.no_name)
                    )
                }, modifier = Modifier.clickable {
                    classes[it].name?.let { onSelect(it) }
                })
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Preview
@Composable
private fun SelectSchoolClassDialogPreview() {
    AppTheme {
        SelectSchoolClassDialog(
            state = SchoolClassesState.Loaded(
                classes = listOf(
                    SchoolClass(name = "5A"),
                    SchoolClass(name = "5B"),
                    SchoolClass(name = "6A")
                )
            ),
            onSelect = {},
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun SelectSchoolClassDialogLoadingPreview() {
    AppTheme {
        SelectSchoolClassDialog(
            state = SchoolClassesState.Initial,
            onSelect = {},
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun SelectSchoolClassDialogErrorPreview() {
    AppTheme {
        SelectSchoolClassDialog(
            state = SchoolClassesState.Failed(Exception()),
            onSelect = {},
            onDismissRequest = {}
        )
    }
}