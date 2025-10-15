import mongoose, { Schema, Document } from 'mongoose';

export interface ITask extends Document {
  _id: mongoose.Types.ObjectId;
  projectId: mongoose.Types.ObjectId;
  title: string;
  description?: string;
  status: 'not_started' | 'in_progress' | 'completed' | 'blocked' | 'backlog';
  assignees: mongoose.Types.ObjectId[];
  createdBy: mongoose.Types.ObjectId;
  deadline?: Date;
  createdAt: Date;
  updatedAt: Date;
  calendarEventId?: string;
}

const taskSchema = new Schema<ITask>(
  {
    projectId: {
      type: Schema.Types.ObjectId,
      ref: 'Project',
      required: true,
      index: true,
    },
    title: {
      type: String,
      required: true,
      trim: true,
      maxlength: 200,
    },
    description: {
      type: String,
      trim: true,
      maxlength: 2000,
    },
    status: {
      type: String,
      enum: ['not_started', 'in_progress', 'completed', 'blocked', 'backlog'],
      default: 'not_started',
      index: true,
    },
    assignees: [{
      type: Schema.Types.ObjectId,
      ref: 'User',
      index: true,
    }],
    createdBy: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    deadline: {
      type: Date,
      index: true,
    },
    calendarEventId: {
      type: String,
      index: true,
    },
  },
  {
    timestamps: true,
  }
);

// Create indexes for performance
taskSchema.index({ projectId: 1, createdAt: -1 });
taskSchema.index({ assignees: 1 });
taskSchema.index({ deadline: 1 });
taskSchema.index({ status: 1 });
taskSchema.index({ calendarEventId: 1 });

export class TaskModel {
  private task: mongoose.Model<ITask>;

  constructor() {
    this.task = mongoose.model<ITask>('Task', taskSchema);
  }

  async create(taskData: Partial<ITask>): Promise<ITask> {
    try {
      return await this.task.create(taskData);
    } catch (error) {
      console.error('Error creating task:', error);
      throw new Error('Failed to create task');
    }
  }

  async findById(taskId: mongoose.Types.ObjectId): Promise<ITask | null> {
    try {
      return await this.task.findById(taskId)
        .populate('assignees', 'name email profilePicture')
        .populate('createdBy', 'name email profilePicture');
    } catch (error) {
      console.error('Error finding task by ID:', error);
      throw new Error('Failed to find task');
    }
  }

  async findByProjectId(projectId: mongoose.Types.ObjectId): Promise<ITask[]> {
    try {
      return await this.task.find({ projectId })
        .populate('assignees', 'name email profilePicture')
        .populate('createdBy', 'name email profilePicture')
        .sort({ createdAt: -1 });
    } catch (error) {
      console.error('Error finding tasks by project:', error);
      throw new Error('Failed to find tasks');
    }
  }

  async findByAssignee(assigneeId: mongoose.Types.ObjectId): Promise<ITask[]> {
    try {
      return await this.task.find({ assignees: assigneeId })
        .populate('projectId', 'name')
        .populate('assignees', 'name email profilePicture')
        .populate('createdBy', 'name email profilePicture')
        .sort({ deadline: 1 });
    } catch (error) {
      console.error('Error finding tasks by assignee:', error);
      throw new Error('Failed to find tasks');
    }
  }

  async findByStatus(status: string, projectId?: mongoose.Types.ObjectId): Promise<ITask[]> {
    try {
      const query: any = { status };
      if (projectId) {
        query.projectId = projectId;
      }
      
      return await this.task.find(query)
        .populate('assignees', 'name email profilePicture')
        .populate('createdBy', 'name email profilePicture')
        .sort({ createdAt: -1 });
    } catch (error) {
      console.error('Error finding tasks by status:', error);
      throw new Error('Failed to find tasks');
    }
  }

  async findUpcomingDeadlines(days: number = 7): Promise<ITask[]> {
    try {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + days);
      
      return await this.task.find({
        deadline: { $lte: futureDate, $gte: new Date() },
        status: { $nin: ['completed'] }
      })
        .populate('projectId', 'name')
        .populate('assignees', 'name email profilePicture')
        .populate('createdBy', 'name email profilePicture')
        .sort({ deadline: 1 });
    } catch (error) {
      console.error('Error finding upcoming deadlines:', error);
      throw new Error('Failed to find upcoming deadlines');
    }
  }

  async update(taskId: mongoose.Types.ObjectId, updateData: Partial<ITask>): Promise<ITask | null> {
    try {
      return await this.task.findByIdAndUpdate(taskId, updateData, { new: true })
        .populate('assignees', 'name email profilePicture')
        .populate('createdBy', 'name email profilePicture');
    } catch (error) {
      console.error('Error updating task:', error);
      throw new Error('Failed to update task');
    }
  }

  async delete(taskId: mongoose.Types.ObjectId): Promise<void> {
    try {
      await this.task.findByIdAndDelete(taskId);
    } catch (error) {
      console.error('Error deleting task:', error);
      throw new Error('Failed to delete task');
    }
  }

  async addAssignee(taskId: mongoose.Types.ObjectId, assigneeId: mongoose.Types.ObjectId): Promise<ITask | null> {
    try {
      return await this.task.findByIdAndUpdate(
        taskId,
        { $addToSet: { assignees: assigneeId } },
        { new: true }
      ).populate('assignees', 'name email profilePicture');
    } catch (error) {
      console.error('Error adding assignee to task:', error);
      throw new Error('Failed to add assignee to task');
    }
  }

  async removeAssignee(taskId: mongoose.Types.ObjectId, assigneeId: mongoose.Types.ObjectId): Promise<ITask | null> {
    try {
      return await this.task.findByIdAndUpdate(
        taskId,
        { $pull: { assignees: assigneeId } },
        { new: true }
      ).populate('assignees', 'name email profilePicture');
    } catch (error) {
      console.error('Error removing assignee from task:', error);
      throw new Error('Failed to remove assignee from task');
    }
  }
}

export const taskModel = new TaskModel();
