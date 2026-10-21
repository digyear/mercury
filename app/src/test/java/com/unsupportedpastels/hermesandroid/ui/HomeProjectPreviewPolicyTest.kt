package com.unsupportedpastels.hermesandroid.ui

import com.unsupportedpastels.hermesandroid.app.DurableSessionId
import com.unsupportedpastels.hermesandroid.app.NO_PROJECT_BUCKET_ID
import com.unsupportedpastels.hermesandroid.app.ProjectId
import com.unsupportedpastels.hermesandroid.app.ProjectSummary
import com.unsupportedpastels.hermesandroid.app.SessionSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeProjectPreviewPolicyTest {
    private fun project(
        id: String,
        label: String = id,
        lastActive: Double? = null,
        previewLastActive: Double? = null,
    ) = ProjectSummary(
        id = ProjectId(id),
        label = label,
        primaryPath = "/work/$id",
        sessionCount = 1,
        previewSessions = listOfNotNull(
            previewLastActive?.let {
                SessionSummary(
                    id = DurableSessionId("session-$id"),
                    title = "Session $id",
                    lastActiveEpochSeconds = it,
                )
            },
        ),
        lastActiveEpochSeconds = lastActive,
    )

    @Test
    fun homePreviewShowsOnlyTheFourMostRecentlyActiveProjects() {
        val projects = listOf(
            project("a", lastActive = 10.0),
            project("b", lastActive = 60.0),
            project("c", lastActive = 30.0),
            project("d", lastActive = 50.0),
            project("e", lastActive = 40.0),
            project("f", lastActive = 20.0),
        )

        val preview = homeProjectPreview(projects)

        assertEquals(listOf("b", "d", "e", "c"), preview.map { it.id.value })
    }

    @Test
    fun allProjectsListKeepsEveryProjectInTheSameOrder() {
        val projects = (1..7).map { project("p$it", lastActive = it.toDouble()) }

        val sorted = sortedHomeProjects(projects)

        assertEquals((7 downTo 1).map { "p$it" }, sorted.map { it.id.value })
        assertEquals(sorted.take(HOME_PROJECT_PREVIEW_LIMIT), homeProjectPreview(projects))
    }

    @Test
    fun noProjectBucketIsExcludedBecauseItsSessionsLiveInRecentSessions() {
        val projects = listOf(
            project(NO_PROJECT_BUCKET_ID, label = "Home", lastActive = 99.0),
            project("real", lastActive = 1.0),
        )

        assertEquals(listOf("real"), sortedHomeProjects(projects).map { it.id.value })
    }

    @Test
    fun previewSessionActivityIsUsedWhenTheServerOmitsProjectActivity() {
        val projects = listOf(
            project("stale", previewLastActive = 5.0),
            project("fresh", previewLastActive = 50.0),
            project("unknown"),
        )

        assertEquals(
            listOf("fresh", "stale", "unknown"),
            sortedHomeProjects(projects).map { it.id.value },
        )
    }

    @Test
    fun equallyActiveProjectsAreOrderedByLabelIgnoringCase() {
        val projects = listOf(
            project("1", label = "zeta"),
            project("2", label = "Alpha"),
            project("3", label = "beta"),
        )

        assertEquals(
            listOf("Alpha", "beta", "zeta"),
            sortedHomeProjects(projects).map { it.label },
        )
    }
}
