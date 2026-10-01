package com.projectx.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.projectx.app.model.PublicProfile
import com.projectx.app.model.User
import com.projectx.app.model.career.*
import java.io.File
import java.io.FileOutputStream

object CvPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points (8.27 in * 72)
    private const val PAGE_HEIGHT = 842 // A4 height in points (11.69 in * 72)
    private const val MARGIN = 36f

    fun generateCvPdf(
        context: Context,
        user: User?,
        profile: PublicProfile?,
        cvProfile: CvProfile,
        certifications: List<Certification>,
        projects: List<ProjectItem>,
        skills: List<SkillItem>,
        achievements: List<Achievement>
    ): Result<File> {
        val pdfDocument = PdfDocument()

        return try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor(if (cvProfile.selectedTemplate == "Modern") "#1E293B" else "#0B1220")
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#3B82F6")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val sectionPaint = Paint().apply {
                color = Color.parseColor("#1E1B4B")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 1f
            }

            var y = MARGIN + 10f

            // --- HEADER ---
            val displayName = profile?.displayName?.ifBlank { null } ?: user?.email?.substringBefore("@") ?: "Student"
            canvas.drawText(displayName.uppercase(), MARGIN, y, titlePaint)
            y += 18f

            canvas.drawText(cvProfile.targetRole, MARGIN, y, subtitlePaint)
            y += 16f

            val contactLine = listOfNotNull(
                user?.email?.ifBlank { null },
                user?.phoneNumber?.ifBlank { null },
                profile?.schoolName?.ifBlank { null } ?: "Demo University"
            ).joinToString(" | ")

            canvas.drawText(contactLine, MARGIN, y, bodyPaint)
            y += 16f

            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 18f

            // Helper to check page bounds & create new page
            fun checkNewPage(neededHeight: Float) {
                if (y + neededHeight > PAGE_HEIGHT - MARGIN) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN + 10f
                }
            }

            // --- SUMMARY ---
            if (cvProfile.personalSummary.isNotBlank()) {
                checkNewPage(40f)
                canvas.drawText("PROFESSIONAL SUMMARY", MARGIN, y, sectionPaint)
                y += 14f
                val lines = wrapText(cvProfile.personalSummary, bodyPaint, PAGE_WIDTH - 2 * MARGIN)
                for (line in lines) {
                    checkNewPage(12f)
                    canvas.drawText(line, MARGIN, y, bodyPaint)
                    y += 12f
                }
                y += 10f
            }

            // --- EDUCATION ---
            checkNewPage(50f)
            canvas.drawText("EDUCATION", MARGIN, y, sectionPaint)
            y += 14f
            val school = profile?.schoolName?.ifBlank { null } ?: "Demo University"
            val prog = listOfNotNull(profile?.program, profile?.department).joinToString(" • ").ifBlank { "Undergraduate Student" }
            canvas.drawText(school, MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
            y += 12f
            canvas.drawText(prog, MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
            y += 16f

            // --- SKILLS ---
            val selectedSkills = skills.filter { cvProfile.selectedSkillIds.contains(it.skillId) }
            if (selectedSkills.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("TECHNICAL SKILLS", MARGIN, y, sectionPaint)
                y += 14f
                val skillLine = selectedSkills.joinToString(", ") { "${it.name} (${it.proficiency ?: "Intermediate"})" }
                val lines = wrapText(skillLine, bodyPaint, PAGE_WIDTH - 2 * MARGIN)
                for (line in lines) {
                    checkNewPage(12f)
                    canvas.drawText(line, MARGIN, y, bodyPaint)
                    y += 12f
                }
                y += 10f
            }

            // --- PROJECTS ---
            val selectedProjects = projects.filter { cvProfile.selectedProjectIds.contains(it.projectId) }
            if (selectedProjects.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("KEY PROJECTS", MARGIN, y, sectionPaint)
                y += 14f

                for (proj in selectedProjects) {
                    checkNewPage(40f)
                    canvas.drawText(proj.projectName, MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                    y += 12f

                    if (proj.technologies.isNotEmpty()) {
                        canvas.drawText("Technologies: ${proj.technologies.joinToString(", ")}", MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) })
                        y += 12f
                    }

                    val descLines = wrapText(proj.description, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }, PAGE_WIDTH - 2 * MARGIN)
                    for (line in descLines) {
                        checkNewPage(12f)
                        canvas.drawText(line, MARGIN, y, bodyPaint)
                        y += 12f
                    }
                    y += 8f
                }
            }

            // --- CERTIFICATIONS ---
            val selectedCerts = certifications.filter { cvProfile.selectedCertificationIds.contains(it.certificationId) }
            if (selectedCerts.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("CERTIFICATIONS", MARGIN, y, sectionPaint)
                y += 14f

                for (cert in selectedCerts) {
                    checkNewPage(30f)
                    canvas.drawText("${cert.name} — ${cert.issuingOrganization}", MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                    y += 12f
                    canvas.drawText("Issued: ${cert.issueDate}${if (!cert.credentialId.isNullOrBlank()) " | ID: ${cert.credentialId}" else ""}", MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) })
                    y += 14f
                }
            }

            // --- ACHIEVEMENTS ---
            val selectedAchieve = achievements.filter { cvProfile.selectedAchievementIds.contains(it.achievementId) }
            if (selectedAchieve.isNotEmpty()) {
                checkNewPage(40f)
                canvas.drawText("HONORS & ACHIEVEMENTS", MARGIN, y, sectionPaint)
                y += 14f

                for (ach in selectedAchieve) {
                    checkNewPage(30f)
                    canvas.drawText("${ach.title} (${ach.date})", MARGIN, y, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                    y += 12f
                    val lines = wrapText(ach.description, bodyPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }, PAGE_WIDTH - 2 * MARGIN)
                    for (line in lines) {
                        checkNewPage(12f)
                        canvas.drawText(line, MARGIN, y, bodyPaint)
                        y += 12f
                    }
                    y += 8f
                }
            }

            pdfDocument.finishPage(page)

            // Save PDF to cache
            val exportDir = File(context.cacheDir, "cv_exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val pdfFile = File(exportDir, "CV_${displayName.replace(" ", "_")}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()

            Result.success(pdfFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pdfDocument.close()
        }
    }

    fun shareCvPdf(context: Context, pdfFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Student Curriculum Vitae - ${pdfFile.nameWithoutExtension}")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Student CV PDF"))
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }
}
