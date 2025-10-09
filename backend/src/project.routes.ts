import { Router } from 'express';
import { ProjectController } from './project.controller';
import { validateBody } from './validation.middleware';
import { z } from 'zod';

const router = Router();
const projectController = new ProjectController();

// Validation schemas
const createProjectSchema = z.object({
  name: z.string().min(1, 'Project name is required').max(100, 'Project name must be less than 100 characters'),
  description: z.string().max(1000, 'Description must be less than 1000 characters').optional()
});

const updateProjectSchema = z.object({
  name: z.string().min(1, 'Project name is required').max(100, 'Project name must be less than 100 characters').optional(),
  description: z.string().max(1000, 'Description must be less than 1000 characters').optional()
});

// Routes
router.post('/', validateBody(createProjectSchema), projectController.createProject);
router.get('/', projectController.getUserProjects);
router.get('/:projectId', projectController.getProjectById);
router.put('/:projectId', validateBody(updateProjectSchema), projectController.updateProject);
router.delete('/:projectId', projectController.deleteProject);

export default router;
