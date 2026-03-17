import { useState } from 'react';
import { DndContext, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent, DragOverEvent } from '@dnd-kit/core';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { useApplicationStore } from '@/store/applicationStore';
import type { JobApplication } from '@/types';
import { STATUS_CONFIG, STATUS_STAGES } from '@/utils/constants';
import { BoardColumn } from './BoardColumn';
import { ApplicationDetailModal } from '@/components/common/ApplicationDetailModal';
import { useStatusTransitions, canTransition } from '@/hooks/useStatusTransitions';
import { toast } from '@/store/toastStore';

export interface BoardViewProps {
  isAiPanelOpen?: boolean;
}

const STATUS_KEYS = Object.keys(STATUS_CONFIG);

function isStatusKey(id: string | number): id is keyof typeof STATUS_CONFIG {
  return STATUS_KEYS.includes(String(id));
}

export function BoardView({ isAiPanelOpen = true }: BoardViewProps) {
  const { applications, updateApplicationStatus, fetchApplications, setDetailOpen } = useApplicationStore();
  const { rules: transitionRules } = useStatusTransitions();
  const [selectedApplication, setSelectedApplication] = useState<number | null>(null);
  const [dragOverStatus, setDragOverStatus] = useState<string | null>(null);
  const [draggingApp, setDraggingApp] = useState<JobApplication | null>(null);
  const [collapsedStages, setCollapsedStages] = useState<Set<string>>(new Set());

  const handleCardClick = (applicationId: number) => {
    setSelectedApplication(applicationId);
    setDetailOpen(true);
  };

  const handleCloseModal = () => {
    setSelectedApplication(null);
    setDetailOpen(false);
  };

  const handleUpdate = () => {
    fetchApplications();
  };

  const selectedApplicationData = applications.find(app => app.id === selectedApplication) || null;

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  const groupedApplications = (Object.keys(STATUS_CONFIG) as Array<keyof typeof STATUS_CONFIG>).reduce((groups, status) => {
    groups[status] = applications.filter(app => app.status === status);
    return groups;
  }, {} as Record<keyof typeof STATUS_CONFIG, JobApplication[]>);

  const getStatusFromOverId = (overId: string | number): string | null => {
    if (isStatusKey(overId)) {
      return overId;
    }
    const targetApp = applications.find(app => app.id === overId);
    return targetApp?.status || null;
  };

  const handleDragStart = (event: DragStartEvent) => {
    const applicationId = event.active.id as number;
    const application = applications.find(app => app.id === applicationId);
    setDraggingApp(application || null);
  };

  const handleDragOver = (event: DragOverEvent) => {
    const { over } = event;
    if (!over) {
      setDragOverStatus(null);
      return;
    }
    const status = getStatusFromOverId(over.id);
    setDragOverStatus(status);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;

    setDragOverStatus(null);
    setDraggingApp(null);

    if (!over || active.id === over.id) {
      return;
    }

    const applicationId = active.id as number;
    const application = applications.find(app => app.id === applicationId);

    if (!application) return;

    const newStatus = getStatusFromOverId(over.id);
    if (!newStatus) {
      toast.error('无效的目标状态');
      return;
    }

    if (!canTransition(transitionRules, application.status, newStatus)) {
      const fromLabel = STATUS_CONFIG[application.status as keyof typeof STATUS_CONFIG]?.label || application.status;
      const toLabel = STATUS_CONFIG[newStatus as keyof typeof STATUS_CONFIG]?.label || newStatus;
      toast.error(`不允许从 "${fromLabel}" 转换到 "${toLabel}"`);
      return;
    }

    try {
      await updateApplicationStatus(applicationId, newStatus);
      toast.success('状态更新成功');
    } catch (error: any) {
      const errorMsg = error?.message || error?.response?.data?.message || '状态更新失败';
      toast.error(errorMsg);
      fetchApplications();
    }
  };

  const toggleStage = (stageKey: string) => {
    setCollapsedStages(prev => {
      const newSet = new Set(prev);
      if (newSet.has(stageKey)) {
        newSet.delete(stageKey);
      } else {
        newSet.add(stageKey);
      }
      return newSet;
    });
  };

  const getStageCount = (statuses: readonly string[]) => {
    return statuses.reduce((sum, status) => sum + (groupedApplications[status as keyof typeof STATUS_CONFIG]?.length || 0), 0);
  };

  return (
    <div className="p-2 md:p-4 h-full overflow-hidden">
      <DndContext
        sensors={sensors}
        onDragStart={handleDragStart}
        onDragOver={handleDragOver}
        onDragEnd={handleDragEnd}
      >
        <div className="flex flex-col gap-3 h-full overflow-y-auto pr-1">
          {(Object.entries(STATUS_STAGES) as [string, { label: string; statuses: readonly string[] }][]).map(([stageKey, stage]) => {
            const isCollapsed = collapsedStages.has(stageKey);
            const stageCount = getStageCount(stage.statuses);

            return (
              <div key={stageKey} className="flex-shrink-0">
                <button
                  onClick={() => toggleStage(stageKey)}
                  className="flex items-center gap-2 px-3 py-2 bg-paper-200/50 rounded-lg w-full hover:bg-paper-200 transition-colors"
                >
                  {isCollapsed ? (
                    <ChevronRight className="w-4 h-4 text-paper-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-paper-600" />
                  )}
                  <span className="font-medium text-paper-700 text-sm">{stage.label}</span>
                  <span className="text-xs text-paper-500 bg-paper-100 px-2 py-0.5 rounded-full">
                    {stageCount}
                  </span>
                </button>

                {!isCollapsed && (
                  <div className="flex gap-1 md:gap-2 mt-2 overflow-x-auto pb-2">
                    {stage.statuses.map((status) => {
                      const canDrop = draggingApp
                        ? canTransition(transitionRules, draggingApp.status, status)
                        : true;

                      return (
                        <BoardColumn
                          key={status}
                          status={status as keyof typeof STATUS_CONFIG}
                          applications={groupedApplications[status as keyof typeof STATUS_CONFIG]}
                          isAiPanelOpen={isAiPanelOpen}
                          onCardClick={handleCardClick}
                          canDrop={canDrop}
                          isDragOver={dragOverStatus === status}
                        />
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </DndContext>

      <ApplicationDetailModal
        application={selectedApplicationData}
        onClose={handleCloseModal}
        onUpdate={handleUpdate}
      />
    </div>
  );
}
