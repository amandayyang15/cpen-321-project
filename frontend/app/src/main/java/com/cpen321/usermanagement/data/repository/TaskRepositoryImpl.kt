class TaskRepositoryImpl @Inject constructor(
    private val taskInterface: TaskInterface
) : TaskRepository {
    override suspend fun createTask(
        projectId: String,
        name: String,
        assignee: String,
        status: String,
        deadline: String?
    ): Task {
        val request = CreateTaskRequest(name, assignee, status, deadline)
        val response = taskInterface.createTask(projectId, request)
        if (response.isSuccessful && response.body()?.data != null) {
            return response.body()!!.data!!
        } else {
            throw Exception(response.body()?.error ?: "Failed to create task")
        }
    }
}
