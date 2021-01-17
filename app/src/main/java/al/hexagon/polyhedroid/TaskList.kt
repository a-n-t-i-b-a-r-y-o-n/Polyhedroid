package al.hexagon.polyhedroid

public class TaskList(var id: Long, var account: String, var owner: String, var name: String, var color: String?) {

    var tasks: ArrayList<TaskItem> = ArrayList()

    override fun toString(): String {
        return name
    }

}