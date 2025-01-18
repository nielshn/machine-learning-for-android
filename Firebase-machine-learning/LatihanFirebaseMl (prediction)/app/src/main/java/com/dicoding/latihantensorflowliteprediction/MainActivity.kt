package com.dicoding.latihantensorflowliteprediction

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dicoding.latihantensorflowliteprediction.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnPredict.isEnabled = false
        val predictionHelper = PredictionHelper(
            context = this,
            onResult = { result ->
                binding.tvResult.text = result
            },
            onError = { errorMessage ->
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            },
            onDownloadSuccess = {
                binding.btnPredict.isEnabled = true
            }
        )
        binding.btnPredict.setOnClickListener {
            val input = binding.edSales.text.toString()
            predictionHelper.predict(input)
        }

    }
}