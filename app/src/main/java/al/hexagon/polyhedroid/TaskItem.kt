package al.hexagon.polyhedroid

public class TaskItem(var id: Long, var account: String = "", var lID: Long, var lName: String? = "", var classification: Int? = 1, var completed: Int? = 0, var status: Int, var percent: Int, var title: String, var description: String? = "", var color: String? = "", var modified: String? = "")

val Statuses = arrayOf("Needs Action", "In Progress", "Complete", "Cancelled")