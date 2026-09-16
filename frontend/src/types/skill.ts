/**
 * Type definitions for AI Skill Store
 */

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

// Category
export interface CategoryDto {
  id: number
  name: string
  description?: string
  icon?: string
}

export interface SkillCategoryInfo {
  id: number
  name: string
  icon?: string
  isPrimary: boolean
}

// Author
export interface AuthorInfo {
  id: number
  name: string
  email: string
  avatarUrl?: string
}

// Skill
export interface SkillResponse {
  id: number
  title: string
  description: string
  categories: SkillCategoryInfo[]
  tags: string[]
  author: AuthorInfo
  status: 'PENDING' | 'PUBLISHED' | 'REJECTED' | 'DEPRECATED' | 'MERGED'
  mergedIntoId?: number
  deprecatedReason?: string
  currentVersion: number
  fileName?: string
  filePath?: string
  usageCount: number
  averageRating: number
  ratingCount: number
  totalMmSaved: number
  createdAt: string
  updatedAt: string
}

export interface SkillVersionResponse {
  id: number
  version: number
  fileName?: string
  changelog?: string
  createdByName?: string
  createdAt: string
}

export interface SkillUploadRequest {
  title: string
  description: string
  categoryIds: number[]
  tagNames: string[]
  changelog?: string
}

export interface SkillUpdateRequest {
  title?: string
  description?: string
  categoryIds?: number[]
  tagNames?: string[]
  changelog?: string
}

// Rating & Review
export interface RatingRequest {
  rating: number
}

export interface RatingResponse {
  id: number
  skillId: number
  userId: number
  userName: string
  rating: number
  createdAt: string
  updatedAt: string
}

export interface ReviewCreateRequest {
  content: string
}

export interface ReviewUpdateRequest {
  content: string
}

export interface ReviewReportRequest {
  category: 'SECURITY_RISK' | 'BUG' | 'SPAM' | 'OTHER'
  detail?: string
}

export interface ReviewResponse {
  id: number
  skillId: number
  userId: number
  userName: string
  userAvatarUrl?: string
  content: string
  helpfulCount: number
  reportCategory?: string
  createdAt: string
  updatedAt: string
}

// Usage & Impact
export interface UsageRecordResponse {
  skillId: number
  userId: number
  usageDate: string
  newlyRecorded: boolean
  totalSkillUsage: number
}

export interface ImpactRecordRequest {
  referenceCode: string
  effectivenessScore: number
  estimatedMmSaved: number
  note?: string
}

export interface ImpactRecordResponse {
  id: number
  skillId: number
  userId: number
  userName: string
  referenceCode: string
  effectivenessScore: number
  estimatedMmSaved: number
  note?: string
  createdAt: string
}

// Approval & Merge
export interface PendingSkillResponse {
  id: number
  title: string
  description: string
  categories: SkillCategoryInfo[]
  tags: string[]
  author: AuthorInfo
  fileName?: string
  currentVersion: number
  securityScanFlag: boolean
  securityScanDetail?: string
  createdAt: string
}

export interface RejectRequest {
  reason: string
}

export interface MergeSkillsRequest {
  sourceSkillId: number
  targetSkillId: number
  reason: string
}

export interface DeprecateRequest {
  reason: string
}

// Stats & Dashboard
export interface CategoryStatsResponse {
  name: string
  skillCount: number
}

export interface DashboardStatsResponse {
  totalSkills: number
  publishedSkills: number
  pendingSkills: number
  deprecatedSkills: number
  newThisWeek: number
  totalUsage: number
  totalMmSavedThisMonth: number
  topCategories: CategoryStatsResponse[]
  recentSkills: SkillResponse[]
}

export interface TopContributorResponse {
  authorId: number
  authorName: string
  publishedSkillsCount: number
}

export interface LeaderboardResponse {
  byRating: SkillResponse[]
  byUsage: SkillResponse[]
  byImpact: SkillResponse[]
  topContributors: TopContributorResponse[]
}

export interface MySkillsStatsResponse {
  myPublishedSkillsCount: number
  myPendingSkillsCount: number
  totalUsageOfMySkills: number
  totalMmSavedByMySkills: number
  skillsUsedByMeCount: number
}

// Audit Log
export interface AuditLogResponse {
  id: number
  userId?: number
  userName?: string
  action: string
  entityType: string
  entityId?: number
  details?: string
  createdAt: string
}
