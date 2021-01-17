package al.hexagon.polyhedroid

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.NinePatchDrawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

import com.h6ah4i.android.widget.advrecyclerview.adapter.*
import com.h6ah4i.android.widget.advrecyclerview.composedadapter.ComposedAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import kotlinx.android.synthetic.main.activity_task_entry.*
import org.dmfs.provider.tasks.TaskContract

class MainActivity : AppCompatActivity() {

    private lateinit var db: TaskDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Check our privilege (heh...)
        if(ActivityCompat.checkSelfPermission(this, "org.dmfs.permission.READ_TASKS") != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, "org.dmfs.permission.WRITE_TASKS") != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf("org.dmfs.permission.READ_TASKS", "org.dmfs.permission.WRITE_TASKS"), 0)
        }

        //var adapter = TaskListAdapter(this, taskCardList)
        cardLayout.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)


        //cardLayout.adapter = TaskListAdapter(this)

        // Required to fix animations with Advanced Recycler View
        (cardLayout.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        db = TaskDBHelper()


        //cardLayout.adapter = TaskListAdapter(this)

        fab.setOnClickListener { startActivityForResult(Intent(this, TaskEntry::class.java), 0) }

        refreshLayout.setOnRefreshListener {
            //cardLayout.adapter = TaskListAdapter(this)
            //cardLayout.startLayoutAnimation()
            //(cardLayout.adapter as TaskListAdapter).notifyDataSetChanged()
            //(cardLayout.adapter as TaskListAdapter).refreshList()
            //(cardLayout.adapter as ListHeaderAdapter).notifyDataSetChanged()
            cardLayout.invalidate()
            refreshLayout.isRefreshing = false
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        var m = menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when(item.itemId){
            R.id.action_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), 0)
        }
        return when (item.itemId) {
            R.id.action_settings -> true
            //R.id.showComplete -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)



        if(data != null && resultCode != -1 && data.extras != null && data.extras!!.getString("op") != null) {

            Log.d("onActivityResult", "${data!!.extras!!.getString("op")}")


            cardLayout.adapter = TaskListAdapter(this)

            when(data.extras!!.getString("op")){
                "update" -> (cardLayout.adapter as TaskListAdapter).notifyItemChanged(data.extras!!.getInt("pos"))
                "add","delete","settings" -> (cardLayout.adapter as TaskListAdapter).notifyDataSetChanged()
            }
        }
        else {
            // refresh anyways?
            //cardLayout.adapter = TaskListAdapter(this)
            //(cardLayout.adapter as TaskListAdapter).notifyDataSetChanged()
        }

    }

     */

    override fun onResume() {
        super.onResume()

        /*
        // This seems heavy-handed, but it's the only thing that actually worked
        cardLayout.adapter = TaskListAdapter(this)
        (cardLayout.adapter as TaskListAdapter).notifyDataSetChanged()

         */

        // This seems heavy-handed, but it's the only thing that actually worked
        var lists: ArrayList<TaskList> = db.getTaskLists(this)
        val p = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        for(l in lists){
            l.tasks = db.getTasksByList(this, l.id, p.getBoolean("showComplete", false), p.getBoolean("showCancelled", false))

            /*
            // Hack to fix all the colors
            for(t in l.tasks){
                val uri: Uri = ContentUris.withAppendedId(TaskContract.Instances.getContentUri("org.dmfs.tasks"), t.id)
                val rows: Int = contentResolver.update(uri, ContentValues().apply{
                    put(TaskContract.Instances.LIST_ID, t.lID)
                    put(TaskContract.Instances.TITLE, t.title)
                    put(TaskContract.Instances.DESCRIPTION, t.description)
                    put(TaskContract.Instances.PERCENT_COMPLETE, t.percent)
                    put(TaskContract.Instances.TASK_COLOR, "" )
                }, null, null)
            }

             */

        }
        var expMgr = RecyclerViewExpandableItemManager(null)
        expMgr.defaultGroupsExpandedState = true

        var wrappedAdapter = expMgr.createWrappedAdapter(ListHeaderAdapter(this, lists, expMgr))

        cardLayout.adapter = wrappedAdapter

        /*

        This is unused for now - it didn't go away if the RecyclerView populated asynchronously

        // Draw a random "table flip" emoji with a hint if there are no tasks to display
        if(cardLayout.childCount == 0) {
            NoTasksHint.text = getString(R.string.no_tasks_hint) + '\n' + getString(R.string.table_flip_1 + ((Math.random() * 10).toInt() % 10))
            NoTasksHint.visibility = View.VISIBLE
        } else {
            NoTasksHint.visibility = View.GONE
        }

        */

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // do nothing for now
        when(requestCode){
            0 -> return
        }
    }

    override fun onStart() {
        super.onStart()

    }



}
