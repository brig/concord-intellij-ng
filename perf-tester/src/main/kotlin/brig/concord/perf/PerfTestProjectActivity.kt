// SPDX-License-Identifier: Apache-2.0
package brig.concord.perf

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.lookup.LookupManager
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
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.searches.ReferencesSearch
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

        // EL expression scenario: completion and editing inside EL expressions
        val elFile = concordFiles.find { it.name == "el-expressions.concord.yaml" }
        if (elFile != null) {
            println("PERF-TEST: Starting EL expression scenario on ${elFile.path}")
            runElScenario(project, elFile)
            println("PERF-TEST: Starting navigation scenario on ${elFile.path}")
            runNavigationScenario(project, elFile)
        } else {
            println("PERF-TEST: el-expressions.concord.yaml not found, skipping EL scenario")
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

    /**
     * EL expression scenario: exercises EL parsing, variable resolution,
     * nested property chain resolution, and completion inside EL expressions.
     */
    private suspend fun runElScenario(project: Project, file: VirtualFile) {
        withContext(Dispatchers.EDT) {
            val fileEditorManager = project.service<FileEditorManager>()
            fileEditorManager.openFile(file, true)
        }

        // Phase 1: Edit inside EL expressions - type property chains that trigger
        // EL re-parsing, variable resolution, and property chain resolution
        val elEditOperations = listOf(
            "# edit:el-prop" to listOf(".host", ".port", ".connectionPool.maxConnections"),
            "# edit:el-api" to listOf(".baseUrl", ".timeout", ".retryPolicy.maxRetries"),
            "# edit:el-metrics" to listOf(".enabled", ".endpoint", ".tags.environment"),
        )

        println("PERF-TEST: EL editing phase - typing property chains inside EL expressions")
        for ((marker, properties) in elEditOperations) {
            for (prop in properties) {
                withContext(Dispatchers.EDT) {
                    typeInElExpression(project, marker, prop)
                }
                delay(300.milliseconds)
                withContext(Dispatchers.EDT) {
                    deleteInElExpression(project, marker, prop.length)
                }
                delay(300.milliseconds)
            }
        }

        // Phase 2: Invoke completion inside EL expressions
        val completionMarkers = listOf(
            "# el-complete:var",
            "# el-complete:builtin-prop",
            "# el-complete:custom-prop",
            "# el-complete:deep-prop",
            "# el-complete:deep-prop2",
        )

        println("PERF-TEST: EL completion phase - invoking code completion inside EL expressions")
        for (marker in completionMarkers) {
            withContext(Dispatchers.EDT) {
                invokeElCompletion(project, marker)
            }
            delay(500.milliseconds)
        }

        println("PERF-TEST: EL expression scenario complete")
    }

    /**
     * Inserts text inside an EL expression (before the closing `}`),
     * triggering re-parsing of the EL expression and property chain resolution.
     */
    private fun typeInElExpression(project: Project, marker: String, textToInsert: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val text = editor.document.text
        val markerOffset = text.indexOf(marker)
        if (markerOffset < 0) {
            println("PERF-TEST: Marker '$marker' not found")
            return
        }

        // Find the closing } of the EL expression on this line, before the marker
        val lineStart = text.lastIndexOf('\n', markerOffset) + 1
        val lineText = text.substring(lineStart, markerOffset)
        val lastBrace = lineText.lastIndexOf('}')
        if (lastBrace < 0) {
            println("PERF-TEST: No EL expression found before '$marker'")
            return
        }

        val insertOffset = lineStart + lastBrace
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(insertOffset, textToInsert)
        }
        editor.caretModel.moveToOffset(insertOffset + textToInsert.length)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    /**
     * Deletes text that was previously inserted inside an EL expression.
     */
    private fun deleteInElExpression(project: Project, marker: String, length: Int) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val text = editor.document.text
        val markerOffset = text.indexOf(marker)
        if (markerOffset < 0) {
            return
        }

        val lineStart = text.lastIndexOf('\n', markerOffset) + 1
        val lineText = text.substring(lineStart, markerOffset)
        val lastBrace = lineText.lastIndexOf('}')
        if (lastBrace < 0) {
            return
        }

        val deleteEnd = lineStart + lastBrace
        val deleteFrom = deleteEnd - length
        if (deleteFrom < lineStart) {
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.deleteString(deleteFrom, deleteEnd)
        }
        editor.caretModel.moveToOffset(deleteFrom)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    /**
     * Positions caret inside an EL expression (before the closing `}`)
     * and invokes code completion programmatically.
     */
    private fun invokeElCompletion(project: Project, marker: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val text = editor.document.text
        val markerOffset = text.indexOf(marker)
        if (markerOffset < 0) {
            println("PERF-TEST: Marker '$marker' not found for completion")
            return
        }

        // Find the closing } of the EL expression on this line
        val lineStart = text.lastIndexOf('\n', markerOffset) + 1
        val lineText = text.substring(lineStart, markerOffset)
        val lastBrace = lineText.lastIndexOf('}')
        if (lastBrace < 0) {
            println("PERF-TEST: No EL expression found before '$marker'")
            return
        }

        // Position caret just before the closing }
        val caretOffset = lineStart + lastBrace
        editor.caretModel.moveToOffset(caretOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)

        try {
            CodeCompletionHandlerBase(CompletionType.BASIC).invokeCompletion(project, editor)
            val lookup = LookupManager.getActiveLookup(editor)
            val itemCount = lookup?.items?.size ?: 0
            println("PERF-TEST: Completion at '$marker': $itemCount items")
            // Keep the popup visible briefly so it appears in JFR and is visible during manual observation
            Thread.sleep(1000)
            lookup?.hideLookup(false)
        } catch (e: Exception) {
            println("PERF-TEST: Completion failed at '$marker': ${e.message}")
        }
    }

    /**
     * Navigation scenario: goto declaration from EL variable references
     * and find usages from variable declarations.
     */
    private suspend fun runNavigationScenario(project: Project, file: VirtualFile) {
        withContext(Dispatchers.EDT) {
            val fileEditorManager = project.service<FileEditorManager>()
            fileEditorManager.openFile(file, true)
        }

        // Phase 1: Goto declaration - resolve references from EL expressions
        val gotoMarkers = listOf(
            "# goto:set-var",
            "# goto:prop",
            "# goto:deep-prop",
            "# goto:builtin-prop",
        )

        println("PERF-TEST: Navigation phase - goto declaration from EL variables")
        for (marker in gotoMarkers) {
            withContext(Dispatchers.EDT) {
                resolveElReference(project, marker)
            }
            delay(500.milliseconds)
        }

        // Phase 2: Find usages - search references from declarations
        val findUsagesMarkers = listOf(
            "# find-usages:var",
            "# find-usages:prop",
        )

        println("PERF-TEST: Navigation phase - find usages from declarations")
        for (marker in findUsagesMarkers) {
            withContext(Dispatchers.EDT) {
                searchUsages(project, marker)
            }
            delay(500.milliseconds)
        }

        println("PERF-TEST: Navigation scenario complete")
    }

    /**
     * Positions caret on a variable/property name inside an EL expression
     * and resolves its reference (goto declaration).
     *
     * For "var" markers: caret on identifier after ${
     * For "prop" markers: caret on last property name after the last dot
     */
    private fun resolveElReference(project: Project, marker: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val text = editor.document.text
        val markerOffset = text.indexOf(marker)
        if (markerOffset < 0) {
            println("PERF-TEST: Marker '$marker' not found for goto")
            return
        }

        val lineStart = text.lastIndexOf('\n', markerOffset) + 1
        val lineText = text.substring(lineStart, markerOffset)
        val elStart = lineText.indexOf("\${")
        if (elStart < 0) {
            println("PERF-TEST: No EL expression found before '$marker'")
            return
        }

        val elEnd = lineText.indexOf('}', elStart)
        val elContent = if (elEnd > 0) lineText.substring(elStart + 2, elEnd) else ""

        // For property markers, position caret on the last segment (after last dot)
        // For variable markers, position caret on the identifier (after ${)
        val caretOffset = if (marker.contains("prop")) {
            val lastDot = elContent.lastIndexOf('.')
            if (lastDot >= 0) {
                lineStart + elStart + 2 + lastDot + 1
            } else {
                lineStart + elStart + 2
            }
        } else {
            lineStart + elStart + 2
        }

        editor.caretModel.moveToOffset(caretOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)

        try {
            val flags = TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED
            val target = TargetElementUtil.findTargetElement(editor, flags)
            if (target != null) {
                val name = (target as? PsiNamedElement)?.name ?: target.text?.take(30)
                val file = target.containingFile?.name ?: "?"
                println("PERF-TEST: Goto '$marker': resolved to '$name' in $file")
            } else {
                // Fallback: try via reference
                val ref = TargetElementUtil.findReference(editor, caretOffset)
                val resolved = ref?.resolve()
                val name = (resolved as? PsiNamedElement)?.name ?: resolved?.text?.take(30)
                println("PERF-TEST: Goto '$marker': ref resolved to '${name ?: "null"}'")
            }
        } catch (e: Exception) {
            println("PERF-TEST: Goto '$marker' failed: ${e.message}")
        }
    }

    /**
     * Positions caret on a YAML key (variable declaration) and searches
     * for all references to it across the file (find usages).
     */
    private fun searchUsages(project: Project, marker: String) {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val text = editor.document.text
        val markerOffset = text.indexOf(marker)
        if (markerOffset < 0) {
            println("PERF-TEST: Marker '$marker' not found for find-usages")
            return
        }

        // Find the YAML key on this line (first non-whitespace text)
        val lineStart = text.lastIndexOf('\n', markerOffset) + 1
        val lineText = text.substring(lineStart, markerOffset)
        val keyStart = lineText.indexOfFirst { !it.isWhitespace() }
        if (keyStart < 0) {
            println("PERF-TEST: No key found on line for '$marker'")
            return
        }

        val caretOffset = lineStart + keyStart
        editor.caretModel.moveToOffset(caretOffset)
        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)

        try {
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)

            var element = psiFile.findElementAt(caretOffset)
            // Walk up to find a PsiNamedElement (YAML key-value)
            while (element != null && element !is PsiNamedElement && element !is PsiFile) {
                element = element.parent
            }

            if (element != null && element !is PsiFile) {
                val usages = ReferencesSearch.search(element).findAll()
                val name = (element as? PsiNamedElement)?.name
                println("PERF-TEST: Find usages '$marker': ${usages.size} references to '$name'")
            } else {
                println("PERF-TEST: Find usages '$marker': no suitable PSI element at offset $caretOffset")
            }
        } catch (e: Exception) {
            println("PERF-TEST: Find usages '$marker' failed: ${e.message}")
        }
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