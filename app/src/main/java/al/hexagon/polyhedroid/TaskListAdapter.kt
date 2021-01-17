package al.hexagon.polyhedroid

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.taskcard.view.*

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.net.Uri
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.activity_task_entry.*
import org.dmfs.provider.tasks.TaskContract
import kotlin.math.roundToInt

class TaskListAdapter(private var context: Context) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {


    // Basic colors
    // val colors = arrayOf("#ff0000","#ffa500","#ffff00","#008000","#0000ff","#4b0082","#ee82ee")

    // Deeper spectrum
    // Based on this: http://colrd.com/gradient/22141/?download=css
    val colors = arrayOf("#8a188c","#9b0078","#b8075e","#d50e44","#ea6142","#ffb440","#ffd932","#ffff25","#92e62d","#26ce35","#13bd8f","#00adea","#2787de","#4f61d3","#6c3caf")

    val helper = TaskDBHelper()

    var taskListList: ArrayList<TaskList> = helper.getTaskLists(context)

    var taskList: ArrayList<TaskItem> = ArrayList()

    lateinit var attachedView: RecyclerView

    init {

        // TODO: Put this on a separate thread
        for(tl in taskListList){
            val p = PreferenceManager.getDefaultSharedPreferences(context)
            //tl.tasks = helper.getTasksByList(context, tl.id, p.getBoolean("showComplete", false), p.getBoolean("showCancelled", false))
            taskList.addAll(
                helper.getTasksByList(
                    context,  // Context
                    tl.id,    // List ID
                    p.getBoolean("showComplete", false), // User setting: showComplete
                    p.getBoolean("showCancelled", false) // User setting: showCancelled
                ))
            //Log.d("Found task list", "(${tl.id}) ${tl.name}")
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedView = recyclerView
    }

    override fun getItemCount(): Int { return taskList.size }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Log.d("onCreateViewHolder()", "parent: $parent  viewType: $viewType")
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.taskcard, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val task = taskList[position]

        // Drop in default ROYGBIV'd color
        // TODO: Make this use the default color
        val accentcolor = ColorDrawable(Color.parseColor(colors[position % colors.size]))


        holder.title.text = task.title

        holder.subhead.text = if(task.description != null) task.description!!.substringBefore("\n") else ""

        if(task.color != null && !task.color.equals("null") && task.color!!.trim().isNotEmpty()) {
            holder.progress.progressDrawable.setColorFilter(
                Color.parseColor(task.color),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.accent.background = ColorDrawable(Color.parseColor(task.color))
        }
        else {
            holder.progress.progressDrawable.setColorFilter(
                Color.parseColor(colors[position % colors.size]),
                PorterDuff.Mode.SRC_ATOP
            )
            holder.accent.background = accentcolor
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
            val i = Intent(context, TaskEntry::class.java).putExtra("id", task.id).putExtra("pos", position)
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

                            // Refresh listing
                            // NOTE: This seems barbaric but it's the only thing that works
                            attachedView.adapter = TaskListAdapter(context)
                            notifyDataSetChanged()

                            // Maintain scroll position
                            // TODO: Test edge cases more thoroughly
                            if(position < (taskList.size-1))
                                attachedView.scrollToPosition(position)
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

    // Clear out any special formatting and reset
    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)

        // Set typeface to normal (from italics)
        holder.title.setTypeface(null, Typeface.NORMAL)

        // Remove strikethrough if present
        if(holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG > 0)
            holder.title.paintFlags = holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        // Reset and progress bar (on by default)
        holder.progress.progress = 0
        holder.progress.visibility = View.VISIBLE

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.title
        var subhead: TextView = itemView.subhead
        var accent: View = itemView.accent
        var progress: ProgressBar = itemView.progressBar
        var layout: ConstraintLayout = itemView.layout
        var base: FrameLayout = itemView.base
    }


    fun refreshList() {
        // TODO: Put this on a separate thread
        for(tl in taskListList){
            val p = PreferenceManager.getDefaultSharedPreferences(context)
            taskList.clear()
            //tl.tasks = helper.getTasksByList(context, tl.id, p.getBoolean("showComplete", false), p.getBoolean("showCancelled", false))
            taskList.addAll(helper.getTasksByList(context, tl.id, p.getBoolean("showComplete", false), p.getBoolean("showCancelled", false)))
            Log.d("Found task list", "(${tl.id}) ${tl.name}")
            attachedView.adapter = TaskListAdapter(context)
            notifyDataSetChanged()
        }
    }


}