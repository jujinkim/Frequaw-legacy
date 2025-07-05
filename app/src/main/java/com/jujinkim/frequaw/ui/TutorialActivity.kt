package com.jujinkim.frequaw.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.adapter.TutorialAdapter

class TutorialActivity : AppCompatActivity() {
    lateinit var vp: ViewPager2
    lateinit var adapter: TutorialAdapter
    lateinit var btnPrev: Button
    lateinit var btnNext: Button
    lateinit var btnFinish: Button
    lateinit var tvIndicator: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        vp = findViewById(R.id.vp_tutorial)
        btnPrev = findViewById(R.id.btn_prev_tutorial)
        btnNext = findViewById(R.id.btn_next_tutorial)
        btnFinish = findViewById(R.id.btn_finish_tutorial)
        tvIndicator = findViewById(R.id.tv_tutorial_progress)

        initTutorial()
        setResult(RESULT_OK)
    }

    private fun initTutorial() {
        adapter = TutorialAdapter()
        vp.adapter = adapter

        btnPrev.setOnClickListener { vp.currentItem = (vp.currentItem - 1).coerceAtLeast(0) }
        btnNext.setOnClickListener { vp.currentItem = (vp.currentItem + 1).coerceAtMost(adapter.itemCount - 1) }

        btnFinish.setOnClickListener { finish() }

        vp.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUI(position)
                super.onPageSelected(position)
            }
        })

        updateUI(vp.currentItem)
    }

    private fun updateUI(position: Int) {
        btnPrev.visibility =
            if (position == 0) View.INVISIBLE
            else View.VISIBLE

        btnNext.visibility =
            if (position == adapter.itemCount - 1) View.INVISIBLE
            else View.VISIBLE

        btnFinish.visibility =
            if (position == adapter.itemCount - 1) View.VISIBLE
            else View.INVISIBLE

        tvIndicator.text = "${position + 1} / ${adapter.itemCount}"
    }
}