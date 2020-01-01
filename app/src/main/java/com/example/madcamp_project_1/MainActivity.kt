package com.example.madcamp_project_1

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private var mOnBackPressedListener: onBackPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        setContentView(R.layout.activity_main)

        val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        viewpager_main.adapter = fragmentAdapter
        viewpager_main.offscreenPageLimit = 2

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
