import api from './api'
import type { PageResponse, AuditLogResponse } from '@/types'

export interface GetAuditLogsParams {
  entityType?: string
  action?: string
  userId?: number
  startDate?: string
  endDate?: string
  page?: number
  size?: number
  sort?: string
}

export const auditLogService = {
  async getAuditLogs(params?: GetAuditLogsParams): Promise<PageResponse<AuditLogResponse>> {
    const response = await api.get<PageResponse<AuditLogResponse>>('/audit-logs', { params })
    return response.data
  },
}
