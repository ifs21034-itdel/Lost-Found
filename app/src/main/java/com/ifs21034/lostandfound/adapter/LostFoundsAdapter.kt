package com.ifs21034.lostandfound.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ifs21034.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21034.lostandfound.databinding.ItemRowLostFoundBinding

class LostFoundsAdapter :
    ListAdapter<LostFoundsItemResponse,
            LostFoundsAdapter.MyViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback
    private var originalData = mutableListOf<LostFoundsItemResponse>()
    private var filteredData = mutableListOf<LostFoundsItemResponse>()

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRowLostFoundBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = originalData[originalData.indexOf(getItem(position))]
        holder.binding.cbItemLostFoundIsCompleted.setOnCheckedChangeListener(null)
        holder.binding.cbItemLostFoundIsCompleted.setOnLongClickListener(null)

        holder.bind(data)

        holder.binding.cbItemLostFoundIsCompleted.setOnCheckedChangeListener { _, isChecked ->
            data.isCompleted = if (isChecked) 1 else 0
            holder.bind(data)
            onItemClickCallback.onCheckedChangeListener(data, isChecked)
        }

        holder.binding.ivItemLostFoundDetail.setOnClickListener {
            onItemClickCallback.onClickDetailListener(data.id)
        }
    }

    class MyViewHolder(val binding: ItemRowLostFoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: LostFoundsItemResponse) {
            binding.apply {
                tvItemLostFoundTitle.text = data.title
                cbItemLostFoundIsCompleted.isChecked = data.isCompleted == 1
                val status = if(data.status.equals("found", ignoreCase = true)){
                    highlightText("Found", Color.GREEN)
                } else {
                    highlightText("Lost", Color.RED)
                }
                tvItemLostFoundStatus.text = status
            }
        }

        private fun highlightText(text: String, color: Int): SpannableString {
            val spannableString =SpannableString(text)
            val foregroundColorSpan = ForegroundColorSpan(color)
            spannableString.setSpan(foregroundColorSpan, 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE )
            return spannableString
        }
    }

    fun submitOriginalList(list: List<LostFoundsItemResponse>) {
        originalData = list.toMutableList()
        filteredData = list.toMutableList()

        submitList(originalData)
    }

    fun filter(query: String) {
        filteredData = if (query.isEmpty()) {
            originalData
        } else {
            originalData.filter {
                (it.title.contains(query, ignoreCase = true))
            }.toMutableList()
        }

        submitList(filteredData)
    }

    interface OnItemClickCallback {
        fun onCheckedChangeListener(lostFound: LostFoundsItemResponse, isChecked: Boolean)
        fun onClickDetailListener(lostFoundId: Int)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LostFoundsItemResponse>() {
            override fun areItemsTheSame(
                oldItem: LostFoundsItemResponse,
                newItem: LostFoundsItemResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: LostFoundsItemResponse,
                newItem: LostFoundsItemResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}