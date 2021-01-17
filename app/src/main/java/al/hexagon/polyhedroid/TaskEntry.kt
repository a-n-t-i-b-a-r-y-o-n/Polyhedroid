package al.hexagon.polyhedroid

import android.annotation.SuppressLint
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.KeyboardView
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.activity_task_entry.*
import kotlinx.android.synthetic.main.activity_task_entry.progressBar

import org.dmfs.provider.tasks.TaskContract
import java.security.MessageDigest

class TaskEntry : AppCompatActivity() {

    private lateinit var task: TaskItem
    private var existing = false

    private var mAccentColor: Int = -1

    private var textHash: String = "";

    // Details frame padding size (also set in XML)
    private var detailsFramePadding: Int = -1

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_entry)

        // Update DetailsFrame padding value
        detailsFramePadding = 140 * resources.displayMetrics.density.toInt()

        // Create a DB helper object
        val helper = TaskDBHelper()

        // Fill in spinner list of TaskLists
        ArrayAdapter(this, android.R.layout.simple_spinner_item, helper.getTaskLists(this)).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            listSpinner.adapter = it
        }

        // If we came to this activity by clicking on a task, fill in the info
        if((intent.extras != null && intent.extras?.get("id") != null) || existing){

            task = helper.getTaskById(this, intent.extras!!.getLong("id"))
            existing = true

            Log.d("Loaded: ", task.id.toString())

            // Fill in info from this Task
            titleBox.setText(task.title)
            editorBox.setText(task.description)
            progressBar.setProgress(task.percent.toFloat())

            // Set all the accent colors
            if(task.color != null && task.color!!.trim().isNotEmpty()){
                mAccentColor = task.color!!.toInt()
            } else {
                // No color value for this note: use the default theme note color
                mAccentColor = resources.getString(R.string.default_note_color).toInt()
            }

            // TODO: Is this actually reliable? Are task lists guaranteed to be contiguous?
            listSpinner.setSelection(task.lID.toInt()-1)

            // Long click also exits the activity on save
            /*
            saveBtn.setOnLongClickListener {
                saveBtn.callOnClick()

                setResult(if(intent.extras != null) intent.extras!!.getInt("pos") else -1)
                val i = Intent()

                if(intent.extras != null && intent.extras!!.getInt("pos") != null) {
                    i.putExtra("op", "update")
                    i.putExtra("pos", intent.extras!!.getInt("pos"))
                }

                setResult(0, i)
                finish()
                true
            }

             */

        }
        else {
            // This is a fresh note!
            task = TaskItem(-1, "", -1, null, null, null, -1, -1, "", null, null)

            // Use default accent color
            mAccentColor = resources.getString(R.string.default_note_color).toInt()


            // Save button performs an INSERT
            /*
            saveBtn.setOnClickListener {

                //Toast.makeImage(this, R.drawable.ic_save_add_24px, Toast.LENGTH_SHORT).show()

            }
            saveBtn.setOnLongClickListener{
                saveBtn.callOnClick()

                finish()
                true
            }

             */
        }

        // Double-check visibility
        bodyView.visibility = View.INVISIBLE
        titleView.visibility = View.INVISIBLE
        accentBanner.visibility = View.INVISIBLE

        // Apply the accent color throughout the activity
        applyStyleColor(mAccentColor)


        /** Button listeners, etc. **/

        /** Left side buttons **/
        tabBtn.setOnClickListener {
            val s = editorBox.selectionStart
            editorBox.setText(editorBox.text?.substring(0, s) + '\t' + editorBox.text?.substring(editorBox.selectionEnd))
            editorBox.setSelection(s+1)
        }

        checkboxBtn.setOnClickListener {
            val s = editorBox.selectionStart
            editorBox.setText(editorBox.text?.substring(0, s) + "- [ ] " + editorBox.text?.substring(editorBox.selectionEnd))
            editorBox.setSelection(s+6)
        }

        listBtn.setOnClickListener {
            val s = editorBox.selectionStart
            editorBox.setText(editorBox.text?.substring(0, s) + "- " + editorBox.text?.substring(editorBox.selectionEnd))
            editorBox.setSelection(s+2)
        }

        boldBtn.setOnClickListener {
            val s = editorBox.selectionStart
            val e = editorBox.selectionEnd
            editorBox.setText(editorBox.text?.substring(0, s) + "**" + editorBox.text?.substring(s, e) + "**" + editorBox.text?.substring(e))
            editorBox.setSelection(s+2, e+2)
        }

        italicBtn.setOnClickListener {
            val s = editorBox.selectionStart
            val e = editorBox.selectionEnd
            editorBox.setText(editorBox.text?.substring(0, s) + "_" + editorBox.text?.substring(s, e) + "_" + editorBox.text?.substring(e))
            editorBox.setSelection(s+1, e+1)
        }

        strikeBtn.setOnClickListener {
            val s = editorBox.selectionStart
            val e = editorBox.selectionEnd
            editorBox.setText(editorBox.text?.substring(0, s) + "~~" + editorBox.text?.substring(s, e) + "~~" + editorBox.text?.substring(e))
            editorBox.setSelection(s+2, e+2)
        }

        homeBtn.setOnClickListener { editorBox.onKeyDown(KeyEvent.KEYCODE_MOVE_HOME, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME)) }

        endBtn.setOnClickListener { editorBox.onKeyDown(KeyEvent.KEYCODE_MOVE_END, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END)) }

        /*
        // Make side buttons re-arrangeable
        for(b in buttonSet1.children)
            buttonSet1.setViewDraggable(b, b)

         for(b in buttonSet2.children)
            buttonSet2.setViewDraggable(b, b)
         */

        // Various Details buttons

        accentColor.setOnClickListener {
            ColorPickerDialogBuilder
                .with(this)
                .setTitle("Pick color")
                .initialColor(Color.parseColor("#" + Integer.toHexString(mAccentColor)))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(20)
                .showAlphaSlider(true)
                .showColorEdit(true)
                .setOnColorSelectedListener {
                    mAccentColor = it
                    accentColor.background = ColorDrawable(it)
                    applyStyleColor(mAccentColor)
                }
                .build()
                .show()
        }

        /** Bottom buttons **/
        cancelBtn.setOnClickListener{

            // If we detect changes, verify the user really does want to cancel
            if(MessageDigest.getInstance("MD5").digest(editorBox.text.toString().toByteArray()).joinToString { "%02x".format(it) } != textHash){
                let{
                    AlertDialog.Builder(it)
                        .apply{
                            setPositiveButton(android.R.string.yes) { dialog, _ ->
                                // User confirmed cancel
                                dialog.dismiss()

                                finish()
                            }
                            setNegativeButton(android.R.string.cancel) {dialog, _ ->
                                // User cancelled cancellation
                                dialog.cancel()
                            }
                            setTitle("Cancel Edit")
                            setMessage("Discard changes?")
                            create()
                            show()
                        }
                }
            }
            else
                finish()
        }

        deleteBtn.setOnClickListener{
            let{

                // Build the dialog window
                AlertDialog.Builder(it)
                    .apply {
                        setPositiveButton(android.R.string.yes
                        ) { dialog, _ ->
                            // User confirmed deletion
                            dialog.dismiss()

                            // Double-check this task even existed
                            if(existing) {
                                // Perform DELETE
                                val uri: Uri = ContentUris.withAppendedId(TaskContract.Instances.getContentUri("org.dmfs.tasks"), task!!.id)
                                contentResolver.delete(uri, null, null)

                                // Show a normal-looking image toast by creating a normal text toast, then replacing
                                // the default inner LinearLayout's child TextView with an ImageView
                                ImageToast.makeImage(context, R.drawable.ic_delete_done_openlid_24px, Toast.LENGTH_SHORT).show()

                                setResult(0, Intent().putExtra("op", "delete"))
                                finish()
                            }

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
        }

        saveBtn.setOnClickListener {
            if(existing) {

                // Perform an UPDATE in the Task DB
                val uri: Uri = ContentUris.withAppendedId(TaskContract.Tasks.getContentUri("org.dmfs.tasks"), task.id)
                val rows: Int = contentResolver.update(uri, getTaskValues(), null, null)

                Log.d("TaskEntry", "Updated $rows rows")

                // Show a normal-looking image toast by creating a normal text toast, then replacing
                // the default inner LinearLayout's child TextView with an ImageView
                ImageToast.makeImage(this, R.drawable.ic_save_update_color_24px, Toast.LENGTH_SHORT).show()
            }
            else {

                // Perform an INSERT in the Task DB
                val uri: Uri = contentResolver.insert(TaskContract.Tasks.getContentUri("org.dmfs.tasks"), getTaskValues())

                Log.d("Added", uri.toString())

                ImageToast.makeImage(this, R.drawable.ic_save_add_color_24px, Toast.LENGTH_SHORT).show()

                // Set our working task object to the newly-inserted task
                //task = helper.getTaskById(this, uri.lastPathSegment.toLong())

                // This is now an existing note
                existing = true;
            }
        }

        saveBtn.setOnLongClickListener {
            saveBtn.callOnClick()
            setResult(0, Intent().putExtra("op", "add"))

            var i: Intent

            finish()
            true
        }

        // Make everything _around_ the render switch toggle it too
        renderSwitchFrame.setOnClickListener { renderSwitch.toggle() }
        editIcon.setOnClickListener { renderSwitch.toggle() }
        renderIcon.setOnClickListener { renderSwitch.toggle() }

        // Render the markdown and hide the details when toggled
        renderSwitch.setOnCheckedChangeListener {_, b -> renderToggle(b) }

        /*

        // Failed attempts with interesting code:

        baseLayout.setOnApplyWindowInsetsListener{v: View, inset: WindowInsets ->
            var bottom = inset.systemWindowInsetBottom
            Log.d("WindowInsets", "Bottom: $bottom")
            inset
        }

        editorBox.setOnApplyWindowInsetsListener{v: View, inset: WindowInsets ->
            var bottom = inset.systemWindowInsetBottom
            Log.d("WindowInsets", "Bottom: $bottom")
            inset
        }

        editorBox.setOnFocusChangeListener {_, b ->
            if(b)
                detailsFrame.visibility = View.GONE
        }


        baseLayout.setOnFocusChangeListener{_, b ->
            if(b)
                detailsFrame.visibility = View.VISIBLE
            else
                detailsFrame.visibility = View.GONE
        }

        baseLayout.setOnFocusChangeListener { v, _ ->

            if(v.id == editorBox.id)
                detailsFrame.visibility = View.GONE
            else
                detailsFrame.visibility = View.VISIBLE

        }

         */


        // HACK: Detect the main editor height changing to catch keyboard opening/closing
        editorBox.onHeightChanged = fun(h: Int, oldH: Int) { onEditorHeightChanged(h, oldH) }

        // Calculate a checksum of title+text to detect changes on exit w/o knowing contents
        textHash = MessageDigest.getInstance("MD5").digest(editorBox.text.toString().toByteArray()).joinToString { "%02x".format(it) }

        /** Render the task if it already existed (i.e. was loaded)  **/
        if(existing)
            renderToggle(true)
        
        

    }

    /*
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("Touch Event: ", "(" + event?.x + ", " + event?.y + ')')
        return super.onTouchEvent(event)
    }
     */

    private fun quickValidateColor(text: String): Boolean {
        return text.length == 7 && text[0] == '#' && !text.contains(Regex("[g-zG-Z!@$%^&*()\\-_=/+\\[\\],.?<> \\n]"))
    }

    private fun renderToggle(checked: Boolean){
        if(checked){

            // Apply rendering to body
            val parsed = Color.parseColor("#" + Integer.toHexString(mAccentColor) ?: getString(R.string.default_note_color))
            Markwon.builder(this)
                .usePlugins(arrayListOf(StrikethroughPlugin.create(), ImagesPlugin.create(), LinkifyPlugin.create()))
                .usePlugin(TaskListPlugin.create(parsed, parsed, (baseLayout.background as ColorDrawable).color))
                .build()
                .setMarkdown(bodyView, editorBox.text.toString())

            // Set View text to title
            titleView.text = titleBox.text

            // Hide Boxes, show Views
            editorBox.visibility = View.INVISIBLE
            bodyView.visibility = View.VISIBLE

            titleBox.visibility = View.INVISIBLE
            titleView.visibility = View.VISIBLE
            titleView.bringToFront()

            accentBanner.visibility = View.VISIBLE
            accentColor.visibility = View.INVISIBLE

            // Hide details frame and other details
            detailsFrame.visibility = View.GONE
            listSpinner.visibility = View.GONE
            leftSideBtnFrame.visibility = View.GONE

            // Hide keyboard, if applicable
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(baseLayout.windowToken, 0)
            Log.d("KeyboardView Visible", "${KeyboardView.VISIBLE}")

            // Trim bodyFrame bottom margin
            val m = bodyFrame.layoutParams as ViewGroup.MarginLayoutParams
            m.setMargins(m.leftMargin, m.topMargin, m.rightMargin, m.bottomMargin - 167 * resources.displayMetrics.density.toInt())

        } else {
            // Show Boxes, hide Views
            editorBox.visibility = View.VISIBLE
            bodyView.visibility = View.INVISIBLE

            titleBox.visibility = View.VISIBLE
            titleBox.bringToFront()
            titleView.visibility = View.INVISIBLE

            accentColor.visibility = View.VISIBLE;
            accentBanner.visibility = View.INVISIBLE

            // Show details frame, etc.
            detailsFrame.visibility = View.VISIBLE
            listSpinner.visibility = View.VISIBLE
            leftSideBtnFrame.visibility = View.VISIBLE
            accentColor.visibility = View.VISIBLE

            // Edit constraints to set the layout_constraintBottom
            var c = ConstraintSet()
            c.clone(baseLayout)
            c.connect(bodyFrame.id,ConstraintSet.BOTTOM,buttonFrame.id,ConstraintSet.BOTTOM)
            c.applyTo(baseLayout)

            // Extend bodyFrame bottom margin
            val m = bodyFrame.layoutParams as ViewGroup.MarginLayoutParams
            m.setMargins(m.leftMargin, m.topMargin, m.rightMargin, m.bottomMargin + 167 * resources.displayMetrics.density.toInt())
        }
    }

    private fun applyStyleColor(color: Int){

        // All items have the background color set by the note/list color,
        // and foreground colors flip between light/dark depending on the background color
        //val parsed: Int = Color.parseColor(if(color != null) Integer.toHexString(color.toInt()) else R.color.defaultNoteColor.toString())
        val bgColor: Int = Color.parseColor("#" + Integer.toHexString(color))
        val fgColor = if(bgColor > Color.parseColor("#CCCCCC")) Color.BLACK else Color.WHITE


        // Backgrounds
        renderSwitch.thumbTintList = ColorStateList.valueOf(bgColor)
        renderSwitch.trackTintList = ColorStateList.valueOf(bgColor)

        progressBar.setBubbleColor(bgColor)
        progressBar.setSecondTrackColor(bgColor)
        progressBar.setThumbColor(bgColor)

        tabBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        checkboxBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        listBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        boldBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        italicBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        strikeBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        endBtn.backgroundTintList = ColorStateList.valueOf(bgColor)
        homeBtn.backgroundTintList = ColorStateList.valueOf(bgColor)

        accentColor.background = ColorDrawable(bgColor)
        accentBanner.background = ColorDrawable(bgColor)

        buttonSwitcher.buttonTintList = ColorStateList.valueOf(bgColor)

        // Foregrounds
        progressBar.configBuilder.bubbleTextColor(fgColor).build()

        tabBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        tabBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        checkboxBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        checkboxBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        checkboxBtn.setTextColor(fgColor)
        listBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        listBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        listBtn.setTextColor(fgColor)
        boldBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        boldBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        italicBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        italicBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        strikeBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        strikeBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        homeBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        homeBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        endBtn.foregroundTintList = ColorStateList.valueOf(fgColor)
        endBtn.foregroundTintMode = PorterDuff.Mode.SRC_ATOP
        buttonSwitcher.foregroundTintList = ColorStateList.valueOf(fgColor)
        buttonSwitcher.foregroundTintMode = PorterDuff.Mode.SRC_ATOP

    }

    private fun applyStylecolor(color: Color){ applyStyleColor(color.toArgb())}

    private fun getTaskValues(): ContentValues {
        return ContentValues().apply{
            put(TaskContract.Instances.LIST_ID, listSpinner.selectedItemId+1)
            put(TaskContract.Instances.TITLE, titleBox.text.toString())
            put(TaskContract.Instances.DESCRIPTION, editorBox.text.toString())
            put(TaskContract.Instances.PERCENT_COMPLETE, progressBar.progress.toInt())
            put(TaskContract.Instances.TASK_COLOR, mAccentColor )
            //put(TaskContract.Instances.LAST_MODIFIED, )
        }
    }

    private fun onEditorHeightChanged(h: Int, oldH: Int){
        if(oldH != 0 && h > oldH && oldH / h < 0.75 && h - oldH != detailsFramePadding && !renderSwitch.isChecked){
            // !!! Editor pane grew, presumably because we closed the keyboard !!!
            Log.d("editorBox", "Grew! $oldH -> $h")

            // Put bodyFrame bottom margin back
            val m = bodyFrame.layoutParams as ViewGroup.MarginLayoutParams
            m.setMargins(m.leftMargin, m.topMargin, m.rightMargin, m.bottomMargin + detailsFramePadding)

            // Can't seem to get this to redo the layout...
            baseLayout.rootView.invalidate()
            baseLayout.rootView.requestLayout()
            editorBox.invalidate()
            editorBox.requestLayout()

            // Make the details frame visible
            detailsFrame.visibility = View.VISIBLE

            // HACK: force the editor to redo layout (requestLayout() and invalidate() did nothing)
            /*
            if(editorBox.hasFocus()){
                val s = editorBox.selectionStart
                val e = editorBox.selectionEnd

                editorBox.setText(editorBox.text)
                // Move the cursor back to the end (causes an unfortunate "skip" or "jump")
                editorBox.setSelection(s, e)
            }
            else if(titleBox.hasFocus()){
                val s = titleBox.selectionStart
                val e = titleBox.selectionEnd
                titleBox.setText(titleBox.text)
                titleBox.setSelection(s, e)
            }

             */

            // Edit constraints to set the layout_constraintBottom
            var c = ConstraintSet()
            c.clone(baseLayout)
            c.connect(bodyFrame.id,ConstraintSet.BOTTOM,buttonFrame.id,ConstraintSet.BOTTOM)
            c.applyTo(baseLayout)

        }
        else if(oldH > h && oldH / h > 1.5 && oldH - h != detailsFramePadding){
            // !!! Editor pane shrank, presumably due to the keyboard !!!
            Log.d("editorBox", "Shrank! $oldH -> $h")

            // Hide the details frame
            detailsFrame.visibility = View.GONE

            // Trim bodyFrame bottom margin
            val m = bodyFrame.layoutParams as ViewGroup.MarginLayoutParams
            m.setMargins(m.leftMargin, m.topMargin, m.rightMargin, m.bottomMargin - detailsFramePadding)

            // HACK: force the editor to redo layout (requestLayout() and invalidate() did nothing)
            if(editorBox.hasFocus()){
                val s = editorBox.selectionStart
                val e = editorBox.selectionEnd

                editorBox.setText(editorBox.text)
                // Move the cursor back to the end (causes an unfortunate "skip" or "jump")
                editorBox.setSelection(s, e)
            }
            else if(titleBox.hasFocus()){
                val s = titleBox.selectionStart
                val e = titleBox.selectionEnd
                titleBox.setText(titleBox.text)
                titleBox.setSelection(s, e)
            }

        }
    }

}
