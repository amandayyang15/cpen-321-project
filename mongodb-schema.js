// MongoDB Schema for Group Project Management Application
// This schema supports the requirements for user management, project collaboration,
// task management, expense tracking, and real-time chat functionality
// Compatible with existing Google authentication implementation

// =============================================================================
// 1. USERS COLLECTION (extends existing user model)
// =============================================================================
const usersSchema = {
  _id: "ObjectId", // Primary key
  googleId: "String", // Google OAuth ID (unique, indexed) - matches existing
  email: "String", // User's email address (unique, indexed) - matches existing
  name: "String", // User's display name from Google - matches existing
  profilePicture: "String", // URL to user's profile picture - matches existing
  bio: "String", // User bio - matches existing
  hobbies: ["String"], // User hobbies - matches existing
  createdAt: "Date", // Account creation timestamp - matches existing
  updatedAt: "Date", // Last update timestamp - matches existing
  
  // Project relationships (new fields for project management)
  ownedProjects: ["ObjectId"], // Array of project IDs where user is owner
  memberProjects: ["ObjectId"], // Array of project IDs where user is member
  
  // Google Calendar integration (for future use)
  calendarRefreshToken: "String", // Encrypted Google Calendar refresh token
  calendarEnabled: "Boolean" // Whether calendar sync is enabled
};

// =============================================================================
// 2. PROJECTS COLLECTION
// =============================================================================
const projectsSchema = {
  _id: "ObjectId", // Primary key
  name: "String", // Project name (required, indexed)
  description: "String", // Project description (optional)
  invitationCode: "String", // Unique code for joining project (indexed)
  
  // Project ownership and membership (simplified to 2 roles)
  ownerId: "ObjectId", // Reference to users._id (indexed)
  members: [{
    userId: "ObjectId", // Reference to users._id
    role: "String", // "owner" or "user" (simplified)
    joinedAt: "Date" // When user joined the project
  }],
  
  // Project metadata
  createdAt: "Date", // Project creation timestamp
  updatedAt: "Date", // Last modification timestamp
  isActive: "Boolean" // Project status (default: true)
};

// =============================================================================
// 3. TASKS COLLECTION (simplified for basic requirements)
// =============================================================================
const tasksSchema = {
  _id: "ObjectId", // Primary key
  projectId: "ObjectId", // Reference to projects._id (indexed)
  
  // Basic task details
  title: "String", // Task title (required)
  description: "String", // Task description (optional)
  status: "String", // "not_started", "in_progress", "completed", "blocked", "backlog"
  
  // Assignment and deadlines
  assignees: ["ObjectId"], // Array of user IDs assigned to task (indexed)
  createdBy: "ObjectId", // Reference to users._id who created the task
  deadline: "Date", // Task deadline (indexed for calendar sync)
  
  // Task metadata
  createdAt: "Date", // Task creation timestamp
  updatedAt: "Date", // Last modification timestamp
  
  // Calendar integration (for future Google Calendar API)
  calendarEventId: "String" // Google Calendar event ID (if synced)
};

// =============================================================================
// 4. EXPENSES COLLECTION (simplified, all users can create)
// =============================================================================
const expensesSchema = {
  _id: "ObjectId", // Primary key
  projectId: "ObjectId", // Reference to projects._id (indexed)
  
  // Basic expense details
  title: "String", // Expense title (required)
  description: "String", // Expense description (optional)
  amount: "Number", // Total expense amount (required, positive)
  
  // Expense management
  createdBy: "ObjectId", // Reference to users._id who created expense
  createdAt: "Date", // Expense creation timestamp
  updatedAt: "Date", // Last modification timestamp
  
  // Expense splitting (who owes whom)
  splits: [{
    userId: "ObjectId", // Reference to users._id
    amount: "Number", // Amount this user owes
    isPaid: "Boolean" // Whether this user has paid their share
  }],
  
  // Expense status
  status: "String" // "pending", "fully_paid", "cancelled"
};

// =============================================================================
// 5. CHAT MESSAGES COLLECTION (simplified for basic chat)
// =============================================================================
const chatMessagesSchema = {
  _id: "ObjectId", // Primary key
  projectId: "ObjectId", // Reference to projects._id (indexed)
  
  // Message content
  content: "String", // Message text content
  messageType: "String", // "text" or "system"
  
  // Sender information
  senderId: "ObjectId", // Reference to users._id (indexed)
  senderName: "String", // Cached sender name for performance
  
  // Message metadata
  createdAt: "Date", // Message timestamp (indexed for chronological order)
  isDeleted: "Boolean" // Whether message was deleted (soft delete)
};

// =============================================================================
// 6. NOTIFICATIONS COLLECTION (simplified)
// =============================================================================
const notificationsSchema = {
  _id: "ObjectId", // Primary key
  userId: "ObjectId", // Reference to users._id (indexed)
  projectId: "ObjectId", // Reference to projects._id (indexed)
  
  // Notification details
  type: "String", // "task_deadline", "expense_added", "chat_message", "task_assigned"
  title: "String", // Notification title
  message: "String", // Notification message content
  
  // Notification metadata
  createdAt: "Date", // Notification timestamp (indexed)
  isRead: "Boolean" // Whether notification has been read
};

// =============================================================================
// 7. PROJECT INVITATIONS COLLECTION (simplified)
// =============================================================================
const projectInvitationsSchema = {
  _id: "ObjectId", // Primary key
  projectId: "ObjectId", // Reference to projects._id (indexed)
  invitationCode: "String", // Unique invitation code (indexed)
  
  // Invitation details
  invitedEmail: "String", // Email address of invited user
  invitedBy: "ObjectId", // Reference to users._id who sent invitation
  role: "String", // "user" - role to be assigned (simplified)
  
  // Invitation status
  status: "String", // "pending", "accepted", "declined", "expired"
  createdAt: "Date", // Invitation creation timestamp
  expiresAt: "Date" // Invitation expiration timestamp
};

// =============================================================================
// INDEXES FOR PERFORMANCE OPTIMIZATION
// =============================================================================
const indexes = {
  users: [
    { email: 1 }, // Unique index for email
    { googleId: 1 }, // Unique index for Google ID
    { "ownedProjects": 1 }, // Index for owned projects
    { "memberProjects": 1 } // Index for member projects
  ],
  
  projects: [
    { invitationCode: 1 }, // Unique index for invitation codes
    { ownerId: 1 }, // Index for project owners
    { "members.userId": 1 }, // Index for project members
    { createdAt: -1 }, // Index for recent projects
    { isActive: 1 } // Index for active projects
  ],
  
  tasks: [
    { projectId: 1, createdAt: -1 }, // Compound index for project tasks
    { assignees: 1 }, // Index for task assignees
    { deadline: 1 }, // Index for deadline queries
    { status: 1 }, // Index for task status
    { "calendarEventId": 1 } // Index for calendar sync
  ],
  
  expenses: [
    { projectId: 1, createdAt: -1 }, // Compound index for project expenses
    { "splits.userId": 1 }, // Index for user expense splits
    { status: 1 }, // Index for expense status
    { createdBy: 1 } // Index for expense creators
  ],
  
  chatMessages: [
    { projectId: 1, createdAt: 1 }, // Compound index for chronological messages
    { senderId: 1 }, // Index for sender queries
    { "parentMessageId": 1 } // Index for message threading
  ],
  
  notifications: [
    { userId: 1, createdAt: -1 }, // Compound index for user notifications
    { projectId: 1 }, // Index for project notifications
    { isRead: 1 }, // Index for unread notifications
    { type: 1 } // Index for notification types
  ],
  
  projectInvitations: [
    { invitationCode: 1 }, // Unique index for invitation codes
    { invitedEmail: 1 }, // Index for email invitations
    { projectId: 1 }, // Index for project invitations
    { status: 1 }, // Index for invitation status
    { expiresAt: 1 } // Index for expiration queries
  ]
};

// =============================================================================
// DATA VALIDATION RULES (compatible with existing user model)
// =============================================================================
const validationRules = {
  users: {
    email: { type: "string", required: true, unique: true, pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ },
    googleId: { type: "string", required: true, unique: true },
    name: { type: "string", required: true, minLength: 1, maxLength: 100 },
    bio: { type: "string", maxLength: 500 },
    hobbies: { type: "array", items: { type: "string" } }
  },
  
  projects: {
    name: { type: "string", required: true, minLength: 1, maxLength: 100 },
    invitationCode: { type: "string", required: true, unique: true, length: 8 },
    ownerId: { type: "ObjectId", required: true, ref: "users" },
    isActive: { type: "boolean", default: true }
  },
  
  tasks: {
    projectId: { type: "ObjectId", required: true, ref: "projects" },
    title: { type: "string", required: true, minLength: 1, maxLength: 200 },
    status: { type: "string", enum: ["not_started", "in_progress", "completed", "blocked", "backlog"] },
    assignees: { type: "array", items: { type: "ObjectId", ref: "users" } }
  },
  
  expenses: {
    projectId: { type: "ObjectId", required: true, ref: "projects" },
    title: { type: "string", required: true, minLength: 1, maxLength: 200 },
    amount: { type: "number", required: true, min: 0.01 },
    status: { type: "string", enum: ["pending", "fully_paid", "cancelled"] }
  },
  
  chatMessages: {
    projectId: { type: "ObjectId", required: true, ref: "projects" },
    content: { type: "string", required: true, minLength: 1, maxLength: 2000 },
    senderId: { type: "ObjectId", required: true, ref: "users" },
    messageType: { type: "string", enum: ["text", "system"] }
  }
};

// =============================================================================
// EXPORT SCHEMA DEFINITIONS
// =============================================================================
module.exports = {
  schemas: {
    users: usersSchema,
    projects: projectsSchema,
    tasks: tasksSchema,
    expenses: expensesSchema,
    chatMessages: chatMessagesSchema,
    notifications: notificationsSchema,
    projectInvitations: projectInvitationsSchema
  },
  indexes: indexes,
  validationRules: validationRules
};

// =============================================================================
// USAGE EXAMPLES
// =============================================================================

/*
// Example: Creating a new project (simplified)
const newProject = {
  name: "Mobile App Development",
  description: "Building a React Native app for task management",
  invitationCode: "ABC12345",
  ownerId: ObjectId("..."),
  members: [{
    userId: ObjectId("..."),
    role: "owner",
    joinedAt: new Date()
  }],
  createdAt: new Date(),
  updatedAt: new Date(),
  isActive: true
};

// Example: Creating a task (simplified)
const newTask = {
  projectId: ObjectId("..."),
  title: "Design user interface mockups",
  description: "Create wireframes and mockups for the main screens",
  status: "not_started",
  assignees: [ObjectId("..."), ObjectId("...")],
  createdBy: ObjectId("..."),
  deadline: new Date("2024-02-15"),
  createdAt: new Date(),
  updatedAt: new Date(),
  calendarEventId: null // Will be set when synced with Google Calendar
};

// Example: Creating an expense (simplified, all users can create)
const newExpense = {
  projectId: ObjectId("..."),
  title: "Team lunch meeting",
  description: "Lunch during project planning session",
  amount: 120.50,
  createdBy: ObjectId("..."),
  createdAt: new Date(),
  updatedAt: new Date(),
  splits: [
    { userId: ObjectId("..."), amount: 30.125, isPaid: false },
    { userId: ObjectId("..."), amount: 30.125, isPaid: false },
    { userId: ObjectId("..."), amount: 30.125, isPaid: false },
    { userId: ObjectId("..."), amount: 30.125, isPaid: false }
  ],
  status: "pending"
};

// Example: Creating a chat message (simplified)
const newChatMessage = {
  projectId: ObjectId("..."),
  content: "Hey team, I've completed the wireframes for the login screen!",
  messageType: "text",
  senderId: ObjectId("..."),
  senderName: "John Doe",
  createdAt: new Date(),
  isDeleted: false
};

// Example: User with Google authentication (compatible with existing model)
const newUser = {
  googleId: "google_oauth_id_12345",
  email: "john.doe@example.com",
  name: "John Doe",
  profilePicture: "https://lh3.googleusercontent.com/...",
  bio: "Software developer passionate about mobile apps",
  hobbies: ["programming", "gaming", "hiking"],
  createdAt: new Date(),
  updatedAt: new Date(),
  ownedProjects: [],
  memberProjects: [],
  calendarRefreshToken: null,
  calendarEnabled: false
};
*/
