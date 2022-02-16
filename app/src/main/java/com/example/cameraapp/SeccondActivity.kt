package com.example.cameraapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class SecondActivity : AppCompatActivity() {
    private lateinit var editText: View
    private var pattern = ""

    companion object {
        const val PATTERN = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_name)
        editText = findViewById(R.id.editText)
        val pattern1 = intent.getStringExtra(PATTERN)
        if (!pattern1.isNullOrEmpty()) {
            Log.d("ACT2", "I use intent")
            pattern = pattern1.toString()
        }
        (editText as TextView).text = pattern
    }

    override fun onBackPressed() {
        val myIntent = Intent(this, MainActivity::class.java)
        myIntent.putExtra(MainActivity.MAINPATTERN, pattern)
        startActivity(myIntent)
        Log.d("ACT2", "Back :pattern = $pattern\n")
        super.onBackPressed()
    }


    fun onClickSave(view: View) {
        editText = findViewById(R.id.editText) as EditText
        pattern = (editText as TextView).text.toString()
        Log.d("ACT2", "Save : pattern = $pattern\n")
    }

}