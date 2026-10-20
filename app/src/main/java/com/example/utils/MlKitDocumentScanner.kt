package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ScannedDriverDocument(
    val detectedPlateNumber: String? = null,
    val detectedPermitNumber: String? = null,
    val rawExtractedText: String = ""
)

object MlKitDocumentScanner {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun scanDocumentFromUri(context: Context, imageUri: Uri): ScannedDriverDocument {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val parsed = extractPlateAndPermit(text)
                        continuation.resume(parsed)
                    }
                    .addOnFailureListener { e ->
                        Log.w("MlKitScanner", "Text recognition failed: ${e.message}")
                        continuation.resume(ScannedDriverDocument(rawExtractedText = ""))
                    }
            } catch (e: Exception) {
                Log.e("MlKitScanner", "Error creating InputImage: ${e.message}")
                continuation.resume(ScannedDriverDocument(rawExtractedText = ""))
            }
        }
    }

    suspend fun scanDocumentFromBitmap(bitmap: Bitmap): ScannedDriverDocument {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val parsed = extractPlateAndPermit(text)
                        continuation.resume(parsed)
                    }
                    .addOnFailureListener { e ->
                        Log.w("MlKitScanner", "Bitmap text recognition failed: ${e.message}")
                        continuation.resume(ScannedDriverDocument(rawExtractedText = ""))
                    }
            } catch (e: Exception) {
                Log.e("MlKitScanner", "Error processing bitmap: ${e.message}")
                continuation.resume(ScannedDriverDocument(rawExtractedText = ""))
            }
        }
    }

    private fun extractPlateAndPermit(fullText: String): ScannedDriverDocument {
        // Nigerian License Plate Regex (e.g. ABA-419-KU, UMU-123-XY, ABA419KU)
        val platePattern = Regex("""([A-Z]{3})[- ]?([0-9]{3})[- ]?([A-Z]{2})""", RegexOption.IGNORE_CASE)
        // Abia State Commercial Tricycle Permit (e.g. AB/KKE/2026, ATRWAN-ABA-01)
        val permitPattern = Regex("""(AB\/KKE\/[0-9]{4}|ATRWAN-[A-Z]{3}-[0-9]{1,4}|ABI\/[A-Z0-9\/]+)""", RegexOption.IGNORE_CASE)

        val plateMatch = platePattern.find(fullText)?.value?.uppercase()
        val permitMatch = permitPattern.find(fullText)?.value?.uppercase()

        return ScannedDriverDocument(
            detectedPlateNumber = plateMatch,
            detectedPermitNumber = permitMatch,
            rawExtractedText = fullText
        )
    }
}
