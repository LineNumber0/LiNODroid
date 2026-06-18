package ma.oprojet.linodroid.adapter
// ThemeAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ma.oprojet.linodroid.models.ThemeItem
import ma.oprojet.linodroid.databinding.ItemThemeBinding

class ThemeAdapter(
    private val onThemeSelected: (ThemeItem) -> Unit
) : ListAdapter<ThemeItem, ThemeAdapter.ThemeViewHolder>(ThemeDiffCallback()) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val binding = ItemThemeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ThemeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = getItem(position)
        holder.bind(theme, position == selectedPosition)
    }

    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition)
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }

    inner class ThemeViewHolder(
        private val binding: ItemThemeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onThemeSelected(getItem(position))
                }
            }
        }

        fun bind(theme: ThemeItem, isSelected: Boolean) {
            binding.apply {
                themeName.setText(theme.nameResId)
                themeIcon.setImageResource(theme.iconResId)
                themeDescription.setText(theme.descriptionResId)
                selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
                root.isSelected = isSelected
            }
        }
    }

    class ThemeDiffCallback : DiffUtil.ItemCallback<ThemeItem>() {
        override fun areItemsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
            return oldItem == newItem
        }
    }
}
