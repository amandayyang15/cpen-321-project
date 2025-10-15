import { Router } from 'express';
import { ExpenseController } from '../features/expenses/expense.controller';
import { validateBody } from '../middleware/validation.middleware';
import { z } from 'zod';

const router = Router({ mergeParams: true }); // Add mergeParams to access parent router params
const expenseController = new ExpenseController();

// Validation schemas
const createExpenseSchema = z.object({
  title: z.string().min(1, 'Title is required').max(200, 'Title must be less than 200 characters'),
  description: z.string().max(1000, 'Description must be less than 1000 characters').optional(),
  amount: z.number().min(0.01, 'Amount must be at least 0.01'),
  splitUserIds: z.array(z.string()).min(1, 'At least one user must be selected for split')
});

const markSplitPaidSchema = z.object({
  userId: z.string().min(1, 'User ID is required'),
  isPaid: z.boolean()
});

// Routes
// All routes are under /projects/:projectId/expenses
router.post('/', validateBody(createExpenseSchema), expenseController.createExpense);
router.get('/', expenseController.getProjectExpenses);
router.patch('/:expenseId/splits', validateBody(markSplitPaidSchema), expenseController.markSplitPaid);
router.delete('/:expenseId', expenseController.deleteExpense);

export default router;
