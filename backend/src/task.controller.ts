import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { taskModel } from './task.model';
import logger from './logger.util';

export class TaskController {
  async createTask(req: Request, res: Response): Promise<void> {
    try {
      const { projectId } = req.params;
      const { name, assignee, status, deadline } = req.body;
      const userId = req.user?.id;

      logger.info('=== CREATE TASK REQUEST ===');
      logger.info('Project ID:', projectId);
      logger.info('User ID:', userId);
      logger.info('Task Data:', { name, assignee, status, deadline });
      logger.info('Request headers:', req.headers);
      logger.info('Request body:', JSON.stringify(req.body, null, 2));

      if (!userId) {
        logger.error('❌ User not authenticated');
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      // Validate required fields
      if (!name || !assignee || !status) {
        logger.error('❌ Missing required fields:', { name, assignee, status });
        res.status(400).json({ success: false, message: 'Missing required fields' });
        return;
      }

      // Validate ObjectId formats
      try {
        const projectObjectId = new mongoose.Types.ObjectId(projectId);
        const assigneeObjectId = new mongoose.Types.ObjectId(assignee);
        const userObjectId = new mongoose.Types.ObjectId(userId);
        
        logger.info('✅ ObjectIds validated successfully');
        logger.info('Project ObjectId:', projectObjectId.toString());
        logger.info('Assignee ObjectId:', assigneeObjectId.toString());
        logger.info('User ObjectId:', userObjectId.toString());
      } catch (objectIdError) {
        logger.error('❌ Invalid ObjectId format:', objectIdError);
        res.status(400).json({ success: false, message: 'Invalid ID format' });
        return;
      }

      const taskData = {
        projectId: new mongoose.Types.ObjectId(projectId),
        title: name,
        assignees: [new mongoose.Types.ObjectId(assignee)],
        status,
        createdBy: new mongoose.Types.ObjectId(userId),
        deadline: deadline ? new Date(deadline) : undefined,
      };

      logger.info('📝 Creating task with data:', JSON.stringify(taskData, null, 2));

      const task = await taskModel.create(taskData);

      logger.info('✅ Task created successfully in database');
      logger.info('Task ID:', task._id.toString());
      logger.info('Task details:', JSON.stringify(task, null, 2));
      
      res.status(201).json({ success: true, data: task });
    } catch (error) {
      logger.error('❌ Error creating task:', error);
      logger.error('Error stack:', error.stack);
      res.status(500).json({ success: false, message: 'Failed to create task' });
    }
  }

  async getTasksByProject(req: Request, res: Response): Promise<void> {
    try {
      const { projectId } = req.params;
      const userId = req.user?.id;

      logger.info('=== GET TASKS BY PROJECT REQUEST ===');
      logger.info('Project ID:', projectId);
      logger.info('User ID:', userId);

      if (!userId) {
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      const tasks = await taskModel.findByProjectId(new mongoose.Types.ObjectId(projectId));
      
      logger.info('✅ Tasks retrieved:', tasks.length);
      res.status(200).json({ success: true, data: tasks });
    } catch (error) {
      logger.error('❌ Error getting tasks:', error);
      res.status(500).json({ success: false, message: 'Failed to get tasks' });
    }
  }

  async getTaskById(req: Request, res: Response): Promise<void> {
    try {
      const { taskId } = req.params;
      const userId = req.user?.id;

      logger.info('=== GET TASK BY ID REQUEST ===');
      logger.info('Task ID:', taskId);
      logger.info('User ID:', userId);

      if (!userId) {
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      const task = await taskModel.findById(new mongoose.Types.ObjectId(taskId));
      
      if (!task) {
        res.status(404).json({ success: false, message: 'Task not found' });
        return;
      }

      logger.info('✅ Task retrieved:', task._id.toString());
      res.status(200).json({ success: true, data: task });
    } catch (error) {
      logger.error('❌ Error getting task:', error);
      res.status(500).json({ success: false, message: 'Failed to get task' });
    }
  }

  async updateTask(req: Request, res: Response): Promise<void> {
    try {
      const { taskId } = req.params;
      const { title, description, status, deadline, assignees } = req.body;
      const userId = req.user?.id;

      logger.info('=== UPDATE TASK REQUEST ===');
      logger.info('Task ID:', taskId);
      logger.info('User ID:', userId);
      logger.info('Update Data:', { title, description, status, deadline, assignees });

      if (!userId) {
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      const updateData: any = {};
      if (title) updateData.title = title;
      if (description !== undefined) updateData.description = description;
      if (status) updateData.status = status;
      if (deadline) updateData.deadline = new Date(deadline);
      if (assignees) updateData.assignees = assignees.map((id: string) => new mongoose.Types.ObjectId(id));

      const task = await taskModel.update(new mongoose.Types.ObjectId(taskId), updateData);
      
      if (!task) {
        res.status(404).json({ success: false, message: 'Task not found' });
        return;
      }

      logger.info('✅ Task updated:', task._id.toString());
      res.status(200).json({ success: true, data: task });
    } catch (error) {
      logger.error('❌ Error updating task:', error);
      res.status(500).json({ success: false, message: 'Failed to update task' });
    }
  }

  async deleteTask(req: Request, res: Response): Promise<void> {
    try {
      const { taskId } = req.params;
      const userId = req.user?.id;

      logger.info('=== DELETE TASK REQUEST ===');
      logger.info('Task ID:', taskId);
      logger.info('User ID:', userId);

      if (!userId) {
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      await taskModel.delete(new mongoose.Types.ObjectId(taskId));

      logger.info('✅ Task deleted:', taskId);
      res.status(200).json({ success: true, message: 'Task deleted successfully' });
    } catch (error) {
      logger.error('❌ Error deleting task:', error);
      res.status(500).json({ success: false, message: 'Failed to delete task' });
    }
  }

  async getAllTasks(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?.id;

      logger.info('=== GET ALL TASKS DEBUG REQUEST ===');
      logger.info('User ID:', userId);

      if (!userId) {
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      // Get all tasks from database (for debugging)
      const allTasks = await taskModel.task.find({}).populate('projectId', 'name').populate('assignees', 'name').populate('createdBy', 'name');
      
      logger.info('🔍 All tasks in database:', allTasks.length);
      logger.info('📋 Tasks details:', JSON.stringify(allTasks, null, 2));
      
      res.status(200).json({ success: true, data: allTasks, count: allTasks.length });
    } catch (error) {
      logger.error('❌ Error getting all tasks:', error);
      res.status(500).json({ success: false, message: 'Failed to get all tasks' });
    }
  }
}

export const taskController = new TaskController();
