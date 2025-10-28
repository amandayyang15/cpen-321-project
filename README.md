# CPEN321 M1

## Backend

You can see how to setup the backend [here](./backend/README.md) 

## Frontend

You can see how to setup the frontend [here](./frontend/README.md)


## Database

db is named cpen321-project with the connection string mongodb://localhost:27017/cpen321-project
`npm run seed-database.ts` to populate with initial data

the .model.ts files contains interface code for that specific table

DB Structure:

Users ↔ Projects: Many-to-many through projects.members and users.ownedProjects/memberProjects
Projects → Tasks: One-to-many via tasks.projectId
Projects → Expenses: One-to-many via expenses.projectId
Projects → Chat Messages: One-to-many via chatmessages.projectId
Users → Tasks: Many-to-many via tasks.assignees
Users → Expenses: Many-to-many via expenses.splits.userId
Users → Notifications: One-to-many via notifications.userId
Projects → Invitations: One-to-many via projectinvitations.projectId

// =============================================================================
// 1. USERS COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                    // Primary key
  googleId: String,                 // Google OAuth ID (unique, indexed)
  email: String,                    // User email (unique, indexed)
  name: String,                     // Display name from Google
  profilePicture: String,           // URL to profile picture (optional)
  bio: String,                      // User biography (optional, max 500 chars)
  hobbies: [String],               // Array of user hobbies
  ownedProjects: [ObjectId],       // Array of project IDs user owns
  memberProjects: [ObjectId],      // Array of project IDs user is member of
  calendarRefreshToken: String,    // Google Calendar token (optional)
  calendarEnabled: Boolean,        // Calendar sync enabled flag
  createdAt: Date,                 // Account creation timestamp
  updatedAt: Date                  // Last update timestamp
}
// Indexes: email (unique), googleId (unique), ownedProjects, memberProjects

// =============================================================================
// 2. PROJECTS COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                   // Primary key
  name: String,                    // Project name (required, max 100 chars, indexed)
  description: String,             // Project description (optional, max 1000 chars)
  invitationCode: String,          // Unique 8-char invitation code (indexed)
  ownerId: ObjectId,              // Reference to users._id (indexed)
  members: [{                     // Array of project members
    userId: ObjectId,             // Reference to users._id
    role: String,                 // "owner" or "user"
    joinedAt: Date               // When user joined project
  }],
  isActive: Boolean,              // Project status (default: true, indexed)
  createdAt: Date,                // Project creation timestamp
  updatedAt: Date                 // Last modification timestamp
}
// Indexes: invitationCode (unique), ownerId, members.userId, createdAt (-1), isActive

// =============================================================================
// 3. TASKS COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                   // Primary key
  projectId: ObjectId,            // Reference to projects._id (indexed)
  title: String,                  // Task title (required, max 200 chars)
  description: String,            // Task description (optional, max 2000 chars)
  status: String,                 // "not_started", "in_progress", "completed", "blocked", "backlog" (indexed)
  assignees: [ObjectId],          // Array of user IDs assigned to task (indexed)
  createdBy: ObjectId,            // Reference to users._id who created task
  deadline: Date,                 // Task deadline (optional, indexed)
  calendarEventId: String,        // Google Calendar event ID (optional, indexed)
  createdAt: Date,                // Task creation timestamp
  updatedAt: Date                 // Last modification timestamp
}
// Indexes: projectId+createdAt (compound), assignees, deadline, status, calendarEventId

// =============================================================================
// 4. EXPENSES COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                   // Primary key
  projectId: ObjectId,            // Reference to projects._id (indexed)
  title: String,                  // Expense title (required, max 200 chars)
  description: String,            // Expense description (optional, max 1000 chars)
  amount: Number,                 // Total expense amount (required, min: 0.01)
  createdBy: ObjectId,            // Reference to users._id who created expense (indexed)
  splits: [{                      // Array of expense splits
    userId: ObjectId,             // Reference to users._id
    amount: Number,               // Amount this user owes (min: 0.01)
    isPaid: Boolean              // Whether user has paid their share (default: false)
  }],
  status: String,                 // "pending", "fully_paid", "cancelled" (indexed)
  createdAt: Date,                // Expense creation timestamp
  updatedAt: Date                 // Last modification timestamp
}
// Indexes: projectId+createdAt (compound), splits.userId, status, createdBy

// =============================================================================
// 5. CHATMESSAGES COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                   // Primary key
  projectId: ObjectId,            // Reference to projects._id (indexed)
  content: String,                // Message text content (required, max 2000 chars)
  messageType: String,            // "text" or "system"
  senderId: ObjectId,             // Reference to users._id (indexed)
  senderName: String,             // Cached sender name for performance
  isDeleted: Boolean,             // Soft delete flag (default: false, indexed)
  createdAt: Date                 // Message timestamp (indexed)
}
// Indexes: projectId+createdAt (compound), senderId, isDeleted

// =============================================================================
// 6. NOTIFICATIONS COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                   // Primary key
  userId: ObjectId,               // Reference to users._id (indexed)
  projectId: ObjectId,            // Reference to projects._id (indexed)
  type: String,                   // Notification type (indexed)
                                  // Types: "task_deadline", "expense_added", "chat_message", 
                                  //        "task_assigned", "project_invitation", "task_completed", "expense_paid"
  title: String,                  // Notification title (required, max 200 chars)
  message: String,                // Notification message content (required, max 500 chars)
  isRead: Boolean,                // Read status (default: false, indexed)
  createdAt: Date                 // Notification timestamp (indexed)
}
// Indexes: userId+createdAt (compound), projectId, isRead, type

// =============================================================================
// 7. PROJECTINVITATIONS COLLECTION SCHEMA
// =============================================================================
{
  _id: ObjectId,                   // Primary key
  projectId: ObjectId,            // Reference to projects._id (indexed)
  invitationCode: String,         // Unique invitation code (indexed)
  invitedEmail: String,           // Email address of invited user (indexed)
  invitedBy: ObjectId,            // Reference to users._id who sent invitation
  role: String,                   // Role to be assigned: "user" (simplified)
  status: String,                 // "pending", "accepted", "declined", "expired" (indexed)
  createdAt: Date,                // Invitation creation timestamp
  expiresAt: Date                 // Invitation expiration timestamp (indexed)
}
// Indexes: invitationCode (unique), invitedEmail, projectId, status, expiresAt

// =============================================================================
// COLLECTION RELATIONSHIPS
// =============================================================================
/*
Users ↔ Projects: Many-to-many (projects.members & users.ownedProjects/memberProjects)
Projects → Tasks: One-to-many (tasks.projectId)
Projects → Expenses: One-to-many (expenses.projectId)
Projects → Chat Messages: One-to-many (chatmessages.projectId)
Users → Tasks: Many-to-many (tasks.assignees)
Users → Expenses: Many-to-many (expenses.splits.userId)
Users → Notifications: One-to-many (notifications.userId)
Projects → Invitations: One-to-many (projectinvitations.projectId)
*/

// =============================================================================
// CURRENT DATA COUNT
// =============================================================================
/*
users: 3 documents
projects: 2 documents
tasks: 4 documents
expenses: 2 documents
chatmessages: 3 documents
notifications: 3 documents
projectinvitations: 2 documents
*/
