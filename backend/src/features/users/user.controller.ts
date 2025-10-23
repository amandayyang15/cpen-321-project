import { NextFunction, Request, Response } from 'express';
import mongoose from 'mongoose';

import { GetProfileResponse, UpdateProfileRequest } from './user.types';
import logger from '../../utils/logger.util';
import { MediaService } from '../media/media.service';
import { userModel } from './user.model';

export class UserController {
  getProfile(req: Request, res: Response<GetProfileResponse>) {
    const user = req.user!;

    res.status(200).json({
      message: 'Profile fetched successfully',
      data: { user },
    });
  }

  async getUserById(req: Request, res: Response<GetProfileResponse>, next: NextFunction) {
    try {
      const { userId } = req.params;

      if (!userId) {
        return res.status(400).json({
          message: 'User ID is required',
        });
      }

      // Validate if userId is a valid ObjectId
      if (!mongoose.Types.ObjectId.isValid(userId)) {
        return res.status(400).json({
          message: 'Invalid user ID format',
        });
      }

      const user = await userModel.findById(new mongoose.Types.ObjectId(userId));

      if (!user) {
        return res.status(404).json({
          message: 'User not found',
        });
      }

      res.status(200).json({
        message: 'User fetched successfully',
        data: { user },
      });
    } catch (error) {
      logger.error('Failed to fetch user by ID:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to fetch user',
        });
      }

      next(error);
    }
  }

  async updateProfile(
    req: Request<unknown, unknown, UpdateProfileRequest>,
    res: Response<GetProfileResponse>,
    next: NextFunction
  ) {
    try {
      const user = req.user!;

      const updatedUser = await userModel.update(user._id, req.body);

      if (!updatedUser) {
        return res.status(404).json({
          message: 'User not found',
        });
      }

      res.status(200).json({
        message: 'User info updated successfully',
        data: { user: updatedUser },
      });
    } catch (error) {
      logger.error('Failed to update user info:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to update user info',
        });
      }

      next(error);
    }
  }

  async deleteProfile(req: Request, res: Response, next: NextFunction) {
    try {
      const user = req.user!;

      await MediaService.deleteAllUserImages(user._id.toString());

      await userModel.delete(user._id);

      res.status(200).json({
        message: 'User deleted successfully',
      });
    } catch (error) {
      logger.error('Failed to delete user:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to delete user',
        });
      }

      next(error);
    }
  }
}
