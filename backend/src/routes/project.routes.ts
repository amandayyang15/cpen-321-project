import { Router } from 'express';
import { ProjectController } from '../features/projects/project.controller';
import { validateBody } from '../middleware/validation.middleware';
import { z } from 'zod';
import expenseRoutes from './expense.routes';

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

const joinProjectSchema = z.object({
  invitationCode: z.string().min(1, 'Invitation code is required').max(8, 'Invitation code must be 8 characters or less')
});

const addResourceSchema = z.object({
  resourceName: z.string().min(1, 'Resource name is required').max(200, 'Resource name must be less than 200 characters'),
  link: z.string().min(1, 'Resource link is required').max(500, 'Resource link must be less than 500 characters')
});

// Routes
router.post('/', validateBody(createProjectSchema), projectController.createProject);
router.post('/join', validateBody(joinProjectSchema), projectController.joinProject);
router.get('/', projectController.getUserProjects);
router.get('/:projectId', projectController.getProjectById);
router.put('/:projectId', validateBody(updateProjectSchema), projectController.updateProject);
router.delete('/:projectId', projectController.deleteProject);
router.post('/:projectId/resources', validateBody(addResourceSchema), projectController.addResource);

// Expense routes (nested under projects)
router.use('/:projectId/expenses', expenseRoutes);

export default router;
