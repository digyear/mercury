package com.unsupportedpastels.hermesandroid.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unsupportedpastels.hermesandroid.app.ProjectId
import com.unsupportedpastels.hermesandroid.app.ProjectSummary
import com.unsupportedpastels.hermesandroid.theme.HermesAndroidTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "en-rUS-w400dp-h900dp")
class AllProjectsScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private val project = ProjectSummary(ProjectId("mercury"), "Mercury", "/work/mercury", 2, emptyList())

    private fun render(showNewTaskAction: Boolean, onNewTask: () -> Unit = {}) {
        composeRule.setContent {
            HermesAndroidTheme {
                AllProjectsScreen(
                    projects = listOf(project),
                    workingProjectIds = emptySet(),
                    showBack = true,
                    showCreateProjectAction = true,
                    canCreateProject = true,
                    onBack = {},
                    onProjectSelected = {},
                    onCreateProject = {},
                    showNewTaskAction = showNewTaskAction,
                    onNewTask = onNewTask,
                )
            }
        }
    }

    @Test fun fullListKeepsTheNewTaskButtonFromHome() {
        var newTasks = 0
        render(showNewTaskAction = true, onNewTask = { newTasks++ })
        composeRule.onNodeWithContentDescription("New task").assertIsDisplayed().performClick()
        assertEquals(1, newTasks)
    }

    @Test fun fullListHidesTheNewTaskButtonWhenTheDockOwnsIt() {
        render(showNewTaskAction = false)
        composeRule.onNodeWithContentDescription("New task").assertDoesNotExist()
    }
}
