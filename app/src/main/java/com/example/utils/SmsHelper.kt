package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object SmsHelper {
    fun dispatchNativeWaybillOtpSms(
        context: Context,
        recipientPhone: String,
        otp: String,
        tripOrigin: String,
        tripDestination: String
    ) {
        val sanitizedPhone = recipientPhone.trim()
        val smsText = "KekeGo Waybill Security OTP: $otp\nYour cargo has been dispatched from $tripOrigin to $tripDestination.\nDo not disclose this code until your items are physically handed to you."

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$sanitizedPhone")
                putExtra("sms_body", smsText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open messaging client: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
