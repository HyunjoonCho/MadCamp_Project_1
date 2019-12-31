package com.example.madcamp_project_1

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private var mOnBackPressedListener: onBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        viewpager_main.adapter = fragmentAdapter

        tabs_main.setupWithViewPager(viewpager_main)
    }

    interface onBackPressedListener {
        fun onBack()
    }

    fun setOnBackPressedListener(mListener: onBackPressedListener?) {
        mOnBackPressedListener = mListener
    }

    override fun onBackPressed() {
        if (mOnBackPressedListener == null) {
            if (backPressedTime == 0.toLong()) {
                Toast.makeText(this, "Exit when back pressed once more", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            }
            else {
                var seconds = System.currentTimeMillis() - backPressedTime

                if (seconds > 2000.toLong()) {
                    Toast.makeText(this, "Exit when back pressed once more", Toast.LENGTH_SHORT).show()
                    backPressedTime = System.currentTimeMillis()
                }
                else {
                    super.onBackPressed()
                }
            }

        }
        else {
            mOnBackPressedListener!!.onBack();
        }
    }

}
