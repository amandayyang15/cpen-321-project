import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { expenseModel } from './expense.model';
import { projectModel } from '../projects/project.model';

export class ExpenseController {
  // Create a new expense for a project
  createExpense = async (req: Request, res: Response): Promise<void> => {
    try {
      const { projectId } = req.params;
      const { title, description, amount, splitUserIds } = req.body;
      const userId = req.user?._id.toString();

      console.log('=== CREATE EXPENSE REQUEST ===');
      console.log('Project ID:', projectId);
      console.log('User ID:', userId);
      console.log('Expense Data:', { title, description, amount, splitUserIds });

      if (!userId) {
        console.log('ERROR: Unauthorized - No user ID');
        res.status(401).json({ success: false, message: 'Unauthorized' });
        return;
      }

      // Validate project exists and user is a member
      const project = await projectModel.findById(new mongoose.Types.ObjectId(projectId));
      if (!project) {
        console.log('ERROR: Project not found:', projectId);
        res.status(404).json({ success: false, message: 'Project not found' });
        return;
      }

      console.log('Project found:', project.name);

      const isMember = project.members.some(
        member => member.userId.toString() === userId || project.ownerId.toString() === userId
      );

      if (!isMember) {
        console.log('ERROR: User is not a member of project');
        res.status(403).json({ success: false, message: 'You are not a member of this project' });
        return;
      }

      console.log('User is a project member - validation passed');

      // Validate all split users are project members
      const projectMemberIds = [
        project.ownerId.toString(),
        ...project.members.map(m => m.userId.toString())
      ];

      console.log('Project member IDs:', projectMemberIds);
      console.log('Requested split user IDs:', splitUserIds);

      const invalidUsers = splitUserIds.filter((id: string) => !projectMemberIds.includes(id));
      if (invalidUsers.length > 0) {
        console.log('ERROR: Invalid users not in project:', invalidUsers);
        res.status(400).json({
          success: false,
          message: 'Some users are not members of this project'
        });
        return;
      }

      console.log('All split users are valid project members');

      // Calculate equal split amount
      const amountPerPerson = amount / splitUserIds.length;
      console.log('Amount per person:', amountPerPerson);

      // Create expense splits
      const splits = splitUserIds.map((userId: string) => ({
        userId: new mongoose.Types.ObjectId(userId),
        amount: amountPerPerson,
        isPaid: false
      }));

      console.log('Created splits:', JSON.stringify(splits, null, 2));

      // Create expense
      console.log('Creating expense in database...');
      const expense = await expenseModel.create({
        projectId: new mongoose.Types.ObjectId(projectId),
        title,
        description,
        amount,
        createdBy: new mongoose.Types.ObjectId(userId),
        splits,
        status: 'pending'
      });

      console.log('✅ EXPENSE CREATED IN DATABASE:');
      console.log('  ID:', expense._id.toString());
      console.log('  Title:', expense.title);
      console.log('  Amount:', expense.amount);
      console.log('  Status:', expense.status);
      console.log('  Splits count:', expense.splits.length);
      console.log('  Created by:', expense.createdBy.toString());

      const populatedExpense = await expenseModel.findById(expense._id);
      console.log('Populated expense retrieved from database');

      res.status(201).json({
        success: true,
        message: 'Expense created successfully',
        data: populatedExpense
      });
      console.log('✅ Response sent to client with expense data');
      console.log('=== END CREATE EXPENSE ===\n');
    } catch (error) {
      console.error('❌ ERROR creating expense:', error);
      console.error('Error stack:', error instanceof Error ? error.stack : 'No stack trace');
      res.status(500).json({
        success: false,
        message: 'Failed to create expense',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
    }
  };

  // Get all expenses for a project
  getProjectExpenses = async (req: Request, res: Response): Promise<void> => {
    try {
      const { projectId } = req.params;
      const userId = req.user?._id.toString();

      console.log('=== GET PROJECT EXPENSES REQUEST ===');
      console.log('Project ID:', projectId);
      console.log('User ID:', userId);

      if (!userId) {
        console.log('ERROR: Unauthorized - No user ID');
        res.status(401).json({ success: false, message: 'Unauthorized' });
        return;
      }

      // Validate project exists and user is a member
      const project = await projectModel.findById(new mongoose.Types.ObjectId(projectId));
      if (!project) {
        console.log('ERROR: Project not found:', projectId);
        res.status(404).json({ success: false, message: 'Project not found' });
        return;
      }

      console.log('Project found:', project.name);

      const isMember = project.members.some(
        member => member.userId.toString() === userId || project.ownerId.toString() === userId
      );

      if (!isMember) {
        console.log('ERROR: User is not a member of project');
        res.status(403).json({ success: false, message: 'You are not a member of this project' });
        return;
      }

      console.log('Fetching expenses from database...');
      const expenses = await expenseModel.findByProjectId(new mongoose.Types.ObjectId(projectId));

      console.log('✅ EXPENSES RETRIEVED FROM DATABASE:');
      console.log('  Count:', expenses.length);
      if (expenses.length > 0) {
        console.log('  Expense IDs:', expenses.map(e => e._id.toString()));
        expenses.forEach((exp, index) => {
          console.log(`  [${index + 1}] ${exp.title} - $${exp.amount} (${exp.status})`);
        });
      }

      res.status(200).json({
        success: true,
        message: 'Expenses retrieved successfully',
        data: expenses
      });
      console.log('✅ Response sent to client with expenses data');
      console.log('=== END GET PROJECT EXPENSES ===\n');
    } catch (error) {
      console.error('❌ ERROR getting expenses:', error);
      console.error('Error stack:', error instanceof Error ? error.stack : 'No stack trace');
      res.status(500).json({
        success: false,
        message: 'Failed to get expenses',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
    }
  };

  // Mark a split as paid
  markSplitPaid = async (req: Request, res: Response): Promise<void> => {
    try {
      const { projectId, expenseId } = req.params;
      const { userId: splitUserId, isPaid } = req.body;
      const currentUserId = req.user?._id.toString();

      console.log('=== MARK SPLIT PAID REQUEST ===');
      console.log('Project ID:', projectId);
      console.log('Expense ID:', expenseId);
      console.log('Split User ID:', splitUserId);
      console.log('Is Paid:', isPaid);
      console.log('Current User ID:', currentUserId);

      if (!currentUserId) {
        console.log('ERROR: Unauthorized - No user ID');
        res.status(401).json({ success: false, message: 'Unauthorized' });
        return;
      }

      // Validate project membership
      const project = await projectModel.findById(new mongoose.Types.ObjectId(projectId));
      if (!project) {
        console.log('ERROR: Project not found:', projectId);
        res.status(404).json({ success: false, message: 'Project not found' });
        return;
      }

      console.log('Project found:', project.name);

      const isMember = project.members.some(
        member => member.userId.toString() === currentUserId || project.ownerId.toString() === currentUserId
      );

      if (!isMember) {
        console.log('ERROR: User is not a member of project');
        res.status(403).json({ success: false, message: 'You are not a member of this project' });
        return;
      }

      console.log('Updating split in database...');
      // Update the split
      const updatedExpense = await expenseModel.updateSplit(
        new mongoose.Types.ObjectId(expenseId),
        new mongoose.Types.ObjectId(splitUserId),
        isPaid
      );

      if (!updatedExpense) {
        console.log('ERROR: Expense not found:', expenseId);
        res.status(404).json({ success: false, message: 'Expense not found' });
        return;
      }

      console.log('✅ Split updated in database');

      // Update expense status based on all splits
      console.log('Checking if all splits are paid to update expense status...');
      const finalExpense = await expenseModel.updateExpenseStatus(new mongoose.Types.ObjectId(expenseId));

      console.log('✅ EXPENSE SPLIT UPDATED:');
      console.log('  Expense ID:', finalExpense?._id.toString());
      console.log('  Status:', finalExpense?.status);
      console.log('  All splits:', finalExpense?.splits.map(s => ({
        userId: s.userId.toString(),
        amount: s.amount,
        isPaid: s.isPaid
      })));

      res.status(200).json({
        success: true,
        message: 'Split status updated successfully',
        data: finalExpense
      });
      console.log('✅ Response sent to client');
      console.log('=== END MARK SPLIT PAID ===\n');
    } catch (error) {
      console.error('❌ ERROR updating split:', error);
      console.error('Error stack:', error instanceof Error ? error.stack : 'No stack trace');
      res.status(500).json({
        success: false,
        message: 'Failed to update split',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
    }
  };

  // Delete an expense
  deleteExpense = async (req: Request, res: Response): Promise<void> => {
    try {
      const { projectId, expenseId } = req.params;
      const userId = req.user?._id.toString();

      console.log('=== DELETE EXPENSE REQUEST ===');
      console.log('Project ID:', projectId);
      console.log('Expense ID:', expenseId);
      console.log('User ID:', userId);

      if (!userId) {
        console.log('ERROR: Unauthorized - No user ID');
        res.status(401).json({ success: false, message: 'Unauthorized' });
        return;
      }

      console.log('Fetching expense from database...');
      // Validate expense exists
      const expense = await expenseModel.findById(new mongoose.Types.ObjectId(expenseId));
      if (!expense) {
        console.log('ERROR: Expense not found:', expenseId);
        res.status(404).json({ success: false, message: 'Expense not found' });
        return;
      }

      console.log('Expense found:', expense.title);
      console.log('Expense created by:', expense.createdBy.toString());

      // Check if user is the creator or project owner
      const project = await projectModel.findById(new mongoose.Types.ObjectId(projectId));
      if (!project) {
        console.log('ERROR: Project not found:', projectId);
        res.status(404).json({ success: false, message: 'Project not found' });
        return;
      }

      console.log('Project found:', project.name);

      const isCreator = expense.createdBy.toString() === userId;
      const isOwner = project.ownerId.toString() === userId;

      console.log('Is creator?', isCreator);
      console.log('Is owner?', isOwner);

      if (!isCreator && !isOwner) {
        console.log('ERROR: User is neither creator nor owner');
        res.status(403).json({
          success: false,
          message: 'Only the expense creator or project owner can delete this expense'
        });
        return;
      }

      console.log('Deleting expense from database...');
      await expenseModel.delete(new mongoose.Types.ObjectId(expenseId));

      console.log('✅ EXPENSE DELETED FROM DATABASE');
      console.log('  Deleted expense ID:', expenseId);

      res.status(200).json({
        success: true,
        message: 'Expense deleted successfully'
      });
      console.log('✅ Response sent to client');
      console.log('=== END DELETE EXPENSE ===\n');
    } catch (error) {
      console.error('❌ ERROR deleting expense:', error);
      console.error('Error stack:', error instanceof Error ? error.stack : 'No stack trace');
      res.status(500).json({
        success: false,
        message: 'Failed to delete expense',
        error: error instanceof Error ? error.message : 'Unknown error'
      });
    }
  };
}
