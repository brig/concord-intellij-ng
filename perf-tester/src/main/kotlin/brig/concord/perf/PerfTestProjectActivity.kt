package brig.concord.perf

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
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
import com.intellij.psi.PsiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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
}
