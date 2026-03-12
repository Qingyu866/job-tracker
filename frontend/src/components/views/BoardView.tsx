import { DndContext, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import type { DragEndEvent } from '@dnd-kit/core';
import { useApplicationStore } from '@/store/applicationStore';
import type { JobApplication } from '@/types';
import { STATUS_CONFIG } from '@/utils/constants';
import { BoardColumn } from './BoardColumn';

export function BoardView() {
  const { applications, updateApplication } = useApplicationStore();
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  // 按状态分组
  const groupedApplications = (Object.keys(STATUS_CONFIG) as Array<keyof typeof STATUS_CONFIG>).reduce((groups, status) => {
    groups[status] = applications.filter(app => app.status === status);
    return groups;
  }, {} as Record<keyof typeof STATUS_CONFIG, JobApplication[]>);

  // 处理拖拽结束
  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const applicationId = active.id as number;
      const newStatus = over.id as keyof typeof STATUS_CONFIG;

      try {
        await updateApplication(applicationId, { status: newStatus });
      } catch (error) {
        console.error('Failed to update application status:', error);
      }
    }
  };

  return (
    <div className="p-6 h-full">
      <DndContext
        sensors={sensors}
        onDragEnd={handleDragEnd}
      >
        <div className="flex gap-4 h-full overflow-x-auto pb-4">
          {(Object.keys(STATUS_CONFIG) as Array<keyof typeof STATUS_CONFIG>).map((status) => (
            <BoardColumn
              key={status}
              status={status}
              applications={groupedApplications[status]}
            />
          ))}
        </div>
      </DndContext>
    </div>
  );
}
