export type Role = 'ADMIN' | 'CLIENT' | 'FREELANCER';
export type RegisterRole = Exclude<Role, 'ADMIN'>;
export type ApprovalStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';

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
}
