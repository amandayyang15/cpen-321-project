import mongoose, { Schema, Document } from 'mongoose';

export interface IProjectMember {
  userId: mongoose.Types.ObjectId;
  role: 'owner' | 'user';
  admin: boolean;
  joinedAt: Date;
}

export interface IResource {
  resourceName: string;
  link: string;
}

export interface IProject extends Document {
  _id: mongoose.Types.ObjectId;
  name: string;
  description?: string;
  invitationCode: string;
  ownerId: mongoose.Types.ObjectId;
  members: IProjectMember[];
  resources: IResource[];
  createdAt: Date;
  updatedAt: Date;
  isActive: boolean;
}

const projectMemberSchema = new Schema<IProjectMember>({
  userId: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true,
  },
  role: {
    type: String,
    enum: ['owner', 'user'],
    required: true,
  },
  admin: {
    type: Boolean,
    default: false,
    required: true,
  },
  joinedAt: {
    type: Date,
    default: Date.now,
  },
});

const resourceSchema = new Schema<IResource>({
  resourceName: {
    type: String,
    required: true,
    trim: true,
    maxlength: 200,
  },
  link: {
    type: String,
    required: true,
    trim: true,
    maxlength: 500,
  },
});

const projectSchema = new Schema<IProject>(
  {
    name: {
      type: String,
      required: true,
      trim: true,
      maxlength: 100,
      index: true,
    },
    description: {
      type: String,
      trim: true,
      maxlength: 1000,
    },
    invitationCode: {
      type: String,
      required: true,
      unique: true,
      length: 8,
      index: true,
    },
    ownerId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: true,
      index: true,
    },
    members: [projectMemberSchema],
    resources: [resourceSchema],
    isActive: {
      type: Boolean,
      default: true,
      index: true,
    },
  },
  {
    timestamps: true,
  }
);

// Create indexes for performance
projectSchema.index({ invitationCode: 1 }, { unique: true });
projectSchema.index({ ownerId: 1 });
projectSchema.index({ 'members.userId': 1 });
projectSchema.index({ createdAt: -1 });
projectSchema.index({ isActive: 1 });

export class ProjectModel {
  private project: mongoose.Model<IProject>;

  constructor() {
    this.project = mongoose.model<IProject>('Project', projectSchema);
  }

  async create(projectData: Partial<IProject>): Promise<IProject> {
    try {
      return await this.project.create(projectData);
    } catch (error) {
      console.error('Error creating project:', error);
      throw new Error('Failed to create project');
    }
  }

  async findById(projectId: mongoose.Types.ObjectId): Promise<IProject | null> {
    try {
      return await this.project.findById(projectId);
    } catch (error) {
      console.error('Error finding project by ID:', error);
      throw new Error('Failed to find project');
    }
  }

  async findByInvitationCode(invitationCode: string): Promise<IProject | null> {
    try {
      return await this.project.findOne({ invitationCode, isActive: true });
    } catch (error) {
      console.error('Error finding project by invitation code:', error);
      throw new Error('Failed to find project');
    }
  }

  async findByOwnerId(ownerId: mongoose.Types.ObjectId): Promise<IProject[]> {
    try {
      return await this.project.find({ ownerId, isActive: true }).sort({ createdAt: -1 });
    } catch (error) {
      console.error('Error finding projects by owner:', error);
      throw new Error('Failed to find projects');
    }
  }

  async findByMemberId(memberId: mongoose.Types.ObjectId): Promise<IProject[]> {
    try {
      return await this.project.find({ 
        'members.userId': memberId, 
        isActive: true 
      }).sort({ createdAt: -1 });
    } catch (error) {
      console.error('Error finding projects by member:', error);
      throw new Error('Failed to find projects');
    }
  }

  async addMember(projectId: mongoose.Types.ObjectId, memberData: IProjectMember): Promise<IProject | null> {
    try {
      return await this.project.findByIdAndUpdate(
        projectId,
        { $push: { members: memberData } },
        { new: true }
      );
    } catch (error) {
      console.error('Error adding member to project:', error);
      throw new Error('Failed to add member to project');
    }
  }

  async removeMember(projectId: mongoose.Types.ObjectId, userId: mongoose.Types.ObjectId): Promise<IProject | null> {
    try {
      return await this.project.findByIdAndUpdate(
        projectId,
        { $pull: { members: { userId } } },
        { new: true }
      );
    } catch (error) {
      console.error('Error removing member from project:', error);
      throw new Error('Failed to remove member from project');
    }
  }

  async addResource(projectId: mongoose.Types.ObjectId, resource: IResource): Promise<IProject | null> {
    try {
      return await this.project.findByIdAndUpdate(
        projectId,
        { $push: { resources: resource } },
        { new: true }
      );
    } catch (error) {
      console.error('Error adding resource to project:', error);
      throw new Error('Failed to add resource to project');
    }
  }

  async removeResource(projectId: mongoose.Types.ObjectId, resourceIndex: number): Promise<IProject | null> {
    try {
      const project = await this.project.findById(projectId);
      if (!project) return null;

      // Remove resource at specific index
      project.resources.splice(resourceIndex, 1);
      return await project.save();
    } catch (error) {
      console.error('Error removing resource from project:', error);
      throw new Error('Failed to remove resource from project');
    }
  }

  async update(projectId: mongoose.Types.ObjectId, updateData: Partial<IProject>): Promise<IProject | null> {
    try {
      return await this.project.findByIdAndUpdate(projectId, updateData, { new: true });
    } catch (error) {
      console.error('Error updating project:', error);
      throw new Error('Failed to update project');
    }
  }

  async delete(projectId: mongoose.Types.ObjectId): Promise<void> {
    try {
      console.log(`Deleting project with ID: ${projectId}`);
      const result = await this.project.findByIdAndDelete(projectId);
      console.log(`Project deletion result:`, result);
    } catch (error) {
      console.error('Error deleting project:', error);
      throw new Error('Failed to delete project');
    }
  }

  async generateInvitationCode(): Promise<string> {
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < 8; i++) {
      result += characters.charAt(Math.floor(Math.random() * characters.length));
    }
    return result;
  }

  async isUserAdmin(projectId: mongoose.Types.ObjectId, userId: mongoose.Types.ObjectId): Promise<boolean> {
    try {
      const project = await this.project.findById(projectId);
      if (!project) return false;
      
      const member = project.members.find(m => m.userId.toString() === userId.toString());
      return member ? member.admin : false;
    } catch (error) {
      console.error('Error checking admin status:', error);
      return false;
    }
  }
}

export const projectModel = new ProjectModel();
