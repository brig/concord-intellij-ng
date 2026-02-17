/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 Concord Plugin Authors
 */

package brig.concord.perf

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PerfTestProjectActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (System.getProperty("concord.perf.test") != "true") return

        println("PERF-TEST: PerfTestProjectActivity started (Kotlin)")

        // Wait for smart mode
        // Note: ProjectActivity runs on a background coroutine (DefaultDispatcher)
        DumbService.getInstance(project).waitForSmartMode()
        println("PERF-TEST: Smart mode reached.")

        delay(5.seconds)

        println("PERF-TEST: Scanning for Concord files...")
        val concordFiles = mutableListOf<VirtualFile>()
        val basePath = project.basePath
        if (basePath != null) {
            val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath)
            if (baseDir != null) {
                VfsUtilCore.visitChildrenRecursively(baseDir, object : VirtualFileVisitor<Any>() {
                    override fun visitFile(file: VirtualFile): Boolean {
                        if (!file.isDirectory && (file.name.endsWith("concord.yaml") || file.name.endsWith("concord.yml"))) {
                            concordFiles.add(file)
                        }
                        return true
                    }
                })
            }
        }
        println("PERF-TEST: Found ${concordFiles.size} Concord files.")

        println("PERF-TEST: Running scenario sequence...")

        for (file in concordFiles) {
            withContext(Dispatchers.EDT) {
                runScenario(project, file)
            }
            // Simulate user reading/interaction delay between files
            delay(2.seconds)
        }

        // Editing scenario: open root concord.yaml and type in resources/dependencies sections
        val rootConcordYaml = concordFiles.find { it.name == "concord.yaml" && it.parent?.name == basePath?.substringAfterLast('/') }
            ?: concordFiles.find { it.name == "concord.yaml" }
        if (rootConcordYaml != null) {
            println("PERF-TEST: Starting editing scenario on ${rootConcordYaml.path}")
            runEditingScenario(project, rootConcordYaml)
        } else {
            println("PERF-TEST: Root concord.yaml not found, skipping editing scenario")
        }

        println("PERF-TEST: Scenario finished. Waiting for JFR capture...")
        delay(10.seconds)

        println("PERF-TEST: Closing editors...")
        withContext(Dispatchers.EDT) {
            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFiles.forEach { file ->
                fileEditorManager.closeFile(file)
            }
            println("PERF-TEST: All editors closed")
        }

        println("PERF-TEST: Exiting...")
        withContext(Dispatchers.EDT) {
            ApplicationManager.getApplication().exit(true, true, true)
        }
    }

    private fun runScenario(project: Project, file: VirtualFile) {
        // Open file
        val fileEditorManager = project.service<FileEditorManager>()
        fileEditorManager.openFile(file, true)
        val editor = fileEditorManager.selectedTextEditor

        if (editor != null) {
            println("PERF-TEST: Opened: ${file.name}")

            // Move caret to marker
            val text = editor.document.text
            val offset = text.indexOf("# move here")

            if (offset > -1) {
                editor.caretModel.moveToOffset(offset)
                editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
                // println("PERF-TEST: Caret moved to marker in ${file.name}")
            } else {
                println("PERF-TEST: Marker '# move here' not found in ${file.name}")
            }

            // Trigger highlighting
            DaemonCodeAnalyzer.getInstance(project).restart()
            // println("PERF-TEST: Highlighting restarted")
        }
    }

    private suspend fun runEditingScenario(project: Project, file: VirtualFile) {
        withContext(Dispatchers.EDT) {
            val fileEditorManager = project.service<FileEditorManager>()
            fileEditorManager.openFile(file, true)
        }

        val markers = listOf("# edit:resources", "# edit:dependencies")
        for (marker in markers) {
            println("PERF-TEST: Editing near '$marker'")
            for (i in 1..5) {
                withContext(Dispatchers.EDT) {
                    typeAtMarker(project, marker, " x")
                }
                delay(500.milliseconds)
                withContext(Dispatchers.EDT) {
                    deleteAtMarker(project, marker, " x".length)
                }
                delay(500.milliseconds)
            }
        }
        println("PERF-TEST: Editing scenario complete")
    }

    private fun typeAtMarker(project: Project, marker: String, textToInsert: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val offset = editor.document.text.indexOf(marker)
        if (offset < 0) {
            println("PERF-TEST: Marker '$marker' not found")
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(offset, textToInsert)
        }
        editor.caretModel.moveToOffset(offset + textToInsert.length)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    private fun deleteAtMarker(project: Project, marker: String, length: Int) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val markerOffset = editor.document.text.indexOf(marker)
        if (markerOffset < 0) {
            return
        }

        val deleteFrom = markerOffset - length
        if (deleteFrom < 0) {
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.deleteString(deleteFrom, markerOffset)
        }
        editor.caretModel.moveToOffset(deleteFrom)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        DaemonCodeAnalyzer.getInstance(project).restart()
    }
}
