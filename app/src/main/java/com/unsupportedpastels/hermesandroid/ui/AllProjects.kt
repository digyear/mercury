package com.unsupportedpastels.hermesandroid.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.unsupportedpastels.hermesandroid.app.ProjectId
import com.unsupportedpastels.hermesandroid.app.ProjectSummary

/** Full project list behind Home's "View all", in the same order as the Home preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AllProjectsScreen(
    projects: List<ProjectSummary>,
    workingProjectIds: Set<ProjectId>,
    showBack: Boolean,
    showCreateProjectAction: Boolean,
    canCreateProject: Boolean,
    onBack: () -> Unit,
    onProjectSelected: (ProjectId) -> Unit,
    onCreateProject: () -> Unit,
    showNewTaskAction: Boolean = false,
    onNewTask: () -> Unit = {},
) {
    val sortedProjects = sortedHomeProjects(projects)
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (showNewTaskAction) NewTaskFab(onClick = onNewTask)
        },
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                navigationIcon = {
                    if (showBack) {
                        TextButton(onClick = dropUnlessResumed { onBack() }) {
                            Text("Back")
                        }
                    }
                },
                actions = {
                    if (showCreateProjectAction) {
                        IconButton(
                            enabled = canCreateProject,
                            onClick = dropUnlessResumed { onCreateProject() },
                            modifier = Modifier.semantics {
                                contentDescription = "Create project"
                            },
                        ) {
                            Icon(Icons.Outlined.CreateNewFolder, contentDescription = null)
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("All projects list"),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
            ),
        ) {
            if (sortedProjects.isEmpty()) {
                item(key = "all-projects-empty") {
                    Text(
                        "No projects yet",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            } else {
                items(sortedProjects, key = { "all-project:${it.id.value}" }) { project ->
                    ProjectHomeRow(
                        project = project,
                        working = project.id in workingProjectIds,
                        onClick = dropUnlessResumed { onProjectSelected(project.id) },
                    )
                }
            }
        }
    }
}
