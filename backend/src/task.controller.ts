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

      if (!userId) {
        res.status(401).json({ message: 'User not authenticated' });
        return;
      }

      const task = await taskModel.create({
        projectId: new mongoose.Types.ObjectId(projectId),
        title: name,
        assignees: [new mongoose.Types.ObjectId(assignee)],
        status,
        createdBy: new mongoose.Types.ObjectId(userId),
        deadline: deadline ? new Date(deadline) : undefined,
      });

      logger.info('✅ Task created in database:', task._id.toString());
      res.status(201).json({ success: true, data: task });
    } catch (error) {
      logger.error('❌ Error creating task:', error);
      res.status(500).json({ success: false, message: 'Failed to create task' });
    }
  }
}

export const taskController = new TaskController();
