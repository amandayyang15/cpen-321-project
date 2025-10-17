import { Router } from 'express';

import { expenseController } from '../features/expenses/expense.controller';

const router = Router();

// Create expense
router.post('/', expenseController.createExpense);

// Get expenses for a project
router.get('/project/:projectId', expenseController.getProjectExpenses);

// Delete expense
router.delete('/:expenseId', expenseController.deleteExpense);

export default router;
