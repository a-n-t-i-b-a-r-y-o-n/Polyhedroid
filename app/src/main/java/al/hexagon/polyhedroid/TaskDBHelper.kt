package al.hexagon.polyhedroid

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import org.dmfs.provider.tasks.TaskContract

class TaskDBHelper {

    private val TASK_PROJECTION: Array<String> = arrayOf(
        TaskContract.Instances._ID,
        TaskContract.Instances.ACCOUNT_NAME,
        TaskContract.Instances.LIST_ID,
        TaskContract.Instances.LIST_NAME,
        TaskContract.Instances.CLASSIFICATION,
        TaskContract.Instances.COMPLETED,
        TaskContract.Instances.STATUS,
        TaskContract.Instances.PERCENT_COMPLETE,
        TaskContract.Instances.TITLE,
        TaskContract.Instances.DESCRIPTION,
        TaskContract.Instances.TASK_COLOR,
        TaskContract.Instances.LAST_MODIFIED
    )

    /*
    // HACK: For some reason, inserting new tasks doesn't return the correct task ID if you've deleted any recently. This returns the most-recent one (that you've just inserted)
    fun getNewestTask(context: Context) : TaskItem {
        var task: TaskItem = TaskItem(-1, "", 0, "", 0, null, 0, 0, "", null, null, null)

        //val uri: Uri = TaskContract.Instances.getContentUri()
    }

     */

    fun getTaskById(context: Context, taskID: Long) : TaskItem {
        var task: TaskItem = TaskItem(-1, "", 0, "", 0, null, 0, 0, "", null, null, null)


        val uri: Uri = TaskContract.Instances.getContentUri("org.dmfs.tasks")
        val selection = TaskContract.Instances._ID + '=' + taskID + " AND " + TaskContract.Instances.VISIBLE + " = 1"
        val c: Cursor? = context.contentResolver.query(uri, TASK_PROJECTION, selection, null, TaskContract.Instances._ID + " ASC")

        if(c != null){
            c.moveToFirst()
            task = TaskItem(
                // id
                c.getLong(0),
                // account
                c.getString(1),
                // list ID
                c.getLong(2),
                // list name
                c.getString(3),
                // classification
                c.getInt(4),
                // completed
                c.getInt(5),
                // status
                c.getInt(6),
                // percent complete
                c.getInt(7),
                // title
                c.getString(8),
                // description
                c.getString(9),
                // color
                c.getString(10),
                // last modified
                c.getString(11)
            )
            c.close()
        } else {
            Log.d("getTask()", "Cursor is null")
        }

        return task
    }

    fun getTasksByList(context: Context, listID: Long, includeComplete: Boolean, includeCancelled: Boolean) : ArrayList<TaskItem> {

        var tasks = ArrayList<TaskItem>()

        TaskContract.Instances.STATUS_CANCELLED

        val uri: Uri = TaskContract.Instances.getContentUri("org.dmfs.tasks")
        var selection = TaskContract.Instances.LIST_ID + '=' + listID + " AND " + TaskContract.Instances.VISIBLE + "=1"
        if(!includeComplete)
            selection += " AND " + TaskContract.Instances.STATUS + "<>" + TaskContract.Instances.STATUS_COMPLETED
        if(!includeCancelled)
            selection += " AND " + TaskContract.Instances.STATUS + "<>" + TaskContract.Instances.STATUS_CANCELLED
        val c: Cursor? = context.contentResolver.query(uri, TASK_PROJECTION, selection, null, TaskContract.Instances.LAST_MODIFIED + " DESC")

        if(c != null){
            while(c.moveToNext()){
                tasks.add(
                    TaskItem(
                        // id
                        c.getLong(0),
                        // account
                        c.getString(1),
                        // list ID
                        c.getLong(2),
                        // list name
                        c.getString(3),
                        // classification
                        c.getInt(4),
                        // completed
                        c.getInt(5),
                        // status
                        c.getInt(6),
                        // percent complete
                        c.getInt(7),
                        // title
                        c.getString(8),
                        // description
                        c.getString(9),
                        // color
                        c.getString(10),
                        // last modified
                        c.getString(11)
                    )
                )

                //Log.d("Task color", "${c.getString(8)}: ${c.getString(10)}")
            }
            c.close()
        } else {
            Log.d("getTasks()", "Cursor is null")
        }

        return tasks
    }

    fun getTaskLists(context: Context): ArrayList<TaskList> {

        // Get task lists

        var list = ArrayList<TaskList>()

        val TASKLIST_PROJECTION: Array<String> = arrayOf(
            TaskContract.TaskLists._ID,
            TaskContract.TaskLists.ACCOUNT_NAME,
            TaskContract.TaskLists.OWNER,
            TaskContract.TaskLists.LIST_NAME,
            TaskContract.TaskLists.LIST_COLOR
        )

        val uri: Uri = TaskContract.TaskLists.getContentUri("org.dmfs.tasks")

        val selection = TaskContract.TaskLists.VISIBLE + " = 1"
        val c: Cursor? = context.contentResolver.query(uri, TASKLIST_PROJECTION, selection, null, TaskContract.TaskLists._ID + " ASC")

        if(c != null){
            while(c.moveToNext()){
                list.add(
                    TaskList(
                        c.getLong(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4)
                    )
                )
                //Log.d("List Color", "${c.getString(3)}: ${c.getString(4)}")
            }

            c.close()
        }
        else {
            Log.d("getTaskLists()", "Cursor is null")
        }

        return list
    }
}