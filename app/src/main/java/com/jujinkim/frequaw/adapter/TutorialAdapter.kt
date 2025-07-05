package com.jujinkim.frequaw.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jujinkim.frequaw.FrequawApp
import com.jujinkim.frequaw.R
import com.jujinkim.frequaw.databinding.VhTutorialBinding

class TutorialAdapter() : RecyclerView.Adapter<TutorialViewHolder>() {
    val context = FrequawApp.appContext
    private val items = listOf(
        Pair(R.drawable.tutorial1, context.getString(R.string.tutorial_1)),
        Pair(R.drawable.tutorial2, context.getString(R.string.tutorial_2)),
        Pair(R.drawable.tutorial3, context.getString(R.string.tutorial_3)),
        Pair(R.drawable.tutorial4, context.getString(R.string.tutorial_4)),
        Pair(R.drawable.tutorial5, context.getString(R.string.tutorial_5)),
        Pair(R.drawable.tutorial6, context.getString(R.string.tutorial_6)),
        Pair(R.drawable.tutorial7, context.getString(R.string.tutorial_7))
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val binding = VhTutorialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TutorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(items[position].first, items[position].second)
    }

    override fun getItemCount() = items.size
}

class TutorialViewHolder(
    private val binding: VhTutorialBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(imgRes: Int, msg: String) {
        binding.image = binding.root.resources.getDrawable(imgRes)
        binding.text = msg
    }
}