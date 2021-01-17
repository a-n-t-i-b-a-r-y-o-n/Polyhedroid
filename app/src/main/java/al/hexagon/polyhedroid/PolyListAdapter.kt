package al.hexagon.polyhedroid

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder
import kotlinx.android.synthetic.main.listheader.view.*
import kotlinx.android.synthetic.main.taskcard.view.*
import org.dmfs.provider.tasks.TaskContract
import kotlin.reflect.typeOf


class PolyListAdapter(var context: Context, var items: ArrayList<TaskList>, private var expandMgr: RecyclerViewExpandableItemManager, savedInstanceState: Bundle) : AbstractExpandableItemAdapter<PolyListAdapter.GroupViewHolder, PolyListAdapter.ChildViewHolder>() {

    init {
        setHasStableIds(true)
    }

    open class BaseViewHolder(itemView: View) : AbstractDraggableSwipeableItemViewHolder(itemView) {


        override fun getSwipeableContainerView(): View {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    class GroupViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var listName: TextView = itemView.listName
        var listAccent: View = itemView.listAccent
    }

    class ChildViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var title: TextView = itemView.title
        var subhead: TextView = itemView.subhead
        var accent: View = itemView.accent
        var progress: ProgressBar = itemView.progressBar
        var base: FrameLayout = itemView.base
    }

    override fun getGroupCount(): Int {
        return items.size }

    override fun getGroupId(groupPosition: Int): Long {
        return items[groupPosition].id }

    override fun getChildCount(groupPosition: Int): Int {
        return items[groupPosition].tasks.size }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long { return items[groupPosition].tasks[childPosition].id }

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): GroupViewHolder {
        return GroupViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.listheader, parent, false))
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ChildViewHolder {
        return ChildViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.taskcard, parent, false))
    }

    override fun onBindChildViewHolder(holder: ChildViewHolder, groupPosition: Int, childPosition: Int, viewType: Int) {
        val task = items[groupPosition].tasks[childPosition]

        holder.title.text = task.title

        holder.subhead.text = if(task.description != null) task.description!!.substringBefore("\n") else ""

        // Set the tasks color based on it's color attribute, list color attribute if that's null, or default it _that's_ also null
        if(task.color != null && !task.color.equals("null") && task.color!!.trim().isNotEmpty()) {
            // Use task color
            holder.progress.progressDrawable.setColorFilter(
                Color.parseColor("#" + Integer.toHexString(task.color!!.toInt())),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.accent.background = ColorDrawable(Color.parseColor("#" + Integer.toHexString(task.color!!.toInt())))

        }
        else if(items[groupPosition].color != null && !items[groupPosition].color.equals("null") && items[groupPosition].color!!.trim().isNotEmpty()) {
            // Use list color
            holder.progress.progressDrawable.setColorFilter(
                Color.parseColor("#" + Integer.toHexString(items[groupPosition].color!!.toInt())),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.accent.background = ColorDrawable(Color.parseColor("#" + Integer.toHexString(items[groupPosition].color!!.toInt())))

        }
        else {
            // Fall back to default color
            holder.progress.progressDrawable.setColorFilter(
                Color.parseColor("#" + Integer.toHexString(context.getString(R.string.default_note_color).toInt())),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.accent.background = ColorDrawable(Color.parseColor("#" + Integer.toHexString(context.getString(R.string.default_note_color).toInt())))
        }

        when(task.status){
            1 -> holder.title.setTypeface(null, Typeface.ITALIC)
            2,3 -> holder.title.paintFlags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        if(task.percent in 1..99)
            holder.progress.progress = task.percent
        else
            holder.progress.visibility = View.INVISIBLE

        holder.base.setOnClickListener{
            val i = Intent(context, TaskEntry::class.java).putExtra("id", task.id).putExtra("pos", childPosition)
            context.startActivity(i)
        }

        holder.base.setOnLongClickListener {
            let{

                // Build the dialog window
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .apply {
                        setPositiveButton(android.R.string.yes
                        ) { dialog, _ ->
                            // User confirmed deletion
                            dialog.dismiss()

                            // Perform DELETE
                            val uri: Uri = ContentUris.withAppendedId(TaskContract.Instances.getContentUri("org.dmfs.tasks"), task.id)
                            context.contentResolver.delete(uri, null, null)

                            // Show a normal-looking image toast by creating a normal text toast, then replacing
                            // the default inner LinearLayout's child TextView with an ImageView
                            ImageToast.makeImage(context, R.drawable.ic_delete_done_openlid_24px, Toast.LENGTH_SHORT).show()

                            /*

                            // Refresh listing
                            // NOTE: This seems barbaric but it's the only thing that works
                            attachedView.adapter = ListHeaderAdapter(context)
                            notifyDataSetChanged()

                            // Maintain scroll position
                            // TODO: Test edge cases more thoroughly
                            if(position < (taskList.size-1))
                                attachedView.scrollToPosition(position)

                             */
                        }
                        setNegativeButton(android.R.string.no
                        ) { dialog, _ ->
                            // User backed out of deletion
                            dialog.cancel()
                        }
                        setTitle("Delete Note")
                        setMessage("Are you sure?\n\nThis cannot be undone.")
                        create()
                        show()
                    }

            }
            true
        }
    }

    override fun onBindGroupViewHolder(holder: GroupViewHolder, groupPosition: Int, viewType: Int) {
        holder.listName.text = items[groupPosition].name
        if(items[groupPosition].color != null) {
            try {
                holder.listAccent.background = ColorDrawable(Color.parseColor("#" + Integer.toHexString(items[groupPosition].color!!.toInt())))
            } catch (e: IllegalArgumentException) {
                Log.d("Unknown List Color:", "${items[groupPosition].color?.toInt()?.inv()?.toString(16)}")
            }

        }
    }

    override fun onCheckCanExpandOrCollapseGroup(holder: GroupViewHolder, groupPosition: Int, x: Int, y: Int, expand: Boolean): Boolean {
        return true
    }

}