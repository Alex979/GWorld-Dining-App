package com.example.gworlddiningcompanion

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

class HomeScreenActivity : AppCompatActivity() {

    // Initialize checkboxes
    private lateinit var burgersCheckbox: CheckBox
    private lateinit var tacosCheckbox: CheckBox
    private lateinit var sushiCheckbox: CheckBox
    private lateinit var sandwichesCheckbox: CheckBox
    private lateinit var pizzaCheckbox: CheckBox
    private lateinit var breakfastCheckbox: CheckBox

    // Initialize select buttons, search area input, error message label, and search button
    private lateinit var selectAllButton: Button
    private lateinit var deselectAllButton: Button
    private lateinit var searchRadiusInput: EditText
    private lateinit var errorMessage: TextView
    private lateinit var searchButton: Button

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences("gworld-dining-companion", Context.MODE_PRIVATE)

        // Instantiate checkbox variables
        burgersCheckbox = findViewById(R.id.burgersCheckbox)
        tacosCheckbox = findViewById(R.id.tacosCheckbox)
        sushiCheckbox = findViewById(R.id.sushiCheckbox)
        sandwichesCheckbox = findViewById(R.id.sandwichesCheckbox)
        pizzaCheckbox = findViewById(R.id.pizzaCheckbox)
        breakfastCheckbox = findViewById(R.id.breakfastCheckbox)

        // Instantiate other UI variables
        selectAllButton = findViewById(R.id.selectAllButton)
        deselectAllButton = findViewById(R.id.deselectAllButton)
        searchRadiusInput = findViewById(R.id.searchRadiusInput)
        errorMessage = findViewById(R.id.errorMessage)
        searchButton = findViewById(R.id.searchButton)

        // Remember previous checkbox values
        burgersCheckbox.isChecked = preferences.getBoolean("BURGER_CHECKBOX", true)
        tacosCheckbox.isChecked = preferences.getBoolean("TACOS_CHECKBOX", false)
        sushiCheckbox.isChecked = preferences.getBoolean("SUSHI_CHECKBOX", false)
        sandwichesCheckbox.isChecked = preferences.getBoolean("SANDWICHES_CHECKBOX", false)
        pizzaCheckbox.isChecked = preferences.getBoolean("PIZZA_CHECKBOX", false)
        breakfastCheckbox.isChecked = preferences.getBoolean("BREAKFAST_CHECKBOX", false)

        // Remember previous search area
        searchRadiusInput.setText(preferences.getString("SEARCH_RADIUS", "1500"))

        // Validate input and open new activity if successful
        searchButton.setOnClickListener {
            if(validateInput()) {
                // Remember user input for next launch
                preferences
                    .edit()
                    .putBoolean("BURGER_CHECKBOX", burgersCheckbox.isChecked)
                    .putBoolean("TACOS_CHECKBOX", tacosCheckbox.isChecked)
                    .putBoolean("SUSHI_CHECKBOX", sushiCheckbox.isChecked)
                    .putBoolean("SANDWICHES_CHECKBOX", sandwichesCheckbox.isChecked)
                    .putBoolean("PIZZA_CHECKBOX", pizzaCheckbox.isChecked)
                    .putBoolean("BREAKFAST_CHECKBOX", breakfastCheckbox.isChecked)
                    .putString("SEARCH_RADIUS", searchRadiusInput.text.toString())
                    .apply()

                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("SEARCH_RADIUS", searchRadiusInput.text.toString().toInt())

                // Generate categories query string from checkboxes and send to intent
                var categories = mutableListOf<String>()
                if(burgersCheckbox.isChecked)
                    categories.add("burgers")
                if(tacosCheckbox.isChecked)
                    categories.add("tacos")
                if(sushiCheckbox.isChecked)
                    categories.add("sushi")
                if(sandwichesCheckbox.isChecked)
                    categories.add("sandwiches")
                if(pizzaCheckbox.isChecked)
                    categories.add("pizza")
                if(breakfastCheckbox.isChecked)
                    categories.add("breakfast")

                if(categories.size > 0)
                    intent.putExtra("CATEGORY_QUERY", categories.joinToString(","))

                startActivity(intent)
            }
        }

        // Select All action
        selectAllButton.setOnClickListener {
            burgersCheckbox.isChecked = true
            tacosCheckbox.isChecked = true
            sushiCheckbox.isChecked = true
            sandwichesCheckbox.isChecked = true
            pizzaCheckbox.isChecked = true
            breakfastCheckbox.isChecked = true
        }

        // Deselect All action
        deselectAllButton.setOnClickListener {
            burgersCheckbox.isChecked = false
            tacosCheckbox.isChecked = false
            sushiCheckbox.isChecked = false
            sandwichesCheckbox.isChecked = false
            pizzaCheckbox.isChecked = false
            breakfastCheckbox.isChecked = false
        }
    }

    private fun validateInput(): Boolean {
        var errorMsg = ""

        if(searchRadiusInput.text.toString() == "" || Integer.parseInt(searchRadiusInput.text.toString()) == 0) {
            errorMsg += "Search radius cannot be empty or 0."
        }

        // Set error message label
        errorMessage.text = errorMsg

        return errorMsg == ""
    }
}
