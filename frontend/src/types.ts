export type Role = 'ADMIN' | 'CLIENT' | 'FREELANCER';
export type RegisterRole = Exclude<Role, 'ADMIN'>;
export type ApprovalStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
export type ProposalStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

export interface Session {
  token: string;
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  firstName: string;
  lastName: string;
  username: string;
  role: RegisterRole;
}

export interface FreelancerProfileRequest {
  bio: string | null;
  minPrice: number | null;
  maxPrice: number | null;
  skillIds: string[];
}

export interface FreelancerProfile {
  id: string;
  userId: string;
  bio: string | null;
  minPrice: number | null;
  maxPrice: number | null;
  availability: 'AVAILABLE' | 'BUSY';
  skillIds: string[];
  approvalStatus: ApprovalStatus;
  rejectionReason: string | null;
  submittedAt: string | null;
  reviewedAt: string | null;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface MessageResponse {
  message: string;
}

export interface Skill {
  id: string;
  name: string;
  active: boolean;
  version: number | null;
}

export interface Category {
  id: string;
  name: string;
  description: string | null;
  skillIds: string[];
  active: boolean;
  version: number | null;
}

export interface UserSummary {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  role: Role;
}

export interface Project {
  id: string;
  clientId: string;
  assignedFreelancerId: string | null;
  title: string;
  description: string;
  budget: number | null;
  categoryId: string | null;
  skillIds: string[];
  deadline: string | null;
  status: string;
  createdAt: string | null;
}

export interface ProjectRequest {
  title: string;
  description: string;
  budget: number;
  deadline: string;
  categoryId: string;
  requiredSkillIds: string[];
}

export interface ApplyRequest {
  coverLetter: string;
  bidAmount: number;
  deliveryDays: number;
}

export interface Proposal {
  id: string;
  projectId: string;
  clientId: string;
  projectTitle: string | null;
  freelancerId: string;
  coverLetter: string;
  bidAmount: number;
  deliveryDays: number;
  status: ProposalStatus;
  createdAt: string;
  decidedAt: string | null;
  version: number | null;
}

export interface Conversation {
  id: string;
  participantIds: string[];
  projectId: string | null;
  lastMessage: string | null;
  lastSenderId: string | null;
  lastMessageAt: string | null;
  createdAt: string;
  unreadCount: number;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  senderId: string;
  recipientId: string;
  content: string;
  clientMessageId: string | null;
  read: boolean;
  createdAt: string;
  pending?: boolean;
  failed?: boolean;
}

export interface ReadReceipt {
  conversationId: string;
  readerId: string;
  readAt: string;
}

export interface Contact {
  id: string;
  firstName: string;
  lastName: string;
  role: Role;
}
