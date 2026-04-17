package com.example.android_project.common.ui

import android.os.Build
import android.text.InputType
import android.view.View
import android.widget.EditText

fun EditText.disableSuggestions() {
    inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
    }
}

