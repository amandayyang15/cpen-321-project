import { Router } from 'express';
import { taskController } from './task.controller';
import { authenticateToken } from './auth.middleware';

const router = Router({ mergeParams: true });

// Debug route to get all tasks
router.get(
  '/debug/all',
  authenticateToken,
  (req, res) => taskController.getAllTasks(req, res)
);

// Individual task routes
router.get(
  '/:taskId',
  authenticateToken,
  (req, res) => taskController.getTaskById(req, res)
);

router.put(
  '/:taskId',
  authenticateToken,
  (req, res) => taskController.updateTask(req, res)
);

router.delete(
  '/:taskId',
  authenticateToken,
  (req, res) => taskController.deleteTask(req, res)
);

export default router;
